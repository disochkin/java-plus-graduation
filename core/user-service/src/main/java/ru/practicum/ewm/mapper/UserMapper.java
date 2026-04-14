package ru.practicum.ewm.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserClientDto;
import ru.practicum.ewm.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User toNewUser(NewUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return user;
    }

    public static UserClientDto toUserDto(User user) {
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setId(user.getId());
        userClientDto.setName(user.getName());
        userClientDto.setEmail(user.getEmail());
        return userClientDto;
    }
}
