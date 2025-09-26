package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.repository.UserActionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationsService {

    private final UserActionRepository userActionRepository;

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIdList) {
        log.info("Request for interactions weight for events with ids {}", eventIdList);

        if (eventIdList.isEmpty()) {
            return Stream.empty();
        }

        List<Object[]> rows = userActionRepository.sumWeightByEventsIds(eventIdList);
        Map<Long, Double> weightMap = rows.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Double) row[1]
        ));

        List<RecommendedEventProto> weightList = eventIdList.stream()
                .map(id -> createEvent(id, weightMap.getOrDefault(id, 0.0)))
                .toList();

        return weightList.stream();
    }

    private RecommendedEventProto createEvent(Long id, Double weight) {
        return RecommendedEventProto.newBuilder()
                .setEventId(id)
                .setScore(weight)
                .build();
    }
}
