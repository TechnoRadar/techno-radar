package es.ulpgc.dacd.business.reader;

import es.ulpgc.dacd.business.util.EventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class EventStoreReader {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreReader.class);
    private final String eventStorePath;
    private final EventProcessor eventProcessor;

    public EventStoreReader(String eventStorePath, EventProcessor eventProcessor) {
        this.eventStorePath = eventStorePath;
        this.eventProcessor = eventProcessor;
    }

    public void processHistoricalEvents() {
        Path storePath = Paths.get(eventStorePath);

        if (!Files.exists(storePath)) {
            logger.warn("La carpeta de historial '{}' no existe aún. Saltando el Cold Start.", storePath.toAbsolutePath());
            return;
        }

        logger.info("Iniciando carga histórica (Cold Start) desde: {}", storePath.toAbsolutePath());

        try (Stream<Path> paths = Files.walk(storePath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".events"))
                    .forEach(this::processFile);

            logger.info("Carga histórica completada. El Datamart está actualizado.");
        } catch (Exception e) {
            logger.error("Error crítico leyendo el event store: {}", e.getMessage());
        }
    }

    private void processFile(Path filePath) {
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.forEach(eventProcessor::process);
        } catch (Exception e) {
            logger.error("Error procesando archivo histórico: {}", filePath, e);
        }
    }
}