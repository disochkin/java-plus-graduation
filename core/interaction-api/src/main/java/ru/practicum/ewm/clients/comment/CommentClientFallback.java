package ru.practicum.ewm.clients.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CommentClientFallback implements CommentClient {

    @Override
    public Map<Long, Long> countCommentForEvents(List<Long> eventIds) {
        log.warn("Сервис событий недоступен");
        return null;
    }
}
