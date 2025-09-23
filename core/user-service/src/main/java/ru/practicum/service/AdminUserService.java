package ru.practicum.service;

import ru.practicum.model.User;
import ru.practicum.user.dto.GetUsersDto;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface AdminUserService {

    List<UserDto> getUsers(GetUsersDto parameters);

    UserDto addUser(NewUserRequest user);

    void deleteUser(Long id);

    User getUser(long id);

    UserDto getUserById(Long userId);
}
