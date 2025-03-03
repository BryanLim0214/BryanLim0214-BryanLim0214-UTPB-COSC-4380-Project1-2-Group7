// LlamaSentenceChecker.java

import java.io.*;
import java.net.*;
import java.util.*;

public class LlamaSentenceChecker {
    // OpenRouter API URL and model
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL_NAME = "meta-llama/llama-3.3-70b-instruct:free";

    // Google Drive File ID (Replace with your actual ID)
    private static final String FILE_ID = "18SP3oz_eZrkYJxOV2gZ3CUKz0nhlADGb";

    // API key fetched from Google Drive
    private static final String API_KEY = fetchAPIKey();

    /**
     * Fetches the OpenRouter API key from a public Google Drive text file.
     */
    private static String fetchAPIKey() {
        try {
            // Construct the Google Drive direct download URL
            String apiUrl = "https://drive.google.com/uc?export=download&id=" + FILE_ID;

            // Open the URL stream
            URL url = new URL(apiUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            // Read the API key (first line of the file)
            String apiKey = reader.readLine().trim();
            reader.close();

            return apiKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch API key from Google Drive", e);
        }
    }

    /**
     * Sends a list of possible decrypted texts with their keys to the Llama model
     * and asks it to identify which one is most likely to be a valid English sentence.
     *
     * @param textsWithKeys A map of potential plaintext decryptions to their corresponding keys
     * @return The model's response containing the best sentence and its key
     * @throws IOException If there's an error communicating with the API
     */
    public static String findMostLikelySentenceWithKey(Map<String, String> textsWithKeys) throws IOException {
        // Set up the HTTP connection to OpenRouter
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build the prompt for the AI model
        StringBuilder prompt = new StringBuilder("Analyze the list of decrypted texts. Identify the most meaningful English sentence. Even if the input is slightly incorrect or lacks spacing, try to reconstruct the best possible English phrase or common english word. Output ONLY:\n\n**Best Sentence/word: [BEST_SENTENCE/BEST_WORD] (Key: [KEY]) (Confidence: XX%)**\n\nList:\n");

        for (Map.Entry<String, String> entry : textsWithKeys.entrySet()) {
            prompt.append("- Text: ").append(entry.getKey()).append(" (Key: ").append(entry.getValue()).append(")\n");
        }

        // Construct the JSON payload for OpenRouter
        String jsonPayload = "{"
                + "\"model\": \"" + MODEL_NAME + "\","
                + "\"messages\": [{\"role\": \"system\", \"content\": \"" + prompt.toString().replace("\"", "\\\"") + "\"}],"
                + "\"temperature\": 0.5"
                + "}";

        // Send the JSON payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read the API response
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Extract the content from the JSON response
        String jsonResponse = response.toString();
        int contentStart = jsonResponse.indexOf("\"content\":") + 11;
        int contentEnd = jsonResponse.indexOf("\"", contentStart);
        String content = jsonResponse.substring(contentStart, contentEnd);

        // Extract the best sentence part from the content
        if (content.contains("Best Sentence:")) {
            return content.substring(content.indexOf("Best Sentence:"));
        } else {
            return "Error: Could not find 'Best Sentence:' in response";
        }
    }

    /**
     * Original method for backward compatibility
     */
    public static String findMostLikelySentence(List<String> texts) throws IOException {
        // Convert list to map with empty keys for backward compatibility
        Map<String, String> textsWithKeys = new HashMap<>();
        for (String text : texts) {
            textsWithKeys.put(text, "N/A");
        }
        return findMostLikelySentenceWithKey(textsWithKeys);
    }
}
