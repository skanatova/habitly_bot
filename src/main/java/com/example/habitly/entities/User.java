package com.example.habitly.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long telegramId;

    private String username;
    private String firstName;
    private String lastName;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_habits",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "habit_id")
    )
    private Set<Habit> habits = new HashSet<>();
    private Integer missedCount = 0;


    public Long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Habit> getHabits() {
        return habits;
    }

    public void setHabits(Set<Habit> habits) {
        this.habits = habits;
    }

    public Integer getMissedCount() {
        return missedCount;
    }

    public void setMissedCount(Integer missedCount) {
        this.missedCount = missedCount;
    }
}