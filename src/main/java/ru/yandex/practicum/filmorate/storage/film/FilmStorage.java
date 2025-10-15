package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    List<Film> findPopularFilms(int count, Integer genreId, Integer year);

    Optional<Film> findById(Integer id);

    void deleteFilmById(Long id);

    boolean existsById(Integer id);
}