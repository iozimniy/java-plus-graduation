package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaCollectorConfig implements KafkaCollectorClient {

    private final ProducerConfig producerConfig;

    @Override
    public Producer<String, SpecificRecordBase> getProducer() {
        return new KafkaProducer<>(producerConfig.getProducerProperties());
    }
}
