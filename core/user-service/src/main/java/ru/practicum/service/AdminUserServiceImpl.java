package ru.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mapper.NewUserRequestToUserMapper;
import ru.practicum.mapper.UserToDtoMapper;
import ru.practicum.model.User;
import ru.practicum.repository.AdminUserRepository;
import ru.practicum.user.dto.GetUsersDto;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserRepository adminUserRepository;

    private final UserToDtoMapper userToDtoMapper;

    private final NewUserRequestToUserMapper userShortMapper;

    @Override
    public List<UserDto> getUsers(GetUsersDto parameters) {
        log.info("\nAdminUserService.getAllUsers {}", parameters);
        int page = parameters.getFrom() / parameters.getSize();
        Pageable pageable = PageRequest.of(page, parameters.getSize());
        Page<User> response = parameters.getIds().isEmpty() ? adminUserRepository.findAll(pageable)
                : adminUserRepository.findByIds(parameters.getIds(), pageable);
        List<User> users = response.getContent().stream().toList();
        return userToDtoMapper.mapUsersListToDtoList(users);
    }

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        log.info("\nAdminUserService.addUser {}", newUserRequest);
        User newUser = userShortMapper.mapNewUserRequestToUser(newUserRequest);
        return userToDtoMapper.mapUserToUserDto(adminUserRepository.save(newUser));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User oldUser = getUser(id);
        adminUserRepository.deleteById(id);
    }

    //используется для получения user при необходимости и проверок существования
    @Override
    public User getUser(long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with " + id + " not found"));
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with " + userId + " not found"));

        return userToDtoMapper.mapUserToUserDto(user);
    }

}
