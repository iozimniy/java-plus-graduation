package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class AggregatorService {

    Map<Long, Map<Long, Double>> eventUserWeight = new HashMap<>();
    Map<Long, Double> eventWeightSum = new HashMap<>();
    Map<Long, Map<Long, Double>> eventsMinSum = new HashMap<>();

    private static final Double DEFAULT_WEIGHT = 0.0;
    private static final Double DEFAULT_SUM = 0.0;

    public List<EventSimilarityAvro> calculateSimilarity(UserActionAvro userAction) throws IllegalAccessException {
        log.info("Processing user action with userId {}, eventId {}",
                userAction.getUserId(), userAction.getEventId());

        Long eventId = userAction.getEventId();
        Long userId  = userAction.getUserId();
        Double newWeight = getWeightByAction(userAction.getActionType());

        //проверяем, изменился ли вес, и если изменился, меняем его в eventUserWeight
        Map<Long, Double> eventWeight = eventUserWeight.computeIfAbsent(eventId, e -> new HashMap<>());
        Double oldWeight = eventWeight.getOrDefault(userId, DEFAULT_WEIGHT);

        if (newWeight > oldWeight) {
            log.info("Changes weight for eventId {} of userId {}, new weight = {}", eventId, userId, newWeight);
            eventWeight.put(userId, newWeight);

            //обновляем сумму весов для event
            Double deltaWeight = newWeight - oldWeight;
            changeWeightSum(eventId, deltaWeight);

            //пересчитываем сходства и отправляем результат в кафку
            List<EventSimilarityAvro> similarityAvroList = new ArrayList<>();

            for (Long eventB : eventUserWeight.keySet()) {

                long first  = Math.min(eventId, eventB);
                long second = Math.max(eventId, eventB);

                if (Objects.equals(eventB, eventId)) {
                    continue;
                }

                //мы ранее внесли вес для eventId, потому он точно есть
                Double weightA = eventUserWeight.get(eventId).get(userId);

                Map<Long, Double> eventBmap = eventUserWeight.getOrDefault(eventB, new HashMap<>());
                log.info("USER {}, EVENT-B {}, MAP {}", userId, eventB, eventBmap.toString());
                Double weightB = eventBmap.getOrDefault(userId, DEFAULT_WEIGHT);
                log.info("User {}, EventA {} weight {}, eventB {} weight {}", userId, eventId, weightA, eventB, weightB);

                //пересчитываем similarity только в случае, если пользователь взаимодействовал с eventB
                if (weightB > DEFAULT_WEIGHT) {
                    log.info("Change similarity eventId = {}, eventB = {}", eventId, eventB);

                    Map<Long, Double> minSums = eventsMinSum
                            .computeIfAbsent(first, e -> new HashMap<>());

                    //обновляем сумму минимальных весов
                    Double oldMinSums = minSums
                            .getOrDefault(second, DEFAULT_SUM);

                    Double oldMin = Math.min(oldWeight, weightB);
                    Double newMin = Math.min(newWeight, weightB);
                    Double newSumMinWeights = oldMinSums + (newMin - oldMin);

                    minSums.put(second, newSumMinWeights);
                    eventsMinSum.put(first, minSums);

                    //расчитываем сходство

                    //числитель формулы
                    Double ch = minSums.get(second);

                    //знаменатель формулы
                    Double sumA = eventWeightSum.get(first);
                    Double sumB = eventWeightSum.get(second);
                    Double zn = Math.sqrt(sumA * sumB); //тесты подозрительно не съели произведение корней

                    Double similarityAB = ch / zn;

                    //складываем в similarityAvroList
                    EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro.newBuilder()
                            .setEventA(first)
                            .setEventB(second)
                            .setScore(similarityAB)
                            .setTimestamp(userAction.getTimestamp())
                            .build();
                    similarityAvroList.add(eventSimilarityAvro);
                }
            }

            return similarityAvroList;

        }

        return Collections.EMPTY_LIST;
    }

    private void changeWeightSum(Long eventId, Double deltaWeight) {
        Double oldSum = eventWeightSum.getOrDefault(eventId, DEFAULT_SUM);
        Double newSum = oldSum + deltaWeight;
        eventWeightSum.put(eventId, newSum);
    }

    private double getWeightByAction(ActionTypeAvro type) throws IllegalAccessException {
        switch (type) {
            case VIEW:
                return 0.4;
            case REGISTER:
                return 0.8;
            case LIKE:
                return 1.0;
            default:
                throw new IllegalAccessException("Unknown UserAction " + type);
        }
    }
}
