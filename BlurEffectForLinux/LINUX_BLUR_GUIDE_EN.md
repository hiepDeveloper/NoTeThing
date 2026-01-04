# Linux Blur Effect Guide

NoTeThing supports a transparent background interface. To achieve the beautiful frosted glass (Blur) effect behind the note windows, you need to enable the Blur feature in your Window Manager/Compositor settings.

App ID / Window Class: `NoTeThing`

## 1. Automatic Configuration (Script)
Run the included script for detailed instructions or automatic configuration (for Hyprland):

```bash
chmod +x setup_linux_blur_en.sh
./setup_linux_blur_en.sh
```

## 2. Manual Configuration

### Hyprland (Wayland)
Add to your `~/.config/hypr/hyprland.conf`:
```ini
windowrule = opacity 0.85 0.85, ^(NoTeThing)$
windowrulev2 = float, class:^(NoTeThing)$
```

### KDE Plasma (KWin)
1. Go to **System Settings** -> **Window Management** -> **Window Rules**.
2. Create **New Rule** -> Click **Detect Window Properties** -> Select a note window.
3. **Appearance & Fixes** tab:
   - Check **Blur background** -> Select **Force**.
   - Check **Active opacity** -> Select **Force** -> Set **85%**.

### GNOME
1. Install **[Blur my Shell](https://extensions.gnome.org/extension/3193/blur-my-shell/)** extension.
2. Open Extension Settings -> **Applications**.
3. Enable Blur for specific apps and enter `NoTeThing`.

### Xfce / i3wm / bspwm (Using Picom)
Use `picom` with `glx` backend or `picom-pijulius` fork for best results.

**Quick Setup:** Copy the entire content of the provided [picom.conf](./picom.conf) file in this directory and replace your current configuration at `~/.config/picom/picom.conf`.

If you prefer manual configuration, ensure you have these settings:
```conf
# 1. Enable rounded corner detection to fix square blur
detect-rounded-corners = true;

# 2. Blur configuration (Frost effect)
blur: {
  method = "dual_kawase";
  strength = 7;
  background = true;
}

# 3. Opacity rules and Popup exclusion
opacity-rule = [
  "85:class_g = 'NoTeThing' && window_type = 'normal'"
];

blur-background-exclude = [
  "class_g = 'NoTeThing' && !window_type = 'normal'",
  "window_type = 'popup_menu'",
  "window_type = 'tooltip'"
];
```
