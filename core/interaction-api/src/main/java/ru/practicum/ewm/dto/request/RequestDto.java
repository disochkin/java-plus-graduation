package ru.practicum.ewm.dto.request;

import lombok.Data;

@Data
public class RequestDto {
    private Long id;

    private Long requester;

    private Long event;

    private String created;

    private RequestStatus status;
}
