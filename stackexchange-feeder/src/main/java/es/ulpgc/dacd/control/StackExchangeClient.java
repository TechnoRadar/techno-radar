package es.ulpgc.dacd.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.model.StackExchangeTrend;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StackExchangeClient implements StackExchangeFeeder {
    private static final Logger logger = LoggerFactory.getLogger(StackExchangeClient.class);
    private final OkHttpClient client = new OkHttpClient();
    private final String baseUrl;

    public StackExchangeClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String getJson(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if (response.body() != null) {
                return response.body().string();
            } else {
                return "";
            }
        }
    }

    @Override
    public List<StackExchangeTrend> getTrends() {
        List<StackExchangeTrend> trends = new ArrayList<>();

        try {
            String url = buildUrl();
            logger.info("Llamando a la API de StackExchange: {}", url);
            String jsonResponse = getJson(url);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray items = jsonObject.getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject tag = element.getAsJsonObject();

                String name = tag.get("name").getAsString();
                int count = tag.get("count").getAsInt();

                trends.add(new StackExchangeTrend(name, count, Instant.now()));
            }
        } catch (IOException e) {
            logger.error("Error al obtener datos de StackExchange", e);        }
        return trends;
    }
    private String buildUrl() {
        return String.format("%s/tags?order=desc&sort=activity&site=stackoverflow&pagesize=30", baseUrl);
    }
}