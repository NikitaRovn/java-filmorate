package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Genre {
    private Integer id;
    private String name;

    public Genre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Genre fromName(String name) {
        switch (name) {
            case "Комедия": return new Genre(1, "Комедия");
            case "Драма": return new Genre(2, "Драма");
            case "Мультфильм": return new Genre(3, "Мультфильм");
            case "Триллер": return new Genre(4, "Триллер");
            case "Документальный": return new Genre(5, "Документальный");
            case "Боевик": return new Genre(6, "Боевик");
            default: throw new IllegalArgumentException("Unknown genre name: " + name);
        }
    }

    public static Genre fromId(Integer id) {
        switch (id) {
            case 1: return new Genre(1, "Комедия");
            case 2: return new Genre(2, "Драма");
            case 3: return new Genre(3, "Мультфильм");
            case 4: return new Genre(4, "Триллер");
            case 5: return new Genre(5, "Документальный");
            case 6: return new Genre(6, "Боевик");
            default: throw new IllegalArgumentException("Unknown genre id: " + id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return id.equals(genre.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}