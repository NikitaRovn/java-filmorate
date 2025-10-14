package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class AgeRating {
    private Integer id;
    private String name;
    private String code;

    public AgeRating(Integer id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public static AgeRating fromCode(String code) {
        switch (code) {
            case "G": return new AgeRating(1, "G", "G");
            case "PG": return new AgeRating(2, "PG", "PG");
            case "PG-13": return new AgeRating(3, "PG-13", "PG-13");
            case "R": return new AgeRating(4, "R", "R");
            case "NC-17": return new AgeRating(5, "NC-17", "NC-17");
            default: throw new IllegalArgumentException("Unknown rating code: " + code);
        }
    }

    public static AgeRating fromId(Integer id) {
        switch (id) {
            case 1: return new AgeRating(1, "G", "G");
            case 2: return new AgeRating(2, "PG", "PG");
            case 3: return new AgeRating(3, "PG-13", "PG-13");
            case 4: return new AgeRating(4, "R", "R");
            case 5: return new AgeRating(5, "NC-17", "NC-17");
            default: throw new IllegalArgumentException("Unknown rating id: " + id);
        }
    }
}