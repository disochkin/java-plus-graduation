package ru.practicum.ewm.clients.comment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.clients.FeignRetryConfig;

import java.util.List;
import java.util.Map;


@FeignClient(name = "comment-service",
        configuration = FeignRetryConfig.class,
        fallback = CommentClientFallback.class)
public interface CommentClient {
    @GetMapping("/int/comments")
    Map<Long, Long> countCommentForEvents(@RequestBody List<Long> eventIds);
}
