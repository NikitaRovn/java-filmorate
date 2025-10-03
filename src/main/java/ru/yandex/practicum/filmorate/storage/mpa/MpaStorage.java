package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.AgeRating;
import java.util.List;
import java.util.Optional;

public interface MpaStorage {
    List<AgeRating> getAllMpa();

    Optional<AgeRating> getMpaById(Integer id);
}