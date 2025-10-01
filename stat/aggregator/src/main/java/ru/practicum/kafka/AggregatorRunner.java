package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.service.AggregatorProcessor;

@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {
    final AggregatorProcessor aggregatorProcessor;

    @Override
    public void run(String... args) {
        aggregatorProcessor.start();
    }
}
