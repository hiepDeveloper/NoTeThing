#!/bin/bash
# install_blur_effects_en.sh
# Guide and Script to automatically configure Blur effects for NoTeThing on Linux
# Supports: KDE Plasma, GNOME, Hyprland, Picom (X11)

APP_CLASS="NoTeThing"
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Setup Blur Effects for NoTeThing ===${NC}"
echo "NoTeThing now includes a Native library to automatically trigger Blur."
echo "You just need to ensure your system is running a Compositor (KWin, Picom, Hyprland...)."

# Detect Desktop Environment
XDG_CURRENT_DESKTOP=${XDG_CURRENT_DESKTOP:-"unknown"}
echo -e "Current Environment: ${GREEN}$XDG_CURRENT_DESKTOP${NC}"

if [[ "$XDG_CURRENT_DESKTOP" == *"KDE"* ]]; then
    echo "KDE Plasma detected."
    echo -e "${GREEN}✓ The Native library will automatically request KWin to Blur.${NC}"
    echo "If you don't see the effect, make sure 'Blur' is enabled in:"
    echo "System Settings -> Desktop Effects -> Blur (Check it)."
    
elif [[ "$XDG_CURRENT_DESKTOP" == *"Hyprland"* ]]; then
    echo "Hyprland detected."
    CONFIG_FILE="$HOME/.config/hypr/hyprland.conf"
    if [ -f "$CONFIG_FILE" ]; then
        if grep -q "windowrulev2 = blur" "$CONFIG_FILE" && grep -q "$APP_CLASS" "$CONFIG_FILE"; then
            echo -e "${GREEN}✓ Blur configuration for NoTeThing already found in hyprland.conf.${NC}"
        else
            echo "Hyprland requires a small rule to enable blur for native apps."
            echo "Adding rule to $CONFIG_FILE..."
            echo "" >> "$CONFIG_FILE"
            echo "# NoTeThing Blur Rules" >> "$CONFIG_FILE"
            echo "windowrulev2 = blur, class:^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo "windowrulev2 = ignorezero, class:^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo -e "${GREEN}✓ Done! Hyprland will reload automatically.${NC}"
        fi
    else
        echo -e "${RED}Hyprland config file not found at $CONFIG_FILE${NC}"
    fi

elif [[ "$XDG_CURRENT_DESKTOP" == *"GNOME"* ]]; then
    echo "GNOME detected."
    echo "GNOME does not support per-app blur by default."
    echo "1. Please install the extension 'Blur my Shell'."
    echo "   Link: https://extensions.gnome.org/extension/3193/blur-my-shell/"
    echo "2. Open Extension Settings -> Applications tab -> Add '$APP_CLASS' to whitelist."

else
    # Check Picom for X11 (Xfce, i3, bspwm, LXQt...)
    if command -v picom >/dev/null 2>&1; then
        echo -e "${GREEN}✓ Picom is detected as installed.${NC}"
        if ! pgrep -x "picom" > /dev/null; then
            echo "Picom is NOT running. Attempting to start Picom..."
            picom --backend glx --blur-method dual_kawase --blur-strength 5 &
            echo -e "${GREEN}✓ Started Picom with Blur configuration.${NC}"
        else
            echo -e "${GREEN}✓ Picom is already running.${NC}"
        fi
        echo "Note: You should configure Picom to start automatically with your system."
    else
        echo -e "${RED}⚠ Picom (X11 Compositor) not found.${NC}"
        read -p "Would you like to install Picom automatically? (y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Installing picom..."
            sudo apt update && sudo apt install -y picom
            echo -e "${GREEN}✓ Picom installed. Please run this script again or restart the application.${NC}"
        else
            echo "Please install Picom or a Blur-supported Compositor manually to use this feature."
        fi
    fi
fi

echo -e "${BLUE}=== Guide Complete ===${NC}"
read -p "Press Enter to exit..."
