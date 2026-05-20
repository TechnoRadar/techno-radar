package es.ulpgc.dacd.business.reader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class EventStoreReader {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreReader.class);
    private final String eventStorePath;
    private final SQLiteDatamart datamart;

    public EventStoreReader(String eventStorePath, SQLiteDatamart datamart) {
        this.eventStorePath = eventStorePath;
        this.datamart = datamart;
    }

    public void processHistoricalEvents() {
        logger.info("Iniciando carga histórica (Cold Start) desde: {}", eventStorePath);

        try (Stream<Path> paths = Files.walk(Paths.get(eventStorePath))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".events"))
                    .forEach(this::processFile);

            logger.info("Carga histórica completada. El Datamart está actualizado.");
        } catch (Exception e) {
            logger.error("Error leyendo el event store: {}", e.getMessage());
        }
    }

    private void processFile(Path filePath) {
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(this::processEventLine);
        } catch (Exception e) {
            logger.error("Error procesando archivo: {}", filePath, e);
        }
    }

    private void processEventLine(String jsonEvent) {
        try {
            JsonObject root = JsonParser.parseString(jsonEvent).getAsJsonObject();
            String sourceSystem = root.get("ss").getAsString().toLowerCase();
            JsonObject payload = root.getAsJsonObject("payload");

            if (sourceSystem.contains("github")) {
                String name = payload.get("language").getAsString();
                int stars = payload.get("stars").getAsInt();

                datamart.updateTrend(name, stars, 0);

            } else if (sourceSystem.contains("stackexchange")) {
                String name = payload.get("tagName").getAsString();
                int count = payload.get("count").getAsInt();

                datamart.updateTrend(name, 0, count);
            }
        } catch (Exception e) {
            logger.error("Error al procesar línea del histórico: {}", jsonEvent, e);
        }
    }
}