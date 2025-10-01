package ru.practicum.model.similarity;

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
@Table(name = "event_similarity")
public class EventSimilarity {

    @EmbeddedId
    private EventSimilarityId eventSimilarityId;
    @Column(name = "similarity")
    private Double similarity;
    @Column(name = "updated")
    private Instant updated;
}
