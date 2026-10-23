# Web demo để thử Dofollow Checker / Backlink Monitor (không cần file Excel)

## Chạy web demo
Mở PowerShell tại thư mục gốc project (nơi có `pom.xml`):

```powershell
java demo\DemoServer.java
```

Nếu báo `java is not recognized`, dùng đường dẫn đầy đủ của JDK trong NetBeans:

```powershell
& "D:\Taixuong\Apache NetBeans\jdk\bin\java.exe" demo\DemoServer.java
```

Thấy dòng `Web demo dang chay...` là được. **Để cửa sổ này mở** trong lúc demo (Ctrl+C để tắt).
Mở trình duyệt `http://localhost:8000/` để xem bảng điều khiển.

## 3 URL dùng để thử
```
http://localhost:8000/bai-viet-1
http://localhost:8000/bai-viet-2
http://localhost:8000/bai-viet-3
```
Ban đầu: bài 1 = Dofollow, bài 2 = Nofollow, bài 3 = đã gỡ link ("Không tìm thấy").

## Kịch bản demo
1. **Dofollow Checker**: dán 3 URL trên (mỗi dòng 1 URL) → Kiểm tra → ra Dofollow / Nofollow / Không tìm thấy.
2. **Backlink Monitor**: Thêm nhiều URL → dán 3 URL → Kiểm tra tất cả.
3. Trình duyệt `http://localhost:8000/` → bấm **Nofollow** ở bài 1 (làm giống chủ website đổi link).
4. App → Cài đặt → **Chạy kiểm tra ngay** → tab Backlink Monitor: bài 1 hiện **MẤT/ĐỔI LINK** (và gửi email nếu đã cấu hình).
5. Bấm **Dofollow** lại ở bài 1 rồi kiểm tra tiếp → link hết cảnh báo.

Lưu ý: Domain đích trong Cài đặt phải là `sapotacorp.vn` (mặc định).
