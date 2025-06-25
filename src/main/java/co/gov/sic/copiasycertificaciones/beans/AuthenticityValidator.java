package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticityValidator {

	public static boolean validateDocument(String originalDocumentPath, String storedHash) {
		try {
			// Leer el contenido del documento original
			byte[] originalDocumentContent = readFile(originalDocumentPath);

			// Calcular el hash del contenido del documento original
			String calculatedHash = calculateHash(originalDocumentContent);

			// Comparar el hash calculado con el hash almacenado
			return storedHash.equals(calculatedHash);
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static byte[] readFile(String filePath) throws IOException {
		FileInputStream inputStream = new FileInputStream(filePath);
		byte[] buffer = new byte[1024];
		int bytesRead;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}

		inputStream.close();
		return outputStream.toByteArray();
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

	public static void main(String[] args) {
		String originalDocumentPath = "/Users/carlossarmiento/Developer/SIC/copias/documentos/SL/Copias/PRUE23/23-000716/c0p14_23-000716- -00000-000.PDF";
		String storedHash = "hash_almacenado_de_la_copia_autentica";

		boolean isValid = validateDocument(originalDocumentPath, storedHash);

		if (isValid) {
			System.out.println("La copia es auténtica.");
		} else {
			System.out.println("La copia no es auténtica.");
		}
	}
}
