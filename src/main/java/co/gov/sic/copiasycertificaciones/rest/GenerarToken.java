package co.gov.sic.copiasycertificaciones.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.primefaces.shaded.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.beans.BeanBase;
import co.gov.sic.copiasycertificaciones.util.Constantes;

public class GenerarToken {

	private static final Logger logger = LoggerFactory.getLogger(BeanBase.class);
	private static final int MAX_RETRIES = 3;
	private static final int CONNECT_TIMEOUT_MS = 15000;
	private static final int READ_TIMEOUT_MS = 30000;

	public static String obtenerToken() throws IOException, SSLException {
		return obtenerTokenConReintentos(MAX_RETRIES);
	}

	private static String obtenerTokenConReintentos(int maxRetries) throws IOException, SSLException {
		Exception lastException = null;

		for (int intento = 1; intento <= maxRetries; intento++) {
			try {
				logger.info("Intento " + intento + " de " + maxRetries + " para obtener token");
				return ejecutarSolicitudToken();
			} catch (SSLException e) {
				logger.error("Error SSL en intento " + intento + ": " + e.getMessage(), e);
				throw e;
			} catch (IOException e) {
				lastException = e;
				logger.error("Error IOException en intento " + intento + ": " + e.getMessage(), e);

				if (intento < maxRetries) {
					try {
						Thread.sleep(1000 * intento);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new IOException("Interrumpido durante reintento", ie);
					}
				}
			} catch (Exception e) {
				lastException = e;
				logger.error("Error en intento " + intento + ": " + e.getMessage(), e);

				if (intento < maxRetries) {
					try {
						Thread.sleep(1000 * intento);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						throw new IOException("Interrumpido durante reintento", ie);
					}
				}
			}
		}

		throw new IOException("Error después de " + maxRetries + " intentos: "
				+ (lastException != null ? lastException.getMessage() : "Error desconocido"), lastException);
	}

	private static String ejecutarSolicitudToken() throws IOException, SSLException {
		String urlString = Constantes.URL_SSO_TOKEN;

		if (urlString == null || urlString.trim().isEmpty()) {
			throw new IllegalArgumentException("URL_SSO_TOKEN no puede estar vacía");
		}

		if (Constantes.RECAUDOS_USERNAME == null || Constantes.RECAUDOS_PASSWORD == null) {
			throw new IllegalArgumentException("Credenciales no pueden estar vacías");
		}

		logger.info("URL: " + urlString);
		logger.info("Username: " + Constantes.RECAUDOS_USERNAME);

		Map<String, String> params = Map.of("client_id", "recaudos-front", "grant_type", "password", "username",
				Constantes.RECAUDOS_USERNAME, "password", Constantes.RECAUDOS_PASSWORD);

		String formData = params.entrySet().stream()
				.map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
						+ URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
				.reduce((a, b) -> a + "&" + b).orElse("");

		logger.info("Form data length: " + formData.length());

		URL url = new URL(urlString);
		HttpURLConnection connection = null;
		
		try {
			// @SuppressWarnings("KIUWAN.SEC.JAVA.UnhandledSSLExceptionRule")
			connection = (HttpURLConnection) url.openConnection();
		} catch (SSLException ssle) {
			logger.error("Error SSL al abrir conexión: " + ssle.getMessage(), ssle);
			throw ssle;
		}

		try {
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("User-Agent", "Java-HttpURLConnection/1.0");
			connection.setRequestProperty("Accept-Charset", "UTF-8");

			connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			connection.setReadTimeout(READ_TIMEOUT_MS);

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);

			try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
				wr.write(formData.getBytes(StandardCharsets.UTF_8));
				wr.flush();
			}

			int statusCode = connection.getResponseCode();
			logger.info("Status Code: " + statusCode);

			String responseBody;
			if (statusCode >= 200 && statusCode < 300) {
				responseBody = readInputStream(connection.getInputStream());
			} else {
				responseBody = readInputStream(connection.getErrorStream());
				logger.warn("Response Body: " + responseBody);
			}

			return procesarRespuesta(statusCode, responseBody);

		} catch (SSLException ssle) {
			logger.error("Error SSL durante comunicación: " + ssle.getMessage(), ssle);
			if (connection != null) {
				connection.disconnect();
			}
			throw ssle;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private static String readInputStream(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return "";
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			return response.toString();
		}
	}

	private static String procesarRespuesta(int statusCode, String responseBody) throws IOException {
		switch (statusCode) {
		case 200:
			try {
				JSONObject json = new JSONObject(responseBody);
				if (!json.has("access_token")) {
					throw new IOException("Token no encontrado en la respuesta");
				}
				String token = json.getString("access_token");
				logger.info("Token obtenido exitosamente");
				return token;
			} catch (Exception e) {
				throw new IOException("Error parseando respuesta JSON: " + e.getMessage(), e);
			}

		case 400:
			throw new IOException("Solicitud inválida (400): " + responseBody);

		case 401:
			throw new IOException("No autorizado (401): revisa usuario o contraseña. Respuesta: " + responseBody);

		case 403:
			throw new IOException(
					"Prohibido (403): posible error en permisos del client_id. Respuesta: " + responseBody);

		case 404:
			throw new IOException("Endpoint no encontrado (404): verifica la URL");

		case 500:
			throw new IOException("Error interno del servidor (500): " + responseBody);

		case 502:
		case 503:
		case 504:
			throw new IOException("Servidor no disponible (" + statusCode + "): " + responseBody);

		default:
			throw new IOException("Error HTTP " + statusCode + ": " + responseBody);
		}
	}
}