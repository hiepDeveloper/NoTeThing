# Hướng Dẫn Hiệu Ứng Kính Mờ (Blur) Cho Linux

Dự án NoTeThing hiện đã tích hợp **Thư viện Native (Rust)** để tự động yêu cầu hệ thống kích hoạt hiệu ứng kính mờ. Bạn không còn cần phải cấu hình thủ công các quy tắc cửa sổ phức tạp như trước.

## 🚀 Cách Cài Đặt Nhanh Nhất (Khuyên dùng)

Chúng tôi đã cung cấp một script tự động để kiểm tra và cài đặt các thành phần thiếu hụt:

1. Mở Terminal tại thư mục của ứng dụng.
2. Chạy lệnh:
   ```bash
   chmod +x setup_linux_blur.sh
   ./setup_linux_blur.sh
   ```

Script này sẽ:
- Nhận diện môi trường Desktop (KDE, GNOME, Hyprland, v.v.).
- Tự động cài đặt **Picom** (nếu bạn dùng X11 như Xfce/i3).
- Cấu hình các rule cần thiết cho **Hyprland**.
- Hướng dẫn các bước bật hiệu ứng trong cài đặt hệ thống (KDE/GNOME).

## 🛠️ Yêu cầu Hệ thống

Để hiệu ứng Blur hoạt động, hệ thống của bạn cần một **Compositor**:
- **KDE Plasma**: KWin (Mặc định đã có).
- **Hyprland / Sway**: (Mặc định đã có).
- **XFCE / i3 / Openbox / LXQt**: Cần cài đặt **Picom**.
- **GNOME**: Cần extension **Blur my Shell**.

## 📝 Lưu ý quan trọng

- **Môi trường X11**: Ứng dụng sẽ tự động gửi tín hiệu đến Compositor thông qua thư viện Native.
- **Môi trường Wayland**: 
  - Nếu chạy qua XWayland (mặc định của JavaFX), KDE vẫn sẽ tự động nhận diện.
  - Với Hyprland, script sẽ thêm 1 dòng cấu hình vào `hyprland.conf` để kích hoạt.

## ❓ Giải quyết sự cố thường gặp

### Lỗi mất Blur khi nhấn vào ghi chú (GNOME)
Nếu bạn đang sử dụng GNOME với extension **Blur my Shell**, bạn có thể thấy ghi chú bị mất hiệu ứng mờ ngay khi bạn nhấn vào (Focus). Để sửa lỗi này:
1. Mở cài đặt của extension **Blur my Shell**.
2. Tìm tùy chọn **"Opaque focused window"**.
3. Chuyển nó sang trạng thái **TẮT (OFF)**.
4. Ghi chú sẽ luôn giữ được hiệu ứng mờ bất kể bạn có đang thao tác hay không.

### ⚠️ Hạn chế đã biết trên GNOME
Do GNOME (Mutter) không hỗ trợ Blur native mà phải thông qua Extension bên thứ ba, nên sẽ có một số hạn chế:
- **Hiệu ứng "Bóng ma" (Ghosting):** Bạn có thể thấy các vệt mờ hoặc bóng đen khi di chuyển các cửa sổ đè lên nhau.
- **Hiệu năng:** Việc thay đổi kích thước cửa sổ có thể cảm giác không mượt bằng trên KDE hoặc X11 dùng Picom.
- **Khuyên dùng:** Để có trải nghiệm Blur "Premium" nhất, chúng tôi khuyến khích sử dụng **KDE Plasma** hoặc các môi trường nhẹ dùng **Picom**.

---
*Nếu gặp lỗi không hiển thị blur, vui lòng chạy script setup để được hỗ trợ kiểm tra lỗi.*
