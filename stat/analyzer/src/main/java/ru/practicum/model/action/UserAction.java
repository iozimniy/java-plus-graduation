package ru.practicum.model.action;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
