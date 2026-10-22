package com.sapota.seo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Đại diện cho 1 dòng kết quả kiểm tra dofollow/nofollow của module
 * Dofollow Checker (tương ứng bảng check_results).
 */
public class CheckResult {

    public static final String STATUS_DOFOLLOW = "Dofollow";
    public static final String STATUS_NOFOLLOW = "Nofollow";
    public static final String STATUS_NOT_FOUND = "Không tìm thấy";
    public static final String STATUS_ERROR = "Lỗi truy cập";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private int id;
    private String url;
    private String status;
    private LocalDateTime checkedAt;
    private String note;

    public CheckResult() {
    }

    public CheckResult(String url, String status, String note) {
        this.url = url;
        this.status = status;
        this.note = note;
        this.checkedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCheckedAtFormatted() {
        return checkedAt == null ? "" : checkedAt.format(FORMATTER);
    }
}
