package org;

import java.lang.reflect.Method;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javafx.stage.Stage;

public class GlassHelper {

    // ================= LINUX NATIVE INTERFACE =================
    public interface LinuxBlurLib extends com.sun.jna.Library {
        // Tên thư viện là "notething_blur" (tương ứng file libnotething_blur.so)
        LinuxBlurLib INSTANCE = com.sun.jna.Native.load("notething_blur", LinuxBlurLib.class);
        int enable_blur_x11(long windowId);
    }

    public static void applyBlur(Stage stage, boolean enable) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            applyWindowsBlur(stage, enable);
        } else if (os.contains("linux")) {
            if (enable) {
                applyLinuxBlur(stage);
            }
        }
    }

    private static void applyLinuxBlur(Stage stage) {
        javafx.application.Platform.runLater(() -> {
            try {
                long xid = getLinuxWindowId(stage);
                if (xid != 0) {
                    int result = LinuxBlurLib.INSTANCE.enable_blur_x11(xid);
                    if (result == 0) {
                        System.out.println("✓ Linux blur enabled for XID: " + xid);
                    } else {
                        System.err.println("✗ Failed to enable Linux blur, result: " + result);
                    }
                }
            } catch (Throwable e) {
                // Thường xảy ra nếu không tìm thấy file .so hoặc không phải môi trường X11
                System.err.println("⚠ Không thể áp dụng blur Linux: " + e.getMessage());
            }
        });
    }

    /**
     * Dùng Reflection để "móc" XID (Native Window ID) từ nội bộ JavaFX trên Linux.
     */
    private static long getLinuxWindowId(Stage stage) {
        try {
            // Stage -> Window -> Peer (TKStage)
            Method getPeer = javafx.stage.Window.class.getDeclaredMethod("getPeer");
            getPeer.setAccessible(true);
            Object peer = getPeer.invoke(stage);

            if (peer != null) {
                // Trên Linux, Peer thường là com.sun.glass.ui.gtk.GtkWindow
                Method getNativeWindow = peer.getClass().getMethod("getNativeWindow");
                getNativeWindow.setAccessible(true);
                Object result = getNativeWindow.invoke(peer);
                if (result instanceof Long) {
                    return (Long) result;
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return 0;
    }

    /**
     * Bật/Tắt chế độ tối cho thanh tiêu đề hệ thống (Windows 10/11).
     */
    public static void setDarkTitleBar(Stage stage, boolean dark) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            javafx.application.Platform.runLater(() -> {
                try {
                    String title = stage.getTitle();
                    if (title == null || title.isEmpty()) return;
                    WinDef.HWND hwnd = User32Wrapper.INSTANCE.FindWindow(null, title);
                    if (hwnd != null) {
                        IntByReference pvAttribute = new IntByReference(dark ? 1 : 0);
                        // DWMWA_USE_IMMERSIVE_DARK_MODE = 20
                        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 20, pvAttribute, 4);
                        
                        // Đối với một số phiên bản Windows cũ hơn, cần attribute 19
                        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 19, pvAttribute, 4);
                    }
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    // ================= WINDOWS IMPLEMENTATION =================

    private static void applyWindowsBlur(Stage stage, boolean enable) {
        javafx.application.Platform.runLater(() -> {
            try {
                String title = stage.getTitle();
                if (title == null || title.isEmpty()) return;

                WinDef.HWND hwnd = User32Wrapper.INSTANCE.FindWindow(null, title);
                
                if (hwnd != null) {
                    WindowCompositionAttributeData data = new WindowCompositionAttributeData();
                    data.Attribute = User32Wrapper.WindowCompositionAttribute.WCA_ACCENT_POLICY;
                    
                    AccentPolicy accent = new AccentPolicy();
                    accent.AccentState = enable ? User32Wrapper.AccentState.ACCENT_ENABLE_BLURBEHIND : User32Wrapper.AccentState.ACCENT_DISABLED;
                    accent.AccentFlags = 0;
                    accent.GradientColor = 0; 
                    accent.AnimationId = 0;
                    accent.write();
                    
                    data.Data = accent.getPointer();
                    data.SizeOfData = accent.size();
                    
                    User32Wrapper.INSTANCE.SetWindowCompositionAttribute(hwnd, data);

                    try {
                        IntByReference preference = new IntByReference(2);
                        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 33, preference, 4);
                    } catch (Exception e) {}

                    // Ngăn việc thêm nhiều listener nếu hàm này được gọi nhiều lần
                    if (stage.getProperties().get("blur_listeners_added") == null) {
                        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
                             updateWindowRegion(hwnd, stage);
                        };
                        stage.widthProperty().addListener(resizeListener);
                        stage.heightProperty().addListener(resizeListener);
                        stage.getProperties().put("blur_listeners_added", true);
                    }
                    
                    // Cập nhật lần đầu
                    updateWindowRegion(hwnd, stage);
                }
            } catch (Exception e) {
                // Ignore
            }
        });
    }

    private static void updateWindowRegion(WinDef.HWND hwnd, Stage stage) {
        try {
            double scaleX = stage.getOutputScaleX();
            double scaleY = stage.getOutputScaleY();
            
            double w = stage.getWidth();
            double h = stage.getHeight();

            if (w <= 0 || h <= 0) return;

            // Tính toán pixel thực
            int pixelW = (int) Math.ceil(w * scaleX);
            int pixelH = (int) Math.ceil(h * scaleY);
            
            int cornerRadius = (int) (7 * scaleX);

            // Gdi32 CreateRoundRectRgn:
            // nWidthEllipse và nHeightEllipse là ĐƯỜNG KÍNH của cung bo tròn.
            int diameter = 2 * cornerRadius;
            WinDef.HRGN rgn = Gdi32Wrapper.INSTANCE.CreateRoundRectRgn(0, 0, pixelW, pixelH, diameter, diameter);
            
            // bRedraw = true để vẽ lại cửa sổ ngay lập tức
            User32Wrapper.INSTANCE.SetWindowRgn(hwnd, rgn, true);
        } catch (Exception e) {}
    }

    private interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    private interface Gdi32Wrapper extends StdCallLibrary {
        Gdi32Wrapper INSTANCE = Native.load("gdi32", Gdi32Wrapper.class, W32APIOptions.DEFAULT_OPTIONS);
        WinDef.HRGN CreateRoundRectRgn(int nLeftRect, int nTopRect, int nRightRect, int nBottomRect, int nWidthEllipse, int nHeightEllipse);
    }

    private interface User32Wrapper extends StdCallLibrary {
        User32Wrapper INSTANCE = Native.load("user32", User32Wrapper.class, W32APIOptions.DEFAULT_OPTIONS);

        WinDef.HWND FindWindow(String lpClassName, String lpWindowName);
        int SetWindowCompositionAttribute(WinDef.HWND hWnd, WindowCompositionAttributeData data);
        int SetWindowRgn(WinDef.HWND hWnd, WinDef.HRGN hRgn, boolean bRedraw);

        interface WindowCompositionAttribute {
            int WCA_ACCENT_POLICY = 19;
        }

        interface AccentState {
            int ACCENT_DISABLED = 0;
            int ACCENT_ENABLE_GRADIENT = 1;
            int ACCENT_ENABLE_TRANSPARENTGRADIENT = 2;
            int ACCENT_ENABLE_BLURBEHIND = 3;
            int ACCENT_ENABLE_ACRYLICBLURBEHIND = 4;
            int ACCENT_INVALID_STATE = 5;
        }
    }

    @Structure.FieldOrder({"Attribute", "Data", "SizeOfData"})
    public static class WindowCompositionAttributeData extends Structure {
        public int Attribute;
        public Pointer Data;
        public int SizeOfData;
    }

    @Structure.FieldOrder({"AccentState", "AccentFlags", "GradientColor", "AnimationId"})
    public static class AccentPolicy extends Structure {
        public int AccentState;
        public int AccentFlags;
        public int GradientColor;
        public int AnimationId;
    }
}
