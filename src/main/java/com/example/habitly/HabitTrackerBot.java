package com.example.habitly;

import com.example.habitly.entities.Habit;
import com.example.habitly.entities.User;
import com.example.habitly.services.HabitService;
import com.example.habitly.services.ReportService;
import com.example.habitly.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class HabitTrackerBot extends TelegramLongPollingBot {

    private static final String MISSING_HABIT_ERROR = "Please specify habit name: /%s [habit name]";
    private static final String NO_HABITS_MESSAGE = "You don't have any habits assigned yet.";
    private static final String UNREGISTERED_USER_MESSAGE = "Your user account is not registered. Please use /start first.";

    private final HabitService habitService;
    private final UserService userService;
    private final ReportService reportService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.groupId}")
    private Long groupId;

    @Autowired
    public HabitTrackerBot(HabitService habitService, UserService userService, ReportService reportService) {
        this.habitService = habitService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception e) {
//            log.error("Error processing update: {}", e.getMessage(), e);

            // Try to send error message to user if possible
            if (update.hasMessage()) {
                sendMessage(update.getMessage().getChatId(), "An error occurred while processing your request.");
            }
        }
    }

    private void handleTextMessage(Message message) {
        // Register user if needed
        registerUser(message);

        String messageText = message.getText();
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        try {
            if (messageText.startsWith("/")) {
                handleCommand(messageText, chatId, userId);
            } else {
                sendMessage(chatId, "Use '/help' to see available commands or send 'done' to mark your tasks as completed for today.");
            }
        } catch (Exception e) {
//            log.error("Error processing message: {}", e.getMessage(), e);
            sendMessage(chatId, "Error processing your message: " + e.getMessage());
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        if (callbackData.startsWith("toggle_")) {
            try {
                Long habitId = Long.parseLong(callbackData.substring(7));
                updateHabitToggleMessage(chatId, messageId, userId, habitId);
            } catch (NumberFormatException e) {
//                log.error("Invalid habit ID in callback data: {}", callbackData, e);
            }
        }
    }

    private void updateHabitToggleMessage(Long chatId, Integer messageId, Long userId, Long habitId) {
        try {
            EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
            editMarkup.setChatId(chatId.toString());
            editMarkup.setMessageId(messageId);
            editMarkup.setReplyMarkup(generateUpdatedMarkup(userId, habitId));

            execute(editMarkup);
        } catch (TelegramApiException e) {
//            log.error("Failed to update message markup: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Transactional
    public void registerUser(Message message) {
        org.telegram.telegrambots.meta.api.objects.User telegramUser = message.getFrom();
        String userName = telegramUser.getUserName();

        if (userName != null && userService.findByUsername(userName).isEmpty()) {
            User user = new User();
            user.setTelegramId(telegramUser.getId());
            user.setUsername(userName);
            user.setFirstName(telegramUser.getFirstName());
            user.setLastName(telegramUser.getLastName());
            userService.save(user);

//            log.info("Registered new user: {}", user.getUsername());
        }
    }

    private void handleCommand(String command, Long chatId, Long userId) {
        String[] parts = command.trim().split("\\s+", 2);
        String mainCommand = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        switch (mainCommand) {
            case "/start":
                sendMessage(chatId, "Welcome to Habit Tracker Bot! Use /help to see available commands.");
                break;

            case "/help":
                sendHelpMessage(chatId);
                break;

            case "/addHabit":
                if (!args.isEmpty()) {
                    handleAddHabit(chatId, userId, args);
                } else {
                    sendMessage(chatId, String.format(MISSING_HABIT_ERROR, "addHabit"));
                }
                break;

            case "/removeHabit":
                if (!args.isEmpty()) {
                    handleRemoveHabit(chatId, userId, args);
                } else {
                    sendMessage(chatId, String.format(MISSING_HABIT_ERROR, "removeHabit"));
                }
                break;

            case "/done":
                markUserReported(chatId, userId);
                break;

            case "/myHabits":
                listUserHabits(chatId, userId);
                break;

            case "/status":
                showDailyStatus(chatId);
                break;

            case "/leaderboard":
                showLeaderboard(chatId);
                break;

            case "/remind":
                sendReminders(chatId);
                break;

            case "/endOfDay":
                processEndOfDay();
                break;

            default:
                sendMessage(chatId, "Unknown command. Use /help for available commands.");
                break;
        }
    }

    private void sendHelpMessage(Long chatId) {
        StringBuilder help = new StringBuilder();
        help.append("**Available Commands:**\n\n");
        help.append("/start - Start the bot\n");
        help.append("/help - Show this help message\n");
        help.append("/addHabit [habit] - Add a habit to your list\n");
        help.append("/removeHabit [habit] - Remove a habit from your list\n");
        help.append("/myHabits - List your habits\n");
        help.append("/status - Show daily status for all users\n");
        help.append("/leaderboard - Show users ranked by missed reports\n");
        help.append("/remind - Send reminders to users who haven't reported\n\n");
        help.append("**Daily Reporting:**\n");
        help.append("To mark your habits as done for today, simply send 'done' in the chat.");

        sendMessage(chatId, help.toString());
    }

    void handleAddHabit(Long chatId, Long userId, String habitName) {
        User user = getUser(chatId, userId);
        if (user == null) return;

        Habit habit = getOrCreateHabit(habitName);

        userService.assignHabit(user, habit);
        sendMessage(chatId, "Habit '" + habitName + "' was successfully added");
    }

    private Habit getOrCreateHabit(String habitName) {
        return habitService.findHabitByName(habitName).orElseGet(() -> {
            Habit newHabit = new Habit();
            newHabit.setName(habitName);
            return habitService.saveHabit(newHabit);
        });
    }

    void handleRemoveHabit(Long chatId, Long userId, String habitName) {
        User user = getUser(chatId, userId);
        if (user == null) return;

        Optional<Habit> habitOpt = habitService.findHabitByName(habitName);
        if (habitOpt.isEmpty()) {
            sendMessage(chatId, "Habit '" + habitName + "' not found.");
            return;
        }

        userService.removeHabit(user, habitOpt.get());
        sendMessage(chatId, "Habit '" + habitName + "' has been removed from your list.");
    }

    private void listUserHabits(Long chatId, Long userId) {
        User user = getUser(chatId, userId);
        if (user == null) return;

        Set<Habit> habits = user.getHabits();
        if (habits.isEmpty()) {
            sendMessage(chatId, NO_HABITS_MESSAGE);
            return;
        }

        StringBuilder response = new StringBuilder("Your habits:\n\n");
        habits.stream()
                .sorted(Comparator.comparing(Habit::getName))
                .forEach(habit -> response.append("- ").append(habit.getName()).append("\n"));

        sendMessage(chatId, response.toString());
    }

    private void markUserReported(Long chatId, Long userId) {
        User user = getUser(chatId, userId);
        if (user == null) return;

        Set<Habit> habits = user.getHabits();
        if (habits.isEmpty()) {
            sendMessage(chatId, NO_HABITS_MESSAGE);
            return;
        }

        sendHabitToggleKeyboard(chatId, userId, habits);
    }

    private void sendHabitToggleKeyboard(Long chatId, Long userId, Set<Habit> habits) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Your habits (сlick to mark/unmark):");
        message.setReplyMarkup(createHabitKeyboard(userId, habits, null));

        try {
            execute(message);
        } catch (TelegramApiException e) {
//            log.error("Failed to send habit toggle keyboard: {}", e.getMessage(), e);
        }
    }

    private void showDailyStatus(Long chatId) {
        LocalDate today = LocalDate.now();
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            sendMessage(chatId, "No users registered yet.");
            return;
        }

        StringBuilder status = new StringBuilder("Today's Status:\n\n");

        users.stream()
                .filter(user -> !user.getHabits().isEmpty())
                .forEach(user -> appendUserStatus(status, user, today));

        sendMessage(chatId, status.toString());
    }

    private void appendUserStatus(StringBuilder status, User user, LocalDate date) {
        status.append(formatUserName(user));

        List<String> incompletedHabits = getPendingHabits(user, date);
        status.append(incompletedHabits.isEmpty() ? "✅ All done" : "❌");

        if (!incompletedHabits.isEmpty()) {
            status.append(": ");
            status.append(String.join(", ", incompletedHabits));
        }

        status.append(" | Missed: ").append(user.getMissedCount()).append("\n");
    }

    private List<String> getPendingHabits(User user, LocalDate date) {
        List<String> incompletedHabits = new ArrayList<>();
        for (Habit habit : user.getHabits()) {
            if (!reportService.isHabitReported(user, habit.getName(), date)) {
                incompletedHabits.add(habit.getName());
            }
        }
        return incompletedHabits;
    }

    private void showLeaderboard(Long chatId) {
        List<User> users = userService.getAllUsers()
                .stream()
                .filter(user -> !user.getHabits().isEmpty())
                .sorted(Comparator.comparingInt(User::getMissedCount).reversed())
                .toList();

        if (users.isEmpty()) {
            sendMessage(chatId, "No users registered yet.");
            return;
        }

        StringBuilder leaderboard = new StringBuilder("📊 Missed Reports Leaderboard:\n\n");

        int rank = 1;
        for (User user : users) {
            leaderboard.append(rank++).append(". ");
            leaderboard.append(formatUserName(user));
            leaderboard.append(user.getMissedCount()).append(" missed\n");
        }

        sendMessage(chatId, leaderboard.toString());
    }

    public void sendReminders(Long chatId) {
        LocalDate today = LocalDate.now();
        List<User> usersToRemind = reportService.getUsersWithNoReportOnDate(today).stream()
                .filter(user -> !user.getHabits().isEmpty())
                .toList();

        if (usersToRemind.isEmpty()) {
            sendMessage(chatId, "Everyone has reported for today! Great job!");
            return;
        }

        StringBuilder reminderMessage = new StringBuilder("⚠️ Reminder: These users still need to report their habits for today:\n\n");

        for (User user : usersToRemind) {
            reminderMessage.append("- ");
            reminderMessage.append(formatUserName(user));
            reminderMessage.append("(missed: ").append(user.getMissedCount()).append(")\n");
        }

        sendMessage(chatId, reminderMessage.toString());

        // Send individual reminders
        for (User user : usersToRemind) {
            try {
                String userReminder = "Hi " + user.getFirstName() + "! Don't forget to report your habits for today by using '/done'";
                sendMessage(user.getTelegramId(), userReminder);
            } catch (Exception e) {
//                log.error("Failed to send private reminder to user {}: {}", user.getTelegramId(), e.getMessage());
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        if (chatId == null) {
//            log.error("Cannot send message: Chat ID is null");
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
//            log.error("Failed to send message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    @PostConstruct
    public void start() {
//        log.info("Habit Tracker Bot started successfully!");
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void processEndOfDay() {
        LocalDate today = LocalDate.now();
        List<User> users = userService.getAllUsers();
        StringBuilder groupMessage = new StringBuilder("⚠️ Daily Habit Report:\n\n");

        for (User user : users) {
            Set<Habit> habits = user.getHabits();
            if (habits.isEmpty()) {
                continue; // Skip users with no habits
            }

            processDailyUserReport(groupMessage, user, habits, today);
        }

        sendMessage(groupId, groupMessage.toString());
    }

    @Scheduled(cron = "0 0 21 * * *")
    public void runDailyReminders() {
        sendReminders(groupId);
    }

    @Transactional
    void processDailyUserReport(StringBuilder message, User user, Set<Habit> habits, LocalDate date) {
        List<String> notDoneHabits = getPendingHabits(user, date);

        message.append(formatUserName(user));
        message.append(habits.size() - notDoneHabits.size()).append(" / ").append(habits.size());

        if (!notDoneHabits.isEmpty()) {
            message.append(" | ❌ Missed: ");
            message.append(String.join(", ", notDoneHabits));
            userService.incrementMissedCount(user);
        }

        message.append("\n");
    }

    private InlineKeyboardMarkup generateUpdatedMarkup(Long userId, Long toggledHabitId) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) return new InlineKeyboardMarkup();

        Set<Habit> habits = user.getHabits();
        return createHabitKeyboard(userId, habits, toggledHabitId);
    }

    private InlineKeyboardMarkup createHabitKeyboard(Long userId, Set<Habit> habits, Long toggledHabitId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Habit habit : sortedHabits(habits)) {
            InlineKeyboardButton button = new InlineKeyboardButton();

            boolean shouldToggle = habit.getId().equals(toggledHabitId);
            if (shouldToggle) {
                User user = userService.findById(userId).orElse(null);
                if (user != null) {
                    boolean currentState = reportService.isHabitReported(user, habit.getName(), today);
                    reportService.markUserReported(user, habit.getName(), today, !currentState);
                }
            }

            boolean reported = isHabitReported(userId, habit.getName());
            String emoji = reported ? "✅" : "❌";
            button.setText(emoji + " " + habit.getName());
            button.setCallbackData("toggle_" + habit.getId());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private List<Habit> sortedHabits(Set<Habit> habits) {
        return habits.stream()
                .sorted(Comparator.comparing(Habit::getName))
                .toList();
    }

    private boolean isHabitReported(Long userId, String habitName) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) return false;

        return reportService.isHabitReported(user, habitName, LocalDate.now());
    }

    private User getUser(Long chatId, Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, UNREGISTERED_USER_MESSAGE);
            return null;
        }
        return userOpt.get();
    }

    private String formatUserName(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append(user.getFirstName());
        if (user.getUsername() != null) {
            sb.append(" (@").append(user.getUsername()).append(")");
        }
        sb.append(": ");
        return sb.toString();
    }
}