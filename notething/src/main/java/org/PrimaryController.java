package org;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Điều khiển cửa sổ ghi chú. (Quay về TextArea thuần túy)
 */
public class PrimaryController {

    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private HBox headerBox;
    @FXML private TextField noteTitle;
    @FXML private StackPane editorContainer;
    @FXML private Button alwaysOnTopButton;
    @FXML private org.kordamp.ikonli.javafx.FontIcon alwaysOnTopIcon;

    private TextArea editor;
    
    private double xOffset = 0;
    private double yOffset = 0;
    
    // Margin để resize cửa sổ
    private static final int RESIZE_MARGIN = 14;
    
    private Note note;
    private boolean isUpdatingFromModel = false;

    /**
     * Khởi tạo và thiết lập sự kiện.
     */
    public void initialize() {
        // Sử dụng TextArea thuần túy
        editor = new TextArea();
        editor.setWrapText(true);
        editor.setPromptText("Viết ghi chú...");
        editor.getStyleClass().add("text-area-editor");
        // Xóa style mặc định của TextArea để nó trong suốt
        editor.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-text-fill: -note-text-color; -fx-prompt-text-fill: -note-prompt-color;");

        editorContainer.getChildren().add(editor);

        // Xử lý sự kiện kéo dãn (resize)
        rootPane.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            boolean nearEdge = isNearEdge(event.getSceneX(), event.getSceneY(), 
                                         rootPane.getScene().getWidth(), rootPane.getScene().getHeight());
            
            // Cho phép chuột tương tác với cạnh cửa sổ
            editor.setMouseTransparent(nearEdge);
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
                        
                        // Ẩn/hiện tiêu đề khi focus thay đổi
                        newWin.focusedProperty().addListener((obs3, wasFocused, isFocused) -> {
                            refreshAutoHideState();
                        });
                    }
                });
            }
        });
        
        // Theo dõi thay đổi text để lưu vào note
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromModel && note != null) {
                note.setContent(newVal);
                NoteManager.getInstance().saveNotes();
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
            noteTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) NoteManager.getInstance().saveNotes();
            });
        }
        
        isUpdatingFromModel = true;
        // Bỏ qua richContent, chỉ dùng content thuần túy
        if (note.getContent() != null) {
            editor.setText(note.getContent());
        }
        isUpdatingFromModel = false;
        
        // Khi model thay đổi (ví dụ từ sync), cập nhật editor
        note.contentProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFromModel && newVal != null && !newVal.equals(editor.getText())) {
                isUpdatingFromModel = true;
                editor.setText(newVal);
                editor.positionCaret(editor.getLength()); // Di chuyển con trỏ xuống cuối
                isUpdatingFromModel = false;
            }
        });
        
        updateAlwaysOnTopUI(note.isAlwaysOnTop());
        note.alwaysOnTopProperty().addListener((obs, oldVal, newVal) -> {
            updateAlwaysOnTopUI(newVal);
            NoteManager.getInstance().saveNotes();
        });
        
        applyColor(note.getColor());
        note.colorProperty().addListener((obs, oldVal, newVal) -> {
            applyColor(newVal);
            NoteManager.getInstance().saveNotes();
        });
        
        applyOpacity(note.getOpacity());
        note.opacityProperty().addListener((obs, oldVal, newVal) -> {
            applyOpacity(newVal.doubleValue());
            NoteManager.getInstance().saveNotes();
        });
        
        // Cập nhật cỡ chữ ban đầu
        refreshFontSize();
        
        // Khởi tạo trạng thái ẩn hiện tiêu đề ban đầu
        refreshAutoHideState();
    }

    public void focusContent() {
        if (editor != null) {
            editor.requestFocus();
            editor.positionCaret(editor.getLength());
        }
    }

    @FXML
    private void openNoteList() {
        try {
            Stage listStage = NoteManager.getInstance().getNoteListStage();
            if (listStage != null) {
                listStage.show();
                listStage.toFront();
                if (listStage.isIconified()) listStage.setIconified(false);
            } else {
                 App.showNoteList(new Stage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void minimizeNote() {
        Stage stage = (Stage) headerBox.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void closeNote() {
        Stage stage = (Stage) headerBox.getScene().getWindow();
        stage.close();
        NoteManager.getInstance().saveNotes();
    }

    @FXML
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) headerBox.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
    
    @FXML
    private void toggleAlwaysOnTop() {
        if (note != null) {
            note.setAlwaysOnTop(!note.isAlwaysOnTop());
        }
    }
    
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
    
    private void toggleHeaderVisibility(boolean isFocused) {
        if (headerBox != null) {
            if (isFocused) {
                headerBox.setVisible(true);
                headerBox.setManaged(true);
                Platform.runLater(() -> {
                    if (editor != null) editor.requestFocus();
                });
            } else if (App.isAutoHideTitleEnabled()) {
                headerBox.setVisible(false);
                headerBox.setManaged(false);
            }
        }
    }

    public void refreshFontSize() {
        if (editor != null) {
             editor.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-text-fill: -note-text-color; -fx-font-family: 'Mali'; -fx-font-size: " + App.getFontSize() + "px;");
        }
    }

    public void refreshAutoHideState() {
        Platform.runLater(() -> {
            if (headerBox != null && headerBox.getScene() != null && headerBox.getScene().getWindow() != null) {
                boolean isFocused = headerBox.getScene().getWindow().isFocused();
                
                if (!App.isAutoHideTitleEnabled()) {
                    headerBox.setVisible(true);
                    headerBox.setManaged(true);
                } else {
                    toggleHeaderVisibility(isFocused);
                }
            }
        });
    }
    
    @FXML
    private void showColorPicker() {
        VBox pickerRoot = new VBox(15);
        pickerRoot.getStyleClass().add("color-menu");
        pickerRoot.setPadding(new Insets(15));
        pickerRoot.setPrefWidth(220);
        
        Label titleLabel = new Label("Màu sắc & Độ trong");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -note-text-color;");
        
        HBox colorPalette = new HBox(8);
        colorPalette.setAlignment(Pos.CENTER);
        
        String[] colors = {"color-yellow", "color-green", "color-blue", "color-orange", "color-red", "color-purple", "color-gray"};
        for (String colorClass : colors) {
            Circle circle = new Circle(12);
            circle.getStyleClass().addAll("color-option", "color-preview", colorClass);
            circle.setOnMouseClicked(e -> {
                if (note != null) {
                    note.setColor(colorClass);
                    NoteManager.getInstance().saveNotes();
                }
            });
            colorPalette.getChildren().add(circle);
        }
        
        VBox opacityBox = new VBox(5);
        Label opacityLabel = new Label("Độ trong suốt");
        opacityLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -note-prompt-color;");
        
        Slider opacitySlider = new Slider(0.1, 1.0, note != null ? note.getOpacity() : 1.0);
        opacitySlider.setBlockIncrement(0.1);
        opacitySlider.valueProperty().addListener((obs, old, newVal) -> {
            if (note != null) {
                note.setOpacity(newVal.doubleValue());
            }
        });
        
        opacityBox.getChildren().addAll(opacityLabel, opacitySlider);
        
        pickerRoot.getChildren().addAll(titleLabel, colorPalette, opacityBox);
        
        pickerRoot.getStylesheets().add(App.class.getResource("css/styles.css").toExternalForm());
        if (App.isDarkMode()) {
            pickerRoot.getStyleClass().add("dark");
        }
        
        Popup popup = new Popup();
        popup.getContent().add(pickerRoot);
        popup.setAutoHide(true);
        
        javafx.scene.Node source = (javafx.scene.Node) alwaysOnTopButton.getParent().getChildrenUnmodifiable().get(1);
        Bounds bounds = source.localToScreen(source.getBoundsInLocal());
        
        // Sửa lỗi góc trắng thừa của Popup
        popup.setOnShowing(e -> {
            if (popup.getScene() != null) {
                popup.getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
                if (popup.getScene().getRoot() != null) {
                    popup.getScene().getRoot().setStyle("-fx-background-color: transparent;");
                }
            }
        });

        popup.show(rootPane.getScene().getWindow(), bounds.getMinX(), bounds.getMaxY() + 5);
    }
    
    private void applyColor(String colorClass) {
        rootPane.getStyleClass().removeIf(s -> s.startsWith("color-"));
        rootPane.getStyleClass().add(colorClass);
        if (note != null) applyOpacity(note.getOpacity());
    }
    
    private void applyOpacity(double opacity) {
        String hex = getHexForColor(note != null ? note.getColor() : "color-yellow");
        String headerHex = getHexForHeader(note != null ? note.getColor() : "color-yellow");
        
        rootPane.setStyle("-fx-background-color: " + toRgba(hex, opacity) + "; -fx-background-radius: 7px;");
        headerBox.setStyle("-fx-background-color: " + toRgba(headerHex, opacity) + "; -fx-background-radius: 7px 7px 0 0;");
    }

    private String toRgba(String hex, double opacity) {
        int r = Integer.valueOf(hex.substring(1, 3), 16);
        int g = Integer.valueOf(hex.substring(3, 5), 16);
        int b = Integer.valueOf(hex.substring(5, 7), 16);
        return String.format("rgba(%d, %d, %d, %.2f)", r, g, b, opacity);
    }

    private String getHexForColor(String colorClass) {
        boolean dark = App.isDarkMode();
        switch (colorClass) {
            case "color-green":  return dark ? "#2D4D36" : "#D1F2D6";
            case "color-blue":   return dark ? "#264766" : "#D0E7FF";
            case "color-orange": return dark ? "#5C3D26" : "#FFE4D1";
            case "color-red":    return dark ? "#5C2D2D" : "#FFD6D6";
            case "color-purple": return dark ? "#3F335C" : "#EBDFFF";
            case "color-gray":   return dark ? "#2D4F4F" : "#D1F0F0";
            default:             return dark ? "#524B26" : "#FFF4C3";
        }
    }

    private String getHexForHeader(String colorClass) {
        boolean dark = App.isDarkMode();
        switch (colorClass) {
            case "color-green":  return dark ? "#23422A" : "#B7E9BE";
            case "color-blue":   return dark ? "#1E3A5F" : "#B3D7FF";
            case "color-orange": return dark ? "#4D311A" : "#FFCBA4";
            case "color-red":    return dark ? "#4D1F1F" : "#FFB3B3";
            case "color-purple": return dark ? "#32264D" : "#D6BCFA";
            case "color-gray":   return dark ? "#204040" : "#A5E8E8";
            default:             return dark ? "#453F1F" : "#FFED99";
        }
    }

    public void refreshTheme() {
        if (note != null) {
            applyOpacity(note.getOpacity());
        }
    }
}
