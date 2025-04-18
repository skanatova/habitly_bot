package com.example.habitly.services;

import com.example.habitly.entities.Habit;
import com.example.habitly.repos.HabitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HabitService {

    private HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public Habit saveHabit(Habit habit) {
        return habitRepository.save(habit);
    }

    public void deleteHabit(Habit habit) {
        habitRepository.delete(habit);
    }

    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    public Optional<Habit> findHabitByName(String name) {
        return Optional.ofNullable(habitRepository.findByName(name));
    }
}
