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
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String content;
    private double x;
    private double y;
    private double width;
    private double height;
    private boolean isOpen;
    private boolean alwaysOnTop;
    private String color;
    private double opacity;
    
    public NoteData(String id, String title, String content, double x, double y, double width, double height, boolean isOpen, boolean alwaysOnTop, String color, double opacity) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isOpen = isOpen;
        this.alwaysOnTop = alwaysOnTop;
        this.color = color;
        this.opacity = (opacity <= 0) ? 1.0 : opacity;
    }
    
    public NoteData(String id, String title, String content, double x, double y, double width, double height, boolean isOpen, boolean alwaysOnTop, String color) {
        this(id, title, content, x, y, width, height, isOpen, alwaysOnTop, color, 1.0);
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isOpen() { return isOpen; }
    public boolean isAlwaysOnTop() { return alwaysOnTop; }
    public String getColor() { return color; }
    public double getOpacity() { return opacity; }
}
