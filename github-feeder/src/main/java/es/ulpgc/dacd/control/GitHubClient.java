package es.ulpgc.dacd.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.model.GitHubTrend;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GitHubClient implements GitHubFeeder{
    private final OkHttpClient client = new OkHttpClient();

    public String getJson (String url) throws IOException {
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
    public List<GitHubTrend> getTrends() {
        List<GitHubTrend> trends = new ArrayList<>();
        String url = "https://api.github.com/search/repositories?q=stars:>50000&sort=stars&per_page=10";

        try {
            String jsonResponse = getJson(url);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray items = jsonObject.getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject repo = element.getAsJsonObject();

                String name = repo.get("name").getAsString();
                int stars = repo.get("stargazers_count").getAsInt();

                String language = repo.has("language") && !repo.get("language").isJsonNull()
                        ? repo.get("language").getAsString()
                        : "Unknown";
                trends.add(new GitHubTrend(name, stars, language, Instant.now()));
            }
        } catch (IOException e) {
            System.err.println("Error al obtener datos: " + e.getMessage());
        }
        return trends;
    }
}
