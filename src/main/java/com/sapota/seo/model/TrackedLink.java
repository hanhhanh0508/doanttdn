package com.sapota.seo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 1 backlink đã đặt được và đang được theo dõi định kỳ (bảng tracked_links). */
public class TrackedLink {

    private int id;
    private String url;
    private String platform;
    private LocalDate placedDate;
    private String lastStatus;
    private String prevStatus;
    private LocalDateTime lastChecked;
    private String note;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public LocalDate getPlacedDate() { return placedDate; }
    public void setPlacedDate(LocalDate placedDate) { this.placedDate = placedDate; }
    public String getLastStatus() { return lastStatus; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
    public String getPrevStatus() { return prevStatus; }
    public void setPrevStatus(String prevStatus) { this.prevStatus = prevStatus; }
    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    /** Link từng là Dofollow ở lần kiểm tra trước nhưng lần này không còn → cảnh báo mất link. */
    public boolean isLost() {
        return CheckResult.STATUS_DOFOLLOW.equals(prevStatus)
                && lastStatus != null && !CheckResult.STATUS_DOFOLLOW.equals(lastStatus);
    }
}
