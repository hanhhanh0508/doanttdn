package com.sapota.seo.model;

/**
 * Đại diện cho 1 dòng dữ liệu trong bảng backlinks
 * (domain hiện đang trỏ backlink về sapotacorp.vn).
 */
public class BacklinkDomain {

    private int id;
    private String domain;
    private int ascore;
    private int backlinks;
    private String country;

    public BacklinkDomain() {
    }

    public BacklinkDomain(String domain, int ascore, int backlinks, String country) {
        this.domain = domain;
        this.ascore = ascore;
        this.backlinks = backlinks;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getAscore() {
        return ascore;
    }

    public void setAscore(int ascore) {
        this.ascore = ascore;
    }

    public int getBacklinks() {
        return backlinks;
    }

    public void setBacklinks(int backlinks) {
        this.backlinks = backlinks;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
