package ru.practicum.ewm.clients.user;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.Optional;


@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/int/admin/users/{userId}")
    UserDto findByIdInt(@PathVariable @NotNull Long userId);
}
