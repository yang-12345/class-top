package io.github.rikkakawaii0612.classtop;

import io.github.rikkakawaii0612.classtop.model.AppConfig;
import io.github.rikkakawaii0612.classtop.ui.ScheduleSettingsWindow;
import io.github.rikkakawaii0612.classtop.ui.SettingsWindow;
import io.github.rikkakawaii0612.classtop.util.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainApp extends Application {
    private SystemTrayManager trayManager;
    private ScheduleManager scheduleManager;
    private LogService logService;
    private AppConfig config;

    @Override
    public void start(Stage primaryStage) {
        // 初始化组件
        config = AppConfig.loadConfig();
        logService = new LogService(config.logPath);

        // 启动今日定时任务
        scheduleManager = new ScheduleManager();
        scheduleManager.scheduleToday(config, logService);
        config.subscribeOnSaved(config -> scheduleManager.scheduleToday(config, logService));

        // 系统托盘（使程序常驻）
        trayManager = new SystemTrayManager();
        trayManager.installTray(this);

        // 不显示主舞台，直接隐藏
        primaryStage.hide();
    }

    public void showSchedules() {
        Platform.runLater(() -> {
            ScheduleSettingsWindow window = new ScheduleSettingsWindow(config);
            window.show();
        });
    }

    public void showSettings() {
        Platform.runLater(() -> {
            SettingsWindow window = new SettingsWindow(config);
            window.show();
        });
    }

    @Override
    public void stop() {
        scheduleManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}