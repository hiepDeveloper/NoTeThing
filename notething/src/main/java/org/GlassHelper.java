package org;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javafx.stage.Stage;

public class GlassHelper {

    public static void applyBlur(Stage stage, boolean enable) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            applyWindowsBlur(stage, enable);
        }
        // Trên Linux/Mac, JavaFX tự handle Transparent Stage.
        // Hiệu ứng Blur sẽ do OS Compositor đảm nhận.
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

    // ================= LINUX IMPLEMENTATION =================
    
    // Trên Linux (Wayland/X11), chúng ta tuân thủ "Native Compositor Rule".
    // Ứng dụng chỉ cần render background trong suốt (StageStyle.TRANSPARENT và CSS rgba).
    // Việc Blur sẽ do Compositor (KWin, Hyprland, Picom...) đảm nhận thông qua Window Rules
    // cấu hình bởi người dùng (dựa trên Window Class/Title).
    // Không can thiệp sâu bằng JNA để đảm bảo độ ổn định và tương thích.

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
