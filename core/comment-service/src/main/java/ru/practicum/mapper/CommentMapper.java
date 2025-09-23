package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentEconomDto;
import ru.practicum.comment.dto.CommentOutputDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.model.Comment;

@Component
public class CommentMapper {

    public static CommentOutputDto commentToOutputDto(Comment comment, EventShortDto eventShortDto) {
        return CommentOutputDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .event(eventShortDto)
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }

    public static CommentEconomDto commentToEconomDto(Comment comment) {
        return CommentEconomDto.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .eventId(comment.getEventId())
                .text(comment.getText())
                .created(comment.getCreated())
                .status(comment.getStatus())
                .build();
    }
}
