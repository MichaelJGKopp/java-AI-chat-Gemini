package io.michaeljgkopp.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// This is a simple Java application that sends a request to the Gemini API
public class Application {
    public static void main(String[] args) throws IOException, InterruptedException {
        // PROMPT
        String prompt = "Tell me something interesting about java";

        // API KEY
        String apiKey = System.getenv("GEMINI_API_KEY");

        // Check if the API key is set
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Please set the GEMINI_API_KEY environment variable.");
            return;
        }

        // API URL
        URI uri = URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey);

        // Request body
        String body = """
                {
                  "contents": [{
                    "parts":[{"text": "%s"}]
                    }]
                   }
                """;

        // BUILD HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.formatted(prompt)))
                .build();

        // SEND REQUEST

        String jsonResponse;
        int statusCode;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            jsonResponse = response.body();
            statusCode = response.statusCode();
        }

        // API RESPONSE
        System.out.println(jsonResponse);

        if (statusCode != 200) {
            System.err.println("Error: " + statusCode);
            return;
        }

        // Parse JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Navigate to the "text" field
        String text = rootNode
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        // Print the extracted text
        System.out.println("RESPONSE: \n============\n" + text);
    }
}