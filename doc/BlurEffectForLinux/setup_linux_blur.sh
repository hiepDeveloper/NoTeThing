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
echo "NoTeThing hiện đã tích hợp thư viện Native để tự động kích hoạt Blur."
echo "Bạn chỉ cần đảm bảo hệ thống đang chạy một Compositor (KWin, Picom, Hyprland...)."

# Phát hiện môi trường Desktop
XDG_CURRENT_DESKTOP=${XDG_CURRENT_DESKTOP:-"unknown"}
echo -e "Môi trường hiện tại: ${GREEN}$XDG_CURRENT_DESKTOP${NC}"

if [[ "$XDG_CURRENT_DESKTOP" == *"KDE"* ]]; then
    echo "Phát hiện KDE Plasma."
    echo -e "${GREEN}✓ Thư viện Native sẽ tự động yêu cầu KWin thực hiện Blur.${NC}"
    echo "Nếu không thấy hiệu ứng, hãy đảm bảo rằng 'Blur' đã được bật trong:"
    echo "System Settings -> Desktop Effects -> Blur (Tích chọn)."
    
elif [[ "$XDG_CURRENT_DESKTOP" == *"Hyprland"* ]]; then
    echo "Phát hiện Hyprland."
    CONFIG_FILE="$HOME/.config/hypr/hyprland.conf"
    if [ -f "$CONFIG_FILE" ]; then
        if grep -q "windowrulev2 = blur" "$CONFIG_FILE" && grep -q "$APP_CLASS" "$CONFIG_FILE"; then
            echo -e "${GREEN}✓ Đã tìm thấy cấu hình Blur cho NoTeThing trong hyprland.conf.${NC}"
        else
            echo "Hyprland yêu cầu một rule nhỏ để kích hoạt blur cho app native."
            echo "Đang thêm rule vào $CONFIG_FILE..."
            echo "" >> "$CONFIG_FILE"
            echo "# NoTeThing Blur Rules" >> "$CONFIG_FILE"
            echo "windowrulev2 = blur, class:^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo "windowrulev2 = ignorezero, class:^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo -e "${GREEN}✓ Đã thêm xong! Hyprland sẽ tự động reload.${NC}"
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
    # Kiểm tra Picom cho X11 (Xfce, i3, bspwm, LXQt...)
    if command -v picom >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Phát hiện Picom đã được cài đặt.${NC}"
        if ! pgrep -x "picom" > /dev/null; then
            echo "Picom đang KHÔNG chạy. Đang thử khởi động Picom..."
            picom --backend glx --blur-method dual_kawase --blur-strength 5 &
            echo -e "${GREEN}✓ Đã khởi động Picom với cấu hình Blur.${NC}"
        else
            echo -e "${GREEN}✓ Picom đang chạy.${NC}"
        fi
        echo "Lưu ý: Bạn nên cấu hình Picom tự động khởi động cùng hệ thống."
    else
        echo -e "${RED}⚠ Không tìm thấy Picom (Compositor cho X11).${NC}"
        read -p "Bạn có muốn cài đặt Picom tự động không? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Đang cài đặt picom..."
            sudo apt update && sudo apt install -y picom
            echo -e "${GREEN}✓ Đã cài đặt Picom. Vui lòng chạy lại script này hoặc khởi động lại ứng dụng.${NC}"
        else
            echo "Vui lòng cài đặt Picom hoặc một Compositor hỗ trợ Blur bằng tay để sử dụng tính năng này."
        fi
    fi
fi

echo -e "${BLUE}=== Hoàn tất hướng dẫn ===${NC}"
read -p "Nhấn Enter để thoát..."
