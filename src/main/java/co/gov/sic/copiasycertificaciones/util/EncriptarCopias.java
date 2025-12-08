package co.gov.sic.copiasycertificaciones.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;

public class EncriptarCopias {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,.!?;:()[]{}@#$%^&*-_+=<>/\\|`~";

    private static long generarSemilla(String key, LocalDate date) {
        return (key + date.toString()).hashCode();
    }

    private static Map<Character, Character> generarSubstitutionMap(String key, LocalDate date) {
        List<Character> alfabetoPermitido = new ArrayList<>();
        for (char c : ALPHABET.toCharArray()) {
            alfabetoPermitido.add(c);
        }

        long semilla = generarSemilla(key, date);
        Random random = new Random(semilla);
        Collections.shuffle(alfabetoPermitido, random);

        Map<Character, Character> substitutionMap = new HashMap<>();
        for (int i = 0; i < ALPHABET.length(); i++) {
            substitutionMap.put(ALPHABET.charAt(i), alfabetoPermitido.get(i));
        }
        return substitutionMap;
    }

    public static String encrypt(String plainText, String key) {
        if (plainText == null || plainText.isEmpty() || key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Texto plano y clave no pueden ser nulos o vacíos.");
        }

        Map<Character, Character> substitutionMap = generarSubstitutionMap(key, LocalDate.now());
        StringBuilder encryptedCharResult = new StringBuilder();

        for (char c : plainText.toCharArray()) {
            if (substitutionMap.containsKey(c)) {
                encryptedCharResult.append(substitutionMap.get(c));
            } else {
                encryptedCharResult.append(c);
            }
        }
        return Base64.getEncoder().encodeToString(encryptedCharResult.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(String encryptedTextBase64, String key) {
        if (encryptedTextBase64 == null || encryptedTextBase64.isEmpty() || key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Texto cifrado y clave no pueden ser nulos o vacíos.");
        }

        Map<Character, Character> substitutionMap = generarSubstitutionMap(key, LocalDate.now());
        Map<Character, Character> reverseSubstitutionMap = new HashMap<>();
        substitutionMap.forEach((original, substituted) -> reverseSubstitutionMap.put(substituted, original));

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedTextBase64);
        String encryptedCharText = new String(decodedBytes, StandardCharsets.UTF_8);

        StringBuilder decryptedText = new StringBuilder();
        for (char c : encryptedCharText.toCharArray()) {
            if (reverseSubstitutionMap.containsKey(c)) {
                decryptedText.append(reverseSubstitutionMap.get(c));
            } else {
                decryptedText.append(c);
            }
        }
        return decryptedText.toString();
    }



    public static void main(String[] args) {
    	String originalText = "LaSicEncripta#2!";
        String encryptionKey = "WRus#2!l0It5";

        System.out.println("Fecha actual: " + LocalDate.now());
        String encrypted = encrypt(originalText, encryptionKey);
        System.out.println("Texto Original: " + originalText);
        System.out.println("Texto Encriptado: " + encrypted);

        String decryptedToday = decrypt(encrypted, encryptionKey);
        System.out.println("Texto Desencriptado (Hoy): " + decryptedToday);

        String wrongDecryption = decrypt(encrypted, "WrongKey");
        System.out.println("Desencriptado de Texto Encriptado, con clave incorrecta: " + wrongDecryption);

    }
}