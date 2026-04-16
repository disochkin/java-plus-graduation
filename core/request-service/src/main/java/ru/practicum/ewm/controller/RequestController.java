package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.service.RequestService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestService requestService;
    private final RequestRepository requestRepository;

    @GetMapping("/users/{userId}/requests")
    public List<RequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.debug("GET /users/{}/requests", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable @Positive Long userId,
                                                           @RequestParam @Positive Long eventId) {
        log.info("POST /users/{}/requests?eventId={}", userId, eventId);
        return requestService.addParticipationRequest(userId, eventId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getUserEventRequest(@PathVariable Long userId,
                                                             @PathVariable Long eventId) {
        log.info("User event participation request, userId={}, eventId={}", userId, eventId);
        List<ParticipationRequestDto> participationRequestDtoList = requestService.getUserEventRequests(userId, eventId);
        log.debug("EVENTS: {}", participationRequestDtoList);
        return participationRequestDtoList;
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatusRequest(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Request to change the status of event participation eventId={}, user userId={}", eventId, userId);
        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = requestService.changeRequestStatus(userId,
                eventId,
                eventRequestStatusUpdateRequest);
        log.debug("EVENTS: {}", eventRequestStatusUpdateResult);
        return eventRequestStatusUpdateResult;
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @PostMapping("/int/requests/confirmed")
    public Map<Long, Long> getConfirmedRequest(@RequestBody List<Long> eventIds) {
        log.info("external query getConfirmedRequest eventsIds - {}", eventIds);
        return requestService.getConfirmedRequest(eventIds);
    }

    @GetMapping("/user/{userId}/event/{eventId}/confirmed")
    public Boolean hasUserConfirmedRequest(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED);
    }

}