package ru.practicum.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class AggregatorProducer {
    private final AggregatorKafkaClient client;

    @Value("${kafka.topics.events-similarity}")
    private String topic;

    public void send(List<? extends SpecificRecordBase> similarities) {
//        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, similarity);
//        client.getProducer().send(record);
//        log.info("Send record {}, similarity {}", record, similarity);

        for (SpecificRecordBase similarity : similarities) {
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, similarity);
            client.getProducer().send(record);
            log.info("Send record {}, similarity {}", record, similarity);
        }

        client.getProducer().flush();
    }
}