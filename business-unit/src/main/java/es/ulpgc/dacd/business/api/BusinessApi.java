package es.ulpgc.dacd.business.api;

import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import es.ulpgc.dacd.persistence.JmsEventStore;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class BusinessApi {
    private static final Logger logger = LoggerFactory.getLogger(BusinessApi.class);

    private final SQLiteDatamart datamart;
    private final int port;

    public BusinessApi(SQLiteDatamart datamart, int port) {
        this.datamart = datamart;
        this.port = port;
    }

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
        }).start(port);

        app.get("/api/trends", ctx -> {
            logger.info("Solicitud GET recibida en /api/trends");
            List<Map<String, Object>> trends = datamart.getAllTrends();

            if (trends.isEmpty()) {
                logger.warn("El Datamart está vacío.");
                ctx.status(404).result("No hay tendencias disponibles.");
            } else {
                ctx.json(trends);
            }
        });

        logger.info("🚀 API y Dashboard iniciados. Entra en: http://localhost:{}", port);
    }
}
