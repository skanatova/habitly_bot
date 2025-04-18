package com.example.habitly.services;

import com.example.habitly.entities.Habit;
import com.example.habitly.entities.User;
import com.example.habitly.repos.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void assignHabit(User user, Habit habit) {
        user.getHabits().add(habit);
        userRepository.save(user);
    }

    public void removeHabit(User user, Habit habit) {
        user.getHabits().remove(habit);
        userRepository.save(user);
    }

    @Transactional
    public void incrementMissedCount(User user) {
        user.setMissedCount(user.getMissedCount() + 1);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsersOrderedByMissedCount() {
        return userRepository.findUsersByMissedCountGreaterThan(0);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Set<Habit> getUserHabits(Long userId) {
        return userRepository.findById(userId)
                .map(User::getHabits)
                .orElse(Set.of());
    }

}
