/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Điều khiển danh sách ghi chú.
 */
public class NoteListController {

    @FXML
    private ListView<Note> noteListView;

    /**
     * Khởi tạo và thiết lập hiển thị danh sách.
     */
    public void initialize() {
        // Gán dữ liệu cho ListView
        noteListView.setItems(NoteManager.getInstance().getNotes());

        // Cấu hình hiển thị từng dòng trong danh sách
        noteListView.setCellFactory(param -> new ListCell<Note>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                
                if (empty || note == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setStyle("-fx-alignment: center-left;");
                    
                    // Hiển thị tiêu đề (cho phép chỉnh sửa trực tiếp)
                    javafx.scene.control.TextField titleField = new javafx.scene.control.TextField();
                    titleField.textProperty().bindBidirectional(note.titleProperty());
                    titleField.getStyleClass().add("list-title-field");
                    titleField.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: transparent; -fx-padding: 0; -fx-text-fill: #333;");
                    
                    // Lưu dữ liệu khi kết thúc chỉnh sửa
                    titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) NoteManager.getInstance().saveNotes();
                    });
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    // Nút Mở ghi chú
                    Button showButton = new Button();
                    showButton.setGraphic(new FontIcon("bi-arrow-right-square"));
                    showButton.getStyleClass().add("action-button");
                    showButton.setOnAction(e -> {
                        if (note.getStage() != null) {
                            note.getStage().show();
                            note.getStage().toFront();
                            if (note.getStage().isIconified()) note.getStage().setIconified(false);
                        }
                    });
                    
                    // Nút Xóa ghi chú
                    Button closeButton = new Button();
                    closeButton.setGraphic(new FontIcon("bi-trash"));
                    closeButton.getStyleClass().addAll("action-button", "delete-button");
                    closeButton.setOnAction(e -> {
                        if (note.getStage() != null) {
                            note.getStage().close();
                        }
                        NoteManager.getInstance().removeNote(note);
                    });
                    
                    hbox.getChildren().addAll(titleField, spacer, showButton, closeButton);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // Nhấn đúp chuột để mở nhanh ghi chú
        noteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null && selectedNote.getStage() != null) {
                    selectedNote.getStage().show();
                    selectedNote.getStage().toFront();
                    if (selectedNote.getStage().isIconified()) selectedNote.getStage().setIconified(false);
                }
            }
        });
    }

    /**
     * Tạo mới ghi chú.
     */
    @FXML
    private void createNewNote() {
        App.newNote();
    }
}
