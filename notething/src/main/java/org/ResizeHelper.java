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
    private static final int RESIZE_MARGIN = 8;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final boolean IS_LINUX = System.getProperty("os.name").toLowerCase().contains("nux") || System.getProperty("os.name").toLowerCase().contains("nix");

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
        } else if (IS_LINUX) {
            addLinuxResizeListener(stage);
        } else {
            addJavaFXResizeListener(stage);
        }
    }

    private static void addWindowsResizeListener(Stage stage) {
        WindowsResizeListener listener = new WindowsResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, listener::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, listener::handleMousePressed);
        listener.getHwnd(); 
    }

    private static void addLinuxResizeListener(Stage stage) {
        LinuxResizeListener listener = new LinuxResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, listener::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, listener::handleMousePressed);
        javafx.application.Platform.runLater(listener::getWindowId);
    }

    public static void startNativeDrag(Stage stage, MouseEvent event) {
        if (IS_WINDOWS) {
            WindowsResizeListener.startMove(stage);
        } else if (IS_LINUX) {
            LinuxResizeListener.startMove(stage, event);
        }
    }

    private static void addJavaFXResizeListener(Stage stage) {
        JavaFXResizeListener listener = new JavaFXResizeListener(stage);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, listener::handleMouseMoved);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, listener::handleMousePressed);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_DRAGGED, listener::handleMouseDragged);
        stage.getScene().addEventFilter(MouseEvent.MOUSE_RELEASED, listener::handleMouseReleased);
    }

    // ================= WINDOWS LISTENER =================
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
            stage.showingProperty().addListener((obs, old, newVal) -> {
                if (newVal) javafx.application.Platform.runLater(this::getHwnd);
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
        private static final int SC_SIZELEFT = 0xF001;
        private static final int SC_SIZERIGHT = 0xF002;
        private static final int SC_SIZETOP = 0xF003;
        private static final int SC_SIZETOPLEFT = 0xF004;
        private static final int SC_SIZETOPRIGHT = 0xF005;
        private static final int SC_SIZEBOTTOM = 0xF006;
        private static final int SC_SIZEBOTTOMLEFT = 0xF007;
        private static final int SC_SIZEBOTTOMRIGHT = 0xF008;
        private static final int SC_MOVE = 0xF010;

        public void handleMousePressed(MouseEvent event) {
            if (resizeDirection != 0) {
                WinDef.HWND windowHandle = getHwnd();
                if (windowHandle != null) {
                    int sizeCommand = switch (resizeDirection) {
                        case HTLEFT -> SC_SIZELEFT;
                        case HTRIGHT -> SC_SIZERIGHT;
                        case HTTOP -> SC_SIZETOP;
                        case HTTOPLEFT -> SC_SIZETOPLEFT;
                        case HTTOPRIGHT -> SC_SIZETOPRIGHT;
                        case HTBOTTOM -> SC_SIZEBOTTOM;
                        case HTBOTTOMLEFT -> SC_SIZEBOTTOMLEFT;
                        case HTBOTTOMRIGHT -> SC_SIZEBOTTOMRIGHT;
                        default -> 0;
                    };
                    
                    if (sizeCommand != 0) {
                        User32API.INSTANCE.ReleaseCapture();
                        User32API.INSTANCE.PostMessage(windowHandle, WM_SYSCOMMAND, new WinDef.WPARAM(sizeCommand), new WinDef.LPARAM(0));
                        event.consume();
                    }
                }
            }
        }

        public static void startMove(Stage stage) {
            String title = stage.getTitle();
            if (title != null && !title.isEmpty()) {
                WinDef.HWND windowHandle = User32API.INSTANCE.FindWindow(null, title);
                if (windowHandle != null) {
                    User32API.INSTANCE.ReleaseCapture();
                    // SC_MOVE (0xF010) + HTCAPTION (2)
                    User32API.INSTANCE.PostMessage(windowHandle, WM_SYSCOMMAND, new WinDef.WPARAM(SC_MOVE | 2), new WinDef.LPARAM(0));
                }
            }
        }
    }

    // ================= LINUX LISTENER =================
    private static class LinuxResizeListener {
        private final Stage stage;
        private com.sun.jna.NativeLong windowId;
        private Cursor cursor = Cursor.DEFAULT;
        private int resizeDetail = -1; 

        private static final int _NET_WM_MOVERESIZE_SIZE_TOPLEFT = 0;
        private static final int _NET_WM_MOVERESIZE_SIZE_TOP = 1;
        private static final int _NET_WM_MOVERESIZE_SIZE_TOPRIGHT = 2;
        private static final int _NET_WM_MOVERESIZE_SIZE_RIGHT = 3;
        private static final int _NET_WM_MOVERESIZE_SIZE_BOTTOMRIGHT = 4;
        private static final int _NET_WM_MOVERESIZE_SIZE_BOTTOM = 5;
        private static final int _NET_WM_MOVERESIZE_SIZE_BOTTOMLEFT = 6;
        private static final int _NET_WM_MOVERESIZE_SIZE_LEFT = 7;
        private static final int _NET_WM_MOVERESIZE_MOVE = 8;

        public LinuxResizeListener(Stage stage) {
            this.stage = stage;
            stage.getProperties().put("linux_resize_listener", this);
            stage.showingProperty().addListener((obs, old, newVal) -> {
                if (newVal) javafx.application.Platform.runLater(this::getWindowId);
            });
        }
        
        private interface X11 extends com.sun.jna.Library {
            X11 INSTANCE = com.sun.jna.Native.load("X11", X11.class);
            class Window extends com.sun.jna.NativeLong { 
                public Window() { super(); }
                public Window(long value) { super(value); }
            }
            class Atom extends com.sun.jna.NativeLong {
                public Atom() { super(); }
                public Atom(long value) { super(value); } 
            }
            class Display extends com.sun.jna.PointerType {}
            Display XOpenDisplay(String display_name);
            Window XDefaultRootWindow(Display display);
            int XCloseDisplay(Display display);
            int XFlush(Display display);
            int XUngrabPointer(Display display, com.sun.jna.NativeLong time);
            Atom XInternAtom(Display display, String atom_name, boolean only_if_exists);
            int XSendEvent(Display display, Window w, boolean propagate, com.sun.jna.NativeLong event_mask, XClientMessageEvent event);
            int XFetchName(Display display, Window w, com.sun.jna.ptr.PointerByReference window_name_return);
            int XQueryTree(Display display, Window w, com.sun.jna.ptr.NativeLongByReference root, com.sun.jna.ptr.NativeLongByReference parent, com.sun.jna.ptr.PointerByReference children, com.sun.jna.ptr.IntByReference nchildren);
            int XFree(com.sun.jna.Pointer data);
        }

        @com.sun.jna.Structure.FieldOrder({"type", "serial", "send_event", "display", "window", "message_type", "format", "data"})
        public static class XClientMessageEvent extends com.sun.jna.Structure {
             public int type;
             public com.sun.jna.NativeLong serial;
             public boolean send_event;
             public X11.Display display;
             public X11.Window window;
             public X11.Atom message_type;
             public int format;
             public com.sun.jna.NativeLong[] data = new com.sun.jna.NativeLong[5];
             public XClientMessageEvent() {
                 this.type = 33; 
                 this.format = 32;
             }
        }
        
        private com.sun.jna.NativeLong getWindowId() {
            if (windowId == null) {
                 try {
                     X11 x11 = X11.INSTANCE;
                     X11.Display display = x11.XOpenDisplay(null);
                     if (display != null) {
                         X11.Window root = x11.XDefaultRootWindow(display);
                         String title = stage.getTitle();
                         if (title != null && !title.isEmpty()) {
                             com.sun.jna.NativeLong rawId = findWindowByTitle(x11, display, root, title);
                             if (rawId != null) {
                                 // Leo lên để tìm Top-level window thực sự (quan trọng cho GNOME)
                                 windowId = getTopLevelWindow(x11, display, new X11.Window(rawId.longValue()), root);
                             }
                         }
                         x11.XCloseDisplay(display);
                     }
                 } catch (Exception e) {}
            }
            return windowId;
        }

        private com.sun.jna.NativeLong getTopLevelWindow(X11 x11, X11.Display display, X11.Window window, X11.Window root) {
            com.sun.jna.ptr.NativeLongByReference rootReturn = new com.sun.jna.ptr.NativeLongByReference();
            com.sun.jna.ptr.NativeLongByReference parentReturn = new com.sun.jna.ptr.NativeLongByReference();
            com.sun.jna.ptr.PointerByReference childrenReturn = new com.sun.jna.ptr.PointerByReference();
            com.sun.jna.ptr.IntByReference nChildrenReturn = new com.sun.jna.ptr.IntByReference();

            X11.Window current = window;
            while (x11.XQueryTree(display, current, rootReturn, parentReturn, childrenReturn, nChildrenReturn) != 0) {
                if (childrenReturn.getValue() != null) {
                    x11.XFree(childrenReturn.getValue());
                }
                long parent = parentReturn.getValue().longValue();
                long rootId = rootReturn.getValue().longValue();
                if (parent == rootId || parent == 0) {
                    return current;
                }
                current = new X11.Window(parent);
            }
            return window;
        }
        
        private com.sun.jna.NativeLong findWindowByTitle(X11 x11, X11.Display display, X11.Window window, String targetTitle) {
             com.sun.jna.ptr.PointerByReference ptr = new com.sun.jna.ptr.PointerByReference();
             if (x11.XFetchName(display, window, ptr) != 0 && ptr.getValue() != null) {
                 String name = ptr.getValue().getString(0);
                 x11.XFree(ptr.getValue());
                 if (targetTitle.equals(name)) return window;
             }
             com.sun.jna.ptr.NativeLongByReference root = new com.sun.jna.ptr.NativeLongByReference();
             com.sun.jna.ptr.NativeLongByReference parent = new com.sun.jna.ptr.NativeLongByReference();
             com.sun.jna.ptr.PointerByReference children = new com.sun.jna.ptr.PointerByReference();
             com.sun.jna.ptr.IntByReference nChildren = new com.sun.jna.ptr.IntByReference();
             if (x11.XQueryTree(display, window, root, parent, children, nChildren) != 0) {
                 int count = nChildren.getValue();
                 if (count > 0) {
                     long[] ids;
                     int size = com.sun.jna.Native.LONG_SIZE;
                     if (size == 8) ids = children.getValue().getLongArray(0, count);
                     else {
                         ids = new long[count];
                         int[] ints = children.getValue().getIntArray(0, count);
                         for(int i=0; i<count; i++) ids[i] = ints[i];
                     }
                     x11.XFree(children.getValue());
                     for (long id : ids) {
                         com.sun.jna.NativeLong res = findWindowByTitle(x11, display, new X11.Window(id), targetTitle);
                         if (res != null) return res;
                     }
                 }
             }
             return null;
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
            resizeDetail = -1;
            cursor = Cursor.DEFAULT;
            if (isNorth && isEast) { cursor = Cursor.NE_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_TOPRIGHT; }
            else if (isNorth && isWest) { cursor = Cursor.NW_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_TOPLEFT; }
            else if (isSouth && isEast) { cursor = Cursor.SE_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_BOTTOMRIGHT; }
            else if (isSouth && isWest) { cursor = Cursor.SW_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_BOTTOMLEFT; }
            else if (isNorth) { cursor = Cursor.N_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_TOP; }
            else if (isSouth) { cursor = Cursor.S_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_BOTTOM; }
            else if (isWest) { cursor = Cursor.W_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_LEFT; }
            else if (isEast) { cursor = Cursor.E_RESIZE; resizeDetail = _NET_WM_MOVERESIZE_SIZE_RIGHT; }
            stage.getScene().setCursor(cursor);
        }

        public static void startMove(Stage stage, MouseEvent event) {
            LinuxResizeListener listener = (LinuxResizeListener) stage.getProperties().get("linux_resize_listener");
            if (listener != null) {
                listener.executeNativeAction(event, _NET_WM_MOVERESIZE_MOVE);
            }
        }

        public void handleMousePressed(MouseEvent event) {
            if (resizeDetail != -1) {
                executeNativeAction(event, resizeDetail);
            }
        }

        private void executeNativeAction(MouseEvent event, int direction) {
            if (getWindowId() != null) {
                new Thread(() -> {
                    try {
                        X11 x11 = X11.INSTANCE;
                        X11.Display display = x11.XOpenDisplay(null);
                        if (display == null) return;
                        X11.Window root = x11.XDefaultRootWindow(display);
                        X11.Window win = new X11.Window(windowId.longValue());
                        x11.XUngrabPointer(display, new com.sun.jna.NativeLong(0));
                        x11.XFlush(display);
                        XClientMessageEvent msg = new XClientMessageEvent();
                        msg.display = display;
                        msg.window = win;
                        msg.message_type = x11.XInternAtom(display, "_NET_WM_MOVERESIZE", false);
                        msg.format = 32;
                        msg.data[0] = new com.sun.jna.NativeLong((long)event.getScreenX());
                        msg.data[1] = new com.sun.jna.NativeLong((long)event.getScreenY());
                        msg.data[2] = new com.sun.jna.NativeLong(direction);
                        msg.data[3] = new com.sun.jna.NativeLong(1);
                        msg.data[4] = new com.sun.jna.NativeLong(1);
                        com.sun.jna.NativeLong mask = new com.sun.jna.NativeLong(0x00100000L | 0x00080000L | 0x00010000L); // Thêm SubstructureRedirect
                        x11.XSendEvent(display, root, false, mask, msg);
                        x11.XFlush(display);
                        x11.XCloseDisplay(display);
                    } catch(Exception e) {}
                }).start();
                event.consume();
            }
        }
    }

    // ================= JAVAFX LISTENER (FALLBACK) =================
    private static class JavaFXResizeListener {
        private final Stage stage;
        private Cursor cursor = Cursor.DEFAULT;
        private boolean resizing = false;
        private double startX, startY, startWidth, startHeight, startStageX, startStageY;
        private boolean isWest, isEast, isNorth, isSouth;
        public JavaFXResizeListener(Stage stage) { this.stage = stage; }
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
                if (newWidth >= minWidth) stage.setWidth(Math.round(newWidth));
            } else if (isWest) {
                double newWidth = startWidth - deltaX;
                if (newWidth >= minWidth) {
                    stage.setX(Math.round(startStageX + deltaX));
                    stage.setWidth(Math.round(newWidth));
                }
            }
            if (isSouth) {
                double newHeight = startHeight + deltaY;
                if (newHeight >= minHeight) stage.setHeight(Math.round(newHeight));
            } else if (isNorth) {
                double newHeight = startHeight - deltaY;
                if (newHeight >= minHeight) {
                    stage.setY(Math.round(startStageY + deltaY));
                    stage.setHeight(Math.round(newHeight));
                }
            }
            event.consume();
        }
        public void handleMouseReleased(MouseEvent event) { resizing = false; }
    }
}
