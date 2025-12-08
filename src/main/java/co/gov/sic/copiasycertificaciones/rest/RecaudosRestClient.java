package co.gov.sic.copiasycertificaciones.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class RecaudosRestClient {

	protected static final Logger logger = LoggerFactory.getLogger(RecaudosRestClient.class);
	private final String baseUrl;
	private final String token;
	private final Jsonb jsonb = JsonbBuilder.create();

	public RecaudosRestClient(String baseUrl, String token) {
		this.baseUrl = baseUrl;
		this.token = token;
	}

	public RegistroRecaudoResponse registrarRecaudo(RegistroRecaudoRequest request) throws IOException, SSLException {
		return post("/recaudos/api/v1/recaudo/registrar", request, RegistroRecaudoResponse.class);
	}

	public GenerarReciboResponse generarRecibo(GenerarReciboRequest request) throws IOException, SSLException {
		return post("/recaudos/api/v1/recibocaja/generarRecibo", request, GenerarReciboResponse.class);
	}

	private <T> T post(String path, Object body, Class<T> responseType) throws IOException, SSLException {
		URL url = new URL(baseUrl + path);
		HttpURLConnection conn = null;
		
		try {
			// @SuppressWarnings("KIUWAN.SEC.JAVA.UnhandledSSLExceptionRule")
			conn = (HttpURLConnection) url.openConnection();
		} catch (SSLException ssle) {
			logger.error("Error SSL al abrir conexión: " + ssle.getMessage(), ssle);
			throw ssle;
		}
		
		try {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			String authHeader = "Bearer " + token;
			logger.info("Authorization: " + authHeader);
			conn.setRequestProperty("Authorization", authHeader);
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
				os.write(jsonb.toJson(body).getBytes(StandardCharsets.UTF_8));
			}

			int status = conn.getResponseCode();

			try (InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream()) {
				if (is == null) {
					return null;
				}
				String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
				logger.info("JSON recibido (" + status + "): " + json);
				return jsonb.fromJson(json, responseType);
			}

		} catch (SSLException ssle) {
			logger.error("Error SSL durante comunicación: " + ssle.getMessage(), ssle);
			if (conn != null) {
				conn.disconnect();
			}
			throw ssle;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}