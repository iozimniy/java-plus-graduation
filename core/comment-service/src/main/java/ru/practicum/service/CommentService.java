package ru.practicum.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentEconomDto;
import ru.practicum.comment.dto.CommentPagedDto;
import ru.practicum.model.Comment;
import ru.practicum.comment.constants.CommentsOrder;

public interface CommentService {

    CommentPagedDto getComments(Long eventId, int page, int size, CommentsOrder sort);

    CommentEconomDto addComment(Long userId, CommentDto commentDto);

    CommentEconomDto updateComment(CommentDto dto);

    Comment getComment(Long id);

    void softDelete(Long userId, Long commentId);

    void deleteById(Long commentId);
}
