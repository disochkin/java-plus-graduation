package ru.practicum.statserver.server;


import ru.practicum.ewm.dto.stat.EndpointHitDto;
import ru.practicum.ewm.dto.stat.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatService {

    void create(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end,
                               Collection<String> uris, Boolean unique);
}
