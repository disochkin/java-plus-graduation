package ru.practicum.ewm.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseDto {
    private int code;
    private String message;
}
