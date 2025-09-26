package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;
import ru.practicum.model.UserActionId;
import ru.practicum.repository.UserActionRepository;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserActionService {

    private final UserActionRepository repository;
    private static final Double DEFAULT_WEIGHT = 0.0;

    @Transactional
    public void processUserAction(UserActionAvro userActionAvro) throws IllegalAccessException {
        log.info("Processing user action with userId {}, eventId {}",
                userActionAvro.getUserId(), userActionAvro.getEventId());

        Double actionWeight = getWeightByAction(userActionAvro.getActionType());

        UserActionId userActionId = UserActionId.builder()
                .userId(userActionAvro.getUserId())
                .eventId(userActionAvro.getEventId())
                .build();
        UserAction userAction = repository.findById(userActionId)
                .orElse(new UserAction(userActionId, DEFAULT_WEIGHT, userActionAvro.getTimestamp()));


        if (userAction.getWeight() < actionWeight) {
            userAction.setWeight(actionWeight);
            repository.save(userAction);
        } else {
            log.info("Action weight of action {} is the same: action weight {}, old weigh: {}",
                    userActionId, actionWeight, userAction.getWeight());
        }
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
