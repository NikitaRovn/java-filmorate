package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность обязательна")
    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    @NotNull(message = "Рейтинг MPA обязателен")
    private AgeRating mpa;
    private Set<Genre> genres = new HashSet<>();
    private Integer mpaId;
    private Set<Integer> genreIds = new HashSet<>();

    // режиссёры
    private Set<Director> directors = new HashSet<>();
    private Set<Integer> directorIds = new HashSet<>();

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }

    public void addGenreId(Integer genreId) {
        genreIds.add(genreId);
    }
}