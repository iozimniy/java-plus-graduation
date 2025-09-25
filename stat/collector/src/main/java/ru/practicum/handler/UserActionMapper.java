package ru.practicum.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserActionMapper {

    public UserActionAvro mapToAvro(UserActionProto userActionProto) {

        ActionTypeAvro actionTypeAvro = ActionTypeMapper.toAvro(userActionProto.getActionType());

        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(actionTypeAvro)
                .setTimestamp(getInstant(userActionProto.getTimestamp()))
                .build();
    }

    private Instant getInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(),
                timestamp.getNanos());
    }
}
