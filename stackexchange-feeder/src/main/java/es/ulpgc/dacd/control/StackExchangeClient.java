package es.ulpgc.dacd.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.model.StackExchangeTrend;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StackExchangeClient implements StackExchangeFeeder {
    private final OkHttpClient client = new OkHttpClient();
    private final String url;

    public StackExchangeClient(String url) {
        this.url = url;
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
            String jsonResponse = getJson(this.url);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray items = jsonObject.getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject tag = element.getAsJsonObject();

                String name = tag.get("name").getAsString();
                int count = tag.get("count").getAsInt();

                trends.add(new StackExchangeTrend(name, count, Instant.now()));
            }
        } catch (IOException e) {
            System.err.println("Error al obtener datos: " + e.getMessage());
        }
        return trends;
    }
}