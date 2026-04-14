package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.ewm.clients")
public class CommentApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CommentApplication.class, args);
    }
}