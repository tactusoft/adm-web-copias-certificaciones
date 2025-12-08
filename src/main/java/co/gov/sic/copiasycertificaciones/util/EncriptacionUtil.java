package co.gov.sic.copiasycertificaciones.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para encriptar/desencriptar información de pasarela de pagos
 */
public class EncriptacionUtil {

	private static final Logger LOGGER = Logger.getLogger(EncriptacionUtil.class.getName());
	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789,.!?;:()[]{}@#$%^&*-_+=<>/\\|`~";

	/**
	 * Genera el mapa de sustitución de caracteres usando GeneradorSemillas
	 */
	private static Map<Character, Character> generarSubstitutionMap(String key, LocalDate date) {
		List<Character> alfabetoPermitido = new ArrayList<>();
		for (char c : ALPHABET.toCharArray()) {
			alfabetoPermitido.add(c);
		}

		// Usar la clase genérica GeneradorSemillas
		long semilla = GeneradorSemillas.generarSemilla(key, date);
		Random random = new Random(semilla);
		Collections.shuffle(alfabetoPermitido, random);

		Map<Character, Character> substitutionMap = new HashMap<>();
		for (int i = 0; i < ALPHABET.length(); i++) {
			substitutionMap.put(ALPHABET.charAt(i), alfabetoPermitido.get(i));
		}
		return substitutionMap;
	}

	/**
	 * Encripta un texto usando la clave proporcionada
	 * 
	 * @param plainText Texto a encriptar
	 * @param key       Clave de encriptación
	 * @return Texto encriptado en Base64
	 */
	public static String encrypt(String plainText, String key) {
		if (plainText == null || plainText.isEmpty()) {
			LOGGER.warning("Intento de encriptar texto nulo o vacío");
			return "";
		}

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("La clave de encriptación no puede ser nula o vacía");
		}

		try {
			Map<Character, Character> substitutionMap = generarSubstitutionMap(key, LocalDate.now());
			StringBuilder encryptedCharResult = new StringBuilder();

			for (char c : plainText.toCharArray()) {
				if (substitutionMap.containsKey(c)) {
					encryptedCharResult.append(substitutionMap.get(c));
				} else {
					encryptedCharResult.append(c);
				}
			}

			String encrypted = Base64.getEncoder()
					.encodeToString(encryptedCharResult.toString().getBytes(StandardCharsets.UTF_8));

			LOGGER.fine("Texto encriptado exitosamente");
			return encrypted;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error al encriptar texto", e);
			throw new RuntimeException("Error en el proceso de encriptación", e);
		}
	}

	/**
	 * Desencripta un texto encriptado en Base64
	 * 
	 * @param encryptedTextBase64 Texto encriptado en Base64
	 * @param key                 Clave de encriptación
	 * @return Texto desencriptado
	 */
	public static String decrypt(String encryptedTextBase64, String key) {
		if (encryptedTextBase64 == null || encryptedTextBase64.isEmpty()) {
			LOGGER.warning("Intento de desencriptar texto nulo o vacío");
			return "";
		}

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("La clave de desencriptación no puede ser nula o vacía");
		}

		try {
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

			LOGGER.fine("Texto desencriptado exitosamente");
			return decryptedText.toString();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error al desencriptar texto", e);
			throw new RuntimeException("Error en el proceso de desencriptación", e);
		}
	}

	/**
	 * Encripta el Invoice para el parámetro RefPasarela
	 * 
	 * @param invoice ID de la factura
	 * @return Invoice encriptado
	 */
	public static String encriptarRefPasarela() {
		try {
			return encrypt(Constantes.ORIGINAL_TEXT, Constantes.ENCRYPTION_KEY);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error encriptando RefPasarela", e);
			return "";
		}
	}

	/**
	 * Desencripta el parámetro RefPasarela
	 * 
	 * @param refPasarelaEncriptado Valor encriptado de RefPasarela
	 * @return Invoice desencriptado
	 */
	public static String desencriptarRefPasarela(String refPasarelaEncriptado) {
		if (refPasarelaEncriptado == null || refPasarelaEncriptado.isEmpty()) {
			LOGGER.warning("RefPasarela nulo o vacío para desencriptación");
			return "";
		}

		try {
			return decrypt(refPasarelaEncriptado, Constantes.ENCRYPTION_KEY);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error desencriptando RefPasarela", e);
			return "";
		}
	}
}