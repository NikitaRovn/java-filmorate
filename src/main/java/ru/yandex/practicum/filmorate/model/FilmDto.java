package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FilmDto {
    private Integer filmId;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Integer ratingId;
}