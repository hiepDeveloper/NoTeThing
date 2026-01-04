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
# Quy tắc cho NoTeThing
windowrule = opacity 0.85 0.85, ^(NoTeThing)$
windowrulev2 = float, class:^(NoTeThing)$
windowrulev2 = blur, class:^(NoTeThing)$
windowrulev2 = ignorezero, class:^(NoTeThing)$
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
Sử dụng `picom` với backend `glx`. Xem hướng dẫn chi tiết tại **[PICOM_BLUR_GUIDE.md](PICOM_BLUR_GUIDE.md)**.
Đảm bảo file cấu hình có đoạn:
```conf
blur: {
  method = "dual_kawase";
  strength = 5;
}
```

## 3. Xử lý lỗi (Troubleshooting)

Nếu hiệu ứng không hoạt động, hãy kiểm tra xem tên lớp cửa sổ (Window Class) có chính xác là `NoTeThing` không:

- **X11 (Picom/KDE/Xfce)**: Chạy lệnh `xprop WM_CLASS` sau đó click vào cửa sổ NoTeThing.
- **Wayland (Hyprland)**: Chạy lệnh `hyprctl clients` và tìm mục `class` của NoTeThing.

Nếu tên lớp khác (ví dụ: `java-lang-Thread`), hãy thay thế tên đó vào các file cấu hình trên.
