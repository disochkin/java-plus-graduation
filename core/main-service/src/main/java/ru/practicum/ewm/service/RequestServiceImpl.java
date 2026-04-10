package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.event.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.event.EventState;
import ru.practicum.ewm.model.request.Request;
import ru.practicum.ewm.model.request.RequestStatus;
import ru.practicum.ewm.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserLookupFacade userLookupFacade;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        log.debug("Get participants: userId = {}, eventId = {}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (!event.getInitiatorId().equals(userId)) {
            throw new ConflictException("User is not event initiator");
        }


        return requestRepository.findByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {
        log.debug("Change request status: userId = {}, eventId = {}, update = {}", userId, eventId, updateRequest);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (!event.getInitiatorId().equals(userId)) {
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
        int availableSlots = event.getParticipantLimit() - confirmedCount.intValue();

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        RequestStatus newStatus = RequestStatus.from(updateRequest.getStatus().toString())
                .orElseThrow(() -> new ValidationException("Invalid status"));

        if (newStatus.equals(RequestStatus.CONFIRMED)) {
            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                for (Request request : requests) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.toDto(request));
                }
            } else {
                for (Request request : requests) {
                    if (availableSlots > 0) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        confirmed.add(RequestMapper.toDto(request));
                        availableSlots--;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        rejected.add(RequestMapper.toDto(request));
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
                rejected.add(RequestMapper.toDto(request));
            }
        }

        requestRepository.saveAll(requests);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }
}