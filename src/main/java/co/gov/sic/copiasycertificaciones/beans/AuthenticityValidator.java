package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticityValidator {

	protected static final Logger loggerStatic = LoggerFactory.getLogger(BeanBase.class);

	public static boolean validateDocument(String originalDocumentPath, String storedHash) {
		try {
			// Leer el contenido del documento original
			byte[] originalDocumentContent = readFile(originalDocumentPath);

			// Calcular el hash del contenido del documento original
			String calculatedHash = calculateHash(originalDocumentContent);

			// Comparar el hash calculado con el hash almacenado
			return storedHash.equals(calculatedHash);
		} catch (IOException | NoSuchAlgorithmException e) {
			loggerStatic.error("validateDocument", e);
			return false;
		}
	}

	private static byte[] readFile(String filePath) throws IOException {
		try (FileInputStream inputStream = new FileInputStream(filePath);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			return outputStream.toByteArray();
		}
	}

	private static String calculateHash(byte[] data) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = md.digest(data);

		// Convertir el hash en una representación hexadecimal
		StringBuilder hexHash = new StringBuilder();
		for (byte b : hashBytes) {
			hexHash.append(String.format("%02x", b));
		}

		return hexHash.toString();
	}
}
