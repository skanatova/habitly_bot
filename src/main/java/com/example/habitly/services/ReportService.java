package com.example.habitly.services;

import com.example.habitly.entities.Habit;
import com.example.habitly.entities.Report;
import com.example.habitly.entities.User;
import com.example.habitly.repos.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserService userService;

    public ReportService(ReportRepository reportRepository, UserService userService) {
        this.reportRepository = reportRepository;
        this.userService = userService;
    }

    @Transactional
    public boolean isHabitReported(User user, String habit, LocalDate date) {
        Optional<Report> reportOpt = reportRepository.findByUserAndHabitNameAndReportDate(user, habit, date);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            return report.isDone();
        }
        return false;
    }

    @Transactional
    public void markUserReported(User user, String habitName, LocalDate date, boolean reported) {
        Optional<Report> reportOpt = reportRepository.findByUserAndHabitNameAndReportDate(user, habitName, date);

        Report report;
        if (reportOpt.isPresent()) {
            report = reportOpt.get();
        } else {
            report = new Report();
            report.setUser(user);
            report.setHabit(user.getHabits().stream()
                    .filter(habit -> habit.getName().equals(habitName))
                    .findFirst()
                    .orElse(null));
            report.setReportDate(date);
        }
        report.setDone(reported);
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReported(User user, LocalDate date) {
        if (user == null || user.getHabits() == null || user.getHabits().isEmpty()) {
            return false;
        }

        int totalHabits = user.getHabits().size();
        int completedHabits = 0;
        for (Habit habit : user.getHabits()) {
            if (isHabitReported(user, habit.getName(), date)) {
                completedHabits++;
            }
        }

        return totalHabits > 0 && completedHabits == totalHabits;
    }

    @Transactional(readOnly = true)
    public List<Long> getUserIdsNotReportedByDate(LocalDate date) {
        return reportRepository.findUserIdsNotReportedByDate(date);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersWithNoReportOnDate(LocalDate date) {
        return reportRepository.findUsersWithNoReportOnDate(date);
    }

    @Transactional
    public void processEndOfDay(LocalDate date) {
        List<User> usersWithNoReport = getUsersWithNoReportOnDate(date);
        for (User user : usersWithNoReport) {
            if (!user.getHabits().isEmpty()) {
                userService.incrementMissedCount(user);
                Report report = new Report();
                report.setUser(user);
                report.setReportDate(date);
                report.setDone(false);
                reportRepository.save(report);
            }
        }
    }
}
