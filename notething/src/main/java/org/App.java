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
    public enum ThemeMode {
        SYSTEM, LIGHT, DARK
    }
    
    private static ThemeMode currentThemeMode = ThemeMode.SYSTEM;
    private static boolean isDarkMode = false;
    private static boolean isGlassEnabled = true;
    private static boolean isAutoHideTitleEnabled = true;
    private static boolean isCloudSyncEnabled = false;
    private static int currentFontSize = 18;

    @Override
    public void start(Stage stage) throws IOException {
        // Tải font chữ Mali mới (Hỗ trợ đầy đủ Bold/Italic)
        try {
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/org/font/Mali/Mali-Regular.ttf"), 18);
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/org/font/Mali/Mali-Bold.ttf"), 18);
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/org/font/Mali/Mali-Italic.ttf"), 18);
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/org/font/Mali/Mali-BoldItalic.ttf"), 18);
            
            System.out.println("✓ Mali fonts loaded successfully");
        } catch (Exception e) {
            System.err.println("✗ Error loading Mali fonts: " + e.getMessage());
        }
        
        // Tải cài đặt giao diện
        String savedMode = NoteManager.getInstance().loadThemeMode();
        currentThemeMode = ThemeMode.valueOf(savedMode);
        isGlassEnabled = NoteManager.getInstance().loadGlassEffect();
        isAutoHideTitleEnabled = NoteManager.getInstance().loadAutoHideTitle();
        currentFontSize = NoteManager.getInstance().loadFontSize();
        isCloudSyncEnabled = NoteManager.getInstance().loadCloudSync();
        
        // CỦNG CỐ BẢO MẬT: Nếu chưa đăng nhập, ép buộc tắt đồng bộ đám mây
        if (!FirebaseAuthManager.getInstance().isLoggedIn()) {
            isCloudSyncEnabled = false;
        }
        
        // Thiết lập giao diện ban đầu
        applyTheme();
        
        // Bắt đầu theo dõi sự thay đổi của hệ thống nếu là chế độ SYSTEM
        startSystemThemeMonitor();
        
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
        
        // Cập nhật màu thanh tiêu đề cho Windows
        GlassHelper.setDarkTitleBar(stage, isDarkMode);
        
        NoteManager.getInstance().setNoteListStage(stage);
        
        stage.setOnCloseRequest(e -> {
            // Kiểm tra xem có ghi chú nào đang mở không
            boolean anyNoteShowing = NoteManager.getInstance().getAllNotes().stream()
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
            
            stage.setMinWidth(220);
            stage.setMinHeight(150);
            
            // Khôi phục kích thước và vị trí
            if (data != null) {
                stage.setX(data.getX());
                stage.setY(data.getY());
                stage.setWidth(data.getWidth());
                stage.setHeight(data.getHeight());
            } else {
                // Kích thước mặc định hình vuông cho ghi chú mới
                stage.setWidth(300);
                stage.setHeight(300);
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
            
            // Áp dụng lại tọa độ nếu có dữ liệu (để chắc chắn sau khi setScene)
            if (data != null) {
                stage.setX(data.getX());
                stage.setY(data.getY());
                stage.setWidth(data.getWidth());
                stage.setHeight(data.getHeight());
            } else {
                 stage.setWidth(300);
                 stage.setHeight(300);
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
                note.setDeleted(data.isDeleted());
            } else {
                note.setTitle(NoteManager.getInstance().getNextDefaultTitle());
            }
            
            // Kích hoạt nhận diện HWND và Blur
            // Đặt tiêu đề cửa sổ là ID duy nhất để JNA luôn tìm thấy đúng cửa sổ
            stage.setTitle(noteId);
            
            stage.showingProperty().addListener((obs, old, isShowing) -> {
                if (isShowing) {
                    // Luôn đảm bảo tiêu đề là ID khi hiển thị (phòng trường hợp bị thay đổi)
                    stage.setTitle(noteId);
                    
                    javafx.application.Platform.runLater(() -> {
                        GlassHelper.applyBlur(stage, isGlassEnabled);
                    });
                }
            });
            
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
                GlassHelper.setDarkTitleBar(stage, isDarkMode); // Đảm bảo title bar đúng màu
                javafx.application.Platform.runLater(() -> {
                    controller.focusContent();
                });
            } else {
                 // Không làm gì thêm, tiêu đề cửa sổ đã được đặt là ID
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
     * Chuyển đổi giữa giao diện Sáng và Tối (cho bản cũ, nay dùng setThemeMode).
     */
    public static void toggleTheme() {
        if (currentThemeMode == ThemeMode.LIGHT) {
            setThemeMode(ThemeMode.DARK);
        } else {
            setThemeMode(ThemeMode.LIGHT);
        }
    }

    public static void setThemeMode(ThemeMode mode) {
        currentThemeMode = mode;
        applyTheme();
        NoteManager.getInstance().saveSettings(mode.name(), isGlassEnabled, isAutoHideTitleEnabled, currentFontSize, isCloudSyncEnabled);
    }

    public static void setGlassEnabled(boolean enabled) {
        isGlassEnabled = enabled;
        // Chỉ cập nhật hiệu ứng cho các tờ ghi chú (vì chúng là Transparent Stage)
        // Cửa sổ danh sách chính là Decorated Stage, không nên áp dụng hiệu ứng này để tránh lỗi UI
        for (Note note : NoteManager.getInstance().getAllNotes()) {
            if (note.getStage() != null) {
                GlassHelper.applyBlur(note.getStage(), isGlassEnabled);
            }
        }
        NoteManager.getInstance().saveSettings(currentThemeMode.name(), isGlassEnabled, isAutoHideTitleEnabled, currentFontSize, isCloudSyncEnabled);
    }

    public static void setAutoHideTitleEnabled(boolean enabled) {
        isAutoHideTitleEnabled = enabled;
        // Cập nhật các cửa sổ đang mở
        for (Note note : NoteManager.getInstance().getAllNotes()) {
            if (note.getController() != null) {
                note.getController().refreshAutoHideState();
            }
        }
        NoteManager.getInstance().saveSettings(currentThemeMode.name(), isGlassEnabled, isAutoHideTitleEnabled, currentFontSize, isCloudSyncEnabled);
    }

    public static void setFontSize(int size) {
        currentFontSize = size;
        // Cập nhật tất cả ghi chú
        for (Note note : NoteManager.getInstance().getAllNotes()) {
            if (note.getController() != null) {
                note.getController().refreshFontSize();
            }
        }
        NoteManager.getInstance().saveSettings(currentThemeMode.name(), isGlassEnabled, isAutoHideTitleEnabled, currentFontSize, isCloudSyncEnabled);
    }

    public static int getFontSize() {
        return currentFontSize;
    }

    public static void triggerSyncCloud() {
        if (isCloudSyncEnabled && FirebaseAuthManager.getInstance().isLoggedIn() && FirebaseManager.getInstance().isInitialized()) {
            // Chạy ngầm để không treo UI
            new Thread(() -> {
                List<NoteData> notes = NoteManager.getInstance().loadNotes();
                FirebaseManager.getInstance().syncToCloud(notes, FirebaseAuthManager.getInstance().getLocalId());
            }).start();
        }
    }

    public static void syncDownFromCloud() {
        if (isCloudSyncEnabled && FirebaseAuthManager.getInstance().isLoggedIn() && FirebaseManager.getInstance().isInitialized()) {
            new Thread(() -> {
                try {
                    List<NoteData> cloudNotes = FirebaseManager.getInstance().loadFromCloud(FirebaseAuthManager.getInstance().getLocalId());
                    if (cloudNotes.isEmpty()) return;

                    Platform.runLater(() -> {
                        for (NoteData data : cloudNotes) {
                            Note existing = NoteManager.getInstance().findNoteById(data.getId());
                            if (existing == null) {
                                restoreNote(data);
                            } else {
                                // Cập nhật nội dung nếu local cũ hơn (Đơn giản hóa: ghi đè bằng Cloud)
                                existing.setTitle(data.getTitle());
                                existing.setContent(data.getContent());
                                if (existing.getController() != null) {
                                    existing.getController().setNote(existing); 
                                }
                            }
                        }
                        NoteManager.getInstance().saveNotes();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void setCloudSyncEnabled(boolean enabled) {
        isCloudSyncEnabled = enabled;
        NoteManager.getInstance().saveSettings(currentThemeMode.name(), isGlassEnabled, isAutoHideTitleEnabled, currentFontSize, isCloudSyncEnabled);
    }

    public static boolean isCloudSyncEnabled() {
        return isCloudSyncEnabled;
    }

    public static boolean isAutoHideTitleEnabled() {
        return isAutoHideTitleEnabled;
    }

    public static boolean isGlassEnabled() {
        return isGlassEnabled;
    }

    public static ThemeMode getThemeMode() {
        return currentThemeMode;
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    private static void applyTheme() {
        if (currentThemeMode == ThemeMode.SYSTEM) {
            isDarkMode = ThemeDetector.isSystemDarkMode();
        } else {
            isDarkMode = (currentThemeMode == ThemeMode.DARK);
        }

        if (isDarkMode) {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        }
        
        // Cập nhật class pseudo-class 'dark' cho tất cả các stage đang mở để CSS update
        updateStylesheetsAcrossWindows();
    }

    private static void startSystemThemeMonitor() {
        // Kiểm tra mỗi 2 giây nếu đang ở chế độ SYSTEM
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                if (currentThemeMode == ThemeMode.SYSTEM) {
                    boolean systemDark = ThemeDetector.isSystemDarkMode();
                    if (systemDark != isDarkMode) {
                        applyTheme();
                    }
                }
            })
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private static void updateStylesheetsAcrossWindows() {
        // 1. Cập nhật Note List
        Stage listStage = NoteManager.getInstance().getNoteListStage();
        if (listStage != null) {
            GlassHelper.setDarkTitleBar(listStage, isDarkMode);
            if (listStage.getScene() != null) {
                updateRootTheme(listStage.getScene().getRoot());
            }
        }

        // 2. Cập nhật tất cả các ghi chú (bao gồm cả ghi chú đang đóng)
        for (Note note : NoteManager.getInstance().getAllNotes()) {
            Stage stage = note.getStage();
            if (stage != null) {
                GlassHelper.setDarkTitleBar(stage, isDarkMode);
                if (stage.getScene() != null) {
                    updateRootTheme(stage.getScene().getRoot());
                    if (note.getController() != null) {
                        note.getController().refreshTheme();
                    }
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
