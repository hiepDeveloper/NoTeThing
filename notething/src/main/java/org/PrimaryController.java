package org;

import javafx.fxml.FXML;

/**
 * Điều khiển cửa sổ ghi chú.
 */
public class PrimaryController {

    @FXML
    private javafx.scene.layout.StackPane rootPane;

    @FXML
    private javafx.scene.layout.VBox contentBox;

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
    
    private static final int RESIZE_MARGIN = 14;
    
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
            noteTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) NoteManager.getInstance().saveNotes();
            });
        }
        noteContent.textProperty().bindBidirectional(note.contentProperty());
        
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
    }

    /**
     * Tập trung con trỏ vào vùng nhập nội dung.
     */
    public void focusContent() {
        if (noteContent != null) {
            noteContent.requestFocus();
            noteContent.positionCaret(noteContent.getText().length());
        }
    }

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

    @FXML
    private void minimizeNote() {
        javafx.stage.Stage stage = (javafx.stage.Stage) headerBox.getScene().getWindow();
        stage.setIconified(true);
    }

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
                javafx.application.Platform.runLater(() -> {
                    if (noteContent != null) {
                        noteContent.requestFocus();
                        noteContent.positionCaret(savedCaretPosition);
                        if (noteContent.getText().isEmpty()) {
                            noteContent.insertText(0, " ");
                            noteContent.deleteText(0, 1);
                            noteContent.positionCaret(0);
                        }
                    }
                });
            } else {
                if (noteContent != null) {
                    savedCaretPosition = noteContent.getCaretPosition();
                }
                headerBox.setVisible(false);
                headerBox.setManaged(false);
            }
        }
    }
    
    @FXML
    private void showColorPicker() {
        javafx.scene.layout.VBox pickerRoot = new javafx.scene.layout.VBox(15);
        pickerRoot.getStyleClass().add("color-menu");
        pickerRoot.setPadding(new javafx.geometry.Insets(15));
        pickerRoot.setPrefWidth(220);
        
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Màu sắc & Độ trong");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -note-text-color;");
        
        javafx.scene.layout.HBox colorPalette = new javafx.scene.layout.HBox(8);
        colorPalette.setAlignment(javafx.geometry.Pos.CENTER);
        
        String[] colors = {"color-yellow", "color-green", "color-blue", "color-orange", "color-red", "color-purple", "color-gray"};
        for (String colorClass : colors) {
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(12);
            circle.getStyleClass().addAll("color-option", "color-preview", colorClass);
            circle.setOnMouseClicked(e -> {
                if (note != null) {
                    note.setColor(colorClass);
                    NoteManager.getInstance().saveNotes();
                }
            });
            colorPalette.getChildren().add(circle);
        }
        
        javafx.scene.layout.VBox opacityBox = new javafx.scene.layout.VBox(5);
        javafx.scene.control.Label opacityLabel = new javafx.scene.control.Label("Độ trong suốt");
        opacityLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -note-prompt-color;");
        
        javafx.scene.control.Slider opacitySlider = new javafx.scene.control.Slider(0.1, 1.0, note != null ? note.getOpacity() : 1.0);
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
        
        javafx.stage.Popup popup = new javafx.stage.Popup();
        popup.getContent().add(pickerRoot);
        popup.setAutoHide(true);
        
        javafx.scene.Node source = (javafx.scene.Node) alwaysOnTopButton.getParent().getChildrenUnmodifiable().get(1);
        javafx.geometry.Bounds bounds = source.localToScreen(source.getBoundsInLocal());
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
