package ru.practicum.ewm.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.UpdateCommentRequest;
import ru.practicum.ewm.model.comment.Comment;
import ru.practicum.ewm.model.event.Event;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {
    public static Comment toNewComment(Long userId, Event event, CommentDto commentDto) {
        Comment comment = new Comment();

        comment.setText(commentDto.getText());
        comment.setUserId(userId);
        comment.setEvent(event);

        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();

        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setUserId(comment.getUserId());
        commentDto.setEventId(comment.getEvent().getId());
        commentDto.setCreatedOn(comment.getCreatedOn());
        commentDto.setEditedOn(comment.getEditedOn());

        return commentDto;
    }

    public static void updateFields(Comment comment, UpdateCommentRequest updateCommentRequest) {
        comment.setText(updateCommentRequest.getText());

        comment.setEditedOn(LocalDateTime.now());
    }
}
