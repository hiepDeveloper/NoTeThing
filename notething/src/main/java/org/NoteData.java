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
    
    public NoteData(String id, String title, String content, double x, double y, double width, double height, boolean isOpen, boolean alwaysOnTop) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isOpen = isOpen;
        this.alwaysOnTop = alwaysOnTop;
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
}

