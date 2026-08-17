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
}
