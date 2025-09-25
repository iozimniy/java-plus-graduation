package ru.practicum.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;
import com.google.protobuf.Timestamp;

import java.time.Instant;

@Service
@Slf4j
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType, Instant timestamp) {

        log.info("Sending user action with userId: {}, eventId: {}, type: {}, timestamp: {}",
                userId, eventId, actionType, timestamp);

        try {
            Timestamp protoTimestamp = Timestamp.newBuilder()
                    .setSeconds(timestamp.getEpochSecond())
                    .setNanos(timestamp.getNano())
                    .build();

            UserActionProto userActionProto = UserActionProto.newBuilder()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setActionType(actionType)
                    .setTimestamp(protoTimestamp)
                    .build();

            client.collectUserAction(userActionProto);
            log.debug("Sent user action with userId: {}, eventId: {}, type: {}, timestamp: {}",
                    userId, eventId, actionType, timestamp);
        } catch (StatusRuntimeException e) {
            log.error("Sending user action with userId: {}, eventId: {}, type: {}, timestamp: {} failed",
                    userId, eventId, actionType, timestamp);
            throw new RuntimeException("Sending user action failed", e);
        }

    }
}
