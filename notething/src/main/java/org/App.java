/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lớp khởi chạy ứng dụng Sticky Note.
 */
public class App extends Application {
    private static boolean isDarkMode = false;

    @Override
    public void start(Stage stage) throws IOException {
        // Tải font chữ viết tay tùy chỉnh
        javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/org/font/FuzzyBubbles-Regular.ttf"), 16);
        javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/org/font/FuzzyBubbles-Bold.ttf"), 16);
        
        // Tải cài đặt giao diện
        isDarkMode = NoteManager.getInstance().loadDarkMode();
        
        // Thiết lập giao diện ban đầu
        applyTheme();
        
        // Tự động đóng ứng dụng khi hết cửa sổ
        Platform.setImplicitExit(true);
        
        // Tải dữ liệu ghi chú đã lưu
        List<NoteData> savedNotes = NoteManager.getInstance().loadNotes();
        
        // Khôi phục các ghi chú từ lần trước
        if (!savedNotes.isEmpty()) {
            for (NoteData data : savedNotes) {
                restoreNote(data);
            }
        }
        
        // Hiển thị danh sách ghi chú (Note List)
        showNoteList(stage);
    }

    /**
     * Hiển thị cửa sổ danh sách ghi chú.
     */
    public static void showNoteList(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/notelist.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 500); 
        scene.getStylesheets().add(App.class.getResource("css/styles.css").toExternalForm()); 
        
        stage.initStyle(javafx.stage.StageStyle.DECORATED);
        stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("images/icon.png")));
        stage.setTitle("NoTeThing");
        stage.setScene(scene);
        if (isDarkMode) {
            scene.getRoot().getStyleClass().add("dark");
        }
        stage.show();
        
        NoteManager.getInstance().setNoteListStage(stage);
        
        stage.setOnCloseRequest(e -> {
            // Kiểm tra xem có ghi chú nào đang mở không
            boolean anyNoteShowing = NoteManager.getInstance().getNotes().stream()
                .anyMatch(n -> n.getStage() != null && n.getStage().isShowing());
            
            if (anyNoteShowing) {
                // Chỉ ẩn danh sách nếu còn ghi chú đang mở
                e.consume();
                stage.hide(); 
            }
        });
    }

    /**
     * Tạo ghi chú mới.
     */
    public static void newNote() {
        createNoteWindow(null);
    }
    
    /**
     * Phục hồi ghi chú từ dữ liệu cũ.
     */
    private static void restoreNote(NoteData data) {
        createNoteWindow(data);
    }
    
    /**
     * Khởi tạo cửa sổ ghi chú.
     */
    private static void createNoteWindow(NoteData data) {
        try {
            Stage stage = new Stage();
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("images/icon.png")));
            
            // Xử lý định danh và tiêu đề tạm thời để ResizeHelper tìm HWND
            String noteId = (data != null) ? data.getId() : "Note-" + java.util.UUID.randomUUID().toString();
            stage.setTitle(noteId);
            
            stage.setMinWidth(200);
            stage.setMinHeight(150);
            
            // Khôi phục kích thước và vị trí
            if (data != null) {
                stage.setX(data.getX());
                stage.setY(data.getY());
                stage.setWidth(data.getWidth());
                stage.setHeight(data.getHeight());
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("fxml/primary.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            scene.getStylesheets().add(App.class.getResource("css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            
            // Áp dụng class theme cho root
            if (isDarkMode) {
                scene.getRoot().getStyleClass().add("dark");
            }
            
            // Áp dụng tọa độ nếu có dữ liệu
            if (data != null) {
                stage.setX(data.getX());
                stage.setY(data.getY());
                stage.setWidth(data.getWidth());
                stage.setHeight(data.getHeight());
            }
            
            ResizeHelper.addResizeListener(stage);
            
            // Khởi tạo model Note
            Note note = new Note(stage, noteId);
            if (data != null) {
                note.setTitle(data.getTitle());
                note.setContent(data.getContent());
                note.setAlwaysOnTop(data.isAlwaysOnTop());
                if (data.getColor() != null) note.setColor(data.getColor());
                note.setOpacity(data.getOpacity());
            } else {
                note.setTitle(NoteManager.getInstance().getNextDefaultTitle());
            }
            
            // Thêm vào quản lý nếu chưa tồn tại
            if (NoteManager.getInstance().findNoteById(noteId) == null) {
                NoteManager.getInstance().addNote(note);
            }
            
            // Cấu hình Controller
            PrimaryController controller = fxmlLoader.getController();
            controller.setNote(note);
            note.setController(controller);
            
            // Hiển thị nếu là ghi chú mới hoặc đang mở
            if (data == null || data.isOpen()) {
                stage.show();
                // Chỉ bind tiêu đề và focus SAU KHI show
                javafx.application.Platform.runLater(() -> {
                    note.bindTitleToStage();
                    controller.focusContent();
                });
            } else {
                // Nếu note cũ đang đóng, vẫn bind để đồng bộ data
                note.bindTitleToStage();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Lưu dữ liệu trước khi thoát
        NoteManager.getInstance().saveNotes();
    }

    /**
     * Chuyển đổi giữa giao diện Sáng và Tối.
     */
    public static void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        // Lưu cài đặt ngay khi thay đổi
        NoteManager.getInstance().saveSettings(isDarkMode);
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    private static void applyTheme() {
        if (isDarkMode) {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        }
        
        // Cập nhật class pseudo-class 'dark' cho tất cả các stage đang mở để CSS update
        updateStylesheetsAcrossWindows();
    }

    private static void updateStylesheetsAcrossWindows() {
        // 1. Cập nhật Note List
        Stage listStage = NoteManager.getInstance().getNoteListStage();
        if (listStage != null && listStage.getScene() != null) {
            updateRootTheme(listStage.getScene().getRoot());
        }

        // 2. Cập nhật tất cả các ghi chú (bao gồm cả ghi chú đang đóng)
        for (Note note : NoteManager.getInstance().getNotes()) {
            if (note.getStage() != null && note.getStage().getScene() != null) {
                updateRootTheme(note.getStage().getScene().getRoot());
                if (note.getController() != null) {
                    note.getController().refreshTheme();
                }
            }
        }
    }

    private static void updateRootTheme(javafx.scene.Parent root) {
        if (root == null) return;
        root.getStyleClass().remove("dark");
        if (isDarkMode) {
            root.getStyleClass().add("dark");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
