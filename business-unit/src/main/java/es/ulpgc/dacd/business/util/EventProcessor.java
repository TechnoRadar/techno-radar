package es.ulpgc.dacd.business.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);
    private final SQLiteDatamart datamart;

    public EventProcessor(SQLiteDatamart datamart) {
        this.datamart = datamart;
    }

    public void process(String jsonEvent) {
        try {
            JsonObject root = JsonParser.parseString(jsonEvent).getAsJsonObject();
            String sourceSystem = root.get("ss").getAsString().toLowerCase();
            JsonObject payload = root.getAsJsonObject("payload");

            if (sourceSystem.contains("github")) {
                String technology = payload.has("language") ? payload.get("language").getAsString() :
                        payload.has("repositoryName") ? payload.get("repositoryName").getAsString() : "unknown";

                int stars = payload.has("stars") ? payload.get("stars").getAsInt() : 0;

                datamart.updateTrend(technology, stars, 0);

            } else if (sourceSystem.contains("stackexchange")) {
                String technology = payload.has("tagName") ? payload.get("tagName").getAsString() :
                        payload.has("tag") ? payload.get("tag").getAsString() :
                        payload.has("name") ? payload.get("name").getAsString() : "unknown";

                int count = payload.has("count") ? payload.get("count").getAsInt() :
                        payload.has("mentions") ? payload.get("mentions").getAsInt() : 0;

                datamart.updateTrend(technology, 0, count);
            }
        } catch (IllegalStateException | NullPointerException e) {
            logger.error("Error leyendo formato del evento JSON. El JSON no tiene la estructura esperada: {}", jsonEvent);
        }
    }
}