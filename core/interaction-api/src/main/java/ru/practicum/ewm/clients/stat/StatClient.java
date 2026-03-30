package ru.practicum.ewm.clients.stat;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.stat.EndpointHitDto;
import ru.practicum.ewm.dto.stat.ViewStatsDto;

import java.util.List;

@FeignClient(name = "stat-server")
public interface StatClient {
    @PostMapping
    void hit(@RequestBody EndpointHitDto endpointHitDto);

    @GetMapping
    List<ViewStatsDto> getStats(@RequestParam(name = "start") String start,
                                      @RequestParam(name = "end") String end,
                                      @RequestParam(name = "uris", required = false) List<String> uris,
                                      @RequestParam(name = "unique", defaultValue = "false", required = false) Boolean unique);
}
