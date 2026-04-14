package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.service.CommentService;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class InternalCommentController {
    private final CommentService commentService;

    @PostMapping("/int/comments")
    public Map<Long, Long> countCommentForEvents(@RequestBody List<Long> eventIds) {
        log.debug("GET /events/comments: ids = {}", eventIds);
        return commentService.countCommentForEvents(eventIds);
    }
}
