package ru.practicum.ewm.clients.event;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.dto.event.EventClientDto;


@FeignClient(name = "main-service")
public interface EventClient {
    @GetMapping("/int/events/{eventId}")
    EventClientDto findById(@PathVariable @NotNull Long eventId);
}
