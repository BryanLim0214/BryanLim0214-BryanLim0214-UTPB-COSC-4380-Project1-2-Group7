import java.util.HashMap;

public class PolyCipher {
    private String key;
    private char[][] square;
    private String alphabetStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,!?'\";:/";
    private HashMap<Character, Integer> charToIndex;

    // HashMap to store Beta matrices
    private static HashMap<String, char[][]> BetaStorage = new HashMap<>();

    // 🔹 Constructor that generates a random key
    public PolyCipher(int keyLength) {
        this.key = generateRandomKey(keyLength);
        initializeCipher();
    }

    // 🔹 Constructor that accepts a predefined key (to retrieve stored Beta)
    public PolyCipher(String key) {
        this.key = key;
        initializeCipher();
    }

    // 🔹 Common initialization method
    private void initializeCipher() {
        square = new char[alphabetStr.length()][alphabetStr.length()];
        charToIndex = new HashMap<>();

        // Map each character to its index
        for (int i = 0; i < alphabetStr.length(); i++) {
            charToIndex.put(alphabetStr.charAt(i), i);
        }

        // Check if Beta matrix already exists for the given key
        if (BetaStorage.containsKey(key)) {
            square = BetaStorage.get(key);
            System.out.println("🔹 Retrieved stored Beta matrix for key: " + key);
        } else {
            generateSquare();
            scrambleSquare();
            BetaStorage.put(key, square);
            System.out.println("✅ Generated and stored new Beta matrix for key: " + key);
        }
    }

    // 🔹 Generate a random key
    private String generateRandomKey(int length) {
        StringBuilder randomKey = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randIndex = Rand.randInt(alphabetStr.length());
            randomKey.append(alphabetStr.charAt(randIndex));
        }
        return randomKey.toString();
    }

    public String getKey() {
        return key;
    }

    // 🔹 Updated getBeta() method to print the key and Beta matrix
    public void getBeta() {
        System.out.println("\n🔹 Beta Matrix for Key: " + key);

        if (!BetaStorage.containsKey(key)) {
            System.out.println("❌ No Beta matrix found for key: " + key);
            return;
        }

        char[][] matrix = BetaStorage.get(key);

        // Print the matrix
        for (char[] row : matrix) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    // 🔹 Encrypt method
    public String encrypt(String plaintext) {
        StringBuilder ciphertext = new StringBuilder();

        System.out.println("\n🔐 Encryption Process:");
        System.out.println("-------------------");
        System.out.println("Plaintext:  " + plaintext);
        System.out.println("Key Used:   " + key);

        for (int i = 0; i < plaintext.length(); i++) {
            char plainChar = plaintext.charAt(i);
            char keyChar = key.charAt(i % key.length());

            Integer row = charToIndex.get(keyChar);
            Integer col = charToIndex.get(plainChar);

            if (row == null || col == null) {
                ciphertext.append(plainChar);
            } else {
                char encryptedChar = square[row][col];
                ciphertext.append(encryptedChar);
                System.out.printf("(%d, %d) -> %c  (%c + %c)\n", row, col, encryptedChar, keyChar, plainChar);
            }
        }

        return ciphertext.toString();
    }

    // 🔹 Decrypt method
    public String decrypt(String ciphertext) {
        StringBuilder plaintext = new StringBuilder();

        System.out.println("\n🔓 Decryption Process:");
        System.out.println("-------------------");

        for (int i = 0; i < ciphertext.length(); i++) {
            char cipherChar = ciphertext.charAt(i);
            char keyChar = key.charAt(i % key.length());

            Integer row = charToIndex.get(keyChar);
            if (row == null) {
                plaintext.append(cipherChar);
                continue;
            }

            int col = -1;
            for (int j = 0; j < square[row].length; j++) {
                if (square[row][j] == cipherChar) {
                    col = j;
                    break;
                }
            }

            if (col != -1) {
                char decryptedChar = alphabetStr.charAt(col);
                plaintext.append(decryptedChar);
                System.out.printf("(%d, %d) -> %c  (%c -> %c)\n", row, col, decryptedChar, cipherChar, decryptedChar);
            } else {
                plaintext.append(cipherChar);
            }
        }

        return plaintext.toString();
    }

    // 🔹 Generate the initial square
    public void generateSquare() {
        for (int row = 0; row < alphabetStr.length(); row++) {
            for (int col = 0; col < alphabetStr.length(); col++) {
                square[row][col] = alphabetStr.charAt((col + row) % alphabetStr.length());
            }
        }
    }

    // 🔹 Scramble the square
    public void scrambleSquare() {
        for (int row = 0; row < square.length * 10; row++) {
            int a = Rand.randInt(square.length);
            int b = Rand.randInt(square.length);
            char[] swap = square[a];
            square[a] = square[b];
            square[b] = swap;
        }
        for (int col = 0; col < square.length * 10; col++) {
            int a = Rand.randInt(square.length);
            int b = Rand.randInt(square.length);
            for (int row = 0; row < square.length; row++) {
                char c = square[row][a];
                square[row][a] = square[row][b];
                square[row][b] = c;
            }
        }
    }

    public static void main(String[] args) {
        // Define plaintext
        String plaintext = "Old friend, I hope this missive finds you well. Though, as you are now essentially immortal, I would guess that wellness on your part is something of a given. I realize that you are probably still angry. That is pleasant to know. Much as your perpetual health, I have come to rely upon your dissatisfaction with me. It is one of the Cosmere's great constants, I should think. Let me first assure you that the element is quite safe. I have found a good home for it. I protect its safety like I protect my own skin, you might say. You do not agree with my quest. I understand that, so much as it is possible to understand someone with whom I disagree so completely. Might I be quite frank? Before, you asked why I was so concerned. It is for the following reason: Ati was once a kind and generous man, and you saw what became of him. Rayse, on the other hand, was among the most loathsome, crafty, and dangerous individuals I had ever met. He holds the most frightening and terrible of all the Shards. Ponder on that for a time, you old reptile, and tell me if your insistence on nonintervention holds firm. Because I assure you, Rayse will not be similarly inhibited. One need only look at the aftermath of his brief visit to Sel to see proof of what I say. In case you have turned a blind eye to that disaster, know that Aona and Skai are both dead, and that which they held has been Splintered. Presumably to prevent anyone from rising up to challenge Rayse. You have accused me of arrogance in my quest. You have accused me of perpetuating my grudge against Rayse and Bavadin. Both accusations are true. Neither point makes the things I have written to you untrue. I am being chased. Your friends of the Seventeenth Shard, I suspect. I believe they're still lost, following a false trail I left for them. They'll be happier that way. I doubt they have any inkling what to do with me should they actually catch me. If anything I have said makes a glimmer of sense to you, I trust that you'll call them off. Or maybe you could astound me and ask them to do something productive for once. For I have never been dedicated to a more important purpose, and the very pillars of the sky will shake with the results of our war here. I ask again. Support me. Do not stand aside and let disaster consume more lives. I've never begged you for something before, old friend. I do so now.";

        //  Step 1: Generate a cipher with a RANDOM key
        int keyLength = plaintext.length();
        PolyCipher cipher1 = new PolyCipher(keyLength);
        String storedKey = cipher1.getKey();  // Save the generated key
        cipher1.getBeta(); // Print key and stored Beta matrix

        //  Step 2: Encrypt using the first cipher
        String encrypted = cipher1.encrypt(plaintext);

        //  Step 3: Create a new cipher using the SAME key (retrieves stored Beta)
        PolyCipher cipher2 = new PolyCipher(storedKey);
        cipher2.getBeta(); // Verify it's the same Beta matrix

        //  Step 4: Decrypt using the second cipher
        String decrypted = cipher2.decrypt(encrypted);

        //  Step 5: Print results
        System.out.println("\nFinal Results:");
        System.out.println("Stored Key: " + storedKey);
        System.out.println("Plaintext:  " + plaintext);
        System.out.println("Encrypted:  " + encrypted);
        System.out.println("Decrypted:  " + decrypted);
    }
}
