/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javafx.stage.Stage;

/**
 * Hỗ trợ hiệu ứng kính mờ (Acrylic/Mica) trên Windows.
 */
public class GlassHelper {
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);

        // DWMWA_SYSTEMBACKDROP_TYPE = 38
        // DWMWA_MICA_EFFECT = 1029 (Old Win11)
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, WinDef.DWORDByReference pvAttribute, int cbAttribute);
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }

    public static void applyBlur(Stage stage) {
        if (!IS_WINDOWS) return;

        // Tìm HWND của cửa sổ
        String title = stage.getTitle();
        if (title == null || title.isEmpty()) return;

        WinDef.HWND hwnd = ResizeHelper.User32API.INSTANCE.FindWindow(null, title);
        if (hwnd == null) return;

        // Bật hiệu ứng Acrylic (System Backdrop Type 38, Value 3)
        // Lưu ý: Chỉ hoạt động tốt trên Windows 11 và một số bản Windows 10 mới
        int DWMWA_SYSTEMBACKDROP_TYPE = 38;
        int DWMSBT_TRANSIENTBACKDROP = 3; // Acrylic

        com.sun.jna.Memory mem = new com.sun.jna.Memory(4);
        mem.setInt(0, DWMSBT_TRANSIENTBACKDROP);
        
        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, DWMWA_SYSTEMBACKDROP_TYPE, mem, 4);
    }
}
