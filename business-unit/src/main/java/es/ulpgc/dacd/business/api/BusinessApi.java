package es.ulpgc.dacd.business.api;

import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import io.javalin.Javalin;

public class BusinessApi {
    private final SQLiteDatamart datamart;
    private Javalin app;

    public BusinessApi(SQLiteDatamart datamart) {
        this.datamart = datamart;
    }

    public void start(int port) {
        app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
        }).start(port);

        app.get("/api/trends", ctx -> ctx.json(datamart.getTrends()));
        app.get("/api/trends/{tech}/history", ctx -> ctx.json(datamart.getTrendHistory(ctx.pathParam("tech"))));
        app.get("/api/trends/emerging", ctx -> ctx.json(datamart.getEmergingTrends()));
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }
}