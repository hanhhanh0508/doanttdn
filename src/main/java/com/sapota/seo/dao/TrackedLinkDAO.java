package com.sapota.seo.dao;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.model.TrackedLink;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO cho bảng tracked_links (backlink đã đặt, theo dõi còn sống hay không). */
public class TrackedLinkDAO {

    public List<TrackedLink> findAll() {
        List<TrackedLink> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT * FROM tracked_links ORDER BY id DESC");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                TrackedLink t = new TrackedLink();
                t.setId(rs.getInt("id"));
                t.setUrl(rs.getString("url"));
                t.setPlatform(rs.getString("platform"));
                java.sql.Date pd = rs.getDate("placed_date");
                t.setPlacedDate(pd == null ? null : pd.toLocalDate());
                t.setLastStatus(rs.getString("last_status"));
                t.setPrevStatus(rs.getString("prev_status"));
                Timestamp ts = rs.getTimestamp("last_checked");
                t.setLastChecked(ts == null ? null : ts.toLocalDateTime());
                t.setNote(rs.getString("note"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tracked_links: " + e.getMessage());
        }
        return list;
    }

    public boolean existsByUrl(String url) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT 1 FROM tracked_links WHERE url = ?")) {
            st.setString(1, url);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insert(TrackedLink t) {
        if (existsByUrl(t.getUrl())) return false;
        String sql = "INSERT INTO tracked_links (url, platform, placed_date, note) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, t.getUrl());
            st.setString(2, t.getPlatform());
            if (t.getPlacedDate() == null) st.setNull(3, Types.DATE);
            else st.setDate(3, java.sql.Date.valueOf(t.getPlacedDate()));
            st.setString(4, t.getNote());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm tracked_link: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật kết quả kiểm tra mới. MySQL xử lý các phép gán trong UPDATE từ trái sang phải
     * nên prev_status nhận đúng giá trị last_status cũ trước khi last_status bị ghi đè.
     */
    public void updateStatus(int id, String newStatus, LocalDateTime checkedAt) {
        String sql = "UPDATE tracked_links SET prev_status = last_status, last_status = ?, last_checked = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, newStatus);
            st.setTimestamp(2, Timestamp.valueOf(checkedAt));
            st.setInt(3, id);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tracked_link: " + e.getMessage());
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM tracked_links WHERE id = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá tracked_link: " + e.getMessage());
        }
    }
}
