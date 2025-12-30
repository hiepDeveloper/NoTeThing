/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;

/**
 * Lớp Model đại diện cho một ghi chú.
 */
public class Note {
    private final StringProperty title;
    private final StringProperty content;
    private final javafx.beans.property.DoubleProperty x;
    private final javafx.beans.property.DoubleProperty y;
    private final javafx.beans.property.DoubleProperty width;
    private final javafx.beans.property.DoubleProperty height;
    private final javafx.beans.property.BooleanProperty alwaysOnTop;
    
    private Stage stage;
    private String id;
    
    public Note(Stage stage, String id) {
        this.stage = stage;
        this.id = id;
        this.title = new SimpleStringProperty("");
        this.content = new SimpleStringProperty("");
        this.alwaysOnTop = new javafx.beans.property.SimpleBooleanProperty(false);
        
        // Khởi tạo các thuộc tính vị trí và kích thước
        this.x = new javafx.beans.property.SimpleDoubleProperty(stage.getX());
        this.y = new javafx.beans.property.SimpleDoubleProperty(stage.getY());
        this.width = new javafx.beans.property.SimpleDoubleProperty(stage.getWidth());
        this.height = new javafx.beans.property.SimpleDoubleProperty(stage.getHeight());
        
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
    public javafx.beans.property.BooleanProperty alwaysOnTopProperty() { return alwaysOnTop; }
    
    public Stage getStage() { return stage; }
    public void setStage(Stage stage) { this.stage = stage; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

