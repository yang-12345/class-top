package io.github.rikkakawaii0612.classtop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    private SystemTrayManager trayManager;
    private ScheduleManager scheduleManager;
    private LogService logService;
    private AppConfig config;
    private AutoStartManager autoStartManager;

    @Override
    public void start(Stage primaryStage) {
        // 初始化组件
        config = AppConfig.loadConfig("./config.json");
        logService = new LogService(config.getLogPath());
        autoStartManager = new AutoStartManager();

        // 启动今日定时任务
        scheduleManager = new ScheduleManager();
        scheduleManager.startTodaySchedule(config, logService);

        // 系统托盘（使程序常驻）
        trayManager = new SystemTrayManager();
        trayManager.installTray(this);

        // 不显示主舞台，直接隐藏
        primaryStage.hide();
    }

    // 由托盘调用的设置窗口
    public void showSettings() {
        Platform.runLater(() -> {
            SettingsWindow settings = new SettingsWindow(autoStartManager, config);
            settings.show();
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