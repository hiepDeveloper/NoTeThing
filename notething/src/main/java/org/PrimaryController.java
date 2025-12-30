/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import javafx.fxml.FXML;

/**
 * Điều khiển cửa sổ ghi chú.
 */
public class PrimaryController {

    @FXML
    private javafx.scene.layout.VBox rootPane;

    @FXML
    private javafx.scene.layout.HBox headerBox;

    @FXML
    private javafx.scene.control.TextField noteTitle;

    @FXML
    private javafx.scene.control.TextArea noteContent;
    
    @FXML
    private javafx.scene.control.Button alwaysOnTopButton;
    
    @FXML
    private org.kordamp.ikonli.javafx.FontIcon alwaysOnTopIcon;

    private double xOffset = 0;
    private double yOffset = 0;
    
    private static final int RESIZE_MARGIN = 10;
    
    private Note note;
    private int savedCaretPosition = 0;

    /**
     * Khởi tạo và thiết lập sự kiện.
     */
    public void initialize() {
        // Xử lý sự kiện kéo dãn (resize)
        rootPane.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, event -> {
            boolean nearEdge = isNearEdge(event.getSceneX(), event.getSceneY(), 
                                         rootPane.getScene().getWidth(), rootPane.getScene().getHeight());
            
            // Cho phép chuột tương tác với cạnh cửa sổ
            noteContent.setMouseTransparent(nearEdge);
            if (noteTitle != null) {
                noteTitle.setMouseTransparent(nearEdge);
            }
        });

        // Tự động focus vào nội dung khi cửa sổ hiển thị
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.showingProperty().addListener((obs3, oldShow, isShowing) -> {
                            if (isShowing) {
                                focusContent();
                            }
                        });
                        
                        // Ẩn/hiện header khi focus thay đổi
                        newWin.focusedProperty().addListener((obs3, wasFocused, isFocused) -> {
                            toggleHeaderVisibility(isFocused);
                        });
                    }
                });
            }
        });
    }

    private boolean isNearEdge(double x, double y, double width, double height) {
        return x < RESIZE_MARGIN || x > width - RESIZE_MARGIN ||
               y < RESIZE_MARGIN || y > height - RESIZE_MARGIN;
    }
    
    public void setNote(Note note) {
        this.note = note;
        
        if (noteTitle != null) {
            noteTitle.textProperty().bindBidirectional(note.titleProperty());
            // Lưu dữ liệu khi kết thúc chỉnh sửa tiêu đề
            noteTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) NoteManager.getInstance().saveNotes();
            });
        }
        noteContent.textProperty().bindBidirectional(note.contentProperty());
        
        // Cập nhật UI cho nút Always on Top
        updateAlwaysOnTopUI(note.isAlwaysOnTop());
        
        // Lắng nghe thay đổi trạng thái Always on Top
        note.alwaysOnTopProperty().addListener((obs, oldVal, newVal) -> {
            updateAlwaysOnTopUI(newVal);
            NoteManager.getInstance().saveNotes();
        });
    }

    /**
     * Tập trung con trỏ vào vùng nhập nội dung.
     */
    public void focusContent() {
        if (noteContent != null) {
            noteContent.requestFocus();
            // Đặt con trỏ ở cuối văn bản
            noteContent.positionCaret(noteContent.getText().length());
        }
    }

    /**
     * Hiển thị danh sách ghi chú.
     */
    @FXML
    private void openNoteList() {
        try {
            javafx.stage.Stage listStage = NoteManager.getInstance().getNoteListStage();
            if (listStage != null) {
                listStage.show();
                listStage.toFront();
                if (listStage.isIconified()) listStage.setIconified(false);
            } else {
                 App.showNoteList(new javafx.stage.Stage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thu nhỏ cửa sổ ghi chú.
     */
    @FXML
    private void minimizeNote() {
        javafx.stage.Stage stage = (javafx.stage.Stage) headerBox.getScene().getWindow();
        stage.setIconified(true);
    }

    /**
     * Đóng và lưu trạng thái ghi chú.
     */
    @FXML
    private void closeNote() {
        javafx.stage.Stage stage = (javafx.stage.Stage) headerBox.getScene().getWindow();
        stage.close();
        NoteManager.getInstance().saveNotes();
    }

    @FXML
    private void handleMousePressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(javafx.scene.input.MouseEvent event) {
        javafx.stage.Stage stage = (javafx.stage.Stage) headerBox.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
    
    /**
     * Bật/tắt chế độ Always on Top.
     */
    @FXML
    private void toggleAlwaysOnTop() {
        if (note != null) {
            note.setAlwaysOnTop(!note.isAlwaysOnTop());
        }
    }
    
    /**
     * Cập nhật giao diện nút Always on Top.
     */
    private void updateAlwaysOnTopUI(boolean isAlwaysOnTop) {
        if (alwaysOnTopIcon != null) {
            if (isAlwaysOnTop) {
                alwaysOnTopIcon.setIconLiteral("bi-pin-angle-fill");
                if (alwaysOnTopButton != null) {
                    alwaysOnTopButton.setStyle("-fx-background-color: rgba(100, 150, 255, 0.3);");
                }
            } else {
                alwaysOnTopIcon.setIconLiteral("bi-pin-angle");
                if (alwaysOnTopButton != null) {
                    alwaysOnTopButton.setStyle("");
                }
            }
        }
    }
    
    /**
     * Ẩn/hiện header khi focus thay đổi.
     */
    private void toggleHeaderVisibility(boolean isFocused) {
        if (headerBox != null) {
            if (isFocused) {
                // Hiện header khi được focus
                headerBox.setVisible(true);
                headerBox.setManaged(true);
                
                // Tự động focus vào noteContent và khôi phục vị trí con trỏ
                javafx.application.Platform.runLater(() -> {
                    if (noteContent != null) {
                        noteContent.requestFocus();
                        // Khôi phục vị trí con trỏ đã lưu
                        noteContent.positionCaret(savedCaretPosition);
                        
                        // Workaround: Insert và xóa ký tự để kích hoạt con trỏ nhấp nháy
                        // Đặc biệt quan trọng khi TextArea trống
                        if (noteContent.getText().isEmpty()) {
                            noteContent.insertText(0, " ");
                            noteContent.deleteText(0, 1);
                            noteContent.positionCaret(0);
                        }
                    }
                });
            } else {
                // Lưu vị trí con trỏ hiện tại trước khi ẩn header
                if (noteContent != null) {
                    savedCaretPosition = noteContent.getCaretPosition();
                }
                
                // Ẩn header khi mất focus
                headerBox.setVisible(false);
                headerBox.setManaged(false);
            }
        }
    }
}

