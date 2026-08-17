# SapotaCorp SEO Dashboard

Ứng dụng desktop **Java (Swing) + MySQL** hiện thực hoá thiết kế ở Chương 4
báo cáo thực tập: **Dashboard Backlink Gap** kết hợp **Dofollow Checker**.

- Dashboard: đọc dữ liệu backlink/backlink-gap (từ Excel hoặc đã có sẵn
  trong MySQL), tính điểm ưu tiên, lọc theo AS/số đối thủ, xuất báo cáo.
- Dofollow Checker: nhập danh sách URL, tự động kiểm tra thuộc tính
  dofollow/nofollow bằng cách tải và phân tích HTML (dùng thư viện Jsoup).

## 1. Yêu cầu môi trường

- JDK 17 trở lên
- Maven 3.8+ (NetBeans/VS Code đều có thể tự tải Maven nếu chưa có)
- MySQL Server hoặc XAMPP (MySQL) đang chạy ở `localhost:3306`

## 2. Cài đặt Database

### Cách 1 — Dùng XAMPP
1. Mở **XAMPP Control Panel**, bấm **Start** ở dòng MySQL.
2. Vào `http://localhost/phpmyadmin`.
3. Chọn tab **Import** → chọn file `sql/schema.sql` trong project → bấm **Go**.
   (Script sẽ tự tạo database `sapota_seo`, 3 bảng, và nạp sẵn vài dòng dữ liệu mẫu.)

### Cách 2 — Dùng MySQL Server / MySQL Workbench
```bash
mysql -u root -p < sql/schema.sql
```

Nếu dùng MySQL Server có mật khẩu, hoặc XAMPP đổi port khác 3306
(ví dụ project này đang cấu hình sẵn cho port **3307**), mở file
`src/main/java/com/sapota/seo/db/DatabaseConnection.java` và sửa lại:
```java
private static final String PORT = "3307";       // đổi port MySQL nếu khác
private static final String USER = "root";
private static final String PASSWORD = "mat_khau_cua_ban";
```

## 3. Chạy bằng NetBeans

1. Mở NetBeans → **File → Open Project** → chọn thư mục `sapota-seo-dashboard`
   (NetBeans tự nhận diện đây là project Maven nhờ file `pom.xml`).
2. Chuột phải vào project → **Clean and Build** (NetBeans sẽ tự tải Jsoup,
   Apache POI, MySQL Connector về thông qua Maven).
3. Chuột phải vào `Main.java` (trong package `com.sapota.seo`) → **Run File**.

## 4. Chạy bằng VS Code

1. Cài extension **Extension Pack for Java** và **Maven for Java**.
2. Mở thư mục `sapota-seo-dashboard` trong VS Code.
3. Đợi VS Code tự tải dependency (thanh trạng thái góc dưới).
4. Mở `src/main/java/com/sapota/seo/Main.java` → bấm **Run** phía trên hàm `main`.

## 5. Chạy bằng dòng lệnh (không cần IDE)

```bash
mvn clean package
java -jar target/sapota-seo-dashboard.jar
```

## 6. Cấu trúc project

```
sapota-seo-dashboard/
├── pom.xml                     # Cấu hình Maven (Jsoup, POI, MySQL Connector)
├── sql/schema.sql              # Script tạo database + bảng + dữ liệu mẫu
├── sample-data/                # Chỗ để copy 2 file Excel backlink thật vào
└── src/main/java/com/sapota/seo/
    ├── Main.java                       # Điểm khởi chạy ứng dụng
    ├── db/DatabaseConnection.java      # Kết nối MySQL
    ├── model/                          # BacklinkDomain, GapDomain, CheckResult
    ├── dao/                            # Thao tác dữ liệu (SELECT/INSERT/DELETE)
    ├── service/
    │   ├── ExcelImportService.java     # Đọc 2 file Excel -> nạp vào MySQL
    │   └── DofollowCheckerService.java # Crawl HTML, xác định dofollow/nofollow
    ├── ui/
    │   ├── MainFrame.java              # Cửa sổ chính (2 tab)
    │   ├── DashboardPanel.java         # Màn hình Dashboard Backlink Gap
    │   └── CheckerPanel.java           # Màn hình Dofollow Checker
    └── util/CsvExporter.java           # Xuất bảng kết quả ra CSV
```

## 7. Cách dùng nhanh

1. Chạy ứng dụng — sẽ tự kiểm tra kết nối MySQL, báo lỗi rõ ràng nếu chưa bật XAMPP.
2. Tab **Dashboard**:
   - Bấm **"Nhập dữ liệu từ Excel..."** để nạp file
     `sapota-gap-refdomains-as30-dofollow.xlsx` (điểm ưu tiên tự động tính lại).
   - Dùng ô lọc AS / số đối thủ rồi bấm **Áp dụng lọc**.
   - Bấm **Xuất báo cáo (CSV)** để lưu bảng đang hiển thị.
3. Tab **Dofollow Checker**:
   - Dán danh sách URL (mỗi dòng 1 URL) vào ô nhập.
   - Bấm **Kiểm tra** — ứng dụng chạy nền, cập nhật bảng kết quả theo thời gian thực.
   - Mỗi kết quả tự động được lưu vào bảng `check_results` trong MySQL.
   - Bấm **Xuất kết quả (CSV)** để tải báo cáo.
4. Tab **Tạo bài Post (AI)**:
   - Lấy Gemini API Key miễn phí tại https://aistudio.google.com/apikey
   - Nhập link bài viết nguồn + dán API Key (tick "Lưu API key" để không phải nhập lại lần sau).
   - Bấm **Tạo nội dung bài Post** — ứng dụng tự đọc nội dung trang, gửi cho Gemini,
     trả về 1 bài đăng LinkedIn tiếng Việt hoàn chỉnh, có thể chỉnh sửa lại trước khi đăng.
   - Bấm **Copy vào Clipboard** để dán thẳng lên LinkedIn.

## 8. Ghi chú

- Mặc định module Dofollow Checker tìm liên kết trỏ về `sapotacorp.vn`. Muốn đổi
  domain đích khác, chỉnh tham số trong `DofollowCheckerService` (constructor
  có overload nhận `targetDomain`).
- Một số trang có thể chặn bot (403/timeout) — hệ thống sẽ ghi trạng thái
  **"Lỗi truy cập"** kèm ghi chú, không làm dừng toàn bộ quá trình kiểm tra.
- Công thức tính điểm ưu tiên hiện tại: `priority_score = maxAS × 1.0 + số đối thủ × 5.0`
  — có thể điều chỉnh trực tiếp trong `GapDomain.calculatePriorityScore()`.
