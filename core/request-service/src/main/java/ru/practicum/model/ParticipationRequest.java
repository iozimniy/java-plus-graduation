package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.constants.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "participation_request", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
@Data
@NoArgsConstructor
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id")
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipationRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime created;
}
