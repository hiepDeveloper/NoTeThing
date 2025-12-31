# Nhật ký thay đổi (Changelog)

[![Tiếng Việt](https://img.shields.io/badge/Ngôn_Ngữ-Tiếng_Việt-green?style=logo&logo=google-translate&logoColor=white)](#)
[![English](https://img.shields.io/badge/Language-English-blue?style=logo&logo=google-translate&logoColor=white)](./CHANGELOG.md)

Tất cả các thay đổi đáng chú ý của dự án này sẽ được ghi lại trong file này.

Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
và dự án này tuân thủ [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2025-12-31

### Đã thêm
- **Chế độ Tối (Dark Mode)**: Hỗ trợ chuyển đổi theme toàn diện, tự động ghi nhớ cài đặt và cập nhật thời gian thực cho tất cả cửa sổ.
- **Độ trong suốt & Kính mờ**: Điều chỉnh độ mờ linh hoạt (30% - 100%) kết hợp hiệu ứng Acrylic "kính mờ" sang trọng từ Windows.
- **Font chữ viết tay**: Mang lại vẻ ngoài như giấy dán thật với font "Fuzzy Bubbles" và hỗ trợ Unicode tiếng Việt hoàn hảo (Patrick Hand, Itim).
- **Bộ sưu tập màu sắc thích ứng**: 7 tông màu chuyên sâu (Vàng, Xanh lá, Xanh dương, Cam, Đỏ, Tím, Teal) tự động tinh chỉnh theo chế độ sáng/tối.

### Đã thay đổi
- **Thiết kế lại thành phần UI**: Làm mới nút di chuyển, bo góc cửa sổ 12px và tối ưu hóa màu sắc các cửa sổ phụ theo theme.
- **Nâng cấp Danh sách ghi chú**: Mở rộng không gian hiển thị (400px) và tinh chỉnh font chữ giúp đọc nội dung dễ dàng hơn.
- **Cải thiện tương tác**: Thanh trượt độ nhạy cao, mở rộng vùng bắt sự kiện và các nút bấm phản hồi thông minh theo màu nền.

## [0.1.0] - 2025-12-30

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
