package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.model.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "requester", source = "request.requesterId")
    @Mapping(target = "event", source = "request.eventId")
    @Mapping(target = "created", source = "request.created", dateFormat = "yyyy-MM-dd HH:mm:ss.SSS")
    RequestDto toDto(Request request);

    @Mapping(target = "event", source = "request.eventId")
    @Mapping(target = "requester", source = "request.requesterId")
    @Mapping(target = "created", source = "request.created", dateFormat = "yyyy-MM-dd HH:mm:ss.SSS")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    List<ParticipationRequestDto> toDtoList(List<Request> requests);

}
