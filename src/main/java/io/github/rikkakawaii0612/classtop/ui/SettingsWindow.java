package io.github.rikkakawaii0612.classtop.ui;

import io.github.rikkakawaii0612.classtop.model.AppConfig;
import io.github.rikkakawaii0612.classtop.util.AutoStartManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SettingsWindow extends Stage {
    private final AppConfig config;
    private CheckBox autoStartCheckbox;

    public SettingsWindow(AppConfig config) {
        this.config = config;

        setTitle("教师日志提醒 - 设置");
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f4f8;");

        // 开机自启开关
        autoStartCheckbox = new CheckBox("开机自动启动");
        autoStartCheckbox.setSelected(AutoStartManager.isAutoStartEnabled());
        autoStartCheckbox.setFont(javafx.scene.text.Font.font("微软雅黑", 16));

        // 保存按钮
        Button saveButton = new Button("保存设置");
        saveButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 20;");
        saveButton.setOnAction(_ -> saveSettings());

        // 关闭按钮
        Button closeButton = new Button("关闭");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 20;");
        closeButton.setOnAction(_ -> hide());

        HBox buttonBox = new HBox(10, saveButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(autoStartCheckbox, buttonBox);

        Scene scene = new Scene(root, 400, 200);
        setScene(scene);
    }

    private void saveSettings() {
        // 根据勾选状态设置自启
        if (autoStartCheckbox.isSelected()) {
            AutoStartManager.enableAutoStart();
        } else {
            AutoStartManager.disableAutoStart();
        }

        // 保存到 config.json（可选：记录用户偏好）
        config.autoStart = autoStartCheckbox.isSelected();
        config.save();

        Alert alert = new Alert(Alert.AlertType.INFORMATION, "设置已保存！");
        alert.showAndWait();
    }
}