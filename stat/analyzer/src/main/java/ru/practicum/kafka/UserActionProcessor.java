package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.config.UserActionConsumerConfig;
import ru.practicum.service.UserActionService;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class UserActionProcessor {

    private final Consumer<String, SpecificRecordBase> consumer;
    private final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(100);
    private final UserActionService service;
    @Value("${kafka.topics.user-actions}")
    private String topic;

    public UserActionProcessor(UserActionConsumerConfig config, UserActionService service) {
        this.consumer = new KafkaConsumer<>(config.getUserActionConsumerConfig());
        this.service = service;
    }

    public void start() {
        try {
            consumer.subscribe(List.of(topic));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    UserActionAvro userAction = (UserActionAvro) record.value();
                    service.processUserAction(userAction);
                    log.info("Coming UserAction from collector userId {}, eventId {}, action {}",
                            userAction.getUserId(), userAction.getEventId(), userAction.getActionType());
                }
            }
        } catch (WakeupException e) {
            //тишина
        } catch (Exception e) {
            log.error("Error action processing {}", e.getMessage());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closer userAction consumer");
                consumer.close();
            }
        }
    }
}
