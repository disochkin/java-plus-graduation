package ru.practicum.ewm.clients.request;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class RequestLookupFacade {
    private final RequestClient requestClient;

    public ParticipationRequestDto findOrThrow(Long userId, Long eventId) {
        try {
            return requestClient.checkUserEventParticipation(eventId, userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }
    }
}