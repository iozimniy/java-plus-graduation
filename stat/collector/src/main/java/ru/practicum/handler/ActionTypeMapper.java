package ru.practicum.handler;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;

@Component
public class ActionTypeMapper {
    public static ActionTypeAvro toAvro(ActionTypeProto protoType) {
        switch (protoType) {
            case ACTION_VIEW:
                return ActionTypeAvro.VIEW;
            case ACTION_REGISTER:
                return ActionTypeAvro.REGISTER;
            case ACTION_LIKE:
                return ActionTypeAvro.LIKE;
            default:
                throw new IllegalArgumentException("Unknown ActionTypeProto: " + protoType);
        }
    }
}
