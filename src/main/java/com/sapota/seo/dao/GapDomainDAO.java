package com.sapota.seo.dao;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.model.GapDomain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) cho bảng gap_domains.
 * Cung cấp các thao tác: lấy danh sách (có lọc), thêm mới, xoá toàn bộ.
 */
public class GapDomainDAO {

    /**
     * Lấy danh sách domain gap, sắp xếp theo điểm ưu tiên giảm dần.
     *
     * @param minAs         lọc AS tối thiểu (0 nếu không lọc)
     * @param minCompetitor lọc số đối thủ tối thiểu (0 nếu không lọc)
     */
    public List<GapDomain> findAll(int minAs, int minCompetitor) {
        List<GapDomain> list = new ArrayList<>();
        String sql = "SELECT * FROM gap_domains WHERE max_as >= ? AND competitor_count >= ? "
                + "ORDER BY priority_score DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, minAs);
            stmt.setInt(2, minCompetitor);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách gap_domains: " + e.getMessage());
        }
        return list;
    }

    /** Như findAll nhưng có thêm tìm kiếm theo tên domain hoặc tên đối thủ. */
    public List<GapDomain> findAll(int minAs, int minCompetitor, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return findAll(minAs, minCompetitor);
        List<GapDomain> list = new ArrayList<>();
        String sql = "SELECT * FROM gap_domains WHERE max_as >= ? AND competitor_count >= ? "
                + "AND (domain LIKE ? OR competitors LIKE ?) ORDER BY priority_score DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String like = "%" + keyword.trim() + "%";
            stmt.setInt(1, minAs);
            stmt.setInt(2, minCompetitor);
            stmt.setString(3, like);
            stmt.setString(4, like);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm gap_domains: " + e.getMessage());
        }
        return list;
    }

    public boolean existsByDomain(String domain) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM gap_domains WHERE domain = ?")) {
            stmt.setString(1, domain);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean update(GapDomain g) {
        String sql = "UPDATE gap_domains SET domain=?, max_as=?, competitor_count=?, competitors=?, priority_score=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, g.getDomain());
            stmt.setInt(2, g.getMaxAs());
            stmt.setInt(3, g.getCompetitorCount());
            stmt.setString(4, g.getCompetitors());
            stmt.setDouble(5, g.getPriorityScore());
            stmt.setInt(6, g.getId());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật gap_domain: " + e.getMessage());
            return false;
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM gap_domains WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá gap_domain: " + e.getMessage());
        }
    }

    /** Tính lại điểm ưu tiên của toàn bộ domain theo trọng số hiện hành trong Cài đặt. */
    public int recalculateAll() {
        List<GapDomain> all = findAll(0, 0);
        for (GapDomain g : all) {
            g.setPriorityScore(GapDomain.calculatePriorityScore(g.getMaxAs(), g.getCompetitorCount()));
            update(g);
        }
        return all.size();
    }

    public void insert(GapDomain g) {
        String sql = "INSERT INTO gap_domains (domain, max_as, competitor_count, competitors, priority_score) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, g.getDomain());
            stmt.setInt(2, g.getMaxAs());
            stmt.setInt(3, g.getCompetitorCount());
            stmt.setString(4, g.getCompetitors());
            stmt.setDouble(5, g.getPriorityScore());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm gap_domain: " + e.getMessage());
        }
    }

    /** Xoá toàn bộ dữ liệu cũ trước khi nhập lại từ file Excel mới. */
    public void deleteAll() {
        String sql = "DELETE FROM gap_domains";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá gap_domains: " + e.getMessage());
        }
    }

    private GapDomain mapRow(ResultSet rs) throws SQLException {
        GapDomain g = new GapDomain();
        g.setId(rs.getInt("id"));
        g.setDomain(rs.getString("domain"));
        g.setMaxAs(rs.getInt("max_as"));
        g.setCompetitorCount(rs.getInt("competitor_count"));
        g.setCompetitors(rs.getString("competitors"));
        g.setPriorityScore(rs.getDouble("priority_score"));
        return g;
    }
}
