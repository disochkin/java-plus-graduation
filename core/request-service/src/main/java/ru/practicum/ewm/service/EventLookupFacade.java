package ru.practicum.ewm.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.clients.event.EventClient;
import ru.practicum.ewm.clients.user.UserClient;
import ru.practicum.ewm.dto.event.EventClientDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class EventLookupFacade {
    private final EventClient eventClient;

    public EventClientDto findOrThrow(Long eventId) {
        try {
            return eventClient.findById(eventId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException(String.format("User with id = %d not found", eventId));
        }
    }
}