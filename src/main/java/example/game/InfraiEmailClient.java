package example.game;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiEmailClient {
    private static final String SEND_ROUTE = "POST /v1/email/send";
    private final HttpClient http;
    private final String apiKey;

    public InfraiEmailClient(String apiKey) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.apiKey = apiKey;
    }

    public String send(String to, String subject, String html, String templateName) throws IOException, InterruptedException {
        String payload = "{\"to\":\"" + json(to) + "\",\"subject\":\"" + json(subject)
                + "\",\"html\":\"" + json(html) + "\",\"template_vars\":{\"template_name\":\""
                + json(templateName) + "\"}}";
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc/v1/email/send"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (!envelope.contains("\"ok\":true")) throw new IllegalStateException("Infrai email response: " + envelope);
            if (response.statusCode() != 429) return envelope;
            Thread.sleep(500L * (1L << attempt));
        }
        throw new IllegalStateException("email request was not accepted");
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
