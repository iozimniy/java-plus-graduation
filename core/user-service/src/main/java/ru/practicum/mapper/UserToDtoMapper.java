package ru.practicum.mapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.practicum.model.User;
import ru.practicum.user.dto.UserDto;

import java.util.List;

@Component
public class UserToDtoMapper {

    public User mapUserDtoToUser(UserDto userDto) {
        User user = new User();
        if (!Strings.isBlank(userDto.getName()))
            user.setName(userDto.getName());
        if (!Strings.isBlank(userDto.getEmail()))
            user.setEmail(userDto.getEmail());
        return user;
    }

    public UserDto mapUserToUserDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    //List<User> mapDtoListToUsersList(List<UserDto> userDtos);

    public List<UserDto> mapUsersListToDtoList(List<User> users) {
        return users.stream()
                .map(this::mapUserToUserDto)
                .toList();
    }

}
