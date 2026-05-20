package io.github.rikkakawaii0612.classtop.ui;

import io.github.rikkakawaii0612.classtop.model.CourseSlot;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CourseSlotDialog extends Stage {

    private final TextField startField;
    private final TextField endField;
    private final CheckBox selfStudyCheck;
    private boolean confirmed = false;
    private CourseSlot result;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public CourseSlotDialog(CourseSlot existing) {
        setTitle(existing == null ? "新增课程" : "编辑课程");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // 开始时间
        startField = new TextField(existing != null ? existing.start : "08:00");
        startField.setPromptText("HH:mm");
        grid.add(new Label("开始时间:"), 0, 0);
        grid.add(startField, 1, 0);

        // 结束时间
        endField = new TextField(existing != null ? existing.end : "08:45");
        endField.setPromptText("HH:mm");
        grid.add(new Label("结束时间:"), 0, 1);
        grid.add(endField, 1, 1);

        // 在构造函数中为两个字段设置焦点监听
        startField.focusedProperty().addListener((_, _, newVal) -> {
            if (!newVal) { // 失去焦点时格式化
                String normalized = normalizeTime(startField.getText());
                startField.setText(normalized);
            }
        });
        endField.focusedProperty().addListener((_, _, newVal) -> {
            if (!newVal) {
                String normalized = normalizeTime(endField.getText());
                endField.setText(normalized);
            }
        });

        // 自习
        selfStudyCheck = new CheckBox("自习课（不提醒）");
        selfStudyCheck.setSelected(existing != null && existing.selfStudy);
        grid.add(selfStudyCheck, 1, 2);

        // 按钮
        Button okButton = new Button("确定");
        okButton.setOnAction(_ -> {
            if (validateInput()) {
                confirmed = true;
                result = new CourseSlot(startField.getText().trim(), endField.getText().trim(), selfStudyCheck.isSelected());
                close();
            }
        });
        Button cancelButton = new Button("取消");
        cancelButton.setOnAction(_ -> close());

        HBox buttonBox = new HBox(10, okButton, cancelButton);
        buttonBox.setStyle("-fx-alignment: center-right;");
        grid.add(buttonBox, 1, 3);

        setScene(new Scene(grid));
    }

    // 自动补零方法：将 "8:5" -> "08:05"
    private static String normalizeTime(String input) {
        if (input == null || input.isEmpty()) return "";
        String[] parts = input.split(":");
        if (parts.length != 2) return input;
        String hour = parts[0].trim();
        String minute = parts[1].trim();
        // 小时补零到2位
        if (hour.length() == 1) hour = "0" + hour;
        // 分钟补零到2位
        if (minute.length() == 1) minute = "0" + minute;
        return hour + ":" + minute;
    }

    private boolean validateInput() {
        try {
            String ss = startField.getText().trim();
            String se = endField.getText().trim();
            LocalTime start = LocalTime.parse(startField.getText().trim(), TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endField.getText().trim(), TIME_FORMATTER);
            if (!end.isAfter(start)) {
                showAlert("结束时间必须晚于开始时间");
                return false;
            }
            return true;
        } catch (Exception e) {
            showAlert("时间格式错误，请输入 HH:mm");
            return false;
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    public boolean isConfirmed() { return confirmed; }
    public CourseSlot getResult() { return result; }
}