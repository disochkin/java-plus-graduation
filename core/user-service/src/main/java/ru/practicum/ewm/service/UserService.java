package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserClientDto;
import ru.practicum.ewm.dto.user.UserParam;

import java.util.List;

public interface UserService {

    List<UserClientDto> get(UserParam userParam);

    UserClientDto create(NewUserRequest request);

    UserClientDto findById(Long userId);

    void deleteById(Long userId);
}
