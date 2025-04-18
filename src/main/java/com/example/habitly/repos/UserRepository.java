package com.example.habitly.repos;

import com.example.habitly.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    List<User> findUsersByMissedCountGreaterThan(int missedCount);
}
