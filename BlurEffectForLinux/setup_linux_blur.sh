#!/bin/bash
# install_blur_effects.sh
# Hướng dẫn và Script tự động cấu hình hiệu ứng kính mờ (Blur) cho NoTeThing trên Linux
# Hỗ trợ: KDE Plasma, GNOME, Hyprland, Picom (X11)

APP_CLASS="NoTeThing"
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Cấu hình Hiệu ứng Kính mờ (Blur) cho NoTeThing ===${NC}"
echo "Ứng dụng này hỗ trợ nền trong suốt. Hiệu ứng làm mờ nền được quản lý bởi Window Manager của bạn."

# Phát hiện môi trường Desktop
XDG_CURRENT_DESKTOP=${XDG_CURRENT_DESKTOP:-"unknown"}
echo -e "Môi trường hiện tại: ${GREEN}$XDG_CURRENT_DESKTOP${NC}"

if [[ "$XDG_CURRENT_DESKTOP" == *"KDE"* ]]; then
    echo "Phát hiện KDE Plasma."
    echo "Để bật Blur trên KDE, bạn cần tạo một Window Rule."
    echo "1. Vào System Settings -> Window Management -> Window Rules."
    echo "2. Tạo Rule mới cho Window Class: '$APP_CLASS'."
    echo "3. Tab Appearance & Fixes -> Tích 'Blur background' -> Chọn 'Force'."
    echo "   Hoặc chạy lệnh kwriteconfig5 (nếu có thể script hóa, nhưng KDE thường yêu cầu GUI)."
    
elif [[ "$XDG_CURRENT_DESKTOP" == *"Hyprland"* ]]; then
    echo "Phát hiện Hyprland."
    CONFIG_FILE="$HOME/.config/hypr/hyprland.conf"
    if [ -f "$CONFIG_FILE" ]; then
        if grep -q "NoTeThing" "$CONFIG_FILE"; then
            echo -e "${GREEN}Đã tìm thấy cấu hình cho NoTeThing trong hyprland.conf.${NC}"
        else
            echo "Đang thêm rule vào $CONFIG_FILE..."
            echo "" >> "$CONFIG_FILE"
            echo "# NoTeThing Blur Rules" >> "$CONFIG_FILE"
            echo "windowrule = opacity 0.85 0.85, ^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo "windowrulev2 = float, class:^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo -e "${GREEN}Đã thêm xong! Vui lòng reload Hyprland (Super + M hoặc command).${NC}"
        fi
    else
        echo -e "${RED}Không tìm thấy file cấu hình Hyprland tại $CONFIG_FILE${NC}"
    fi

elif [[ "$XDG_CURRENT_DESKTOP" == *"GNOME"* ]]; then
    echo "Phát hiện GNOME."
    echo "GNOME mặc định không hỗ trợ blur cho từng app."
    echo "1. Vui lòng cài đặt Extension 'Blur my Shell'."
    echo "   Link: https://extensions.gnome.org/extension/3193/blur-my-shell/"
    echo "2. Mở cài đặt Extension -> Tab Applications -> Thêm '$APP_CLASS' vào whitelist."

else
    # Kiểm tra Picom cho X11 (Xfce, i3, bspwm...)
    if pgrep -x "picom" > /dev/null; then
        echo "Phát hiện Picom đang chạy."
        PICOM_CONF="$HOME/.config/picom/picom.conf"
        if [ ! -f "$PICOM_CONF" ]; then
            PICOM_CONF="$HOME/.config/picom.conf"
        fi
        
        if [ -f "$PICOM_CONF" ]; then
            echo "Vui lòng thêm dòng sau vào phần 'opacity-rule' trong $PICOM_CONF:"
            echo -e "${GREEN}\"90:class_g = '$APP_CLASS'\"${NC}"
            echo "Và đảm bảo 'blur-background' đã được bật với method 'dual_kawase'."
        else
            echo "Không tìm thấy file cấu hình Picom. Vui lòng kiểm tra tài liệu distro của bạn."
        fi
    else
        echo -e "${RED}Không phát hiện Compositor hỗ trợ Blur nào (như Picom, Hyprland, KWin).${NC}"
        echo "Bạn cần cài đặt và chạy một Compositor (ví dụ: picom) để có hiệu ứng trong suốt/mờ."
    fi
fi

echo -e "${BLUE}=== Hoàn tất hướng dẫn ===${NC}"
read -p "Nhấn Enter để thoát..."
