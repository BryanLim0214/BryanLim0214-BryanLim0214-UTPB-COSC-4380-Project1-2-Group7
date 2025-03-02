
# UTPB-COSC-4380-Project1-2-Group7

# Group 7: Alejandro Sotelo, Bryan Lim, Roman Huerta

## 📜 Project Overview
This project is a collection of cipher algorithms implemented in **Java**, designed to **encrypt, decrypt, and crack ciphers** using various cryptographic techniques. It includes implementations of **PolyAlphabetic Cipher**, **Columnar Transposition Cipher**, and a **Llama-based Sentence Checker** for analyzing decrypted text.

---

## 🚀 Features
- **PolyCipher**: Implements a **polyalphabetic cipher** with randomly generated keys and a character mapping matrix.
- **ColTransCipher**: Implements the **Columnar Transposition Cipher**, supporting both encryption and decryption with flexible key handling.
- **ColumnarTranspositionCracker**: A brute-force cracking tool that **attempts different key permutations** to decrypt a transposition cipher.
- **LlamaSentenceChecker**: Uses an **AI model (Llama 3.3-70B)** to analyze decrypted text and identify the **most likely meaningful sentence**.
- **Cipher (Base Class)**: Provides fundamental **alphabet and character manipulation** utilities for various ciphers.

---

## 🛠️ Installation & Usage
### Prerequisites
Ensure you have:
- **Java 11+**
- **An active internet connection** (for API-based decryption evaluation)
- **Git** (optional, for cloning the repository)

### Cloning the Repository
```sh
git clone https://github.com/BryanLim0214/UTPB-COSC-4380-Project1-2-Group7.git
cd UTPB-COSC-4380-Project1-2-Group7
```

### Running the Encryption/Decryption
Compile and execute the desired cipher file:

#### Example: Running **PolyCipher**
```sh
javac src/PolyCipher.java
java src/PolyCipher
```

#### Example: Running **Columnar Transposition Cracker**
```sh
javac src/ColumnarTranspositionCracker.java
java src/ColumnarTranspositionCracker
```

---

## 🔑 Cipher Breakdown

### **PolyCipher.java**
- Generates a **random key** or accepts a predefined one.
- Creates and stores **Beta Matrices** for encryption and decryption.
- Encrypts/decrypts text using a **matrix-based lookup system**.
- **Example Usage:**
```java
PolyCipher cipher = new PolyCipher(10);
String encrypted = cipher.encrypt("Hello World");
String decrypted = cipher.decrypt(encrypted);
```

### **ColTransCipher.java**
- Implements **Columnar Transposition Cipher** with adjustable key formats.
- Supports **ascending/descending key order**.
- Adds **optional padding** for structured encryption.
- **Example Usage:**
```java
ColTransCipher ctc = new ColTransCipher("57183", null, true, false);
String encrypted = ctc.encrypt("Hello World");
String decrypted = ctc.decrypt(encrypted);
```

### **ColumnarTranspositionCracker.java**
- Attempts to **break** the Columnar Transposition Cipher by testing different **key permutations**.
- Uses **dictionary-based scoring** to determine the best decryption result.
- **Example Usage:**
```sh
javac ColumnarTranspositionCracker.java
java ColumnarTranspositionCracker
```

### **LlamaSentenceChecker.java**
- Communicates with the **OpenRouter AI (Llama-3.3-70B-Instruct)**.
- Identifies the **most meaningful sentence** from a list of possible decryptions.
- **Example API Usage:**
```java
List<String> possibleDecryptions = Arrays.asList("Hello wrld!", "Hello world!", "H3ll0 w0r1d");
String bestSentence = LlamaSentenceChecker.findMostLikelySentence(possibleDecryptions);
```

---

## 📌 Known Issues
- **Columnar Transposition Cracker** may take a long time for **keys longer than 6 characters** due to the exponential growth of permutations.
- **LlamaSentenceChecker** requires a **valid API key** from OpenRouter.
- The **PolyCipher** encryption matrix might generate **ambiguous results** if two keys are very similar.

---

## 📜 License
This project is open-source and available under the **MIT License**.

---

## 📬 Contact
For questions or feedback, please open an **Issue** or contact **@BryanLim0214** on GitHub.

---

Would you like any modifications to the README? 😊
