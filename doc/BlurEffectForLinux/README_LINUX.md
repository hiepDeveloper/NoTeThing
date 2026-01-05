# Linux Blur Effect Guide

NoTeThing now includes a **Native Library (Rust)** to automatically request the system to enable the blur effect. You no longer need to manually configure complex window rules for most desktop environments.

## 🚀 Quick Setup (Recommended)

We provide an automated script to check and install any missing components:

1. Open a Terminal in the application directory.
2. Run the command:
   ```bash
   chmod +x setup_linux_blur_en.sh
   ./setup_linux_blur_en.sh
   ```

This script will:
- Detect your Desktop Environment (KDE, GNOME, Hyprland, etc.).
- Automatically install **Picom** (if you are on X11 like Xfce/i3).
- Configure necessary rules for **Hyprland**.
- Provide instructions for enabling effects in system settings (KDE/GNOME).

## 🛠️ System Requirements

For the Blur effect to work, your system needs a **Compositor**:
- **KDE Plasma**: KWin (Enabled by default).
- **Hyprland / Sway**: (Enabled by default).
- **XFCE / i3 / Openbox / LXQt**: Requires **Picom**.
- **GNOME**: Requires the **Blur my Shell** extension.

## 📝 Important Notes

- **X11 Environment**: The app automatically sends signals to the Compositor via the Native library.
- **Wayland Environment**: 
  - If running via XWayland (JavaFX default), KDE will still detect it automatically.
  - For Hyprland, the setup script will add a configuration line to `hyprland.conf`.

## ❓ Troubleshooting

### Blur disappears when focusing a note (GNOME)
If you are using GNOME with the **Blur my Shell** extension, the blur effect might disappear as soon as you focus (click on) a note. To fix this:
1. Open the **Blur my Shell** extension settings.
2. Go to the **Applications** or general settings tab.
3. Find the **"Opaque focused window"** option.
4. Set it to **OFF**.
5. The note will now remain blurred regardless of focus.

---
*If the blur effect is not visible, please run the setup script for troubleshooting.*
