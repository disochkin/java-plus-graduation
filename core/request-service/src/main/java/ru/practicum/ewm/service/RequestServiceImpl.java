package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.clients.EventLookupFacade;
import ru.practicum.ewm.clients.UserLookupFacade;
import ru.practicum.ewm.dto.event.EventClientDto;
import ru.practicum.ewm.dto.event.EventState;
import ru.practicum.ewm.dto.request.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserLookupFacade userLookupFacade;
    private final RequestMapper requestMapper;
    private final EventLookupFacade eventLookupFacade;

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getUserRequests(Long userId) {
        log.debug("Get requests for user with id = {}", userId);
        userLookupFacade.findOrThrow(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        log.debug("Add participation request: userId = {}, eventId = {}", userId, eventId);
        userLookupFacade.findOrThrow(userId);
        EventClientDto eventClientDto = eventLookupFacade.findOrThrow(eventId);

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        if (eventClientDto.getInitiatorId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (!eventClientDto.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event is not published");
        }

        if (eventClientDto.getParticipantLimit() > 0) {
            Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
            if (confirmedCount >= eventClientDto.getParticipantLimit()) {
                throw new ConflictException("Participant limit reached");
            }
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEventId(eventClientDto.getId());
        request.setRequesterId(userId);

        if (!eventClientDto.getRequestModeration() || eventClientDto.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        Request savedRequest = requestRepository.save(request);
        log.info("Request created: {}", savedRequest);

        return requestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Cancel request: userId = {}, requestId = {}", userId, requestId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id = %d not found", requestId)));

        userLookupFacade.findOrThrow(userId);

        if (!request.getRequesterId().equals(userId)) {
            throw new ConflictException("User is not request owner");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        log.info("Request cancelled: {}", request);

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        log.debug("Get user requests: eventId = {}, userId = {}", eventId, userId);
        userLookupFacade.findOrThrow(userId);
        EventClientDto eventClientDto = eventLookupFacade.findOrThrow(eventId);

        if (!eventClientDto.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not event initiator");
        }

        List<Request> requestList = requestRepository.findByEventId(eventId);

        return requestMapper.toDtoList(requestList);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedRequest(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> raw = requestRepository.countConfirmedRequestsByEventIds(eventIds);

        return raw.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }


    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.debug("Change request status: userId = {}, eventId = {}, update = {}", userId, eventId, updateRequest);

        EventClientDto eventClientDto = eventLookupFacade.findOrThrow(eventId);

        if (!eventClientDto.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not event initiator");
        }

        List<Request> requests = requestRepository.findByIdInAndEventId(updateRequest.getRequestIds(), eventId);

        if (requests.isEmpty()) {
            throw new NotFoundException("Requests not found");
        }

        for (Request request : requests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        Long confirmedCount = requestRepository.countConfirmedRequestsByEventId(eventId);
        int availableSlots = eventClientDto.getParticipantLimit() - confirmedCount.intValue();

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        RequestStatus newStatus = RequestStatus.from(updateRequest.getStatus().toString())
                .orElseThrow(() -> new ValidationException("Invalid status"));

        if (newStatus.equals(RequestStatus.CONFIRMED)) {
            if (eventClientDto.getParticipantLimit() == 0 || !eventClientDto.getRequestModeration()) {
                for (Request request : requests) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(requestMapper.toParticipationRequestDto(request));
                }
            } else {
                for (Request request : requests) {
                    if (availableSlots > 0) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        confirmed.add(requestMapper.toParticipationRequestDto(request));
                        availableSlots--;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        rejected.add(requestMapper.toParticipationRequestDto(request));
                    }
                }

                if (availableSlots == 0) {
                    List<Request> pendingRequests = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
                    for (Request pending : pendingRequests) {
                        if (!updateRequest.getRequestIds().contains(pending.getId())) {
                            pending.setStatus(RequestStatus.REJECTED);
                            requestRepository.save(pending);
                        }
                    }
                }
            }
        } else if (newStatus.equals(RequestStatus.REJECTED)) {
            for (Request request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(requestMapper.toParticipationRequestDto(request));
            }
        }

        requestRepository.saveAll(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }
}