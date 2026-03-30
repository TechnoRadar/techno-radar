package es.ulpgc.dacd.persistence;

import es.ulpgc.dacd.model.StackExchangeTrend;
import java.sql.*;

public class SqliteDatabaseManager {
    private final String url;

    public SqliteDatabaseManager(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        initTable();
    }

    private void initTable() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS stackexchange_trends (" +
                    "name TEXT, count INTEGER, captured_at TEXT)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void save(StackExchangeTrend trend) {
        String sql = "INSERT INTO stackexchange_trends(name, count, captured_at) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trend.name());
            pstmt.setInt(2, trend.count());
            pstmt.setString(3, trend.capturedAt().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
