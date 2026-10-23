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
├── src/test/java/              # Unit test JUnit 5 (mvn test)
├── sample-data/                # Chỗ để copy 2 file Excel backlink thật vào
└── src/main/java/com/sapota/seo/   (các file mới: ui/{Stats,Backlink,Outreach,Monitor,History,Settings}Panel,
    #   ui/SimpleChartPanel, service/{Stats,Report,BatchCheck}Service, dao/{Outreach,TrackedLink}DAO,
    #   util/{ExcelExporter,AppSettings}, db/SchemaInitializer)
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

5. Các tab bổ sung (phiên bản 2.0):
   - **Thống kê**: 10 thẻ chỉ số tổng quan + 4 biểu đồ (phân bố AS, đối thủ xuất hiện nhiều nhất,
     tỷ lệ Dofollow, tiến độ Outreach) và nút **Xuất báo cáo tổng hợp (Excel)** gồm 6 sheet.
   - **Backlink hiện có**: xem/tìm kiếm/lọc AS các domain đang trỏ về sapotacorp.vn, nhập từ Excel, xuất CSV/Excel.
   - **Outreach Tracker**: theo dõi tiến độ xin backlink từng domain (Chưa liên hệ → Đã liên hệ → Đang trao đổi
     → Đã đặt link / Từ chối), ngày hẹn follow-up (tô đỏ khi quá hạn), ghi chú, chuột phải để đổi trạng thái nhanh,
     nút **Soạn email mẫu** (tiếng Anh) điền sẵn domain để copy gửi đi.
   - **Backlink Monitor**: danh sách backlink đã đặt, kiểm tra lại song song, cảnh báo **MẤT/ĐỔI LINK**
     khi link từng Dofollow nhưng nay không còn.
   - **Lịch sử kiểm tra**: xem lại mọi kết quả Dofollow đã lưu, lọc theo trạng thái/URL, kiểm tra lại, xoá, xuất.
   - **Cài đặt**: chỉnh trọng số công thức điểm ưu tiên, domain đích mặc định; lưu xong tự tính lại điểm.
6. Nâng cấp các tab cũ:
   - Dashboard: tìm kiếm domain/đối thủ, sắp xếp mọi cột (đúng theo số), thêm/sửa/xoá domain thủ công,
     cột Mức ưu tiên + tô màu, cột trạng thái Outreach, nút **Thêm vào Outreach**, mở website (nhấp đúp), xuất Excel (.xlsx).
   - Dofollow Checker: kiểm tra **song song 5 luồng**, nút đưa các link Dofollow vào Backlink Monitor.

> Hai bảng mới (`outreach`, `tracked_links`) được app **tự tạo** khi khởi động nếu chưa có,
> nên không cần import lại `schema.sql` nếu đã có database cũ.

## 8. Đăng nhập, phân quyền, kiểm tra tự động, unit test (phiên bản 3.0)

### Đăng nhập & phân quyền
- Lần chạy đầu app tự tạo tài khoản quản trị **`admin` / `admin123`** → đăng nhập xong hãy vào tab **Người dùng**
  → **Đổi mật khẩu của tôi**.
- Mật khẩu được băm bằng **PBKDF2-HMAC-SHA256** (120.000 vòng, salt ngẫu nhiên mỗi tài khoản), không lưu bản rõ.
- Hai vai trò: **Quản trị viên (ADMIN)** thấy toàn bộ 10 tab; **Nhân viên (STAFF)** chỉ thấy 8 tab nghiệp vụ SEO,
  không thấy **Cài đặt** và **Người dùng**.
- Tab **Người dùng** (chỉ admin): thêm tài khoản, đặt lại mật khẩu, đổi vai trò, khoá/mở khoá, xoá.
  App không cho tự khoá/xoá chính mình hoặc làm mất quản trị viên cuối cùng. Đăng nhập sai 5 lần thì đóng app.

### Kiểm tra backlink tự động + email cảnh báo
Tab **Cài đặt → Kiểm tra backlink tự động & email cảnh báo**:
1. Tick *Tự động kiểm tra backlink mỗi ngày*, nhập giờ chạy (HH:mm, mặc định 08:00).
2. Tick *Gửi email...* rồi điền SMTP (Gmail: `smtp.gmail.com`, cổng `587`; cần bật xác minh 2 bước và tạo
   **Mật khẩu ứng dụng** tại https://myaccount.google.com/apppasswords).
3. Bấm **Gửi email thử** để kiểm tra cấu hình, **Chạy kiểm tra ngay** để chạy 1 lượt thủ công.
- Mỗi lượt: kiểm tra song song 5 luồng tất cả link trong Backlink Monitor → cập nhật trạng thái + lưu lịch sử →
  nếu có link **từng Dofollow nhưng nay mất/đổi** thì gửi 1 email liệt kê các link đó (không báo lặp ở lượt sau).
- Lỗi truy cập (timeout/chặn bot) **không** bị coi là mất link, để tránh báo động giả.
- Lưu ý: đây là app desktop nên lịch chỉ chạy **khi ứng dụng đang mở**.

### Giao diện FlatLaf
Giao diện Swing dùng thư viện **FlatLaf** (khai báo trong `pom.xml`, bật ở `Main.java`).

### Unit test (JUnit 5)
```bash
mvn test
```
63 test trong `src/test/java/...`, **không cần MySQL hay Internet**: công thức điểm ưu tiên và các mức,
phân loại Dofollow/Nofollow bằng HTML mẫu, kiểm tra song song (đủ URL, nhiều luồng, 1 URL lỗi không dừng cả batch),
quy tắc mất link, soạn email cảnh báo, tính giờ chạy kế tiếp của lịch, băm mật khẩu, phân tích ngày/domain.

## 9. Ghi chú

- Mặc định module Dofollow Checker tìm liên kết trỏ về `sapotacorp.vn`. Muốn đổi
  domain đích khác, chỉnh tham số trong `DofollowCheckerService` (constructor
  có overload nhận `targetDomain`).
- Một số trang có thể chặn bot (403/timeout) — hệ thống sẽ ghi trạng thái
  **"Lỗi truy cập"** kèm ghi chú, không làm dừng toàn bộ quá trình kiểm tra.
- Công thức tính điểm ưu tiên mặc định: `priority_score = maxAS × 1.0 + số đối thủ × 5.0`
  — chỉnh trọng số ngay trong tab **Cài đặt** (không cần sửa code).
- Mức ưu tiên: Rất cao (≥100), Cao (≥80), Trung bình (≥60), Thấp (<60).
