package es.ulpgc.dacd.control;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class GitHubClient {
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
}
