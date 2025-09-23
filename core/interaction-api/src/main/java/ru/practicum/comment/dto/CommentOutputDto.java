package ru.practicum.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comment.constants.CommentsStatus;
import ru.practicum.event.dto.EventShortDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentOutputDto {

    private Long id;

    private Long userId;

    private EventShortDto event;

    private String text;

    private LocalDateTime created;

    private CommentsStatus status;

}
