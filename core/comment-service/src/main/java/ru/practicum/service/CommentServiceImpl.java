package ru.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentEconomDto;
import ru.practicum.comment.dto.CommentOutputDto;
import ru.practicum.comment.dto.CommentPagedDto;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.comment.constants.CommentsOrder;
import ru.practicum.comment.constants.CommentsStatus;
import ru.practicum.commons.errors.AccessDeniedException;
import ru.practicum.commons.errors.ForbiddenActionException;
import ru.practicum.repository.CommentRepository;
import ru.practicum.user.client.UserClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.mapper.CommentMapper.commentToOutputDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final UserClient userClient;

    private final EventClient eventClient;

    private final CommentRepository commentRepository;

    @Override
    public CommentPagedDto getComments(Long eventId, int page, int size, CommentsOrder sort) {
        if (eventId == null || eventId <= 0)
            throw new IllegalArgumentException("Event ID must be a positive number.");
        if (page <= 0)
            throw new IllegalArgumentException("Page number must be positive and greater than 0.");
        if (size <= 0)
            throw new IllegalArgumentException("Page size must be positive and greater than 0.");
        if (sort == null)
            throw new IllegalArgumentException("Sort parameter cannot be null.");

//        eventRepository.findById(eventId)
//                .orElseThrow(() -> new EntityNotFoundException("Event with " + id + " not found"));

        EventShortDto eventShortDto = eventClient.getShortEvent(eventId);

        Sort sortType = sort == CommentsOrder.NEWEST ?
                Sort.by("id").descending() : Sort.by("id").ascending();

        Pageable pageable = PageRequest.of(page - 1, size, sortType);

        Page<Comment> commentPage = commentRepository
                .findByEventIdAndStatus(eventId, CommentsStatus.PUBLISHED, pageable);

        List<CommentOutputDto> comments = commentPage.getContent().stream()
                .map(comment -> commentToOutputDto(comment, eventShortDto))
                .collect(Collectors.toList());

        return CommentPagedDto.builder()
                .page(page)
                .total(commentPage.getTotalPages())
                .comments(comments)
                .build();
    }

    @Override
    @Transactional
    public CommentEconomDto addComment(Long userId, CommentDto commentDto) {

        EventFullDto eventFullDto = null;

        try {
            eventFullDto = eventClient.getEventAnyStatusWithViews(commentDto.getEventId());
        } catch (Exception e) {
            log.error("Request for get event id {} with any status with views is failed with error {}",
                    commentDto.getEventId(), e);
        }

        Comment comment = Comment.builder()
                .userId(userClient.getUser(userId).getId())
                .eventId(eventFullDto.getId())
                .text(commentDto.getText())
                .created(LocalDateTime.now())
                .status(CommentsStatus.PUBLISHED)
                .build();
        return CommentMapper.commentToEconomDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentEconomDto updateComment(CommentDto dto) {
        Comment comment = getComment(dto.getId());
        if (!comment.getUserId().equals(dto.getUserId())) {
            throw new AccessDeniedException("User " + dto.getUserId() + "can't edit this comment.");
        }
        comment.setText(dto.getText());
        log.info("CommentServiceImpl: Comment for update {}", comment);
        return CommentMapper.commentToEconomDto(commentRepository.save(comment));
    }

    @Override
    public Comment getComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with " + id + " not found"));
    }

    @Transactional
    @Override
    public void deleteById(Long commentId) {
        Comment comment = getComment(commentId);

        if (!comment.getStatus().equals(CommentsStatus.PUBLISHED)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new ForbiddenActionException("The comment's status doesn't allow it to be deleted");
        }
    }

    @Transactional
    @Override
    public void softDelete(Long userId, Long commentId) {
        Comment comment = getComment(commentId);

        if (!comment.getUserId().equals(userId)) {
           throw new AccessDeniedException("Not enough rights");
        }

        comment.setStatus(CommentsStatus.DELETED);
        commentRepository.save(comment);
    }
}
