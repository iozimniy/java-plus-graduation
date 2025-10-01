package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class CollectorProducer {

    private final KafkaCollectorClient client;

    @Value("${kafka.producer.topics.user-actions}")
    private String topic;

    public void send(SpecificRecordBase action) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, action);
        client.getProducer().send(record);
        log.info("Send record {}, action {}", record, action);
    }
}
