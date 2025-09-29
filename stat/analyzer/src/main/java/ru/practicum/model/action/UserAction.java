package ru.practicum.model.action;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "user_action")
public class UserAction {
    @EmbeddedId
    UserActionId userActionId;
    @Column(name = "weight")
    private Double weight;
    @Column(name = "updated")
    private Instant updated;
}
