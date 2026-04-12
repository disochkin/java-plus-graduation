package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.CommentParam;
import ru.practicum.ewm.service.CommentService;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class InternalCommentController {
    private final CommentService commentService;

    @PostMapping("/int/comments")
    public Map<Long, Long> countCommentForEvents(@RequestBody List<Long> eventIds) {
        log.debug("GET /events/comments: ids = {}", eventIds);
        return commentService.countCommentForEvents(eventIds);
    }
}
