package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.UserActionConsumerConfig;
import ru.practicum.kafka.producer.AggregatorProducer;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class AggregatorProcessor {
    private final Consumer<String, SpecificRecordBase> consumer;
    private final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(50);
    private final AggregatorService service;
    private final AggregatorProducer producer;
    @Value("${kafka.topics.user-actions}")
    private String topic;


    public AggregatorProcessor(UserActionConsumerConfig config,
                               AggregatorService service,
                               AggregatorProducer producer) {
        this.consumer = new KafkaConsumer<>(config.getUserActionConsumerConfig());
        this.service = service;
        this.producer = producer;
    }

    public void start() {
        try {
            log.info("Start aggregator consumer");
            consumer.subscribe(List.of(topic));

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    UserActionAvro userAction = (UserActionAvro) record.value();
                    log.info("Coming UserAction from collector userId {}, eventId {}",
                            userAction.getUserId(), userAction.getEventId());

                    List<EventSimilarityAvro> results = service.calculateSimilarity(userAction);
                    if (!results.isEmpty()) {
                        producer.send(results); // теперь пачкой
                    }
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
