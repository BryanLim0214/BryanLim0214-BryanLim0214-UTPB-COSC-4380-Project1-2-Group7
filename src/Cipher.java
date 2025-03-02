import java.util.ArrayList;
import java.util.Locale;

public abstract class Cipher {
    public static char[] lowercaseAlpha = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    public static char[] uppercaseAlpha = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    public static char[] numbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static char[] symbols = {'@', '#', '$', '%', '^', '&', '*', '(', ')', '{', '}', '[', ']', '-', '_', '=', '+', '<', '>', '|', '\\', '`', '~'};
    public static char[] punctuation = {'.', ',', '!', '?', '\'', '\"', ';', ':', '/'};
    public static char[] whitespace = {' ', '\t', '\n'};
    protected ArrayList<Character> alphabet;

    public Cipher() {}

    void swap(char[] alpha) {
        int a = (int)(Math.random() * alpha.length);
        int b = (int)(Math.random() * alpha.length);
        char c = alpha[a];
        alpha[a] = alpha[b];
        alpha[b] = c;
    }

    void scramble(char[] alpha) {
        for(int i = 0; i < alpha.length; i++) {
            for (int j = 0; j < alpha.length; j++) {
                swap(alpha);
            }
        }
    }

    ArrayList<Character> getPermutation() {
        ArrayList<Character> p = new ArrayList<>();
        char[] alpha = new char[alphabet.size()];
        for (int i = 0; i < alpha.length; i++) {
            alpha[i] = alphabet.get(i);
        }
        scramble(alpha);
        for (char c : alpha) {
            p.add(c);
        }
        return p;
    }

    public static char[] getAlphabet(String name) {
        return switch (name.toLowerCase()) {
            case "lowercasealpha", "lowercase", "lower" -> lowercaseAlpha;
            case "uppercasealpha", "uppercase", "upper" -> uppercaseAlpha;
            case "numbers", "nums" -> numbers;
            case "symbols", "syms", "math" -> symbols;
            case "punctuation" -> punctuation;
            case "whitespace", "space", "spaces" -> whitespace;
            default -> new char[] {};
        };
    }

    public static ArrayList<Character> concatAlphabet(ArrayList<Character> a, char[] b) {
        for (char c : b) {
            a.add(c);
        }
        return a;
    }

    public static ArrayList<Character> getAlphabet(String[] names) {
        ArrayList<Character> a = new ArrayList<>();
        for (String name : names) {
            a = concatAlphabet(a, getAlphabet(name));
        }
        return a;
    }

    boolean validate(String s) {
        for (char c : s.toCharArray()) {
            if (!alphabet.contains(c)) {
                return false;
            }
        }
        return true;
    }

    boolean validate(char c) {
        return alphabet.contains(c);
    }

    public abstract String encrypt(String plaintext);
    public abstract String decrypt(String ciphertext);
    //public abstract void crack(String ciphertext);
}