package ru.practicum.ewm.dto.event;

import lombok.Data;
import ru.practicum.ewm.dto.request.RequestUpdateAction;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private RequestUpdateAction status;
}
