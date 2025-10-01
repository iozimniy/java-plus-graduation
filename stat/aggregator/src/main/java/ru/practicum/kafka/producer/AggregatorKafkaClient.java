package ru.practicum.kafka.producer;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface AggregatorKafkaClient {
    Producer<String, SpecificRecordBase> getProducer();
}
