package ru.practicum.kafka;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties("kafka")
public class KafkaConfig implements ProducerConfig {

    @Autowired
    private CollectorProducerConfig producerConfig;

    @Override
    public Properties getProducerProperties() {
        return producerConfig.getProperties();
    }


    @Getter
    @ConfigurationProperties("kafka.producer")
    @Component
    public static class CollectorProducerConfig {
        private final Properties properties;

        public CollectorProducerConfig(Properties properties) {
            this.properties = properties;
        }
    }
}
