package es.ulpgc.dacd.control;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.ulpgc.dacd.model.GitHubTrend;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GitHubClient implements GitHubFeeder{
    private static final Logger logger = LoggerFactory.getLogger(GitHubClient.class);
    private final OkHttpClient client = new OkHttpClient();
    private final String baseUrl;

    public GitHubClient(String baseUrl) {
        this.baseUrl =  baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

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

        try {
            String dynamicUrl = buildUrl();
            logger.info("Llamando a la API de GitHub: {}", dynamicUrl);
            String jsonResponse = getJson(dynamicUrl);

            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray items = jsonObject.getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject repo = element.getAsJsonObject();

                String repoName = repo.get("name").getAsString();
                int stars = repo.get("stargazers_count").getAsInt();

                String language = repo.has("language") && !repo.get("language").isJsonNull()
                        ? repo.get("language").getAsString()
                        : "Unknown";
                trends.add(new GitHubTrend(repoName, stars, language, Instant.now()));
            }
        } catch (IOException e) {
            logger.error("Error al obtener datos de GitHub", e);
        }
        return trends;
    }

    private String buildUrl(){
        LocalDate lastWeek = LocalDate.now().minusDays(7);

       return String.format("%s/search/repositories?q=created:>%s&sort=stars&order=desc&per_page=10",
               baseUrl, lastWeek);
    }
}
