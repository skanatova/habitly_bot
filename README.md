# Habit Tracker Bot User Documentation

## Introduction

Welcome to Habit Tracker Bot! This Telegram bot helps you track daily habits and build consistency with your routine. Whether you're trying to exercise more, read daily, or develop any other positive habit, this bot will help you stay accountable.

## Getting Started

1. Find the bot on Telegram by searching for its username
2. Start a conversation with the bot by sending `/start`
3. The bot will register your account automatically
4. Add habits you want to track using the `/addHabit` command

## Available Commands

### Basic Commands

| Command | Description | Example |
|---------|-------------|---------|
| `/start` | Initialize the bot | `/start` |
| `/help` | Show the list of available commands | `/help` |

### Managing Your Habits

| Command | Description | Example |
|---------|-------------|---------|
| `/addHabit [name]` | Add a new habit to track | `/addHabit meditation` |
| `/removeHabit [name]` | Remove a habit from your list | `/removeHabit running` |
| `/myHabits` | View all your current habits | `/myHabits` |

### Daily Reporting

| Command | Description | Example |
|---------|-------------|---------|
| `/done` | Mark your habits as completed for today | `/done` or `done` |

When you send the `/done` command, the bot will display an interactive list of your habits. Click on each habit to toggle its completion status:
- ❌ means the habit is not completed
- ✅ means the habit is completed

### Group Features

| Command | Description | Example |
|---------|-------------|---------|
| `/status` | See today's status for all group members | `/status` |
| `/leaderboard` | View users ranked by missed habit reports | `/leaderboard` |
| `/remind` | Send reminders to users who haven't reported yet | `/remind` |

## Daily Workflow

1. **Complete your habits** in real life throughout the day
2. **Report completion** by sending `/done` in the chat
3. **Click on each habit** in the interactive menu to mark it as complete
4. **Check the status** of others in your group with `/status`
5. **Receive a daily summary** automatically at the end of the day

## Understanding the Reports

### Daily Status Report

The `/status` command shows:
- Who has completed all their habits (✅)
- Who hasn't completed all habits yet (❌)
- Which specific habits are still pending for each user
- How many total reports each user has missed historically

Example:
```
Today's Status:

Sezim (@sezim): ❌ Not done (Pending: meditation, jogging) | Missed: 5
Aidai (@aidai): ✅ All done | Missed: 2
```

### Leaderboard

The `/leaderboard` command ranks users based on how reliable they are (fewer missed reports = higher rank):

Example:
```
📊 Missed Reports Leaderboard:

1. Aikanysh (@aikanysh): 2 missed
2. Torayim (@torayim): 5 missed
```

### End of Day Report

At the end of each day, the bot automatically generates a summary for the group showing:
- How many habits each person completed vs. their total
- Which specific habits were missed
- Users who miss habits will have their missed count increased

Example:
```
⚠️ Daily Habit Report:

Adina (@adina): 1 / 3 | ❌ Missed: meditation, jogging
Aizhan (@aizhan): 3 / 3
```

## Tips for Success

1. **Be consistent** - Report your habits daily
2. **Use reminders** - The `/remind` command can prompt those who haven't reported yet
3. **Stay accountable** - The group format helps everyone stay motivated
4. **Review your progress** - Check the leaderboard to see your improvement over time

## Troubleshooting

- If you don't see your habits, make sure you've added them using `/addHabit`
- If the bot isn't responding, try sending `/start` again
- Make sure you type habit names exactly as they were created when removing them
- If you need to report habits for today, simply type "done" (not case-sensitive)

## Support

If you encounter any issues or have questions about using the bot, please contact the bot administrator.

Happy habit tracking!