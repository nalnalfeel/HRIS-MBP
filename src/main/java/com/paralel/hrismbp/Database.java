package com.paralel.hrismbp;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:hris_data.db";

    public static Connection connect() throws SQLException {
        // 1. Ambil path ke folder Home user Windows (C:\Users\NamaUser)
        String userHome = System.getProperty("user.home");

        // 2. Buat folder khusus untuk aplikasi Anda di Documents atau AppData
        String dbDir = userHome + File.separator + "HRIS-MBP";
        File directory = new File(dbDir);

        // 3. Buat foldernya jika belum ada
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 4. Arahkan koneksi SQLite ke folder tersebut
        String url = "jdbc:sqlite:" + dbDir + File.separator + "hris_data.db";
        return DriverManager.getConnection(url);
    }

    public static void initDatabase() {
        try (InputStream is = Database.class.getResourceAsStream("/schema.sql")) {
            if (is == null) {
                System.err.println("File schema.sql tidak ditemukan!");
                return;
            }
            String fullSql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] sqlStatements = fullSql.split(";");
            try (Connection conn = connect()) {
                for (String sql : sqlStatements) {
                    String trimmedSql = sql.trim();
                    if (!trimmedSql.isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(trimmedSql);
                        }
                    }
                }
                String checkUserSql = "INSERT OR IGNORE INTO users (id, username, password) VALUES (1, 'admin', 'mbp26')";
                try (PreparedStatement pstmt = conn.prepareStatement(checkUserSql)) {
                    pstmt.executeUpdate();
                }
                System.out.println("Database SQLite berhasil diinisialisasi.");
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat skema database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*public static String getNikByEmployeeName(String fullName) {
        String sql = "SELECT nik FROM employees WHERE LOWER(full_name) = LOWER(?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nik");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }*/

    public static boolean checkAdminPassword(String currentPassword) {
        // DI-FIX: Mengubah 'usersa' menjadi 'users'
        String sql = "SELECT * FROM users WHERE username = 'admin' AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentPassword);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateAdminPassword(String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = 'admin'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}