# Hướng Dẫn Bật Hiệu Ứng Kính Mờ (Blur) Trên Linux

NoTeThing hỗ trợ giao diện nền trong suốt (Transparency). Để có hiệu ứng kính mờ (Blur) đẹp mắt phía sau cửa sổ ghi chú, bạn cần kích hoạt tính năng Blur trên Trình quản lý cửa sổ (Window Manager/Compositor) của mình.

Mã định danh ứng dụng (App ID / Window Class): `NoTeThing`

## 1. Tự Động Cấu Hình (Script)
Chạy file script đi kèm để nhận hướng dẫn chi tiết hoặc tự động cấu hình (với Hyprland):

```bash
chmod +x setup_linux_blur.sh
./setup_linux_blur.sh
```

## 2. Cấu Hình Thủ Công

### Hyprland (Wayland)
Thêm vào file `~/.config/hypr/hyprland.conf`:
```ini
windowrule = opacity 0.85 0.85, ^(NoTeThing)$
windowrulev2 = float, class:^(NoTeThing)$
```

### KDE Plasma (KWin)
1. Vào **System Settings** -> **Window Management** -> **Window Rules**.
2. Tạo Rule mới -> Bấm **Detect Window Properties** -> Chọn cửa sổ ghi chú.
3. Tab **Appearance & Fixes**:
   - Tích **Blur background** -> Chọn **Force**.
   - Tích **Active opacity** -> Chọn **Force** -> Set **85%**.

### GNOME
1. Cài đặt Extension **[Blur my Shell](https://extensions.gnome.org/extension/3193/blur-my-shell/)**.
2. Mở cài đặt Extension -> **Applications**.
3. Bật Blur cho ứng dụng cụ thể và nhập `NoTeThing`.

### Xfce / i3wm / bspwm (Dùng Picom)
Sử dụng `picom` với backend `glx` hoặc fork `picom-pijulius` để hỗ trợ blur đẹp.
Trong file `picom.conf`:
```conf
blur: {
  method = "dual_kawase";
  strength = 6;
  background = true;
}
opacity-rule = [
  "85:class_g = 'NoTeThing'"
];
shadow-exclude = [
  "class_g = 'NoTeThing' && window_type = 'popup_menu'",
  "class_g = 'NoTeThing' && window_type = 'tooltip'"
];
```
