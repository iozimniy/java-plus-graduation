package ru.practicum.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AggregatorProducerConfig implements AggregatorKafkaClient {
    private final EventSimilarityProducerConfig config;

    @Override
    public Producer<String, SpecificRecordBase> getProducer() {
        return new KafkaProducer<>(config.getEventSimilarityProducerConfig());
    }
}
