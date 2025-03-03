import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A tool to crack columnar transposition ciphers by testing different key permutations
 * and evaluating the resulting plaintext using dictionary and language model scoring.
 *
 * NOTE: This tool works best for key sizes 6 and under due to the exponential growth
 * of permutations with larger key sizes. Key sizes above 6 may result in extremely
 * long processing times.
 */
public class ColumnarTranspositionCracker {
    // Set to store loaded dictionary words for evaluating decryption quality
    private static Set<String> dictionaryWords = new HashSet<>();

    /**
     * Main method that handles user input and orchestrates the encryption/decryption process
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Display program header
        System.out.println("=======================================");
        System.out.println("  Columnar Transposition Cracker Tool  ");
        System.out.println("=======================================");
        System.out.println("  NOTE: This tool works best for key sizes 6 and under.");
        System.out.println("  Works best with ENGLISH SENTENCES");
        System.out.println("  Larger key sizes will result in extremely long");
        System.out.println("  processing times due to the number of permutations.");
        System.out.println("=======================================");

        // Collect user input for encryption
        System.out.print("Enter the plaintext to encrypt: ");
        String plaintext = scanner.nextLine();

        System.out.print("Enter the numeric key (e.g., 57183): ");
        String keyInput = scanner.nextLine();

        // Create cipher object and encrypt the plaintext
        ColTransCipher cipher = new ColTransCipher(keyInput, null, true, false);
        String cipherText = cipher.encrypt(plaintext);

        // Display the encrypted text
        System.out.println("\n=======================================");
        System.out.println("  Encrypted Ciphertext:");
        System.out.println("=======================================");
        System.out.println(cipherText);

        System.out.println("\nStarting decryption process...\n");

        // Get maximum key size from user (with validation)
        int maxKeySize = 6; // Default value
        while (true) {
            System.out.println("Enter the maximum key size to try (2-6 recommended): ");
            try {
                maxKeySize = Integer.parseInt(scanner.nextLine().trim());
                if (maxKeySize >= 2) {
                    break;
                } else {
                    System.out.println("Key size must be at least 2. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        // Display information about the decryption process
        System.out.println("=======================================");
        System.out.println("  Decryption Process Initiated ");
        System.out.println("=======================================");
        System.out.println("  Note: This decryption method is best suited for");
        System.out.println("  long English sentences. Spaces are optional, and");
        System.out.println("  omitting spaces may yield better results.");
        System.out.println("=======================================");

        try {
            // Load dictionary for scoring the decryption attempts
            loadDictionary("dict.txt");
        } catch (IOException e) {
            System.err.println("Error loading dictionary file: " + e.getMessage());
            return; // Exit if dictionary loading fails
        }

        // Countdown before starting the decryption process
        System.out.println("\nDecryption will begin in:");
        for (int i = 2; i > 0; i--) {
            System.out.print(i + "...\n");
            try {
                TimeUnit.SECONDS.sleep(1); // Wait for 1 second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Starting decryption now...\n");

        // Begin the decryption process with user-specified max key size
        decrypt(cipherText, maxKeySize);

        // Close scanner to prevent resource leaks
        scanner.close();
    }

    /**
     * Main decryption method that tries different key sizes and permutations
     * and evaluates the quality of each decryption attempt
     *
     * @param cipherText The encrypted text to be decrypted
     * @param maxKeySize The maximum key size to try (specified by user)
     */
    public static void decrypt(String cipherText, int maxKeySize) {
        // Lists to store results for all decryption attempts
        List<String> allDecryptedWords = new ArrayList<>();
        List<String> formattedDecryptedWords = new ArrayList<>();
        List<Double> dictionaryScores = new ArrayList<>();
        List<List<Integer>> keyMappings = new ArrayList<>();

        // Minimum key size is always 2
        final int MIN_KEY_SIZE = 2;

        // Try keys from the minimum size up to the user-specified maximum size
        for (int keySize = MIN_KEY_SIZE; keySize <= maxKeySize; keySize++) {
            System.out.println("\n=======================================");
            System.out.printf("Trying key size: %d\n", keySize);
            System.out.println("=======================================");

            // Create a template key of the current size (e.g., [1,2,3,4] for size 4)
            List<Integer> keyTemplate = new ArrayList<>();
            for (int i = 1; i <= keySize; i++) {
                keyTemplate.add(i);
            }

            // Generate all possible permutations of the key template
            List<List<Integer>> allPermutations = generatePermutations(keyTemplate);
            System.out.printf("Testing %d possible key permutations\n", allPermutations.size());

            // Try each permutation as a potential key
            for (List<Integer> key : allPermutations) {
                System.out.printf("Testing key: %s\n", key);

                // Create a grid representation for this key
                List<List<Character>> grid = createDecryptionGrid(cipherText, key);
                printColumnHeaders(key);
                printDecryptionGrid(grid);

                // Reconstruct potential plaintext from the grid
                String possiblePlaintext = reconstructPlaintext(grid);
                String formattedPlaintext = possiblePlaintext.replaceAll("\\s+", ""); // Remove spaces for comparison

                // Score the quality of decryption using dictionary matching
                double dictionaryScore = scoreDictionaryWords(possiblePlaintext);

                System.out.printf("Dictionary Score: %.4f\n", dictionaryScore);
                System.out.printf("Decrypted Text: %s\n\n", possiblePlaintext);

                // Store all results for later comparison
                allDecryptedWords.add(possiblePlaintext);
                formattedDecryptedWords.add(formattedPlaintext);
                dictionaryScores.add(dictionaryScore);
                keyMappings.add(new ArrayList<>(key));
            }
        }

        // Find the best decryption based on dictionary scoring
        int bestDictionaryIndex = dictionaryScores.indexOf(Collections.max(dictionaryScores));
        String bestDictionaryDecryption = allDecryptedWords.get(bestDictionaryIndex);
        List<Integer> bestDictionaryKey = keyMappings.get(bestDictionaryIndex);

        // Display the best dictionary-based result
        System.out.println("===============================================");
        System.out.println("=== Best Decryption Based on Dictionary Scoring ===");
        System.out.println("===============================================");
        System.out.println("Key: " + bestDictionaryKey);
        System.out.println("Decrypted Text: " + bestDictionaryDecryption);
        System.out.printf("Dictionary Score: %.4f\n", dictionaryScores.get(bestDictionaryIndex));

        // Use language model to evaluate all decryption attempts
        System.out.println("\n===============================================");
        System.out.println("=== Evaluating Decrypted Words Using LlamaSentenceChecker ===");
        System.out.println("===============================================");

        // Create a map of decrypted texts to their corresponding keys
        Map<String, String> textsWithKeys = new HashMap<>();
        for (int i = 0; i < allDecryptedWords.size(); i++) {
            textsWithKeys.put(allDecryptedWords.get(i), keyMappings.get(i).toString());
        }

        String llamaResponse;
        try {
            // Use LLM to evaluate the most likely correct sentence, now including keys
            llamaResponse = LlamaSentenceChecker.findMostLikelySentenceWithKey(textsWithKeys);
        } catch (IOException e) {
            System.err.println("Error communicating with Llama API: " + e.getMessage());
            llamaResponse = "Error retrieving response"; // Fallback in case of API failure
        }

        // Display the LLM's result
        System.out.println("\n===============================================");
        System.out.println("=== Best Decryption Based on LlamaSentenceChecker ===");
        System.out.println("===============================================");

        // Parse the response to extract both the sentence and key
        String bestSentence = "";
        String bestKey = "";

        // Extract the best sentence and key from the LLM response
        if (llamaResponse.contains("(Key:") && llamaResponse.contains(")")) {
            bestSentence = llamaResponse.substring(0, llamaResponse.indexOf("(Key:")).trim();
            bestKey = llamaResponse.substring(
                    llamaResponse.indexOf("(Key:") + 5,
                    llamaResponse.indexOf(")", llamaResponse.indexOf("(Key:"))
            ).trim();

            System.out.println("Best Possible Decryption: " + bestSentence);
            System.out.println("Key Associated with Best Decryption: " + bestKey);

            // Find the full decryption that matches this sentence
            for (int i = 0; i < allDecryptedWords.size(); i++) {
                if (allDecryptedWords.get(i).contains(bestSentence)) {
                    System.out.println("Full Decrypted Text: " + allDecryptedWords.get(i));
                    System.out.printf("Dictionary Score: %.4f\n", dictionaryScores.get(i));
                    break;
                }
            }
        } else {
            // Fallback if response format is unexpected
            System.out.println("Unexpected response format from LLM: " + llamaResponse);
        }
    }

    /**
     * Score plaintext based on dictionary word matches
     *
     * @param text The text to score
     * @return A score indicating how many dictionary words were found
     */
    private static double scoreDictionaryWords(String text) {
        double score = 0.0;
        Set<String> countedWords = new HashSet<>(); // Track words to avoid double counting

        // For each position in the text, try to find dictionary words
        for (int start = 0; start < text.length(); start++) {
            String bestWord = "";
            double bestScore = 0.0;

            // Check word lengths from 3 to 10 characters
            for (int end = start + 3; end <= Math.min(text.length(), start + 10); end++) {
                String word = text.substring(start, end);
                if (dictionaryWords.contains(word)) {
                    // Longer words get higher scores
                    double wordScore = word.length() * 0.5;
                    if (wordScore > bestScore) {
                        bestScore = wordScore;
                        bestWord = word;
                    }
                }
            }

            // Add the best word found to the total score
            if (!bestWord.isEmpty() && !countedWords.contains(bestWord)) {
                score += bestScore;
                countedWords.add(bestWord);
            }
        }
        return score;
    }

    /**
     * Generate all possible permutations of a given key
     *
     * @param key The key to permute
     * @return List of all possible permutations
     */
    private static List<List<Integer>> generatePermutations(List<Integer> key) {
        List<List<Integer>> result = new ArrayList<>();
        permuteHelper(key, 0, result);
        return result;
    }

    /**
     * Helper method for the permutation generator
     * Uses recursive approach to create all permutations
     */
    private static void permuteHelper(List<Integer> key, int index, List<List<Integer>> result) {
        if (index == key.size() - 1) {
            // Base case: add current permutation to results
            result.add(new ArrayList<>(key));
            return;
        }

        // Recursive case: swap each element with the current position
        for (int i = index; i < key.size(); i++) {
            Collections.swap(key, i, index); // Swap elements
            permuteHelper(key, index + 1, result); // Recurse with next position
            Collections.swap(key, i, index); // Swap back (backtrack)
        }
    }

    /**
     * Create a grid representation for decryption based on the key
     *
     * @param text The ciphertext
     * @param key The key to test
     * @return A 2D grid representation of the decrypted text
     */
    private static List<List<Character>> createDecryptionGrid(String text, List<Integer> key) {
        int keySize = key.size();
        int rows = (int) Math.ceil((double) text.length() / keySize);

        // Initialize grid with spaces
        List<List<Character>> grid = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            grid.add(new ArrayList<>(Collections.nCopies(keySize, ' ')));
        }

        // Calculate column heights (last row may be incomplete)
        int[] colHeights = new int[keySize];
        int fullCols = text.length() % keySize;
        for (int i = 0; i < keySize; i++) {
            colHeights[i] = (text.length() / keySize) + (i < fullCols ? 1 : 0);
        }

        // Create a sorted version of the key for column mapping
        List<Integer> sortedKey = new ArrayList<>(key);
        Collections.sort(sortedKey);

        // Fill the grid based on the key ordering
        int index = 0;
        for (int sortedCol : sortedKey) {
            int originalColIndex = key.indexOf(sortedCol);
            for (int row = 0; row < colHeights[originalColIndex]; row++) {
                if (index < text.length()) {
                    grid.get(row).set(originalColIndex, text.charAt(index++));
                }
            }
        }
        return grid;
    }

    /**
     * Reconstruct plaintext from a grid representation
     *
     * @param grid The decryption grid
     * @return The reconstructed plaintext
     */
    private static String reconstructPlaintext(List<List<Character>> grid) {
        StringBuilder plaintext = new StringBuilder();
        // Read grid row by row to reconstruct plaintext
        for (List<Character> row : grid) {
            for (char c : row) {
                if (c != ' ') {
                    plaintext.append(c);
                }
            }
        }
        return plaintext.toString();
    }

    /**
     * Print column headers for visualization
     */
    private static void printColumnHeaders(List<Integer> key) {
        System.out.println("\n=== Column Order (Key): " + key + " ===");
    }
    /**
     * Print the decryption grid for visualization
     */
    private static void printDecryptionGrid(List<List<Character>> grid) {
        System.out.println("=== Decryption Grid ===");
        for (List<Character> row : grid) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    /**
     * Load dictionary words from a file for scoring decryption attempts
     *
     * @param filename The dictionary file path
     * @throws IOException If file reading fails
     */
    private static void loadDictionary(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Only add words that are at least 3 characters long
                if (line.trim().length() >= 3) {
                    dictionaryWords.add(line.trim().toLowerCase());
                }
            }
        }
    }
}
   
