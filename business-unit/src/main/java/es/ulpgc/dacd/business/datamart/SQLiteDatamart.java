package es.ulpgc.dacd.business.datamart;

import es.ulpgc.dacd.business.util.TechnologyNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class SQLiteDatamart {
    private static final Logger logger = LoggerFactory.getLogger(SQLiteDatamart.class);
    private final Connection connection;

    public SQLiteDatamart(String dbPath) {
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
                stack_questions INTEGER DEFAULT 0
            );
            """;
        String sqlHistory = """
            CREATE TABLE IF NOT EXISTS history (
                technology TEXT,
                stars INTEGER,
                questions INTEGER,
                date TEXT DEFAULT (date('now')),
                PRIMARY KEY (technology, date)
            );
            """;
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTrends);
            stmt.execute(sqlHistory);
        } catch (SQLException e) {
            logger.error("Error inicializando la base de datos: {}", e.getMessage());
        }
    }

    public void updateGithubTrend(String technology, int stars) {
        String cleanTech = TechnologyNormalizer.normalize(technology);
        if (cleanTech.equals("unknown")) return;

        Map<String, Integer> current = getTrend(cleanTech);
        String sql = """
            INSERT INTO tech_trends (technology, github_stars, stack_questions)
            VALUES (?, ?, ?)
            ON CONFLICT(technology) DO UPDATE SET github_stars = excluded.github_stars;
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cleanTech);
            pstmt.setInt(2, stars);
            pstmt.setInt(3, current.get("questions"));
            pstmt.executeUpdate();
            saveHistory(cleanTech, stars, current.get("questions"));
        } catch (SQLException e) {
            logger.error("Error actualizando GitHub: {}", e.getMessage());
        }
    }

    public void updateStackExchangeTrend(String technology, int questions) {
        String cleanTech = TechnologyNormalizer.normalize(technology);
        if (cleanTech.equals("unknown")) return;

        Map<String, Integer> current = getTrend(cleanTech);
        String sql = """
            INSERT INTO tech_trends (technology, github_stars, stack_questions)
            VALUES (?, ?, ?)
            ON CONFLICT(technology) DO UPDATE SET stack_questions = excluded.stack_questions;
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cleanTech);
            pstmt.setInt(2, current.get("stars"));
            pstmt.setInt(3, questions);
            pstmt.executeUpdate();
            saveHistory(cleanTech, current.get("stars"), questions);
        } catch (SQLException e) {
            logger.error("Error actualizando StackExchange: {}", e.getMessage());
        }
    }

    private void saveHistory(String technology, int stars, int questions) {
        String sql = """
            INSERT INTO history(technology, stars, questions, date) 
            VALUES(?, ?, ?, date('now'))
            ON CONFLICT(technology, date) DO UPDATE SET 
            stars = excluded.stars, questions = excluded.questions;
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, technology);
            pstmt.setInt(2, stars);
            pstmt.setInt(3, questions);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error guardando histórico: {}", e.getMessage());
        }
    }

    private Map<String, Integer> getTrend(String technology) {
        String sql = "SELECT github_stars, stack_questions FROM tech_trends WHERE technology = ?";
        Map<String, Integer> current = new HashMap<>();
        current.put("stars", 0);
        current.put("questions", 0);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, technology);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                current.put("stars", rs.getInt("github_stars"));
                current.put("questions", rs.getInt("stack_questions"));
            }
        } catch (SQLException e) {
            logger.error("Error consultando trend: {}", e.getMessage());
        }
        return current;
    }

    public List<Map<String, Object>> getAllTrends() {
        String sql = "SELECT * FROM tech_trends ORDER BY github_stars DESC, stack_questions DESC";
        List<Map<String, Object>> trends = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> tech = new HashMap<>();
                tech.put("technology", rs.getString("technology"));
                tech.put("githubStars", rs.getInt("github_stars"));
                tech.put("stackExchangeQuestions", rs.getInt("stack_questions"));
                trends.add(tech);
            }
        } catch (SQLException e) {
            logger.error("Error leyendo trends: {}", e.getMessage());
        }
        return trends;
    }
    public void close() throws SQLException {
        if (connection != null) connection.close();
    }
}