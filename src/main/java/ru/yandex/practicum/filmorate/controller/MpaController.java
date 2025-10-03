package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.AgeRating;
import ru.yandex.practicum.filmorate.service.MpaService;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/mpa")
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    @Autowired
    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<AgeRating> getAllMpa() {
        log.info("Получен запрос на список всех рейтингов MPA");
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public AgeRating getMpaById(@PathVariable Integer id) {
        log.info("Получен запрос на получение рейтинга MPA с ID: {}", id);
        return mpaService.getMpaById(id)
                .orElseThrow(() -> new NoSuchElementException("Рейтинг MPA с id " + id + " не найден"));
    }
}