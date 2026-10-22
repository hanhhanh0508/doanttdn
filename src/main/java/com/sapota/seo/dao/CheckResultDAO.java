package com.sapota.seo.dao;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.model.CheckResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** DAO cho bảng check_results (lịch sử kiểm tra dofollow/nofollow). */
public class CheckResultDAO {

    public void insert(CheckResult r) {
        String sql = "INSERT INTO check_results (url, status, checked_at, note) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, r.getUrl());
            stmt.setString(2, r.getStatus());
            stmt.setTimestamp(3, Timestamp.valueOf(r.getCheckedAt()));
            stmt.setString(4, r.getNote());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu kết quả kiểm tra: " + e.getMessage());
        }
    }

    /** Lấy lịch sử kiểm tra gần đây nhất, mới nhất lên trước. */
    public List<CheckResult> findRecent(int limit) {
        List<CheckResult> list = new ArrayList<>();
        String sql = "SELECT * FROM check_results ORDER BY checked_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CheckResult r = new CheckResult();
                    r.setId(rs.getInt("id"));
                    r.setUrl(rs.getString("url"));
                    r.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("checked_at");
                    if (ts != null) {
                        r.setCheckedAt(ts.toLocalDateTime());
                    }
                    r.setNote(rs.getString("note"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử kiểm tra: " + e.getMessage());
        }
        return list;
    }

    /** Lịch sử có lọc theo trạng thái (null/rỗng = tất cả) và từ khoá trong URL. */
    public List<CheckResult> findFiltered(String status, String keyword, int limit) {
        List<CheckResult> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM check_results WHERE 1=1");
        if (status != null && !status.isEmpty()) sql.append(" AND status = ?");
        if (keyword != null && !keyword.isEmpty()) sql.append(" AND url LIKE ?");
        sql.append(" ORDER BY checked_at DESC, id DESC LIMIT ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (status != null && !status.isEmpty()) stmt.setString(i++, status);
            if (keyword != null && !keyword.isEmpty()) stmt.setString(i++, "%" + keyword + "%");
            stmt.setInt(i, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CheckResult r = new CheckResult();
                    r.setId(rs.getInt("id"));
                    r.setUrl(rs.getString("url"));
                    r.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("checked_at");
                    if (ts != null) r.setCheckedAt(ts.toLocalDateTime());
                    r.setNote(rs.getString("note"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lọc lịch sử kiểm tra: " + e.getMessage());
        }
        return list;
    }

    public void deleteById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM check_results WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá kết quả kiểm tra: " + e.getMessage());
        }
    }

    public void deleteAll() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM check_results")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá lịch sử kiểm tra: " + e.getMessage());
        }
    }

    /** Số lượng kết quả theo từng trạng thái (Dofollow/Nofollow/Không tìm thấy/Lỗi truy cập). */
    public java.util.Map<String, Integer> countByStatus() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        map.put(CheckResult.STATUS_DOFOLLOW, 0);
        map.put(CheckResult.STATUS_NOFOLLOW, 0);
        map.put(CheckResult.STATUS_NOT_FOUND, 0);
        map.put(CheckResult.STATUS_ERROR, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT status, COUNT(*) FROM check_results GROUP BY status");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) {
            System.err.println("Lỗi khi thống kê kết quả kiểm tra: " + e.getMessage());
        }
        return map;
    }
}
