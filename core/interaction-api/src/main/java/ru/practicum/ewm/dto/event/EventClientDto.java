package ru.practicum.ewm.dto.event;

import lombok.Data;
import ru.practicum.ewm.dto.category.CategoryDto;

import java.time.LocalDateTime;

@Data
public class EventClientDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    private Long initiatorId;

    private Location location;

    private Boolean paid;

    private Integer participantLimit;

    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private Double rating;

    private EventState state = EventState.PENDING;

    private String title;
}