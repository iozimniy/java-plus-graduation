package ru.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.producer.EventSimilarityProducerConfig;

import java.util.Properties;

@Getter
@Setter
@Component
public class AggregatorKafkaConfig implements UserActionConsumerConfig, EventSimilarityProducerConfig {

    @Autowired
    private ConsumerConfig consumerConfig;

    @Autowired
    private ProducerConfig producerConfig;

    @Override
    public Properties getUserActionConsumerConfig() {
        return consumerConfig.getProperties();
    }

    @Override
    public Properties getEventSimilarityProducerConfig() {
        return producerConfig.getProperties();
    }

    @Getter
    @Setter
    @ConfigurationProperties("kafka.consumer")
    @Component
    public static class ConsumerConfig {
        private Properties properties;
    }

    @Getter
    @Setter
    @ConfigurationProperties("kafka.producer")
    @Component
    public static class ProducerConfig {
        private Properties properties;
    }
}
