package co.gov.sic.copiasycertificaciones.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sic.ws.interop.entities.radicacion.Radicacion;

public class Functions {

	private static String normalizeFileName(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
		}

		fileName = fileName.trim();
		fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
		fileName = fileName.replaceAll("\\.\\.", "");
		fileName = fileName.replaceAll("^\\.", "");

		if (fileName.contains("/") || fileName.contains("\\")) {
			throw new IllegalArgumentException("El nombre del archivo contiene caracteres no permitidos");
		}

		if (fileName.length() > 255) {
			fileName = fileName.substring(0, 255);
		}

		return fileName;
	}

	public static String saveFile(Radicacion radi, ByteArrayOutputStream fileContent, String inputFileName)
			throws Exception {
		String normalizedFileName = normalizeFileName(inputFileName);

		String num_radi = "";
		if (String.valueOf(radi.getNumero()).length() < 5) {
			num_radi = String.valueOf(radi.getAnio()) + "-00" + String.valueOf(radi.getNumero());
		} else if (String.valueOf(radi.getNumero()).length() < 6) {
			num_radi = String.valueOf(radi.getAnio()) + "-0" + String.valueOf(radi.getNumero());
		}
		String fileName = String.format("%s_%s", num_radi, normalizedFileName);
		return saveFile(radi.getAnio(), radi.getNumero(), fileName, fileContent);
	}

	public static String saveFile(Radicacion radi, ByteArrayOutputStream fileContent) throws Exception {
		String fileName = getRadicacionileName(radi);
		return saveFile(radi.getAnio(), radi.getNumero(), fileName, fileContent);
	}

	public static String getRadicacionileName(Radicacion radi) {
		return String.format("%s.%s", radi.getFullNumeroRadicacion(), Constantes.PDF_EXTENSION);
	}

	public static Path getRadicacionFullPathFileName(Radicacion radi) throws Exception {
		String fileName = getRadicacionileName(radi);
		String directory = getRadicacionFolderPath(radi);
		Path targetLocation = Paths.get(directory).normalize();
		targetLocation = targetLocation.resolve(fileName).normalize();

		if (!targetLocation.startsWith(Paths.get(directory).normalize())) {
			throw new SecurityException("Intento de path traversal detectado");
		}

		return targetLocation;
	}

	public static String saveFile(Radicacion radi, String fileName, ByteArrayOutputStream fileContent)
			throws Exception {
		String normalizedFileName = normalizeFileName(fileName);
		return saveFile(radi.getAnio(), radi.getNumero(), normalizedFileName, fileContent);
	}

	public static String saveFile(Radicacion radi, String fileName, byte[] fileBytes) throws Exception {
		if (fileBytes != null) {
			String normalizedFileName = normalizeFileName(fileName);
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream(fileBytes.length)) {
				baos.write(fileBytes, 0, fileBytes.length);
				return saveFile(radi.getAnio(), radi.getNumero(), normalizedFileName, baos);
			}
		}
		return null;
	}

	public static String getRadicacionFolderPath(Radicacion radi) {
		return getRadicacionFolderPath(radi.getAnio(), radi.getNumero());
	}

	public static String getRadicacionFolderPath(short ano_radi, int nume_radi) {
		/*if (ano_radi < 1900 || ano_radi > 2100) {
			throw new IllegalArgumentException("Año de radicación inválido");
		}*/
		if (nume_radi < 0) {
			throw new IllegalArgumentException("Número de radicación inválido");
		}

		String directory = String.format("%s%02d/%02d-%06d/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, ano_radi,
				ano_radi, nume_radi);
		return directory;
	}

	public static String getAdjuntosFolderPath(long idenPers) {
		if (idenPers < 0) {
			throw new IllegalArgumentException("ID de persona inválido");
		}

		String directory = String.format("%s/temp/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, idenPers);
		return directory;
	}

	public static String saveFile(short ano_radi, int nume_radi, String fileName, ByteArrayOutputStream fileContent)
			throws Exception {
		String normalizedFileName = normalizeFileName(fileName);
		String directory = getRadicacionFolderPath(ano_radi, nume_radi);
		Path basePath = Paths.get(directory).normalize();

		if (!Files.exists(basePath)) {
			Files.createDirectories(basePath);
		}

		Path targetLocation = basePath.resolve(normalizedFileName).normalize();

		if (!targetLocation.startsWith(basePath)) {
			throw new SecurityException("Intento de path traversal detectado");
		}

		String fullPath = targetLocation.toString();

		try (OutputStream outputStream = new FileOutputStream(fullPath)) {
			fileContent.writeTo(outputStream);
		}

		return fullPath;
	}
}