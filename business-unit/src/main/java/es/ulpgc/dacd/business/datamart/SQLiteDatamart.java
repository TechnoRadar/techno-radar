package es.ulpgc.dacd.business.datamart;

import es.ulpgc.dacd.business.util.TechnologyNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

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

    public void updateGithubTrend(String technology, int stars) {
        String cleanTech = TechnologyNormalizer.normalize(technology);
        if (cleanTech.equals("unknown")) return;

        String sql = """
            INSERT INTO tech_trends (technology, github_stars, last_updated)
            VALUES (?, ?, datetime('now'))
            ON CONFLICT(technology) DO UPDATE SET 
                github_stars = excluded.github_stars,
                last_updated = datetime('now');
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cleanTech);
            pstmt.setInt(2, stars);
            pstmt.executeUpdate();

            // Guardar en historial
            Map<String, Integer> current = getTrend(cleanTech);
            saveHistory(cleanTech, stars, current.get("questions"));

            logger.info("✅ GitHub Actualizado: {} ({} estrellas)", cleanTech, stars);
        } catch (SQLException e) {
            logger.error("Error actualizando GitHub: {}", e.getMessage());
        }
    }

    public void updateStackExchangeTrend(String technology, int questions) {
        String cleanTech = TechnologyNormalizer.normalize(technology);
        if (cleanTech.equals("unknown")) return;

        String sql = """
            INSERT INTO tech_trends (technology, stack_questions, last_updated)
            VALUES (?, ?, datetime('now'))
            ON CONFLICT(technology) DO UPDATE SET 
                stack_questions = excluded.stack_questions,
                last_updated = datetime('now');
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cleanTech);
            pstmt.setInt(2, questions);
            pstmt.executeUpdate();

            // Guardar en historial
            Map<String, Integer> current = getTrend(cleanTech);
            saveHistory(cleanTech, current.get("stars"), questions);

            logger.info("✅ StackExchange Actualizado: {} ({} preguntas)", cleanTech, questions);
        } catch (SQLException e) {
            logger.error("Error actualizando StackExchange: {}", e.getMessage());
        }
    }

    private void saveHistory(String technology, int stars, int questions) {
        String sql = """
            INSERT INTO history(technology, stars, questions, date, timestamp) 
            VALUES(?, ?, ?, date('now'), datetime('now'))
            ON CONFLICT(technology, date) DO UPDATE SET 
                stars = excluded.stars, 
                questions = excluded.questions,
                timestamp = datetime('now');
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

    // ✨ NUEVO: Calcular Score Combinado (0-100)
    public double calculateCombinedScore(int stars, int questions) {
        // Normalizar ambas métricas a escala 0-100
        double normalizedStars = Math.min(stars / 10.0, 100); // GitHub típicamente tiene 0-1000
        double normalizedQuestions = Math.min(questions / 10000.0, 100); // StackExchange típicamente 0-1M

        // Score = 30% GitHub + 70% StackExchange
        return (normalizedStars * 0.3) + (normalizedQuestions * 0.7);
    }

    // ✨ NUEVO: Calcular tendencia (crecimiento/decrecimiento)
    public String calculateTrend(String technology) {
        String sql = "SELECT stars, questions FROM history WHERE technology = ? ORDER BY timestamp DESC LIMIT 2";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, technology);
            ResultSet rs = pstmt.executeQuery();

            int latest = 0, previous = 0;
            int count = 0;
            while (rs.next()) {
                int total = rs.getInt("stars") + rs.getInt("questions");
                if (count == 0) latest = total;
                if (count == 1) previous = total;
                count++;
            }

            if (previous == 0) return "STABLE";
            double change = ((double)(latest - previous) / previous) * 100;

            if (change > 10) return "UP";
            if (change < -10) return "DOWN";
            return "STABLE";

        } catch (SQLException e) {
            logger.error("Error calculando tendencia: {}", e.getMessage());
            return "STABLE";
        }
    }

    // ✨ NUEVO: Calcular Índice de Salud (0-100)
    public double calculateHealthIndex(int stars, int questions) {
        double starScore = Math.min(stars / 10.0, 50); // Max 50 puntos
        double questionScore = Math.min(questions / 10000.0, 50); // Max 50 puntos
        return starScore + questionScore;
    }

    // ✨ MODIFICADO: Ahora incluye Score, Tendencia e Índice de Salud
    public List<Map<String, Object>> getAllTrends() {
        String sql = "SELECT * FROM tech_trends ORDER BY github_stars DESC, stack_questions DESC";
        List<Map<String, Object>> trends = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String technology = rs.getString("technology");
                int stars = rs.getInt("github_stars");
                int questions = rs.getInt("stack_questions");

                Map<String, Object> tech = new HashMap<>();
                tech.put("technology", technology);
                tech.put("githubStars", stars);
                tech.put("stackExchangeQuestions", questions);
                tech.put("lastUpdated", rs.getString("last_updated"));

                // ✨ VALOR AÑADIDO:
                tech.put("combinedScore", calculateCombinedScore(stars, questions));
                tech.put("trend", calculateTrend(technology));
                tech.put("healthIndex", calculateHealthIndex(stars, questions));

                trends.add(tech);
            }

            // Ordenar por Score Combinado (descendente)
            trends.sort((a, b) -> Double.compare(
                    (double) b.get("combinedScore"),
                    (double) a.get("combinedScore")
            ));

        } catch (SQLException e) {
            logger.error("Error leyendo trends: {}", e.getMessage());
        }
        return trends;
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