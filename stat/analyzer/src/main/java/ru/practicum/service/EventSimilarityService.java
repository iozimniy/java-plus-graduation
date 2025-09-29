package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.similarity.EventSimilarity;
import ru.practicum.model.similarity.EventSimilarityId;
import ru.practicum.repository.EventSimilarityRepository;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EventSimilarityService {

    private final EventSimilarityRepository repository;

    private static final Double DEFAULT_SIMILARITY = 0.0;

    public void processEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Processing event similarity with eventA {}, eventB {}, similarity {}",
                eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB(), eventSimilarityAvro.getScore());

        EventSimilarityId eventSimilarityId = EventSimilarityId.builder()
                .eventA(eventSimilarityAvro.getEventA())
                .eventB(eventSimilarityAvro.getEventB())
                .build();

        EventSimilarity eventSimilarity = repository.findById(eventSimilarityId)
                .orElse(new EventSimilarity(eventSimilarityId,
                        DEFAULT_SIMILARITY, eventSimilarityAvro.getTimestamp()));

        if (!Objects.equals(eventSimilarity.getSimilarity(), eventSimilarityAvro.getScore())) {
            log.info("Change similarity for eventA {} and eventB {}",
                    eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
            eventSimilarity.setSimilarity(eventSimilarityAvro.getScore());
            eventSimilarity.setUpdated(eventSimilarityAvro.getTimestamp());
            repository.save(eventSimilarity);
        } else {
            log.info("Similarity for eventA {} and eventB {} is the same: old similarity: {}, " +
                    "new similarity", eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB(),
                    eventSimilarity.getSimilarity(), eventSimilarityAvro.getScore());
        }
    }
}
