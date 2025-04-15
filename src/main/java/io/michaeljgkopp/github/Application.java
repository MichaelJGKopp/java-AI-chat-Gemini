package io.michaeljgkopp.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

// This is a simple Java application that sends a request to the Gemini API
public class Application {
    // Path to the markdown file where chat logs will be saved
    public static final String Chat_Log_MD_PATH = "Response.md";
    // API KEY
    private static String apiKey = System.getenv("GEMINI_API_KEY");
    // API URL
    private static URI uri = URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey);

    public static void main(String[] args) throws IOException, InterruptedException {

        // Check if the API key is set
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Please set the GEMINI_API_KEY environment variable.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        // CHAT WITH AI
        while (true) {
            System.out.println("Enter a prompt (type 'END' on a new line to finish, or 'exit' to quit):");
            StringBuilder promptBuilder = new StringBuilder();
            String line;

            while (!(line = scanner.nextLine()).equalsIgnoreCase("END")) {
                if (line.equalsIgnoreCase("exit")) {
                    return; // Exit the program
                }
                promptBuilder.append(line).append("\n");
            }
            String prompt = promptBuilder.toString().trim();
            if (!prompt.isEmpty()) {
                // send the prompt to the AI
                chatAI(prompt);
            }
        }
    }

    /**
     * Sends a prompt to the Gemini API and prints the response.
     *
     * @param prompt The prompt to send to the AI.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the request is interrupted.
     */
    private static void chatAI(String prompt) throws IOException, InterruptedException {

        // BUILD HTTP request
        HttpRequest request = requestFromPrompt(prompt);

        // SEND REQUEST

        String jsonResponse;
        int statusCode;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            jsonResponse = response.body();
            statusCode = response.statusCode();
        }

        // API RESPONSE

        // ERROR CASE
        if (statusCode != 200) {
            System.err.println("Error: " + statusCode);
            System.out.println(jsonResponse);
            return;
        }

        // HTTP.OK CASE
        String text = extractTextFromResponse(jsonResponse);

        appendChatToFile(prompt, text);
    }

    private static HttpRequest requestFromPrompt(String prompt) throws JsonProcessingException {
        // Construct JSON body using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = rootNode.putArray("contents");
        ObjectNode contentNode = contentsArray.addObject();
        ArrayNode partsArray = contentNode.putArray("parts");
        ObjectNode partNode = partsArray.addObject();
        partNode.put("text", prompt);

        String body = objectMapper.writeValueAsString(rootNode);

        // BUILD HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return request;
    }

    private static String extractTextFromResponse(String jsonResponse) throws JsonProcessingException {
        // Parse JSON using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode2 = objectMapper.readTree(jsonResponse);

        // Navigate to the "text" field
        String text = rootNode2
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();
        return text;
    }

    private static void appendChatToFile(String prompt, String text) throws IOException {
        // Print the extracted text
        System.out.println("\nRESPONSE: \n============\n" + text);

        // Save the response to a file
        Path path = Path.of(Chat_Log_MD_PATH);
        Files.writeString(path,
                """
                        REQUEST:
                        ---------------------------------------------------
                        %s
                        
                        RESPONSE:
                        ---------------------------------------------------
                        %s
                        ===================================================
                        
                        """.formatted(prompt, text),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}