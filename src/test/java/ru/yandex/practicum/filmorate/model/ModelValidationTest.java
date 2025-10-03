package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ModelValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void user_InvalidEmail_ShouldFail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Email должен содержать символ @", violations.iterator().next().getMessage());
    }

    @Test
    void user_LoginWithSpaces_ShouldFail() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void user_EmptyLogin_ShouldFail() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("");
        user.setBirthday(LocalDate.now().minusYears(20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<User>> loginError = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("login"))
                .findFirst();

        assertTrue(loginError.isPresent(), "Должна быть ошибка валидации для поля login");
    }

    @Test
    void user_NullBirthday_ShouldFail() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения обязательна", violations.iterator().next().getMessage());
    }

    @Test
    void user_FutureBirthday_ShouldFail() {
        User user = new User();
        user.setEmail("valid@email.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void film_BlankName_ShouldFail() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<Film>> nameError = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("name"))
                .findFirst();

        assertTrue(nameError.isPresent(), "Должна быть ошибка валидации для поля name");
        assertEquals("Название не может быть пустым", nameError.get().getMessage());
    }

    @Test
    void film_DescriptionTooLong_ShouldFail() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<Film>> descriptionError = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("description"))
                .findFirst();

        assertTrue(descriptionError.isPresent(), "Должна быть ошибка валидации для поля description");
        assertEquals("Максимальная длина описания — 200 символов", descriptionError.get().getMessage());
    }

    @Test
    void film_FutureReleaseDate_ShouldFail() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.now().plusDays(1));
        film.setDuration(120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<Film>> releaseDateError = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("releaseDate"))
                .findFirst();

        assertTrue(releaseDateError.isPresent(), "Должна быть ошибка валидации для поля releaseDate");
        assertEquals("Дата релиза не может быть в будущем", releaseDateError.get().getMessage());
    }

    @Test
    void film_NegativeDuration_ShouldFail() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<Film>> durationError = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("duration"))
                .findFirst();

        assertTrue(durationError.isPresent(), "Должна быть ошибка валидации для поля duration");
        assertEquals("Продолжительность должна быть положительным числом", durationError.get().getMessage());
    }

    @Test
    void film_NullMpa_ShouldFail() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());

        Optional<ConstraintViolation<Film>> mpaError = violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals("mpa"))
                .findFirst();

        assertTrue(mpaError.isPresent(), "Должна быть ошибка валидации для поля mpa");
        assertEquals("Рейтинг MPA обязателен", mpaError.get().getMessage());
    }

    @Test
    void film_ValidData_ShouldPass() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Не должно быть ошибок валидации для корректных данных");
    }

    @Test
    void film_EmptyDescription_ShouldPass() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription("");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Пустое описание должно быть допустимо");
    }

    @Test
    void film_NullDescription_ShouldPass() {
        Film film = new Film();
        film.setName("Valid name");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new AgeRating(3, "PG-13", "PG-13"));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "null описание должно быть допустимо");
    }
}