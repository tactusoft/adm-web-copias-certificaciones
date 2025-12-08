package co.gov.sic.copiasycertificaciones.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Clase genérica para la generación de semillas utilizadas en procesos de
 * encriptación y aleatorización
 * 
 * @author SIC
 * @version 1.0
 */
public class GeneradorSemillas {

	private static final Logger LOGGER = Logger.getLogger(GeneradorSemillas.class.getName());

	/**
	 * Genera una semilla basada en una clave y la fecha actual
	 * 
	 * @param key Clave base para la generación
	 * @return Semilla numérica generada
	 */
	public static long generarSemilla(String key) {
		return generarSemilla(key, LocalDate.now());
	}

	/**
	 * Genera una semilla basada en una clave y una fecha específica
	 * 
	 * @param key  Clave base para la generación
	 * @param date Fecha a usar en la generación
	 * @return Semilla numérica generada
	 */
	public static long generarSemilla(String key, LocalDate date) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("La clave no puede ser nula o vacía");
		}
		if (date == null) {
			throw new IllegalArgumentException("La fecha no puede ser nula");
		}

		String combinacion = key + date.toString();
		long semilla = combinacion.hashCode();

		LOGGER.fine("Semilla generada para fecha: " + date + " = " + semilla);
		return semilla;
	}

	/**
	 * Genera una semilla basada en una clave y fecha-hora específica Útil cuando se
	 * necesita mayor granularidad temporal
	 * 
	 * @param key      Clave base para la generación
	 * @param dateTime Fecha y hora a usar en la generación
	 * @return Semilla numérica generada
	 */
	public static long generarSemilla(String key, LocalDateTime dateTime) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("La clave no puede ser nula o vacía");
		}
		if (dateTime == null) {
			throw new IllegalArgumentException("La fecha-hora no puede ser nula");
		}

		String combinacion = key + dateTime.toString();
		long semilla = combinacion.hashCode();

		LOGGER.fine("Semilla generada para fecha-hora: " + dateTime + " = " + semilla);
		return semilla;
	}

	/**
	 * Genera una semilla basada en múltiples parámetros
	 * 
	 * @param params Parámetros variables a combinar para la semilla
	 * @return Semilla numérica generada
	 */
	public static long generarSemilla(String... params) {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("Debe proporcionar al menos un parámetro");
		}

		StringBuilder combinacion = new StringBuilder();
		for (String param : params) {
			if (param != null) {
				combinacion.append(param);
			}
		}

		if (combinacion.length() == 0) {
			throw new IllegalArgumentException("Los parámetros no pueden ser todos nulos");
		}

		long semilla = combinacion.toString().hashCode();
		LOGGER.fine("Semilla generada con " + params.length + " parámetros = " + semilla);
		return semilla;
	}

	/**
	 * Genera una semilla con un formato de fecha personalizado
	 * 
	 * @param key        Clave base para la generación
	 * @param date       Fecha a usar
	 * @param dateFormat Formato de fecha deseado (ej: "yyyy-MM-dd", "yyyyMMdd")
	 * @return Semilla numérica generada
	 */
	public static long generarSemillaConFormato(String key, LocalDate date, String dateFormat) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("La clave no puede ser nula o vacía");
		}
		if (date == null) {
			throw new IllegalArgumentException("La fecha no puede ser nula");
		}
		if (dateFormat == null || dateFormat.isEmpty()) {
			throw new IllegalArgumentException("El formato de fecha no puede ser nulo o vacío");
		}

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
			String fechaFormateada = date.format(formatter);
			String combinacion = key + fechaFormateada;
			long semilla = combinacion.hashCode();

			LOGGER.fine("Semilla generada con formato " + dateFormat + " = " + semilla);
			return semilla;
		} catch (Exception e) {
			LOGGER.warning("Error con formato de fecha: " + e.getMessage() + ". Usando formato ISO por defecto");
			return generarSemilla(key, date);
		}
	}

	/**
	 * Genera una semilla positiva (útil para índices de arrays)
	 * 
	 * @param key  Clave base para la generación
	 * @param date Fecha a usar en la generación
	 * @return Semilla numérica positiva
	 */
	public static long generarSemillaPositiva(String key, LocalDate date) {
		long semilla = generarSemilla(key, date);
		return Math.abs(semilla);
	}

	/**
	 * Genera una semilla en un rango específico
	 * 
	 * @param key  Clave base para la generación
	 * @param date Fecha a usar en la generación
	 * @param min  Valor mínimo del rango (inclusivo)
	 * @param max  Valor máximo del rango (inclusivo)
	 * @return Semilla numérica dentro del rango especificado
	 */
	public static long generarSemillaEnRango(String key, LocalDate date, long min, long max) {
		if (min >= max) {
			throw new IllegalArgumentException("El valor mínimo debe ser menor que el máximo");
		}

		long semillaBase = generarSemilla(key, date);
		long rango = max - min + 1;
		long semillaEnRango = min + (Math.abs(semillaBase) % rango);

		LOGGER.fine("Semilla en rango [" + min + ", " + max + "] = " + semillaEnRango);
		return semillaEnRango;
	}

	/**
	 * Genera una semilla combinando clave, fecha y un salt adicional Útil para
	 * mayor seguridad
	 * 
	 * @param key  Clave base
	 * @param date Fecha
	 * @param salt Valor adicional para mayor entropía
	 * @return Semilla numérica generada
	 */
	public static long generarSemillaConSalt(String key, LocalDate date, String salt) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("La clave no puede ser nula o vacía");
		}
		if (date == null) {
			throw new IllegalArgumentException("La fecha no puede ser nula");
		}
		if (salt == null) {
			salt = "";
		}

		String combinacion = key + date.toString() + salt;
		long semilla = combinacion.hashCode();

		LOGGER.fine("Semilla generada con salt = " + semilla);
		return semilla;
	}
}