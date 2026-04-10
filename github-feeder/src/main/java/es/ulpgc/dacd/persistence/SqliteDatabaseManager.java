package es.ulpgc.dacd.persistence;

import es.ulpgc.dacd.model.GitHubTrend;
import java.sql.*;

public class SqliteDatabaseManager implements  GitHubStore {
    private final String url;

    public SqliteDatabaseManager(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
        initTable();
    }

    private void initTable() {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS github_trends (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, stars INTEGER, language TEXT, captured_at TEXT)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void save(GitHubTrend trend) {
        String sql = "INSERT INTO github_trends(name, stars, language, captured_at) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, trend.name());
            pstmt.setInt(2, trend.stars());
            pstmt.setString(3, trend.language());
            pstmt.setString(4, trend.capturedAt().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
