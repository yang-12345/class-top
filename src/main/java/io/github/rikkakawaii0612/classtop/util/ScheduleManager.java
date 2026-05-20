package io.github.rikkakawaii0612.classtop.util;

import io.github.rikkakawaii0612.classtop.model.AppConfig;
import io.github.rikkakawaii0612.classtop.model.CourseSlot;
import io.github.rikkakawaii0612.classtop.model.WeeklySchedule;
import io.github.rikkakawaii0612.classtop.ui.ReminderWindow;
import javafx.application.Platform;
import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class ScheduleManager {
    private ScheduledExecutorService scheduler;
    private ReminderWindow currentWindow;
    private volatile boolean buttonClicked = false;

    public ScheduleManager() {
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    public void scheduleToday(AppConfig config, LogService logService) {
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(2);

        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        List<CourseSlot> todayCourses = getTodayCourses(config.schedule, dayOfWeek);

        if (todayCourses.isEmpty()) return;

        // 遍历今天每节课
        for (CourseSlot course : todayCourses) {
            if (course.selfStudy) {
                // 自习课，不弹窗
                continue;
            }
            LocalTime startTime = LocalTime.parse(course.start);
            LocalTime endTime = LocalTime.parse(course.end);

            long delayToStart = delayToTarget(startTime, today);
            long delayToEnd = delayToTarget(endTime, today);

            if (delayToStart >= 0) {
                scheduler.schedule(() -> showReminderWindow(logService, today),
                        delayToStart, TimeUnit.MILLISECONDS);
            }
            if (delayToEnd >= 0) {
                scheduler.schedule(() -> autoCloseWindow(logService, today),
                        delayToEnd, TimeUnit.MILLISECONDS);
            }
        }
    }

    private List<CourseSlot> getTodayCourses(WeeklySchedule schedule, DayOfWeek day) {
        if (schedule == null) return Collections.emptyList();
        return schedule.getDailySchedule(day);
    }

    private long delayToTarget(LocalTime targetTime, LocalDate today) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = LocalDateTime.of(today, targetTime);
        return Duration.between(now, target).toMillis();
    }

    private void showReminderWindow(LogService logService, LocalDate today) {
        buttonClicked = false;
        Platform.runLater(() -> currentWindow = new ReminderWindow(() -> {
            buttonClicked = true;
            logService.logButtonClick(today, LocalTime.now(), true);
        }, () -> {
            buttonClicked = true;
            logService.logUnclicked(today, LocalTime.now());
        }));
    }

    private void autoCloseWindow(LogService logService, LocalDate today) {
        Platform.runLater(() -> {
            if (currentWindow != null && currentWindow.isShowing()) {
                currentWindow.close();
                currentWindow = null;
                if (!buttonClicked) {
                    logService.logUnclicked(today, LocalTime.now());
                }
            }
        });
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}