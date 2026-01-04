# Nhật ký thay đổi (Changelog)

[![Tiếng Việt](https://img.shields.io/badge/Ngôn_Ngữ-Tiếng_Việt-green?style=logo&logo=google-translate&logoColor=white)](#)
[![English](https://img.shields.io/badge/Language-English-blue?style=logo&logo=google-translate&logoColor=white)](./CHANGELOG.md)

Tất cả các thay đổi đáng chú ý của dự án này sẽ được ghi lại trong file này.

Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
và dự án này tuân thủ [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.1] - 2026-01-04 - Vá lỗi & Tối ưu hóa Linux

### Đã sửa lỗi
- **Bản dựng build (Artifact)**: Khắc phục lỗi bản build đóng gói không sử dụng được tính năng đồng bộ đám mây do thiếu cấu hình tài nguyên.
- **Tương thích Linux**:
    - **Tự động ẩn cài đặt**: Ẩn nút gạt hiệu ứng kính mờ trong phần cài đặt khi chạy trên Linux (vì hiệu ứng này do hệ điều hành/compositor quản lý).
    - **Hướng dẫn chi tiết**: Bổ sung bộ tài liệu hướng dẫn và script tự động cấu hình Blur cho Hyprland, KDE, GNOME và Picom (X11).

## [0.3.0] - 2026-01-03 - Đồng bộ Đám mây, Thùng rác & Cài đặt

### Đã thêm
- **Đồng bộ Đám mây**: Đồng bộ ghi chú mượt mà qua các thiết bị sử dụng Google Firebase (Realtime Database).
    - Hỗ trợ cấu hình `serviceAccountKey.json`.
    - Quy trình đăng nhập/đăng xuất bảo mật.
- **Thùng rác & Khôi phục**:
    - **Xóa mềm (Soft Delete)**: Ghi chú chuyển vào thùng rác có thể khôi phục lại bất cứ lúc nào.
    - **An toàn là trên hết**: Hộp thoại xác nhận trước khi xóa vĩnh viễn.
    - **Giao diện Thùng rác**: Khu vực riêng biệt để quản lý các ghi chú đã xóa.
- **Cài đặt Tập trung**: Giao diện cấu hình mới trong Danh sách ghi chú.
    - **Hệ thống Chủ đề**: Tùy chọn đồng bộ theo **Chủ đề Máy tính** (mặc định), hoặc ép sáng/tối.
    - **Kiểm soát Hiệu ứng**: Bật/Tắt hiệu ứng **Kính mờ (Acrylic)** và **Tự động ẩn tiêu đề** (mặc định Bật).
- **Nâng cấp Typography**: Tích hợp bộ font **Mali** hỗ trợ đầy đủ các kiểu **Đậm**, *Nghiêng*, và ***Đậm Nghiêng***.

## [0.2.1] - 2026-01-01 - Sửa UI & Hỗ trợ Linux

### Đã sửa lỗi
- **Đồng bộ hóa Chủ đề**: Khắc phục lỗi thay đổi theme từ Danh sách ghi chú không áp dụng ngay cho các ghi chú hiện có (trước đây yêu cầu phải chỉnh độ trong suốt mới cập nhật).
- **Tinh chỉnh Hiệu ứng Kính mờ**: Cải thiện độ ổn định của Blur và sửa logic bo tròn vùng cắt để đồng bộ hoàn hảo với giao diện.
- **Tương thích Linux**: Tối ưu hóa hiệu năng Blur và khả năng tương tác cửa sổ trên các hệ điều hành Linux.
- **UI & Tương tác**:
    - Tinh chỉnh bán kính bo góc ghi chú về 7px giúp giao diện thanh thoát hơn.
    - Điều chỉnh chiều ngang tối thiểu về 220px để linh hoạt hơn trong việc sắp xếp không gian màn hình.
    - Cải thiện độ nhạy của vùng bắt sự kiện resize và tính ổn định khi xử lý sự kiện native.

## [0.2.0] - 2025-12-31 - Nâng cấp giao diện tùy chỉnh

### Đã thêm
- **Chế độ Tối (Dark Mode)**: Hỗ trợ chuyển đổi theme toàn diện, tự động ghi nhớ cài đặt và cập nhật thời gian thực cho tất cả cửa sổ.
- **Độ trong suốt & Kính mờ**: Điều chỉnh độ mờ linh hoạt (30% - 100%) kết hợp hiệu ứng Acrylic "kính mờ" sang trọng từ Windows.
- **Font chữ viết tay**: Mang lại vẻ ngoài như giấy dán thật với font "Fuzzy Bubbles" và hỗ trợ Unicode tiếng Việt hoàn hảo (Patrick Hand, Itim).
- **Bộ sưu tập màu sắc thích ứng**: 7 tông màu chuyên sâu (Vàng, Xanh lá, Xanh dương, Cam, Đỏ, Tím, Teal) tự động tinh chỉnh theo chế độ sáng/tối.

### Đã thay đổi
- **Thiết kế lại thành phần UI**: Làm mới nút di chuyển, bo góc cửa sổ 12px và tối ưu hóa màu sắc các cửa sổ phụ theo theme.
- **Nâng cấp Danh sách ghi chú**: Mở rộng không gian hiển thị (400px) và tinh chỉnh font chữ giúp đọc nội dung dễ dàng hơn.
- **Cải thiện tương tác**: Thanh trượt độ nhạy cao, mở rộng vùng bắt sự kiện và các nút bấm phản hồi thông minh theo màu nền.

## [0.1.0] - 2025-12-30 - Bản dựng cơ bản

### Đã thêm
- **Cốt lõi**: Phiên bản đầu tiên của ứng dụng ghi chú NoTeThing.
- **Giao diện hiện đại**: Tích hợp AtlantaFX (chủ đề Primer Light) cho giao diện chuyên nghiệp và bóng bẩy.
- **Quản lý ghi chú**: 
    - Hỗ trợ đầy đủ các thao tác cơ bản (Tạo, Đọc, Cập nhật, Xóa).
    - Cửa sổ "Danh sách ghi chú" tập trung để quản lý tất cả các ghi chú hiện có.
- **Tính năng cửa sổ**:
    - **Ghim trên cùng (Always on Top)**: Khả năng ghim ghi chú luôn hiển thị trên các cửa sổ khác bằng JNA.
    - **Không khung (Frameless)**: Thiết kế cửa sổ hiện đại, không viền, có thể kéo thả.
    - **Thay đổi kích thước**: Hỗ trợ thay đổi kích thước đa nền tảng (Native Windows qua JNA và JavaFX dự phòng).
    - **Thanh công cụ thông minh**: Tự động ẩn/hiện tiêu đề và thanh công cụ khi cửa sổ được tập trung (focus).
- **Lưu trữ dữ liệu**: 
    - Tự động lưu và tải nội dung ghi chú, vị trí và kích thước cửa sổ.
    - Dữ liệu được lưu cục bộ trong thư mục người dùng `.notething`.
- **Hình ảnh**: Tích hợp bộ biểu tượng Bootstrap Icons thông qua Ikonli.
- **Tài liệu**: 
    - Hướng dẫn (README) song ngữ Anh - Việt.
    - Giấy phép MIT.
    - Chú thích bản quyền trong mã nguồn.
