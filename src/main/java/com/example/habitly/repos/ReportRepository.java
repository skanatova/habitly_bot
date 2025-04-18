package com.example.habitly.repos;

import com.example.habitly.entities.Report;
import com.example.habitly.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByUserAndReportDate(User user, LocalDate date);

    Optional<Report> findByUserAndHabitNameAndReportDate(User user, String habitName, LocalDate date);

    @Query("SELECT r.user.telegramId FROM Report r WHERE r.reportDate = :date AND r.done = false")
    List<Long> findUserIdsNotReportedByDate(LocalDate date);

    @Query("SELECT u FROM User u WHERE u.telegramId NOT IN " +
            "(SELECT r.user.telegramId FROM Report r WHERE r.reportDate = :date)")
    List<User> findUsersWithNoReportOnDate(LocalDate date);
}
