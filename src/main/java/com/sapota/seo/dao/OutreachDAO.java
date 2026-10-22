package com.sapota.seo.dao;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.model.OutreachRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** DAO cho bảng outreach (theo dõi tiến độ liên hệ xin backlink). */
public class OutreachDAO {

    public List<OutreachRecord> findAll(String status, String keyword) {
        List<OutreachRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM outreach WHERE 1=1");
        if (status != null && !status.isEmpty()) sql.append(" AND status = ?");
        if (keyword != null && !keyword.isEmpty()) sql.append(" AND (domain LIKE ? OR contact LIKE ? OR note LIKE ?)");
        sql.append(" ORDER BY (follow_up IS NULL), follow_up ASC, domain ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (status != null && !status.isEmpty()) st.setString(i++, status);
            if (keyword != null && !keyword.isEmpty()) {
                String like = "%" + keyword + "%";
                st.setString(i++, like);
                st.setString(i++, like);
                st.setString(i, like);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách outreach: " + e.getMessage());
        }
        return list;
    }

    public boolean existsByDomain(String domain) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT 1 FROM outreach WHERE domain = ?")) {
            st.setString(1, domain);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra outreach: " + e.getMessage());
            return false;
        }
    }

    /** Thêm mới; trả về false nếu domain đã tồn tại hoặc lỗi. */
    public boolean insert(OutreachRecord r) {
        if (existsByDomain(r.getDomain())) return false;
        String sql = "INSERT INTO outreach (domain, status, contact, note, last_contact, follow_up) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, r.getDomain());
            st.setString(2, r.getStatus());
            st.setString(3, r.getContact());
            st.setString(4, r.getNote());
            setDate(st, 5, r.getLastContact());
            setDate(st, 6, r.getFollowUp());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm outreach: " + e.getMessage());
            return false;
        }
    }

    public boolean update(OutreachRecord r) {
        String sql = "UPDATE outreach SET status=?, contact=?, note=?, last_contact=?, follow_up=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, r.getStatus());
            st.setString(2, r.getContact());
            st.setString(3, r.getNote());
            setDate(st, 4, r.getLastContact());
            setDate(st, 5, r.getFollowUp());
            st.setInt(6, r.getId());
            st.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật outreach: " + e.getMessage());
            return false;
        }
    }

    public void updateStatus(int id, String status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE outreach SET status=?, last_contact = IF(?<>'Chưa liên hệ', CURDATE(), last_contact) WHERE id=?")) {
            st.setString(1, status);
            st.setString(2, status);
            st.setInt(3, id);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi đổi trạng thái outreach: " + e.getMessage());
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM outreach WHERE id=?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá outreach: " + e.getMessage());
        }
    }

    /** domain (chữ thường) → trạng thái outreach; dùng để hiển thị chéo trên Dashboard. */
    public Map<String, String> findStatusMap() {
        Map<String, String> map = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT domain, status FROM outreach");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1).toLowerCase(), rs.getString(2));
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy trạng thái outreach: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> countByStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String s : OutreachRecord.STATUSES) map.put(s, 0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT status, COUNT(*) FROM outreach GROUP BY status");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) map.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) {
            System.err.println("Lỗi khi thống kê outreach: " + e.getMessage());
        }
        return map;
    }

    private void setDate(PreparedStatement st, int idx, LocalDate d) throws SQLException {
        if (d == null) st.setNull(idx, Types.DATE);
        else st.setDate(idx, java.sql.Date.valueOf(d));
    }

    private OutreachRecord map(ResultSet rs) throws SQLException {
        OutreachRecord r = new OutreachRecord();
        r.setId(rs.getInt("id"));
        r.setDomain(rs.getString("domain"));
        r.setStatus(rs.getString("status"));
        r.setContact(rs.getString("contact"));
        r.setNote(rs.getString("note"));
        java.sql.Date lc = rs.getDate("last_contact");
        java.sql.Date fu = rs.getDate("follow_up");
        r.setLastContact(lc == null ? null : lc.toLocalDate());
        r.setFollowUp(fu == null ? null : fu.toLocalDate());
        return r;
    }
}
