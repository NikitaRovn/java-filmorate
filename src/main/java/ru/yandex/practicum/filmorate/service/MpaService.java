package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.AgeRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import java.util.List;
import java.util.Optional;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<AgeRating> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public Optional<AgeRating> getMpaById(Integer id) {
        return mpaStorage.getMpaById(id);
    }
}