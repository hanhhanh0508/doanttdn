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
