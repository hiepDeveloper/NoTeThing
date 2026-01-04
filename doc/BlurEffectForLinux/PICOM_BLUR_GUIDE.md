# Hướng dẫn cấu hình hiệu ứng Kính mờ (Blur) trên XFCE/Linux
Để ứng dụng NoTeThing hiển thị hiệu ứng kính mờ (Acrylic/Blur) chân thực trên các môi trường Linux nhẹ như XFCE, i3wm, hoặc BSPWM, bạn cần sử dụng một trình dồn ảnh (Compositor) có tên là Picom.

## 1. Cài đặt Picom
Mở Terminal và chạy lệnh cài đặt tương ứng với bản phân phối của bạn:

- Debian / Ubuntu / Kali Linux:
```bash
sudo apt update && sudo apt install picom
```

- Fedora:
```bash
sudo dnf install picom
```

- Arch Linux:
```bash
sudo pacman -S picom
```

## 2. Cấu hình file `picom.conf`
Picom cần được thiết lập để sử dụng đúng thuật toán làm mờ và xử lý các góc bo tròn của JavaFX.
1. Tạo thư mục cấu hình:
```bash
mkdir -p ~/.config/picom
```

2. Tạo file cấu hình:
```bash
cp /etc/xdg/picom.conf ~/.config/picom/picom.conf
```

3. Sửa file cấu hình:
```bash
mousepad ~/.config/picom/picom.conf
```

4. Dán đoạn mã sau vào file cấu hình:

```Đoạn mã
# --- Cấu hình Backend ---
# Chế độ 'glx' cho hiệu năng tốt nhất trên GPU
backend = "glx";
glx-no-stencil = true;
glx-copy-from-front = false;

# --- Hiệu ứng Blur (Kính mờ) ---
blur: {
  method = "dual_kawase";
  strength = 5;      # Độ nhòe (1-20)
  background = true;
  background-frame = false;
  background-fixed = false;
}

# Loại trừ các thành phần không cần blur
blur-background-exclude = [
  "window_type = 'dock'",
  "window_type = 'desktop'",
  "_GTK_FRAME_EXTENTS@:c"
];

# --- Bo góc (Rounded Corners) ---
corner-radius = 12;  # Khớp với -fx-background-radius trong JavaFX
rounded-corners-exclude = [
  "window_type = 'dock'",
  "window_type = 'desktop'"
];

# --- Bóng đổ (Shadow) ---
shadow = true;
shadow-radius = 12;
shadow-offset-x = -12;
shadow-offset-y = -12;
shadow-opacity = 0.4;
# Tránh đổ bóng chồng lên góc bo tròn của JavaFX
shadow-exclude = [
  "class_g = 'NoTeThing'",
  "_GTK_FRAME_EXTENTS@:c"
];
```

## 3. Các bước kích hoạt hiệu ứng
**Bước 1: Tắt Compositor mặc định của XFCE**

Để Picom hoạt động mà không bị xung đột (gây lỗi trắng cửa sổ):

Vào **Settings -> Window Manager Tweaks**.

Chọn tab **Compositor**.

Bỏ chọn mục **"Enable display compositing"**.

**Bước 2: Chạy Picom**

Chạy lệnh sau trong terminal (hoặc thêm vào *Startup Applications* của hệ thống):

```bash
picom --config ~/.config/picom/picom.conf --backend glx &
```
**Bước 3: Chạy ứng dụng NoTeThing**

```bash
./NoTeThing
```

## ⚠️ Lưu ý quan trọng cho Máy ảo (VMware/VirtualBox)
Nếu bạn sử dụng máy ảo và gặp hiện tượng cửa sổ bị trắng xóa:

Hãy đảm bảo đã bật **3D Acceleration** trong cài đặt máy ảo.

Nếu phần cứng không hỗ trợ GLX, hãy đổi `backend = "xrender"` trong file `picom.conf`. (Lưu ý: `xrender` không hỗ trợ thuật toán `dual_kawase`, hiệu ứng blur sẽ yếu hơn).

Mẹo: Bạn có thể kiểm tra xem Picom đã nhận diện đúng ứng dụng chưa bằng cách gõ `xprop WM_CLASS` và click vào cửa sổ NoTeThing. Tên class trả về phải trùng với `class_g` trong file cấu hình.