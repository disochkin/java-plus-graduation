package ru.practicum.ewm.clients.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.event.EventClientDto;

@Component
@Slf4j
public class EventClientFallback implements EventClient {

    @Override
    public EventClientDto findById(Long eventId) {
        log.warn("Сервис событий недоступен");
        return null;
    }
}
