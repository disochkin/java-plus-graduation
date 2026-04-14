package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.CommentParam;

import java.util.List;
import java.util.Map;

public interface CommentService {

    CommentDto create(CommentParam commentParam);

    CommentDto update(CommentParam commentParam);

    void deleteByUser(CommentParam commentParam);

    void deleteByAdmin(Long eventId, Long commentId);

    List<CommentDto> findAllByEventId(CommentParam commentParam);

    Map<Long, Long> countCommentForEvents(List<Long> eventIds);

}
