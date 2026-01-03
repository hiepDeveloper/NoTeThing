/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

/**
 * Lớp Model đại diện cho một ghi chú.
 */
public class Note {
    private final StringProperty title;
    private final StringProperty content;
    private final DoubleProperty x;
    private final DoubleProperty y;
    private final DoubleProperty width;
    private final DoubleProperty height;
    private final BooleanProperty alwaysOnTop;
    private final StringProperty color;
    private final DoubleProperty opacity;
    private final StringProperty richContent;
    private final BooleanProperty deleted; // Đánh dấu đã xóa vào thùng rác

    
    private Stage stage;
    private String id;
    
    private PrimaryController controller;

    public Note(Stage stage, String id) {
        this.stage = stage;
        this.id = id;
        this.title = new SimpleStringProperty("");
        this.content = new SimpleStringProperty("");
        this.alwaysOnTop = new SimpleBooleanProperty(false);
        this.color = new SimpleStringProperty("color-yellow");
        this.opacity = new SimpleDoubleProperty(1.0);
        this.richContent = new SimpleStringProperty("");
        this.deleted = new SimpleBooleanProperty(false);
        
        // Khởi tạo các thuộc tính vị trí và kích thước
        this.x = new SimpleDoubleProperty(stage != null ? stage.getX() : 0);
        this.y = new SimpleDoubleProperty(stage != null ? stage.getY() : 0);
        this.width = new SimpleDoubleProperty(stage != null ? stage.getWidth() : 300);
        this.height = new SimpleDoubleProperty(stage != null ? stage.getHeight() : 400);
        
        // Cập nhật thuộc tính khi cửa sổ thay đổi
        if (stage != null) {
            stage.xProperty().addListener((obs, old, newVal) -> this.x.set(newVal.doubleValue()));
            stage.yProperty().addListener((obs, old, newVal) -> this.y.set(newVal.doubleValue()));
            stage.widthProperty().addListener((obs, old, newVal) -> this.width.set(newVal.doubleValue()));
            stage.heightProperty().addListener((obs, old, newVal) -> this.height.set(newVal.doubleValue()));
            
            // Đồng bộ alwaysOnTop với Stage
            this.alwaysOnTop.addListener((obs, old, newVal) -> stage.setAlwaysOnTop(newVal));
        }
    }
    
    /**
     * Thực hiện liên kết tiêu đề Ghi chú với tiêu đề Cửa sổ.
     */
    public void bindTitleToStage() {
        if (stage != null) {
            stage.titleProperty().bind(this.title);
        }
    }
    
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }
    
    public String getContent() { return content.get(); }
    public void setContent(String value) { content.set(value); }
    public StringProperty contentProperty() { return content; }
    
    public double getX() { return x.get(); }
    public void setX(double value) { x.set(value); }
    
    public double getY() { return y.get(); }
    public void setY(double value) { y.set(value); }
    
    public double getWidth() { return width.get(); }
    public void setWidth(double value) { width.set(value); }
    
    public double getHeight() { return height.get(); }
    public void setHeight(double value) { height.set(value); }
    
    public boolean isAlwaysOnTop() { return alwaysOnTop.get(); }
    public void setAlwaysOnTop(boolean value) { alwaysOnTop.set(value); }
    public BooleanProperty alwaysOnTopProperty() { return alwaysOnTop; }
    
    public String getColor() { return color.get(); }
    public void setColor(String value) { color.set(value); }
    public StringProperty colorProperty() { return color; }
    
    public double getOpacity() { return opacity.get(); }
    public void setOpacity(double value) { opacity.set(value); }
    public DoubleProperty opacityProperty() { return opacity; }
    
    public String getRichContent() { return richContent.get(); }
    public void setRichContent(String value) { richContent.set(value); }
    public StringProperty richContentProperty() { return richContent; }

    public boolean isDeleted() { return deleted.get(); }
    public void setDeleted(boolean value) { deleted.set(value); }
    public BooleanProperty deletedProperty() { return deleted; }
    
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    
    public PrimaryController getController() { return controller; }
    public void setController(PrimaryController controller) { this.controller = controller; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
