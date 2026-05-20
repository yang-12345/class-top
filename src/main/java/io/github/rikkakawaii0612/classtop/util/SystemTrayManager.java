package io.github.rikkakawaii0612.classtop.util;

import io.github.rikkakawaii0612.classtop.MainApp;
import io.github.rikkakawaii0612.classtop.ui.ReminderWindow;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SystemTrayManager {
    private Stage menuStage; // 自定义菜单窗口
    private MainApp mainApp;

    public void installTray(MainApp app) {
        this.mainApp = app;
        Platform.setImplicitExit(false);

        if (!SystemTray.isSupported()) {
            return;
        }
        SystemTray tray = SystemTray.getSystemTray();

        java.awt.Image image = Toolkit.getDefaultToolkit()
                .getImage(SystemTrayManager.class.getClassLoader().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(image, "Class Top");
        trayIcon.setImageAutoSize(true);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {   // 右键点击（不同平台触发方式略有差异）
                    Platform.runLater(() -> showCustomMenu(e.getX(), e.getY()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Platform.runLater(() -> showCustomMenu(e.getX(), e.getY()));
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 显示自定义的 JavaFX 菜单 Stage
     * @param x 鼠标点击的屏幕 X 坐标
     * @param y 鼠标点击的屏幕 Y 坐标
     */
    private void showCustomMenu(int x, int y) {
        if (menuStage != null) {
            this.closeMenu();
        }

        // 创建菜单项
        Button schedulesButton = new Button("课程时间");
        Button settingsButton = new Button("程序设置");
        Button reminderButton = new Button("唤出提醒");
        Button exitButton = new Button("退出程序");

        // 设置菜单项样式（不使用 CSS 文件，直接代码设置）
        String itemStyle = "-fx-background-color: transparent; -fx-text-fill: #333; " +
                "-fx-font-size: 13px; -fx-padding: 8 15 8 15; -fx-alignment: center-left;";
        schedulesButton.setStyle(itemStyle);
        settingsButton.setStyle(itemStyle);
        reminderButton.setStyle(itemStyle);
        exitButton.setStyle(itemStyle);

        // 鼠标悬浮效果
        String hoverStyle = itemStyle + "-fx-background-color: #e0e0e0;";
        schedulesButton.setOnMouseEntered(_ -> schedulesButton.setStyle(hoverStyle));
        schedulesButton.setOnMouseExited(_ -> schedulesButton.setStyle(itemStyle));
        settingsButton.setOnMouseEntered(_ -> settingsButton.setStyle(hoverStyle));
        settingsButton.setOnMouseExited(_ -> settingsButton.setStyle(itemStyle));
        reminderButton.setOnMouseEntered(_ -> reminderButton.setStyle(hoverStyle));
        reminderButton.setOnMouseExited(_ -> reminderButton.setStyle(itemStyle));
        exitButton.setOnMouseEntered(_ -> exitButton.setStyle(hoverStyle));
        exitButton.setOnMouseExited(_ -> exitButton.setStyle(itemStyle));

        // 菜单项功能
        schedulesButton.setOnAction(_ -> {
            this.mainApp.showSchedules();
            closeMenu();
        });
        settingsButton.setOnAction(_ -> {
            this.mainApp.showSettings();
            closeMenu();
        });
        reminderButton.setOnAction(_ -> {
            new ReminderWindow(null, null);
            closeMenu();
        });
        exitButton.setOnAction(_ -> {
            Platform.exit();
            System.exit(0);
        });

        // 容器 VBox
        VBox menuBox = new VBox(schedulesButton, settingsButton, reminderButton, exitButton);

        menuBox.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.web("#f8f8f8"), new CornerRadii(5), javafx.geometry.Insets.EMPTY)));
        menuBox.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));
        menuBox.setEffect(new DropShadow(5, javafx.scene.paint.Color.gray(0.4)));

        Stage parentStage = new Stage();
        parentStage.initStyle(StageStyle.UTILITY);
        parentStage.setOpacity(0);
        parentStage.setWidth(1);
        parentStage.setHeight(1);
        parentStage.setX(-10000);
        parentStage.setY(-10000);
        parentStage.show();

        // 创建舞台
        Stage stage = new Stage();
        stage.initOwner(parentStage);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setScene(new Scene(menuBox));

        // 自动关闭：点击菜单外部区域
        stage.getScene().setOnMousePressed(event -> {
            // 检查鼠标是否点在菜单窗口外部（简单实现：直接关闭）
            // 更严谨的做法：判断 event 的目标是否属于菜单内的节点，这里因为场景只有一个菜单，外部点击自动关闭即可
            if (!menuBox.getBoundsInLocal().contains(event.getSceneX(), event.getSceneY())) {
                Platform.runLater(this::closeMenu);
            }
        });
        // 失去焦点时关闭
        stage.focusedProperty().addListener((_, _, newVal) -> {
            if (!newVal) {
                Platform.runLater(this::closeMenu);
            }
        });

        stage.show();

        // 设置位置：鼠标坐标处（可根据屏幕边缘调整偏移）
        stage.setX(x);
        stage.setY(y - stage.getHeight());
        menuStage = parentStage;
    }

    private void closeMenu() {
        if (menuStage != null) {
            menuStage.hide();
        }
    }
}