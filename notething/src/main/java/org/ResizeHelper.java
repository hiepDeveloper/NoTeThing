/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Hỗ trợ thay đổi kích thước cửa sổ đa nền tảng.
 */
public class ResizeHelper {
    private static final int RESIZE_MARGIN = 10;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public interface User32API extends StdCallLibrary {
        User32API INSTANCE = Native.load("user32", User32API.class, W32APIOptions.DEFAULT_OPTIONS);
        WinDef.HWND FindWindow(String lpClassName, String lpWindowName);
        boolean ReleaseCapture();
        WinDef.LRESULT SendMessage(WinDef.HWND hWnd, int Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
        boolean PostMessage(WinDef.HWND hWnd, int Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
    }

    /**
     * Thêm bộ lắng nghe thay đổi kích thước cho Stage.
     */
    public static void addResizeListener(Stage stage) {
        if (IS_WINDOWS) {
            addWindowsResizeListener(stage);
        } else {
            addJavaFXResizeListener(stage);
        }
    }

    private static void addWindowsResizeListener(Stage stage) {
        WindowsResizeListener listener = new WindowsResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, listener::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, listener::handleMousePressed);
        // Tìm HWND ngay khi khởi tạo nếu stage đã có title unique
        listener.getHwnd(); 
    }

    private static void addJavaFXResizeListener(Stage stage) {
        JavaFXResizeListener listener = new JavaFXResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, listener::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, listener::handleMousePressed);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, listener::handleMouseDragged);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, listener::handleMouseReleased);
    }

    /**
     * Bộ lắng nghe cho Windows (sử dụng JNA).
     */
    private static class WindowsResizeListener {
        private final Stage stage;
        private WinDef.HWND hwnd;

        private static final int HTLEFT = 10;
        private static final int HTRIGHT = 11;
        private static final int HTTOP = 12;
        private static final int HTTOPLEFT = 13;
        private static final int HTTOPRIGHT = 14;
        private static final int HTBOTTOM = 15;
        private static final int HTBOTTOMLEFT = 16;
        private static final int HTBOTTOMRIGHT = 17;

        private Cursor cursor = Cursor.DEFAULT;
        private int resizeDirection = 0;

        public WindowsResizeListener(Stage stage) {
            this.stage = stage;
            // Tự động tìm HWND ngay khi cửa sổ được hiển thị (để bắt được tiêu đề unique)
            stage.showingProperty().addListener((obs, old, newVal) -> {
                if (newVal) {
                    javafx.application.Platform.runLater(this::getHwnd);
                }
            });
        }

        WinDef.HWND getHwnd() {
            if (hwnd == null) {
                String title = stage.getTitle();
                if (title != null && !title.isEmpty()) {
                    hwnd = User32API.INSTANCE.FindWindow(null, title);
                }
            }
            return hwnd;
        }

        public void handleMouseMoved(MouseEvent event) {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            double sceneWidth = stage.getScene().getWidth();
            double sceneHeight = stage.getScene().getHeight();

            boolean isWest = mouseX < RESIZE_MARGIN;
            boolean isEast = mouseX > sceneWidth - RESIZE_MARGIN;
            boolean isNorth = mouseY < RESIZE_MARGIN;
            boolean isSouth = mouseY > sceneHeight - RESIZE_MARGIN;

            resizeDirection = 0;
            cursor = Cursor.DEFAULT;

            if (isNorth && isEast) { cursor = Cursor.NE_RESIZE; resizeDirection = HTTOPRIGHT; }
            else if (isNorth && isWest) { cursor = Cursor.NW_RESIZE; resizeDirection = HTTOPLEFT; }
            else if (isSouth && isEast) { cursor = Cursor.SE_RESIZE; resizeDirection = HTBOTTOMRIGHT; }
            else if (isSouth && isWest) { cursor = Cursor.SW_RESIZE; resizeDirection = HTBOTTOMLEFT; }
            else if (isNorth) { cursor = Cursor.N_RESIZE; resizeDirection = HTTOP; }
            else if (isSouth) { cursor = Cursor.S_RESIZE; resizeDirection = HTBOTTOM; }
            else if (isWest) { cursor = Cursor.W_RESIZE; resizeDirection = HTLEFT; }
            else if (isEast) { cursor = Cursor.E_RESIZE; resizeDirection = HTRIGHT; }

            stage.getScene().setCursor(cursor);
        }

        private static final int WM_SYSCOMMAND = 0x0112;
        private static final int SC_SIZE = 0xF000;
        private static final int SC_SIZELEFT = 0xF001;
        private static final int SC_SIZERIGHT = 0xF002;
        private static final int SC_SIZETOP = 0xF003;
        private static final int SC_SIZETOPLEFT = 0xF004;
        private static final int SC_SIZETOPRIGHT = 0xF005;
        private static final int SC_SIZEBOTTOM = 0xF006;
        private static final int SC_SIZEBOTTOMLEFT = 0xF007;
        private static final int SC_SIZEBOTTOMRIGHT = 0xF008;

        public void handleMousePressed(MouseEvent event) {
            if (resizeDirection != 0) {
                WinDef.HWND windowHandle = getHwnd();
                if (windowHandle != null) {
                    int sizeCommand = SC_SIZE;
                    switch (resizeDirection) {
                        case HTLEFT: sizeCommand = SC_SIZELEFT; break;
                        case HTRIGHT: sizeCommand = SC_SIZERIGHT; break;
                        case HTTOP: sizeCommand = SC_SIZETOP; break;
                        case HTTOPLEFT: sizeCommand = SC_SIZETOPLEFT; break;
                        case HTTOPRIGHT: sizeCommand = SC_SIZETOPRIGHT; break;
                        case HTBOTTOM: sizeCommand = SC_SIZEBOTTOM; break;
                        case HTBOTTOMLEFT: sizeCommand = SC_SIZEBOTTOMLEFT; break;
                        case HTBOTTOMRIGHT: sizeCommand = SC_SIZEBOTTOMRIGHT; break;
                    }
                    
                    User32API.INSTANCE.ReleaseCapture();
                    User32API.INSTANCE.PostMessage(windowHandle, WM_SYSCOMMAND, 
                        new WinDef.WPARAM(sizeCommand), 
                        new WinDef.LPARAM(0));
                    event.consume();
                }
            }
        }
    }

    /**
     * Bộ lắng nghe cho JavaFX (macOS/Linux).
     */
    private static class JavaFXResizeListener {
        private final Stage stage;
        private Cursor cursor = Cursor.DEFAULT;
        private boolean resizing = false;
        
        private double startX, startY;
        private double startWidth, startHeight;
        private double startStageX, startStageY;
        
        private boolean isWest, isEast, isNorth, isSouth;

        public JavaFXResizeListener(Stage stage) {
            this.stage = stage;
        }

        public void handleMouseMoved(MouseEvent event) {
            if (resizing) return;
            
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();
            double sceneWidth = stage.getScene().getWidth();
            double sceneHeight = stage.getScene().getHeight();

            isWest = mouseX < RESIZE_MARGIN;
            isEast = mouseX > sceneWidth - RESIZE_MARGIN;
            isNorth = mouseY < RESIZE_MARGIN;
            isSouth = mouseY > sceneHeight - RESIZE_MARGIN;

            if (isNorth && isEast) cursor = Cursor.NE_RESIZE;
            else if (isNorth && isWest) cursor = Cursor.NW_RESIZE;
            else if (isSouth && isEast) cursor = Cursor.SE_RESIZE;
            else if (isSouth && isWest) cursor = Cursor.SW_RESIZE;
            else if (isNorth) cursor = Cursor.N_RESIZE;
            else if (isSouth) cursor = Cursor.S_RESIZE;
            else if (isWest) cursor = Cursor.W_RESIZE;
            else if (isEast) cursor = Cursor.E_RESIZE;
            else cursor = Cursor.DEFAULT;

            stage.getScene().setCursor(cursor);
        }

        public void handleMousePressed(MouseEvent event) {
            if (cursor != Cursor.DEFAULT) {
                resizing = true;
                startX = event.getScreenX();
                startY = event.getScreenY();
                startWidth = stage.getWidth();
                startHeight = stage.getHeight();
                startStageX = stage.getX();
                startStageY = stage.getY();
                event.consume();
            }
        }

        public void handleMouseDragged(MouseEvent event) {
            if (!resizing) return;

            double deltaX = event.getScreenX() - startX;
            double deltaY = event.getScreenY() - startY;
            
            double minWidth = stage.getMinWidth();
            double minHeight = stage.getMinHeight();

            if (isEast) {
                double newWidth = startWidth + deltaX;
                if (newWidth >= minWidth) {
                    stage.setWidth(newWidth);
                }
            } else if (isWest) {
                double newWidth = startWidth - deltaX;
                if (newWidth >= minWidth) {
                    stage.setX(startStageX + deltaX);
                    stage.setWidth(newWidth);
                }
            }

            if (isSouth) {
                double newHeight = startHeight + deltaY;
                if (newHeight >= minHeight) {
                    stage.setHeight(newHeight);
                }
            } else if (isNorth) {
                double newHeight = startHeight - deltaY;
                if (newHeight >= minHeight) {
                    stage.setY(startStageY + deltaY);
                    stage.setHeight(newHeight);
                }
            }
            
            event.consume();
        }

        public void handleMouseReleased(MouseEvent event) {
            resizing = false;
        }
    }
}

