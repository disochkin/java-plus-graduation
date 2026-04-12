package ru.practicum.ewm.clients.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RequestClientFallback implements RequestClient {

    @Override
    public ParticipationRequestDto checkUserEventParticipation(Long eventId, Long userId) {
        log.warn("Сервис запросов недоступен, eventId={}, userId={}", eventId, userId);
        return null;
    }

    @Override
    public Map<Long, Long> getConfirmedRequest(List<Long> eventIds) {
        log.warn("Сервис запросов недоступен, eventIds={}", eventIds);
        return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
    }
}
