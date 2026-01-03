/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import java.io.Serializable;

/**
 * Đối tượng lưu trữ dữ liệu ghi chú.
 */
public class NoteData implements Serializable {
    private static final long serialVersionUID = 2L; // Bump version for new field
    
    public NoteData() {} 
    
    private String id;
    private String title;
    private String content;
    private String richContent;
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean isOpen;
    private boolean alwaysOnTop;
    private String color;
    private double opacity;
    private boolean deleted; // Trạng thái đã xóa
    
    public NoteData(String id, String title, String content, String richContent, double x, double y, double width, double height, boolean isOpen, boolean alwaysOnTop, String color, double opacity, boolean deleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.richContent = richContent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isOpen = isOpen;
        this.alwaysOnTop = alwaysOnTop;
        this.color = color;
        this.opacity = (opacity <= 0) ? 1.0 : opacity;
        this.deleted = deleted;
    }

    // Constructor cũ để tương thích ngược nếu cần (hoặc dùng làm constructor convenience)
    public NoteData(String id, String title, String content, String richContent, double x, double y, double width, double height, boolean isOpen, boolean alwaysOnTop, String color, double opacity) {
        this(id, title, content, richContent, x, y, width, height, isOpen, alwaysOnTop, color, opacity, false);
    }
    
    public NoteData(String id, String title, String content, String richContent, double x, double y, double width, double height, boolean isOpen, boolean alwaysOnTop, String color) {
        this(id, title, content, richContent, x, y, width, height, isOpen, alwaysOnTop, color, 1.0, false);
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getRichContent() { return richContent; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isOpen() { return isOpen; }
    public boolean isAlwaysOnTop() { return alwaysOnTop; }
    public String getColor() { return color; }
    public double getOpacity() { return opacity; }
    public boolean isDeleted() { return deleted; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setRichContent(String richContent) { this.richContent = richContent; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    public void setOpen(boolean open) { isOpen = open; }
    public void setAlwaysOnTop(boolean alwaysOnTop) { this.alwaysOnTop = alwaysOnTop; }
    public void setColor(String color) { this.color = color; }
    public void setOpacity(double opacity) { this.opacity = opacity; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
