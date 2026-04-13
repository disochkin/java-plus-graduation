package ru.practicum.ewm.clients.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.clients.FeignRetryConfig;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;


@FeignClient(name = "request-service",
        configuration = FeignRetryConfig.class,
        fallback = RequestClientFallback.class)

public interface RequestClient {
    @GetMapping("/int/requests/{eventId}/participation/{userId}")
    ParticipationRequestDto checkUserEventParticipation(@PathVariable @NotNull Long eventId,
                                                        @PathVariable @NotNull Long userId);

    @PostMapping("/int/requests/confirmed")
    Map<Long, Long> getConfirmedRequest(@RequestBody List<Long> eventIds);
}
