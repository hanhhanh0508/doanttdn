package com.sapota.seo.model;

import java.time.LocalDate;

/** 1 domain đang được theo dõi trong quy trình outreach xin backlink (bảng outreach). */
public class OutreachRecord {

    public static final String NOT_CONTACTED = "Chưa liên hệ";
    public static final String CONTACTED = "Đã liên hệ";
    public static final String NEGOTIATING = "Đang trao đổi";
    public static final String WON = "Đã đặt link";
    public static final String REJECTED = "Từ chối";

    public static final String[] STATUSES = {NOT_CONTACTED, CONTACTED, NEGOTIATING, WON, REJECTED};

    private int id;
    private String domain;
    private String status = NOT_CONTACTED;
    private String contact;
    private String note;
    private LocalDate lastContact;
    private LocalDate followUp;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDate getLastContact() { return lastContact; }
    public void setLastContact(LocalDate lastContact) { this.lastContact = lastContact; }
    public LocalDate getFollowUp() { return followUp; }
    public void setFollowUp(LocalDate followUp) { this.followUp = followUp; }

    /** Trạng thái đã kết thúc (thành công hoặc bị từ chối) thì không cần nhắc follow-up nữa. */
    public boolean isClosed() {
        return WON.equals(status) || REJECTED.equals(status);
    }

    /** "Quá hạn", "Hôm nay" hoặc rỗng — dùng để nhắc việc. */
    public String getReminder() {
        if (followUp == null || isClosed()) return "";
        LocalDate today = LocalDate.now();
        if (followUp.isBefore(today)) return "Quá hạn";
        if (followUp.isEqual(today)) return "Hôm nay";
        return "";
    }
}
