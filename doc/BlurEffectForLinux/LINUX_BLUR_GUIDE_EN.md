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
# Rules for NoTeThing
windowrule = opacity 0.85 0.85, ^(NoTeThing)$
windowrulev2 = float, class:^(NoTeThing)$
windowrulev2 = blur, class:^(NoTeThing)$
windowrulev2 = ignorezero, class:^(NoTeThing)$
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
Use `picom` with `glx` backend. See detailed guide at **[PICOM_BLUR_GUIDE_EN.md](PICOM_BLUR_GUIDE_EN.md)**.
Ensure your config includes:
```conf
blur: {
  method = "dual_kawase";
  strength = 5;
}
```

## 3. Troubleshooting

If the effect is not working, check if the Window Class name is exactly `NoTeThing`:

- **X11 (Picom/KDE/Xfce)**: Run `xprop WM_CLASS` and click on a NoTeThing window.
- **Wayland (Hyprland)**: Run `hyprctl clients` and look for the `class` of NoTeThing.

If the class name is different (e.g., `java-lang-Thread`), replace `NoTeThing` with that name in your configuration.
