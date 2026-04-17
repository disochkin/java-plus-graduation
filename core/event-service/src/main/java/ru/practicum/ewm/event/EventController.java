package ru.practicum.ewm.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.clients.stat.StatClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.stat.EndpointHitDto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@RestController
public class EventController {
    private final EventService eventService;
    private final StatClient statClient;

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto newEventDto, HttpServletRequest request) throws IOException {
        log.info("Request to create event, userId={}", userId);
        log.debug("newEventDto: {}", newEventDto);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/users/{userId}/events")
    public Collection<EventShortDto> getEventsOfUser(@PathVariable Long userId,
                                                     EventInitiatorIdFilter eventInitiatorIdFilter,
                                                     PageRequestDto pageRequestDto) {
        log.info("User event request, userId={}", userId);
        Collection<EventShortDto> events = eventService.getEventByUserId(eventInitiatorIdFilter,
                pageRequestDto.toPageable());
        log.info("Found events: {}", events.size());
        events.forEach(ev -> log.debug("EVENT: {}", ev));
        return events;
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventFullDescription(@PathVariable Long userId,
                                                @PathVariable Long eventId) {
        log.info("User requested the event with detailed description, userId={}, eventId={}", userId, eventId);
        EventFullDto eventFullDto = eventService.getEventFullDescription(userId, eventId);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventByCreator(@PathVariable Long userId,
                                             @PathVariable Long eventId,
                                             @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Event edit request by user, userId={}, eventId={}", userId, eventId);
        log.debug("{}", updateEventRequest);
        EventFullDto eventFullDto = eventService.updateEventByCreator(userId, eventId, updateEventRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable Long eventId,
                                           @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("Request to edit the event by the admin, eventId={}", eventId);
        log.debug("{}", updateEventRequest);

        EventFullDto eventFullDto = eventService.updateEventByAdmin(eventId, updateEventRequest);
        log.debug("EVENTS: {}", eventFullDto);
        return eventFullDto;
    }

    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsAdmin(EventAdminFilter eventAdminFilter,
                                             PageRequestDto pageRequestDto) {
        log.debug("Admin event request with parameters: {}", eventAdminFilter);
        return eventService.adminSearchEvents(eventAdminFilter, pageRequestDto);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@Valid EventPublicFilter eventPublicFilter,
                                         PageRequestDto pageRequestDto,
                                         HttpServletRequest request) {
        log.info("Public query of events with parameters: {}", eventPublicFilter);
        log.debug("Request parameters: {}", eventPublicFilter);
        log.info("client ip: {}", request.getRemoteAddr());
        return eventService.publicSearchEvents(eventPublicFilter, pageRequestDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long eventId,
                                 @RequestHeader("X-EWM-USER-ID") Long userId,
                                 HttpServletRequest request) {
        log.info("Public request for detailed information on the event with id: {}", eventId);
        log.info("client ip: {}", request.getRemoteAddr());
        return eventService.getEvent(eventId);
    }

    @GetMapping("/int/events/{eventId}")
    public EventClientDto getEventInt(@PathVariable Long eventId,
                                      HttpServletRequest request) {
        log.info("Internal request for detailed information on the event with id: {}", eventId);
        return eventService.getEventInt(eventId);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(
            @PathVariable Long eventId,
            @RequestHeader("X-EWM-USER-ID") Long userId) {

        eventService.likeEvent(userId, eventId);
    }

    @GetMapping("/recommendations")
    public Stream<RecommendedEventProto> getRecommendations(
            @RequestHeader("X-EWM-USER-ID") Long userId,
            @RequestParam(defaultValue = "10") int maxResults) {

        return eventService.getRecommendations(userId, maxResults);
    }
}