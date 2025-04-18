package com.example.habitly.repos;

import com.example.habitly.entities.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface HabitRepository extends JpaRepository<Habit, Long> {

    public Habit  findByName(String name);
}
