package com.sapota.seo.dao;

import com.sapota.seo.db.DatabaseConnection;
import com.sapota.seo.model.BacklinkDomain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO cho bảng backlinks (backlink hiện có của sapotacorp.vn). */
public class BacklinkDAO {

    public List<BacklinkDomain> findAll() {
        List<BacklinkDomain> list = new ArrayList<>();
        String sql = "SELECT * FROM backlinks ORDER BY ascore DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                BacklinkDomain b = new BacklinkDomain();
                b.setId(rs.getInt("id"));
                b.setDomain(rs.getString("domain"));
                b.setAscore(rs.getInt("ascore"));
                b.setBacklinks(rs.getInt("backlinks"));
                b.setCountry(rs.getString("country"));
                list.add(b);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách backlinks: " + e.getMessage());
        }
        return list;
    }

    public void insert(BacklinkDomain b) {
        String sql = "INSERT INTO backlinks (domain, ascore, backlinks, country) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, b.getDomain());
            stmt.setInt(2, b.getAscore());
            stmt.setInt(3, b.getBacklinks());
            stmt.setString(4, b.getCountry());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm backlink: " + e.getMessage());
        }
    }

    public void deleteAll() {
        String sql = "DELETE FROM backlinks";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xoá backlinks: " + e.getMessage());
        }
    }
}
