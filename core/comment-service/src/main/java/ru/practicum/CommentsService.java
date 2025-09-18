package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.event.client.EventClient;
import ru.practicum.user.client.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {EventClient.class, UserClient.class})
public class CommentsService {
    public static void main(String[] args) {
        SpringApplication.run(CommentsService.class, args);
    }
}