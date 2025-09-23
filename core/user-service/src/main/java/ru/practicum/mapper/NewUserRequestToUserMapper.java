package ru.practicum.mapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.practicum.model.User;
import ru.practicum.user.dto.NewUserRequest;

@Component
public class NewUserRequestToUserMapper {

    public User mapNewUserRequestToUser(NewUserRequest newUserRequest) {
        User user = new User();
        if (!Strings.isBlank(newUserRequest.getName()))
            user.setName(newUserRequest.getName());
        if (!Strings.isBlank(newUserRequest.getEmail()))
            user.setEmail(newUserRequest.getEmail());
        return user;
    }

}
