import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A utility class that uses the OpenRouter API to access Llama language model
 * for identifying the most meaningful sentence from a list of possible decryptions.
 * This acts as an AI-powered oracle to help determine which decryption is most likely correct.
 */
public class LlamaSentenceChecker {
    // API configuration constants
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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"; // OpenRouter API Key
    private static final String MODEL_NAME = "meta-llama/llama-3.3-70b-instruct:free"; // Using Llama 3.3 70B model

    /**
     * Sends a list of possible decrypted texts to the Llama model and asks it
     * to identify which one is most likely to be a valid English sentence.
     *
     * @param texts A list of potential plaintext decryptions to evaluate
     * @return The model's response containing the best sentence and confidence score
     * @throws IOException If there's an error communicating with the API
     */
    public static String findMostLikelySentence(List<String> texts) throws IOException {
        // Set up the HTTP connection to the API
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build the prompt for the language model
        // The prompt instructs the model to analyze the texts and identify the most meaningful one
        StringBuilder prompt = new StringBuilder("Analyze the list of decrypted words. Identify the most meaningful sentence. Even if the input is slightly incorrect or lacks spacing, try to reconstruct the best possible English phrase. Output ONLY:\n\n**Best Sentence: [BEST_SENTENCE] (Confidence: XX%)**\n\nList:\n");

        // Add each potential decryption to the prompt
        for (String text : texts) {
            prompt.append("- ").append(text).append("\n");
        }

        // Construct the JSON payload for the API request
        // This includes the model to use, the prompt as a system message, and temperature setting
        String jsonPayload = "{"
                + "\"model\": \"" + MODEL_NAME + "\","
                + "\"messages\": ["
                + "{\"role\": \"system\", \"content\": \"" + prompt.toString().replace("\"", "\\\"") + "\"}"
                + "],"
                + "\"temperature\": 0.5" // Controls randomness: lower values make output more focused/deterministic
                + "}";

        // Send the JSON payload to the API
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

        // Extract just the best sentence part from the full API response
        // The regex removes everything before "Best Sentence: " and the ** markers
        return response.toString().replaceAll(".*Best Sentence: ", "").replace("**", "").trim();
    }
}