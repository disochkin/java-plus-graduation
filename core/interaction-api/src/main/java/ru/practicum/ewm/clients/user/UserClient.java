package ru.practicum.ewm.clients.user;

import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.dto.user.UserClientDto;


@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/int/admin/users/{userId}")
    UserClientDto findByIdInt(@PathVariable @NotNull Long userId);
}
