package es.ulpgc.dacd.business.reader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.ulpgc.dacd.business.datamart.SQLiteDatamart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class EventStoreReader {
    private final SQLiteDatamart datamart;
    private final String eventStorePath;
    private final Gson gson;

    public EventStoreReader(SQLiteDatamart datamart, String eventStorePath) {
        this.datamart = datamart;
        this.eventStorePath = eventStorePath;
        this.gson = new Gson();
    }

    public void loadHistory() {
        File rootDir = new File(eventStorePath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            System.err.println("Atención: La ruta del EventStore no existe o está vacía: " + eventStorePath);
            return;
        }
        System.out.println("Cargando historial desde: " + eventStorePath);
        processDirectory(rootDir);
        System.out.println("Historial cargado correctamente.");
    }

    private void processDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else if (file.getName().endsWith(".events")) {
                processFile(file);
            }
        }
    }

    private void processFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processEvent(line);
            }
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo " + file.getName() + ": " + e.getMessage());
        }
    }

    private void processEvent(String jsonLine) {
        try {
            JsonObject event = gson.fromJson(jsonLine, JsonObject.class);
            if (!event.has("payload")) return; // Si no hay datos útiles, ignoramos

            JsonObject payload = event.getAsJsonObject("payload");

            if (payload.has("repositoryName")) {
                String language = payload.has("language") && !payload.get("language").isJsonNull()
                        ? payload.get("language").getAsString() : "unknown";
                int stars = payload.has("stars") ? payload.get("stars").getAsInt() : 0;
                datamart.updateGithubTrend(language, stars);

            } else if (payload.has("tagName")) {
                String tag = payload.get("tagName").getAsString();
                int count = payload.has("count") ? payload.get("count").getAsInt() : 0;
                datamart.updateStackExchangeTrend(tag, count);
            }
        } catch (Exception e) {
            System.err.println("Error parseando JSON: " + e.getMessage());
        }
    }
}