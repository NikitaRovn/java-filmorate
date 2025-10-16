package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    private Integer id;

    @NotBlank(message = "Имя режиссёра должно быть заполнено")
    private String name;

    public Director(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
