package ru.practicum.ewm.dto.event;

import lombok.Data;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

    private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
}
