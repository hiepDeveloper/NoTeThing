# Picom Blur Effect Guide for XFCE/Linux
To display a true "Acrylic/Blur" effect for the NoTeThing application on lightweight Linux environments like XFCE, i3wm, or BSPWM, you need to use a compositor called **Picom**.

## 1. Install Picom
Open your Terminal and run the installation command corresponding to your distribution:

- **Debian / Ubuntu / Kali Linux**:
```bash
sudo apt update && sudo apt install picom
```

- **Fedora**:
```bash
sudo dnf install picom
```

- **Arch Linux**:
```bash
sudo pacman -S picom
```

## 2. Configure `picom.conf`
Picom needs to be configured to use the correct blur algorithm and handle JavaFX rounded corners.

1. Create the configuration directory:
```bash
mkdir -p ~/.config/picom
```

2. Copy the default configuration file:
```bash
cp /etc/xdg/picom.conf ~/.config/picom/picom.conf
```

3. Edit the configuration file:
```bash
nano ~/.config/picom/picom.conf
```

4. Paste the following settings into the file:

```conf
# --- Backend Configuration ---
# Use 'glx' for best performance on GPU
backend = "glx";
glx-no-stencil = true;
glx-copy-from-front = false;

# --- Blur Effect (Frosted Glass) ---
blur: {
  method = "dual_kawase";
  strength = 5;      # Blur strength (1-20)
  background = true;
  background-frame = false;
  background-fixed = false;
}

# Exclude components that don't need blur
blur-background-exclude = [
  "window_type = 'dock'",
  "window_type = 'desktop'",
  "_GTK_FRAME_EXTENTS@:c"
];

# --- Rounded Corners ---
corner-radius = 12;  # Should match -fx-background-radius in JavaFX
rounded-corners-exclude = [
  "window_type = 'dock'",
  "window_type = 'desktop'"
];

# --- Shadows ---
shadow = true;
shadow-radius = 12;
shadow-offset-x = -12;
shadow-offset-y = -12;
shadow-opacity = 0.4;
# Avoid double shadows on JavaFX rounded corners
shadow-exclude = [
  "class_g = 'NoTeThing'",
  "_GTK_FRAME_EXTENTS@:c"
];
```

## 3. Activation Steps

**Step 1: Disable XFCE's Default Compositor**
To allow Picom to work without conflicts (which can cause white windows):
1. Go to **Settings -> Window Manager Tweaks**.
2. Select the **Compositor** tab.
3. Uncheck **"Enable display compositing"**.

**Step 2: Start Picom**
Run the following command in your terminal (or add it to your *Startup Applications*):
```bash
picom --config ~/.config/picom/picom.conf --backend glx &
```

**Step 3: Launch NoTeThing**
```bash
./NoTeThing
```

## ⚠️ Important Note for Virtual Machines (VMware/VirtualBox)
If you are using a VM and the window appears completely white:
- Ensure **3D Acceleration** is enabled in your VM settings.
- If hardware acceleration is not supported, change `backend = "xrender"` in `picom.conf`. (Note: `xrender` does not support `dual_kawase`, so the blur effect will be weaker).

**Tip**: You can verify if Picom recognizes the application correctly by running `xprop WM_CLASS` and clicking on the NoTeThing window. The returned class must match the `class_g` in your configuration.
