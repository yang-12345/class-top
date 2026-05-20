package io.github.rikkakawaii0612.classtop.ui;

import io.github.rikkakawaii0612.classtop.model.CourseSlot;
import io.github.rikkakawaii0612.classtop.model.WeeklySchedule;
import io.github.rikkakawaii0612.classtop.model.AppConfig;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

public class ScheduleSettingsWindow extends Stage {
    private final Map<DayOfWeek, DaySchedulePanel> panels = new LinkedHashMap<>();
    private final AppConfig config;

    public ScheduleSettingsWindow(AppConfig config) {
        this.config = config;
        setTitle("课程时间设置");
        setMinWidth(500);
        setMinHeight(400);

        TabPane tabPane = new TabPane();
        // 按周一到周日顺序创建标签页
        for (DayOfWeek day :  DayOfWeek.values()) {
            List<CourseSlot> courses = getCoursesForDay(config, day);
            DaySchedulePanel panel = new DaySchedulePanel(day, courses);
            panels.put(day, panel);
            Tab tab = new Tab(day.getDisplayName(TextStyle.FULL, Locale.CHINESE), panel);
            tab.setClosable(false);
            tabPane.getTabs().add(tab);
        }

        // 底部保存按钮
        Button saveButton = new Button("保存并关闭");
        saveButton.setOnAction(_ -> {
            saveToConfig();
            close();
        });

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(saveButton);
        BorderPane.setMargin(saveButton, new Insets(10));

        setScene(new Scene(root, 600, 450));
    }

    private List<CourseSlot> getCoursesForDay(AppConfig config, DayOfWeek day) {
        WeeklySchedule schedule = config.schedule;
        if (schedule == null) return new ArrayList<>();
        return schedule.getDailySchedule(day);
    }

    private void saveToConfig() {
        WeeklySchedule schedule = config.schedule;
        if (schedule == null) {
            schedule = new WeeklySchedule();
            config.schedule = schedule;
        }
        for (Map.Entry<DayOfWeek, DaySchedulePanel> entry : panels.entrySet()) {
            List<CourseSlot> list = new ArrayList<>(entry.getValue().getCourses());
            schedule.setDailySchedule(entry.getKey(), list);
        }
        config.save();  // 调用之前的保存方法
    }
}