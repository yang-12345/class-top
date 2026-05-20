package io.github.rikkakawaii0612.classtop.ui;

import io.github.rikkakawaii0612.classtop.model.CourseSlot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.MouseButton;

import java.time.DayOfWeek;
import java.util.List;

public class DaySchedulePanel extends VBox {
    private final ObservableList<CourseSlot> courses;
    private final ListView<CourseSlot> listView;
    private final DayOfWeek day;

    public DaySchedulePanel(DayOfWeek day, List<CourseSlot> initialList) {
        this.day = day;
        courses = FXCollections.observableArrayList(initialList);
        listView = new ListView<>(courses);
        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(CourseSlot item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String label = String.format("%s - %s %s",
                            item.start, item.end,
                            item.selfStudy ? "[自习]" : "");
                    setText(label);
                }
            }
        });

        // 双击或右键编辑
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                editSelected();
            }
        });

        Button addButton = new Button("+");
        addButton.setOnAction(_ -> {
            CourseSlotDialog dialog = new CourseSlotDialog(null);
            dialog.showAndWait();
            if (dialog.isConfirmed()) {
                courses.add(dialog.getResult());
            }
        });

        Button editButton = new Button("编辑");
        editButton.setOnAction(_ -> editSelected());

        Button deleteButton = new Button("删除");
        deleteButton.setOnAction(_ -> {
            CourseSlot selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                courses.remove(selected);
            }
        });

        HBox buttonBar = new HBox(10, addButton, editButton, deleteButton);
        buttonBar.setStyle("-fx-padding: 5 0 0 0;");

        getChildren().addAll(listView, buttonBar);
        VBox.setVgrow(listView, Priority.ALWAYS);
    }

    private void editSelected() {
        CourseSlot selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            CourseSlotDialog dialog = new CourseSlotDialog(selected);
            dialog.showAndWait();
            if (dialog.isConfirmed()) {
                int index = courses.indexOf(selected);
                courses.set(index, dialog.getResult());
            }
        }
    }

    public ObservableList<CourseSlot> getCourses() {
        return courses;
    }

    public DayOfWeek getDay() { return day; }
}