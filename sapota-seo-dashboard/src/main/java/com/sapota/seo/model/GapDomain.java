package com.sapota.seo.model;

/**
 * Đại diện cho 1 dòng dữ liệu trong bảng gap_domains
 * (domain tiềm năng mà đối thủ đang có backlink nhưng sapotacorp.vn chưa có).
 */
public class GapDomain {

    private int id;
    private String domain;
    private int maxAs;
    private int competitorCount;
    private String competitors;
    private double priorityScore;

    public GapDomain() {
    }

    public GapDomain(String domain, int maxAs, int competitorCount, String competitors, double priorityScore) {
        this.domain = domain;
        this.maxAs = maxAs;
        this.competitorCount = competitorCount;
        this.competitors = competitors;
        this.priorityScore = priorityScore;
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

    public int getMaxAs() {
        return maxAs;
    }

    public void setMaxAs(int maxAs) {
        this.maxAs = maxAs;
    }

    public int getCompetitorCount() {
        return competitorCount;
    }

    public void setCompetitorCount(int competitorCount) {
        this.competitorCount = competitorCount;
    }

    public String getCompetitors() {
        return competitors;
    }

    public void setCompetitors(String competitors) {
        this.competitors = competitors;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    /**
     * Công thức tính điểm ưu tiên: kết hợp AS cao nhất và số lượng đối thủ
     * đang sở hữu backlink từ domain này (mô tả tại mục 4.3.4 báo cáo).
     * Có thể điều chỉnh trọng số (1.0 và 5.0) tuỳ nhu cầu thực tế.
     */
    public static double calculatePriorityScore(int maxAs, int competitorCount) {
        return (maxAs * 1.0) + (competitorCount * 5.0);
    }
}
