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
echo "This application supports transparency. The verify background blur effect is managed by your Window Manager/Compositor."

# Detect Desktop Environment
XDG_CURRENT_DESKTOP=${XDG_CURRENT_DESKTOP:-"unknown"}
echo -e "Current Environment: ${GREEN}$XDG_CURRENT_DESKTOP${NC}"

if [[ "$XDG_CURRENT_DESKTOP" == *"KDE"* ]]; then
    echo "KDE Plasma detected."
    echo "To enable Blur on KDE, you need to create a Window Rule."
    echo "1. Go to System Settings -> Window Management -> Window Rules."
    echo "2. Create a New Rule for Window Class: '$APP_CLASS'."
    echo "3. Appearance & Fixes tab -> Check 'Blur background' -> Select 'Force'."
    
elif [[ "$XDG_CURRENT_DESKTOP" == *"Hyprland"* ]]; then
    echo "Hyprland detected."
    CONFIG_FILE="$HOME/.config/hypr/hyprland.conf"
    if [ -f "$CONFIG_FILE" ]; then
        if grep -q "NoTeThing" "$CONFIG_FILE"; then
            echo -e "${GREEN}Configuration for NoTeThing already found in hyprland.conf.${NC}"
        else
            echo "Adding rule to $CONFIG_FILE..."
            echo "" >> "$CONFIG_FILE"
            echo "# NoTeThing Blur Rules" >> "$CONFIG_FILE"
            echo "windowrule = opacity 0.85 0.85, ^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo "windowrulev2 = float, class:^($APP_CLASS)$" >> "$CONFIG_FILE"
            echo -e "${GREEN}Done! Please reload Hyprland (Super + M or command).${NC}"
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
    # Check Picom for X11 (Xfce, i3, bspwm...)
    if pgrep -x "picom" > /dev/null; then
        echo "Picom detected running."
        PICOM_CONF="$HOME/.config/picom/picom.conf"
        if [ ! -f "$PICOM_CONF" ]; then
            PICOM_CONF="$HOME/.config/picom.conf"
        fi
        
        if [ -f "$PICOM_CONF" ]; then
            echo "Please add the following line to the 'opacity-rule' section in $PICOM_CONF:"
            echo -e "${GREEN}\"90:class_g = '$APP_CLASS'\"${NC}"
            echo "And ensure 'blur-background' is enabled with 'dual_kawase' method."
        else
            echo "Picom configuration file not found. Please check your distro documentation."
        fi
    else
        echo -e "${RED}No Blur-supported Compositor detected (like Picom, Hyprland, KWin).${NC}"
        echo "You need to install and run a Compositor (e.g., picom) to get transparency/blur effects."
    fi
fi

echo -e "${BLUE}=== Guide Complete ===${NC}"
read -p "Press Enter to exit..."
