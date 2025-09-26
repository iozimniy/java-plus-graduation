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
import ru.practicum.kafka.config.EventsSimilarityConsumerConfig;
import ru.practicum.service.RecommendationsService;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class EventsSimilarityProcessor implements Runnable {
    private final Consumer<String, SpecificRecordBase> consumer;
    private final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(100);
    private final RecommendationsService service;
    @Value("${kafka.topics.events-similarity}")
    private String topic;

    public EventsSimilarityProcessor(EventsSimilarityConsumerConfig config, RecommendationsService service) {
        this.consumer = new KafkaConsumer<>(config.getEventSimilarityConsumerConfig());
        this.service = service;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(topic));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    UserActionAvro userAction = (UserActionAvro) record.value();
                    //service.processUserAction(userAction);
                    log.info("Coming UserAction from collector userId {}, eventId {}",
                            userAction.getUserId(), userAction.getEventId());
                }
            }
        } catch (WakeupException e) {
            //тишина
        } catch (Exception e) {
            log.error("Error event processing {}", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closer event similarity consumer");
                consumer.close();
            }
        }
    }
}
