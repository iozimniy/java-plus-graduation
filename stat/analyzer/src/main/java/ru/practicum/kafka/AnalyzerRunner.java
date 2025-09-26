package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyzerRunner implements CommandLineRunner {

    final UserActionProcessor userActionProcessor;

    final EventsSimilarityProcessor eventsSimilarityProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread hubEventsThread = new Thread(eventsSimilarityProcessor);
        hubEventsThread.setName("HubEventHandlerThread");
        hubEventsThread.start();

        userActionProcessor.start();
    }
}
