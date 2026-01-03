/*
 * Copyright (c) 2025 hiepDeveloper
 * Licensed under the MIT License.
 * Xem chi tiết tại file LICENSE ở thư mục gốc.
 */
package org;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Tiện ích nhận diện giao diện hệ điều hành.
 */
public class ThemeDetector {
    private static final String REG_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
    private static final String REG_VALUE = "AppsUseLightTheme";

    /**
     * Kiểm tra xem hệ thống có đang ở chế độ Tối hay không.
     * Hiện tại hỗ trợ Windows. Mặc định trả về false (Light) nếu không nhận diện được.
     */
    public static boolean isSystemDarkMode() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Đọc registry của Windows
                if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REG_KEY, REG_VALUE)) {
                    int result = Advapi32Util.registryGetIntValue(
                        WinReg.HKEY_CURRENT_USER, REG_KEY, REG_VALUE
                    );
                    return result == 0; // 0 là Dark, 1 là Light
                }
            }
            // TODO: Bổ sung hỗ trợ Linux (DBus) nếu cần trong tương lai
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
