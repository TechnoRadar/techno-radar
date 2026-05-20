package es.ulpgc.dacd.business.datamart;

import es.ulpgc.dacd.business.util.TechnologyNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteDatamart {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteDatamart.class);
    private final Connection connection;
    private final String dbPath;

    public SQLiteDatamart(String dbPath) {
        this.dbPath = dbPath;
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            initDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }

    private void initDatabase() {
        String sqlTrends = """
            CREATE TABLE IF NOT EXISTS tech_trends (
                technology TEXT PRIMARY KEY,
                github_stars INTEGER DEFAULT 0,
                stack_questions INTEGER DEFAULT 0,
                is_monitored INTEGER DEFAULT 0,
                last_updated TEXT DEFAULT (datetime('now'))
            );
            """;
        String sqlHistory = """
            CREATE TABLE IF NOT EXISTS history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                technology TEXT,
                stars INTEGER,
                questions INTEGER,
                date TEXT DEFAULT (date('now')),
                timestamp TEXT DEFAULT (datetime('now')),
                UNIQUE(technology, date)
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlTrends);
            stmt.execute(sqlHistory);
        } catch (SQLException e) {
            logger.error("Error inicializando la base de datos: {}", e.getMessage());
        }
    }

    public void updateTrend(String technology, int starsDelta, int questionsDelta) {
        String normalizedTech = TechnologyNormalizer.normalize(technology);

        int isMonitoredValue = TechnologyNormalizer.isMonitored(normalizedTech) ? 1 : 0;

        String sqlTrends = """
            INSERT INTO tech_trends (technology, github_stars, stack_questions, is_monitored, last_updated)
            VALUES (?, ?, ?, ?, datetime('now'))
            ON CONFLICT(technology) DO UPDATE SET
                github_stars = github_stars + excluded.github_stars,
                stack_questions = stack_questions + excluded.stack_questions,
                last_updated = datetime('now');
            """;

        String sqlHistory = """
            INSERT INTO history (technology, stars, questions, date, timestamp)
            VALUES (?, ?, ?, date('now'), datetime('now'))
            ON CONFLICT(technology, date) DO UPDATE SET
                stars = stars + excluded.stars,
                questions = questions + excluded.questions,
                timestamp = datetime('now');
            """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt1 = connection.prepareStatement(sqlTrends);
                 PreparedStatement pstmt2 = connection.prepareStatement(sqlHistory)) {

                pstmt1.setString(1, normalizedTech);
                pstmt1.setInt(2, starsDelta);
                pstmt1.setInt(3, questionsDelta);
                pstmt1.setInt(4, isMonitoredValue);
                pstmt1.executeUpdate();

                pstmt2.setString(1, normalizedTech);
                pstmt2.setInt(2, starsDelta);
                pstmt2.setInt(3, questionsDelta);
                pstmt2.executeUpdate();

                connection.commit();
                logger.info("Datamart actualizado: {} (+{} stars, +{} q) | Monitored: {}",
                        normalizedTech, starsDelta, questionsDelta, isMonitoredValue);

            } catch (SQLException e) {
                connection.rollback();
                logger.error("Error en la transacción. Rollback ejecutado: {}", e.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error de base de datos general: {}", e.getMessage());
        }
    }

    public List<Map<String, Object>> getTrends() {
        String sql = """
            SELECT technology, github_stars, stack_questions, 
                   ((github_stars) + stack_questions) AS combinedScore 
            FROM tech_trends 
            WHERE is_monitored = 1 
            ORDER BY combinedScore DESC 
            LIMIT 100
            """;

        List<Map<String, Object>> trends = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("technology", rs.getString("technology"));
                entry.put("stars", rs.getInt("github_stars"));
                entry.put("questions", rs.getInt("stack_questions"));
                entry.put("combinedScore", rs.getInt("combinedScore"));
                trends.add(entry);
            }
        } catch (SQLException e) {
            logger.error("Error leyendo trends: {}", e.getMessage());
        }
        return trends;
    }

    public List<Map<String, Object>> getEmergingTrends() {
        String sql = "SELECT * FROM tech_trends WHERE is_monitored = 0 ORDER BY stack_questions DESC LIMIT 20";
        List<Map<String, Object>> emerging = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("technology", rs.getString("technology"));
                entry.put("stars", rs.getInt("github_stars"));
                entry.put("questions", rs.getInt("stack_questions"));
                emerging.add(entry);
            }
        } catch (SQLException e) {
            logger.error("Error consultando tendencias emergentes: {}", e.getMessage());
        }
        return emerging;
    }

    public List<Map<String, Object>> getTrendHistory(String technology) {
        String sql = "SELECT * FROM history WHERE technology = ? ORDER BY timestamp DESC LIMIT 30";
        List<Map<String, Object>> history = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, technology);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("technology", rs.getString("technology"));
                entry.put("stars", rs.getInt("stars"));
                entry.put("questions", rs.getInt("questions"));
                entry.put("date", rs.getString("date"));
                entry.put("timestamp", rs.getString("timestamp"));
                history.add(entry);
            }
        } catch (SQLException e) {
            logger.error("Error consultando historial: {}", e.getMessage());
        }
        return history;
    }

    public void close() throws SQLException {
        if (connection != null) connection.close();
    }
}