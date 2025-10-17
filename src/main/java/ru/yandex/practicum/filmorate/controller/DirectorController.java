package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/directors")
@Slf4j
public class DirectorController {
    private final DirectorStorage directorStorage;

    public DirectorController(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Получен запрос на список всех режиссёров");
        return directorStorage.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        log.info("Запрос на получение режиссёра с id={}", id);
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NoSuchElementException("Режиссёр с id=" + id + " не найден"));
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        Director newDirector = directorStorage.createDirector(director);
        log.info("Добавление режиссёра: {}", newDirector);
        return newDirector;
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        directorStorage.getDirectorById(director.getId())
                .orElseThrow(() -> new NoSuchElementException("Режиссёр с id=" + director.getId() + " не найден"));
        Director updatedDirector = directorStorage.updateDirector(director);
        log.info("Обновлен режиссёр: {}", updatedDirector);
        return updatedDirector;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        directorStorage.deleteDirectorById(id);
    }
}
