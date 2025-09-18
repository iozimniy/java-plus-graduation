package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comment.constants.CommentsStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentsStatus status;

}
