# NoTeThing 📝

[![Tiếng Việt](https://img.shields.io/badge/Ngôn_Ngữ-Tiếng_Việt-green?style=logo&logo=google-translate&logoColor=white)](#)
[![English](https://img.shields.io/badge/Language-English-blue?style=logo&logo=google-translate&logoColor=white)](./README.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=logo&logo=opensourceinitiative&logoColor=white)](./LICENSE)
[![Phiên bản](https://img.shields.io/badge/Phiên_bản-0.3.1-orange?style=logo&logo=github&logoColor=white)](./CHANGELOG_VN.md)
[![Build Status](https://github.com/hiepDeveloper/NoTeThing/actions/workflows/build.yml/badge.svg)](https://github.com/hiepDeveloper/NoTeThing/actions)
[![Java](https://img.shields.io/badge/Java-21+-red?style=logo&logo=openjdk&logoColor=white)](https://openjdk.org/)

Ứng dụng ghi chú nhẹ, hiện đại và đa nền tảng được xây dựng bằng JavaFX và AtlantaFX.

## Tính năng
- 🌙 **Chế độ tối (Dark Mode)**: Chuyển đổi giữa giao diện Sáng và Tối với cài đặt được lưu trữ bền vững.
- ✨ **Giao diện hiện đại**: Sử dụng AtlantaFX để mang lại vẻ ngoài bóng bẩy và chuyên nghiệp.
- 📌 **Luôn hiển thị trên cùng**: Giữ cho các ghi chú của bạn luôn hiển thị trên các cửa sổ khác.
- 🍷 **Độ trong suốt & Kính mờ**: Tùy chỉnh độ mờ của từng ghi chú với hiệu ứng "kính mờ" (Acrylic) nguyên bản (Hỗ trợ tốt Windows & Linux).
- ✍️ **Font chữ viết tay**: Hỗ trợ các font chữ viết tay (như Fuzzy Bubbles, Patrick Hand) hỗ trợ tiếng Việt hoàn hảo.
- 🎨 **Chủ đề thích ứng**: 7 bộ màu sắc được tuyển chọn với thanh tiêu đề và nền cửa sổ thích ứng linh hoạt.
- 💾 **Tự động lưu**: Không bao giờ mất ý tưởng của bạn; nội dung được lưu tự động.
- 📦 **Quản lý ghi chú**: Dễ dàng quản lý tất cả ghi chú của bạn trong một danh sách tập trung.
- 🖼️ **Thiết kế không khung**: Giao diện sạch sẽ, có thể kéo thả và thay đổi kích thước dễ dàng.

## 🚀 Chạy nhanh (Bản Portable)
Để trải nghiệm nhanh mà không cần cài đặt Java, bạn có thể sử dụng bản Portable:
1. Truy cập trang [Releases](https://github.com/hiepDeveloper/NoTeThing/releases).
2. Tải về bản Portable (file .zip) tương ứng với hệ điều hành của bạn (**Windows** hoặc **Linux**).
3. Giải nén và chạy trình khởi chạy:
   - **Windows**: Chạy file **`NoTeThing.exe`**.
   - **Linux**: Chạy file thực thi **`NoTeThing`** trong thư mục.
   > **Lưu ý cho Linux**: Để bật hiệu ứng Kính mờ (Blur) tự động, hãy chạy script `setup_linux_blur.sh` hoặc xem hướng dẫn tại [README_LINUX_VN.md](./doc/BlurEffectForLinux/README_LINUX_VN.md).


## Yêu cầu hệ thống
- **Java**: JDK 21 trở lên.
- **Maven**: 3.8 trở lên.

## Bắt đầu
### Tải mã nguồn
```bash
git clone https://github.com/hiepDeveloper/NoTeThing.git
cd NoTeThing/notething
```

### Chạy ứng dụng
```bash
mvn clean javafx:run
```

## Công nghệ sử dụng
- [JavaFX](https://openjfx.io/) - Framework giao diện đồ họa.
- [AtlantaFX](https://github.com/mkpaz/atlantafx) - Thư viện giao diện hiện đại cho JavaFX.
- [Ikonli](https://github.com/kordamp/ikonli) - Bộ icon Bootstrap cho giao diện.
- [JNA](https://github.com/java-native-access/jna) - Truy cập native API cho các tính năng cửa sổ nâng cao.

## Giấy phép
Dự án được phát hành theo giấy phép MIT. Xem file LICENSE để biết thêm chi tiết.
