package telegram.Calories_Bot.util;

import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class HTTPRequestSender {

    /**
     * @return HTTP request status code
     */
    public int setTelegramBotWebHook(String botUrl, String botToken) {

        String urlToSend = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + botUrl;

        try (HttpClient client = HttpClient.newHttpClient()){
            URI uri = URI.create(urlToSend);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("WebHook was set. Status " + response.statusCode());
            return response.statusCode();

        } catch (Exception e) {
            log.error("WebHook was NOT set! Status 500");
            e.printStackTrace();
            return HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
    }
}
