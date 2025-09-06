package ru.practicum.user.client;

import jakarta.validation.constraints.Min;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.user.dto.UserDto;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {

    @GetMapping("/{userId}")
    UserDto getUser(@PathVariable Long userId);
}
