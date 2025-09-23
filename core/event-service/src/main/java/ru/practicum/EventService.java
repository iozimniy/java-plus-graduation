package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.request.client.ParticipationRequestClient;
import ru.practicum.user.client.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {UserClient.class, ParticipationRequestClient.class})
public class EventService {
    public static void main(String[] args) {
        SpringApplication.run(EventService.class, args);
    }
}