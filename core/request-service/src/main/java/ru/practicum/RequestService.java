package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.event.client.EventClient;
import ru.practicum.user.client.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {EventClient.class, UserClient.class})
public class RequestService {
    public static void main(String[] args) {
        SpringApplication.run(RequestService.class, args);
    }
}