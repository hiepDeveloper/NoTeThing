/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import org.kordamp.ikonli.javafx.FontIcon;

import atlantafx.base.controls.ToggleSwitch;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Điều khiển danh sách ghi chú.
 */
public class NoteListController {

    @FXML
    private ListView<Note> noteListView;
    
    @FXML
    private javafx.scene.layout.VBox noteListViewContent;
    @FXML
    private javafx.scene.layout.VBox settingsViewContent;
    @FXML
    private ToggleSwitch syncToggle;
    @FXML
    private javafx.scene.control.ComboBox<String> themeCombo;
    @FXML
    private ToggleSwitch acrylicToggle;
    @FXML
    private ToggleSwitch autoHideTitleToggle;
    @FXML
    private javafx.scene.control.Spinner<Integer> fontSizeSpinner;
    @FXML
    private javafx.scene.control.Label userStatusLabel;
    @FXML
    private javafx.scene.control.Label userEmailLabel;
    @FXML
    private javafx.scene.control.Button loginButton;

    @FXML
    private javafx.scene.control.Label appTitleLabel;
    @FXML
    private Button trashToggleBtn;

    private boolean isTrashMode = false;
    public void initialize() {
        // ... (Code cũ của ComboBox/Spinner giữ nguyên)
        // Thiết lập ComboBox giao diện
        themeCombo.getItems().addAll("Tự động (Hệ thống)", "Sáng", "Tối");
        
        App.ThemeMode currentMode = App.getThemeMode();
        switch (currentMode) {
            case SYSTEM: themeCombo.getSelectionModel().select(0); break;
            case LIGHT: themeCombo.getSelectionModel().select(1); break;
            case DARK: themeCombo.getSelectionModel().select(2); break;
        }
        
        javafx.scene.control.SpinnerValueFactory<Integer> fontSizeFactory = 
            new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(12, 36, App.getFontSize());
        fontSizeSpinner.setValueFactory(fontSizeFactory);

        updateAccountUI();

        acrylicToggle.setSelected(App.isGlassEnabled());
        autoHideTitleToggle.setSelected(App.isAutoHideTitleEnabled());
        syncToggle.setSelected(App.isCloudSyncEnabled());
        
        // ... (Listeners cũ giữ nguyên)
        themeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Tự động (Hệ thống)": App.setThemeMode(App.ThemeMode.SYSTEM); break;
                case "Sáng": App.setThemeMode(App.ThemeMode.LIGHT); break;
                case "Tối": App.setThemeMode(App.ThemeMode.DARK); break;
            }
        });

        acrylicToggle.selectedProperty().addListener((obs, oldVal, newVal) -> App.setGlassEnabled(newVal));

        syncToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            App.setCloudSyncEnabled(newVal);
            if (newVal) {
                if (!FirebaseAuthManager.getInstance().isLoggedIn()) {
                    syncToggle.setSelected(false);
                    handleLoginAction();
                    return;
                }
                // Với REST API, không cần khởi tạo Service Account
                App.syncDownFromCloud();
            }
        });

        autoHideTitleToggle.selectedProperty().addListener((obs, oldVal, newVal) -> App.setAutoHideTitleEnabled(newVal));
        fontSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> App.setFontSize(newVal));

        // Mặc định hiển thị danh sách chưa xóa
        updateListViewSource();

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
                    
                    javafx.scene.control.TextField titleField = new javafx.scene.control.TextField();
                    titleField.textProperty().bindBidirectional(note.titleProperty());
                    titleField.getStyleClass().add("list-title-field");
                    titleField.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: transparent; -fx-padding: 0;");
                    titleField.setEditable(!isTrashMode); // Không cho sửa khi ở trong thùng rác
                    
                    titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) NoteManager.getInstance().saveNotes();
                    });
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    hbox.getChildren().add(titleField);
                    hbox.getChildren().add(spacer);

                    if (isTrashMode) {
                        // Nút Khôi phục
                        Button restoreButton = new Button();
                        restoreButton.setGraphic(new FontIcon("bi-arrow-counterclockwise"));
                        restoreButton.getStyleClass().add("action-button");
                        restoreButton.setTooltip(new javafx.scene.control.Tooltip("Khôi phục"));
                        restoreButton.setOnAction(e -> {
                            NoteManager.getInstance().restoreNote(note);
                        });
                        
                        // Nút Xóa vĩnh viễn
                        Button deleteForeverButton = new Button();
                        deleteForeverButton.setGraphic(new FontIcon("bi-trash-fill"));
                        deleteForeverButton.getStyleClass().addAll("action-button", "danger");
                        deleteForeverButton.setTooltip(new javafx.scene.control.Tooltip("Xóa vĩnh viễn"));
                        deleteForeverButton.setOnAction(e -> {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Xác nhận xóa vĩnh viễn");
                            alert.setHeaderText("Bạn có chắc chắn muốn xóa ghi chú này không?");
                            alert.setContentText("Hành động này không thể hoàn tác.");
                            alert.showAndWait().ifPresent(response -> {
                                if (response == javafx.scene.control.ButtonType.OK) {
                                    NoteManager.getInstance().hardDeleteNote(note);
                                }
                            });
                        });
                        
                        hbox.getChildren().addAll(restoreButton, deleteForeverButton);
                    } else {
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
                        
                        // Nút Xóa (Soft Delete)
                        Button closeButton = new Button();
                        closeButton.setGraphic(new FontIcon("bi-trash"));
                        closeButton.getStyleClass().addAll("action-button", "delete-button");
                        closeButton.setOnAction(e -> {
                            // Thêm xác nhận trước khi xóa vào thùng rác theo yêu cầu
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Xác nhận xóa");
                            alert.setHeaderText("Chuyển ghi chú vào thùng rác?");
                            alert.setContentText("Bạn có thể khôi phục lại sau.");
                            
                            alert.showAndWait().ifPresent(response -> {
                                if (response == javafx.scene.control.ButtonType.OK) {
                                    NoteManager.getInstance().softDeleteNote(note);
                                }
                            });
                        });
                        
                        hbox.getChildren().addAll(showButton, closeButton);
                    }
                    
                    setGraphic(hbox);
                    setText(null);
                    
                    Runnable refreshColor = () -> {
                        getStyleClass().removeAll("color-yellow", "color-green", "color-pink", "color-purple", "color-blue", "color-gray", "color-charcoal");
                        getStyleClass().add(note.getColor());
                    };
                    refreshColor.run();
                    note.colorProperty().addListener((obs, oldV, newV) -> refreshColor.run());
                }
            }
        });

        noteListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !isTrashMode) { // Chỉ mở khi không ở thùng rác
                Note selectedNote = noteListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null && selectedNote.getStage() != null) {
                    selectedNote.getStage().show();
                    selectedNote.getStage().toFront();
                    if (selectedNote.getStage().isIconified()) selectedNote.getStage().setIconified(false);
                }
            }
        });
    }

    private void updateListViewSource() {
        if (isTrashMode) {
            noteListView.setItems(NoteManager.getInstance().getDeletedNotes());
        } else {
            noteListView.setItems(NoteManager.getInstance().getVisibleNotes());
        }
    }


    @FXML
    private void toggleTrashView() {
        isTrashMode = !isTrashMode;
        updateListViewSource();
        
        // Cập nhật giao diện nút thùng rác
        if (trashToggleBtn != null) {
             if (isTrashMode) {
                 trashToggleBtn.getStyleClass().add("active");
                 if (appTitleLabel != null) appTitleLabel.setText("Thùng rác");
             } else {
                 trashToggleBtn.getStyleClass().remove("active");
                 if (appTitleLabel != null) appTitleLabel.setText("NoTeThing");
             }
        }
    }

    /**
     * Chuyển sang giao diện cài đặt.
     */
    @FXML
    private void showSettings() {
        noteListViewContent.setVisible(false);
        settingsViewContent.setVisible(true);
    }

    /**
     * Quay lại danh sách ghi chú.
     */
    @FXML
    private void showNoteList() {
        settingsViewContent.setVisible(false);
        noteListViewContent.setVisible(true);
    }

    /**
     * Xử lý đăng nhập.
     */
    @FXML
    private void handleLoginAction() {
        if (FirebaseAuthManager.getInstance().isLoggedIn()) {
            FirebaseAuthManager.getInstance().logout();
            updateAccountUI();
            syncToggle.setSelected(false);
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("fxml/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            scene.getStylesheets().add(App.class.getResource("css/styles.css").toExternalForm());
            
            Stage stage = new Stage();
            stage.setTitle("Đăng nhập Đám mây");
            stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("images/icon.png")));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            // Áp dụng chủ đề tối nếu cần
            if (App.isDarkMode()) {
                if (!scene.getRoot().getStyleClass().contains("dark")) {
                    scene.getRoot().getStyleClass().add("dark");
                }
                // Ép màu nền trực tiếp để tránh lỗi CSS resolution
                scene.getRoot().setStyle("-fx-background-color: #1A202C;");
                scene.setFill(javafx.scene.paint.Color.valueOf("#1A202C"));
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    GlassHelper.setDarkTitleBar(stage, true);
                }
            } else {
                scene.getRoot().setStyle("-fx-background-color: #FFFFFF;");
                scene.setFill(javafx.scene.paint.Color.WHITE);
            }
            
            LoginController controller = loader.getController();
            controller.setOnLoginSuccess(() -> {
                updateAccountUI();
            });
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateAccountUI() {
        if (FirebaseAuthManager.getInstance().isLoggedIn()) {
            userStatusLabel.setText("Đã đăng nhập");
            userEmailLabel.setText(FirebaseAuthManager.getInstance().getEmail());
            loginButton.setText("Đăng xuất");
            loginButton.getStyleClass().remove("accent");
            loginButton.getStyleClass().add("danger");
        } else {
            userStatusLabel.setText("Chưa đăng nhập");
            userEmailLabel.setText("Đăng nhập để đồng bộ ghi chú");
            loginButton.setText("Đăng nhập");
            loginButton.getStyleClass().remove("danger");
            loginButton.getStyleClass().add("accent");
        }
    }

    /**
     * Tạo mới ghi chú.
     */
    @FXML
    private void createNewNote() {
        App.newNote();
    }
}
