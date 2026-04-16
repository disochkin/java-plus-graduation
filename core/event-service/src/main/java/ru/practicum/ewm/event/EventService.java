package ru.practicum.ewm.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface EventService {
    EventFullDto create(Long userId, NewEventDto newEventDto);

    Collection<EventShortDto> getEventByUserId(EventInitiatorIdFilter eventInitiatorIdFilter,
                                               Pageable pageable);

    EventFullDto getEventFullDescription(Long userId, Long eventId);

    EventFullDto updateEventByCreator(Long userId, Long eventId, UpdateEventRequest updateEventRequest);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> adminSearchEvents(EventAdminFilter eventAdminFilter, PageRequestDto pageRequestDto);

    List<EventShortDto> publicSearchEvents(EventPublicFilter eventPublicFilter, PageRequestDto pageRequestDto);

    EventFullDto getEvent(Long eventId);

    EventClientDto getEventInt(Long eventId);

    Stream<RecommendedEventProto> getRecommendations(Long userId, int maxResults);

    void likeEvent(Long userId, Long eventId);
}
