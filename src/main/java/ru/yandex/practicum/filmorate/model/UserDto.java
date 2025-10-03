package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserDto {
    private Integer userId;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}