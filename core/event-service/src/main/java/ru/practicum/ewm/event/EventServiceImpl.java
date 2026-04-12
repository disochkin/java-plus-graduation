package ru.practicum.ewm.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.clients.comment.CommentClient;
import ru.practicum.ewm.clients.request.RequestClient;
import ru.practicum.ewm.clients.user.UserLookupFacade;
import ru.practicum.ewm.clients.stat.StatClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.stat.StatsParamDto;
import ru.practicum.ewm.dto.stat.ViewStatsDto;
import ru.practicum.ewm.dto.user.UserClientDto;
import ru.practicum.ewm.exception.AccessViolationException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.event.Location;
import ru.practicum.ewm.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
    private final CategoryRepository categoryRepository;
    private final CommentClient commentClient;
    private final UserLookupFacade userLookupFacade;
    private final RequestClient requestClient;

    private Map<Long, Long> getViews(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        List<String> uriList = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        StatsParamDto statsParamDto = new StatsParamDto();
        // Используем более узкий временной диапазон (не забыть)
        statsParamDto.setStart(LocalDateTime.now().minusHours(1));
        statsParamDto.setEnd(LocalDateTime.now().plusHours(1));
        statsParamDto.setUris(uriList);
        statsParamDto.setIsUnique(true);

        try {
            List<ViewStatsDto> viewStatsDtoList = statClient.getStats(
                    LocalDateTime.now().minusHours(1).toString(),
                    LocalDateTime.now().plusHours(1).toString(),
                    uriList,
                    true
            );
            return viewStatsDtoList.stream()
                    .collect(Collectors.toMap(
                            dto -> Long.parseLong(dto.getUri().substring(dto.getUri().lastIndexOf('/') + 1)),
                            ViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Error retrieving view statistics: {}", e.getMessage());
            return Map.of();
        }
    }

    private long getViewCount(Event event) {
        Map<Long, Long> map = getViews(List.of(event));
        return map.getOrDefault(event.getId(), 0L);
    }

    private long getRequestCount(Event event) {
        Map<Long, Long> map = getRequests(List.of(event));
        return map.getOrDefault(event.getId(), 0L);
    }

    private Map<Long, Long> getRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        return requestClient.getConfirmedRequest(
                events.stream().map(Event::getId).toList()
        );

    }

    private Map<Long, Boolean> checkAvailable(List<Event> events, Map<Long, Long> requestMap) {
        Map<Long, Boolean> availableMap = new HashMap<>();
        for (Event event : events) {
            if (event.getParticipantLimit() > 0) {
                if (requestMap.getOrDefault(event.getId(), 0L) < event.getParticipantLimit()) {
                    availableMap.put(event.getId(), true);
                } else {
                    availableMap.put(event.getId(), false);
                }
            } else {
                availableMap.put(event.getId(), true);
            }
        }
        return availableMap;
    }

    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        UserClientDto userClientDto = userLookupFacade.findOrThrow(userId);
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("The event date must be at least 2 hours from now");
        }
        Event savedEvent = eventRepository.save(eventMapper.toEvent(newEventDto, userClientDto));
        return eventMapper.toFullDto(savedEvent, 0L, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                                      Pageable pageable) {
        Specification<Event> spec = EventSpecification.withInitiatorId(eventInitiatorIdFilter);
        Page<Event> events = eventRepository.findAll(spec, pageable);

        Map<Long, Long> viewsMap = getViews(events.getContent());

        Map<Long, Long> requestsMap = requestClient.getConfirmedRequest(events.stream().map(Event::getId).toList());
        Map<Long, Long> commentsMap = commentClient.countCommentForEvents(events.stream()
                        .map(Event::getId).toList());

        return events.getContent().stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L),
                        commentsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventFullDescription(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));
        userLookupFacade.findOrThrow(userId);
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new AccessViolationException(String.format("Access denied! User userId=%s is not the creator of the event " +
                    "eventId=%s", userId, eventId));
        }

        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event),
                commentClient.countCommentForEvents(List.of(eventId)).get(0));
    }

    @Transactional
    public EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Events can only be edited when in Pending Moderation or Cancelled status");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Editing events is allowed no later than 2 hours before they start.");
        }
        if (updateEventRequest.getEventDate() != null && updateEventRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Editing past events is prohibited");
        }
        userLookupFacade.findOrThrow(userId);
        if (!Objects.equals(event.getInitiatorId(), userId)) {
            throw new AccessViolationException(String.format("Access denied! User userId=%s is not the creator of the event " +
                    "eventId=%s", userId, eventId));
        }

        EventState newState = event.getState();
        if (updateEventRequest.getStateAction() != null) {
            newState =
                    StateTransitionValidator.changeState(event.getState(), updateEventRequest.getStateAction(), false);
        }

        Category category = null;
        if (updateEventRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location newLocation = null;
        if (updateEventRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventRequest.getLocationDto());
        }
        updateEventRequest.applyTo(event, category, newLocation, newState);
        eventRepository.save(event);

        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event),
                commentClient.countCommentForEvents(List.of(eventId)).getOrDefault(eventId,0L));
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Events can only be edited when in Pending Moderation or Cancelled state");
        }
        if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Editing events is allowed no later than 1 hour before they start");
        }
        if (updateEventRequest.getEventDate() != null && updateEventRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Editing past events is prohibited");
        }

        EventState state = event.getState();
        if (updateEventRequest.getStateAction() != null) {
            state = StateTransitionValidator.changeState(event.getState(), updateEventRequest.getStateAction(), true);
        }

        if (event.getState() == EventState.PENDING &&  // <-- старое состояние
                state == EventState.PUBLISHED) {       // <-- новое состояние
            event.setPublishedOn(LocalDateTime.now());
        }

        Category category = null;
        if (updateEventRequest.hasCategory()) {
            category = categoryRepository.findById(updateEventRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
        }

        Location newLocation = null;
        if (updateEventRequest.hasLocationDto()) {
            newLocation = eventMapper.toLocation(updateEventRequest.getLocationDto());
        }

        updateEventRequest.applyTo(event, category, newLocation, state);
        eventRepository.save(event);

        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event),
                commentClient.countCommentForEvents(List.of(eventId)).getOrDefault(eventId, 0L));
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.toPageable();
        Specification<Event> spec = EventSpecification.withAdminFilter(eventAdminFilter);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        Map<Long, Long> requestsMap = getRequests(events);
        log.debug("requestsMap: {}", requestsMap);
        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Long> commentsMap = commentClient.countCommentForEvents(events.stream()
                        .map(Event::getId).toList());

        return events.stream()
                .map(event -> eventMapper.toFullDto(
                        event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L),
                        commentsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> publicSearchEvents(EventPublicFilter eventPublicFilter, PageRequestDto pageRequestDto) {

        Pageable pageable = pageRequestDto.toPageable();
        EventSort sort = pageRequestDto.getSort();

        boolean sorByDate = sort == EventSort.EVENT_DATE;
        boolean sortByViews = sort == EventSort.VIEWS;
        boolean noSort = sort == null;

        Specification<Event> spec = EventSpecification.withPublicFilter(eventPublicFilter);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> requestsMap = getRequests(events);
        Map<Long, Long> viewsMap = getViews(events);
        Map<Long, Boolean> availableMap = checkAvailable(events, requestsMap);
        Map<Long, Long> commentsMap = commentClient.countCommentForEvents(events.stream()
                        .map(Event::getId).toList());

        if (eventPublicFilter.getOnlyAvailable() == true) {
            events = events.stream()
                    .filter(e -> availableMap.getOrDefault(e.getId(), false)).toList();
        }
        if (sortByViews) {
            events = events.stream()
                    .sorted(Comparator.comparingLong(
                            e -> viewsMap.getOrDefault(e.getId(), 0L)))
                    .toList().reversed();
        }
        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        requestsMap.getOrDefault(event.getId(), 0L),
                        viewsMap.getOrDefault(event.getId(), 0L),
                        commentsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        return eventMapper.toFullDto(event, getRequestCount(event), getViewCount(event),
                commentClient.countCommentForEvents(List.of(eventId)).getOrDefault(eventId, 0L));
    }

    @Transactional(readOnly = true)
    public EventClientDto getEventInt(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event id=%s not found", eventId)));

        return eventMapper.toEventClientDto(event);
    }

}