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
     * Trọng số lấy từ tab Cài đặt (mặc định AS x 1.0 + số đối thủ x 5.0).
     */
    public static double calculatePriorityScore(int maxAs, int competitorCount) {
        return calculatePriorityScore(maxAs, competitorCount,
                com.sapota.seo.util.AppSettings.getWeightAs(),
                com.sapota.seo.util.AppSettings.getWeightCompetitor());
    }

    /** Phiên bản thuần (không đọc file cấu hình) để dễ kiểm thử bằng unit test. */
    public static double calculatePriorityScore(int maxAs, int competitorCount,
                                                double weightAs, double weightCompetitor) {
        return (maxAs * weightAs) + (competitorCount * weightCompetitor);
    }

    /** Phân mức ưu tiên theo điểm: Rất cao (>=100), Cao (>=80), Trung bình (>=60), còn lại Thấp. */
    public static String levelFor(double score) {
        if (score >= 100) return "Rất cao";
        if (score >= 80) return "Cao";
        if (score >= 60) return "Trung bình";
        return "Thấp";
    }

    public String getPriorityLevel() {
        return levelFor(priorityScore);
    }
}
