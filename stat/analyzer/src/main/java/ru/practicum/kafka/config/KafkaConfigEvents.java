package ru.practicum.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Getter
@Setter
@Component
public class KafkaConfigEvents implements UserActionConsumerConfig, EventsSimilarityConsumerConfig {

    @Autowired
    private UserActionConsumerConfig userActionConsumer;
    @Autowired
    private EventsSimilarityConsumerConfig eventSimilarityConsumer;

    @Override
    public Properties getUserActionConsumerConfig() {
        return userActionConsumer.getProperties();
    }

    @Override
    public Properties getEventSimilarityConsumerConfig() {
        return eventSimilarityConsumer.getProperties();
    }

    @Getter
    @Setter
    @ConfigurationProperties("kafka.consumer.user-action-consumer")
    @Component
    public static class UserActionConsumerConfig {
        private Properties properties;
    }

    @Getter
    @Setter
    @ConfigurationProperties("kafka.consumer.event-similarity-consumer")
    @Component
    public static class EventsSimilarityConsumerConfig {
        private Properties properties;
    }
}
