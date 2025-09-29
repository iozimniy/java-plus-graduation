package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.similarity.EventSimilarity;
import ru.practicum.model.similarity.EventSimilarityId;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {

    @Query(value = "SELECT * FROM event_similarity e " +
            "WHERE e.event_a IN :eventIds OR e.event_b IN :eventIds", nativeQuery = true)
    List<EventSimilarity> findByListEventAOrEventB(@Param("eventIds") List<Long> eventIds);

    @Query(value = "SELECT * FROM event_similarity " +
            "WHERE event_a = :eventId OR event_b = :eventId " +
            "ORDER BY similarity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<EventSimilarity> findTopByEventId(@Param("eventId") Long eventId,
                                                 @Param("limit") int limit);
}
