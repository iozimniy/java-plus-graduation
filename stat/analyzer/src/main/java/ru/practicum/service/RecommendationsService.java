package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.model.action.UserAction;
import ru.practicum.model.similarity.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationsService {

    private static final int ACTIONS_LIMIT = 20;
    private static final int SIMILARITY_LIMIT = 10;
    private static final double DEFAULT_USER_RATING = 0.4;
    private final UserActionRepository userActionRepository;
    private final UserActionService userActionService;
    private final EventSimilarityRepository eventSimilarityRepository;

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

    public Stream<RecommendedEventProto> getRecommendationForUser(long userId, long maxResults) {
        //получаем последние взаимодействия пользователя с мероприятиями
        List<UserAction> recentUserActions = userActionService.getRecentActionsByUserId(userId, ACTIONS_LIMIT);

        //получаем список похожих мероприятий и сортируем его
        List<Long> userActionEventId = recentUserActions.stream()
                .map(act -> act.getUserActionId().getEventId())
                .toList();
        List<EventSimilarity> similarities = eventSimilarityRepository.findByListEventAOrEventB(userActionEventId);
        similarities.sort(Comparator.comparing(EventSimilarity::getSimilarity).reversed());

        //отбираем мероприятия для рекомендаций
        List<Long> recommendationEventsIds = new ArrayList<>();

        for (EventSimilarity similarity : similarities) {

            Long eventA = similarity.getEventSimilarityId().getEventA();
            Long eventB = similarity.getEventSimilarityId().getEventB();

            //проверяем, подойдёт ли нам рекомендация
            if (userActionEventId.contains(eventA) && userActionEventId.contains(eventB)) continue;

            Long unseenEvent = userActionEventId.contains(eventA) ? eventB : eventA;
            recommendationEventsIds.add(unseenEvent);
        }

        recommendationEventsIds.stream()
                .limit(maxResults)
                .toList();

        //Предсказываем оценку пользователя по каждому из мероприятий
        Map<Long, Double> recommendationUserRating = new HashMap<>();

        for (Long eventId : recommendationEventsIds) {
            //получаем соседей
            List<EventSimilarity> neighbors = eventSimilarityRepository.findTopByEventId(eventId, SIMILARITY_LIMIT);

            //берём только те, с которыми взаимодействовал пользователь
            List<EventSimilarity> userActionsNeighbors = neighbors.stream()
                    .filter(neighbor ->
                            userActionEventId.contains(neighbor.getEventSimilarityId().getEventA()) ||
                                    userActionEventId.contains(neighbor.getEventSimilarityId().getEventB()))
                    .sorted(Comparator.comparing(EventSimilarity::getSimilarity).reversed())
                    .limit(5)
                    .toList();

            //рассчитываем оценки
            if (userActionsNeighbors.isEmpty()) {
                recommendationUserRating.put(eventId, DEFAULT_USER_RATING);
            }

            double sumWeightRating = 0.0;
            double sumSimilarity = 0.0;

            for (EventSimilarity userActionsNeighbor : userActionsNeighbors) {

                sumSimilarity = sumSimilarity + userActionsNeighbor.getSimilarity();

                Long eventA = userActionsNeighbor.getEventSimilarityId().getEventA();
                Long eventB = userActionsNeighbor.getEventSimilarityId().getEventB();

                Long eventAction = userActionEventId.contains(eventA) ? eventA : eventB;
                UserAction userAction = recentUserActions.stream()
                        .filter(act ->
                                act.getUserActionId().getEventId().equals(eventAction))
                        .findFirst().get();

                sumWeightRating = sumWeightRating + (userActionsNeighbor.getSimilarity()
                        * userAction.getWeight());
            }

            recommendationUserRating.put(eventId, sumWeightRating / sumSimilarity);
        }

        return recommendationEventsIds.stream()
                .map(event -> createRecommendationForUser(event, recommendationUserRating))
                .toList().stream();


    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, long maxResults) {
        //получаем похожие мероприятия
        List<EventSimilarity> eventSimilarities =
                eventSimilarityRepository.findByListEventAOrEventB(List.of(eventId));
        eventSimilarities.sort(Comparator.comparing(EventSimilarity::getSimilarity).reversed());

        //Исключаем просмотренные пользователем мероприятия
        List<UserAction> recentUserActions = userActionService.getRecentActionsByUserId(userId, ACTIONS_LIMIT);
        List<Long> userActionEventId = recentUserActions.stream()
                .map(userAction -> userAction.getUserActionId().getEventId())
                .toList();

        List<Long> recommendationEventsIds = new ArrayList<>();

        Map<Long, Double> similarities = new HashMap<>();

        for (EventSimilarity eventSimilarity : eventSimilarities) {
            Long eventA = eventSimilarity.getEventSimilarityId().getEventA();
            Long eventB = eventSimilarity.getEventSimilarityId().getEventB();

            if (userActionEventId.contains(eventA) && userActionEventId.contains(eventB)) continue;

            Long unseenEvent = userActionEventId.contains(eventA) ? eventB : eventA;
            similarities.put(unseenEvent, eventSimilarity.getSimilarity());
            recommendationEventsIds.add(unseenEvent);
        }

        recommendationEventsIds.stream().limit(maxResults);

        return recommendationEventsIds.stream()
                .map(event -> createRecommendationForUser(event, similarities))
                .toList().stream();
    }

    private RecommendedEventProto createEvent(Long id, Double weight) {
        log.info("Create RecommendedEventProto for getInteractionsCount: eventId {}, weight {}",
                id, weight);
        return RecommendedEventProto.newBuilder()
                .setEventId(id)
                .setScore(weight)
                .build();
    }

    private RecommendedEventProto createRecommendationForUser(Long eventId, Map<Long, Double> ratings) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(ratings.get(eventId))
                .build();
    }
}
