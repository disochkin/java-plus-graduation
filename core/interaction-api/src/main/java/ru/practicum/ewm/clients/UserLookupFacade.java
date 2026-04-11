package ru.practicum.ewm.clients;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.clients.user.UserClient;
import ru.practicum.ewm.dto.user.UserClientDto;
import ru.practicum.ewm.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class UserLookupFacade {
    private final UserClient userClient;

    public UserClientDto findOrThrow(Long userId) {
        try {
            return userClient.findByIdInt(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }
    }
}