package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.practicum.kafka.CollectorProducer;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserActionHandler {

    private final CollectorProducer producer;
    private final UserActionMapper mapper;

    public void handle(UserActionProto userActionProto) {
      log.info("Processing user action with params userId: {}, eventId: {}, type: {}, timestamp: {}",
              userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType(),
              userActionProto.getTimestamp());

        UserActionAvro userActionAvro = mapper.mapToAvro(userActionProto);
        producer.send(userActionAvro);
        log.info("Sending user action with params userId: {}, eventId: {}, type: {}, timestamp: {}",
                userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType(),
                userActionProto.getTimestamp());
    }
}
