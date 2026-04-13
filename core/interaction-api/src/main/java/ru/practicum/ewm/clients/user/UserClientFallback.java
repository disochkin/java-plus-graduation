package ru.practicum.ewm.clients.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.user.UserClientDto;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public UserClientDto findByIdInt(Long userId) {
        log.warn("Сервис пользователей недоступен");
        return null;
    }
}
