package co.gov.sic.copiasycertificaciones.util;

import co.gov.sic.copiasycertificaciones.enums.TipoAmbienteEnum;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sic.ws.interop.entities.Adjunto;
import sic.ws.interop.entities.radicacion.Radicacion;

public class MailService {

	protected static final Logger logger = LoggerFactory.getLogger(MailService.class);
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final int MAX_SUBJECT_LENGTH = 255;

	private static String normalizeEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("El email no puede estar vacío");
		}

		email = email.trim().toLowerCase();

		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new IllegalArgumentException("Formato de email inválido: " + email);
		}

		if (email.contains("\n") || email.contains("\r")) {
			throw new IllegalArgumentException("El email contiene caracteres no permitidos");
		}

		return email;
	}

	private static String normalizeSubject(String subject) {
		if (subject == null) {
			return "";
		}

		subject = subject.replaceAll("[\r\n]", " ");
		subject = subject.trim();

		if (subject.length() > MAX_SUBJECT_LENGTH) {
			subject = subject.substring(0, MAX_SUBJECT_LENGTH);
		}

		return subject;
	}

	private static String normalizeFilePath(String filePath) {
		if (filePath == null || filePath.trim().isEmpty()) {
			throw new IllegalArgumentException("La ruta del archivo no puede estar vacía");
		}

		filePath = filePath.trim();

		if (filePath.contains("..") || filePath.contains("~") || filePath.contains("\\..")
				|| filePath.contains("/..")) {
			throw new SecurityException("Path traversal detectado: " + filePath);
		}

		String normalizedPath = filePath.replace("\\", "/");
		String basePathStr = Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS.replace("\\", "/");

		if (!normalizedPath.startsWith(basePathStr)) {
			throw new SecurityException("Acceso fuera del directorio permitido: " + filePath);
		}

		File file = new File(filePath);

		if (!file.exists()) {
			logger.warn("El archivo no existe: " + filePath);
			return null;
		}

		if (!file.isFile()) {
			throw new SecurityException("La ruta no es un archivo válido: " + filePath);
		}

		String canonicalPath = null;
		try {
			canonicalPath = file.getCanonicalPath().replace("\\", "/");
			if (!canonicalPath.startsWith(basePathStr)) {
				throw new SecurityException("Path traversal detectado después de canonicalización: " + filePath);
			}
		} catch (Exception e) {
			throw new SecurityException("Error validando path: " + filePath, e);
		}

		return canonicalPath;
	}

	public static void Send(String to, String subject, String content, String fullPath) {
		List<String> files = new ArrayList<>();
		files.add(fullPath);
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(tos, subject, content, files);
	}

	public static void Send(String to, String subject, String content, List<String> filesPath) {
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(tos, subject, content, filesPath);
	}

	public static void Send(Radicacion radi, String subject, String content) {
		String to = radi.getRadicador().getEmails().get(0).getDescripcion();
		List<String> tos = new ArrayList<>();
		tos.add(to);
		Send(subject, content, tos, radi.getAdjuntos());
	}

	public static void Send(String subject, String content, List<String> to, List<Adjunto> files) {
		List<String> filesPath = null;
		if (files != null && files.size() > 0) {
			filesPath = new ArrayList<>();
			for (Adjunto adj : files) {
				filesPath.add(adj.getPathToRead());
			}
		}
		Send(to, subject, content, filesPath);
	}

	public static void Send(List<String> to, String subject, String content, List<String> filesPath) {
		if (Constantes.SEND_MAIL_ENABLE) {
			Properties properties = System.getProperties();
			properties.setProperty("mail.smtp.host", Constantes.MAIL_HOST);
			Session session = Session.getDefaultInstance(properties);

			try {
				System.setProperty("java.net.preferIPv4Stack", "true");
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(Constantes.MAIL_FROM, "Superintendencia de Industria y Comercio"));

				for (int index = 0; index < to.size(); index++) {
					String normalizedEmail = normalizeEmail(to.get(index));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(normalizedEmail));
				}

				String nombreAmbiente = Constantes.STR_EMPTY;
				if (Constantes.AMBIENTE_ACTIVO != TipoAmbienteEnum.PRODUCCION) {
					nombreAmbiente = Constantes.AMBIENTE_ACTIVO.toString() + ": ";
				} else {
					String normalizedSoporte = normalizeEmail(Constantes.EMAIL_SOPORTE);
					message.addRecipient(Message.RecipientType.BCC,
							new InternetAddress(normalizedSoporte, "Soporte Copias y Certificaciones"));
				}

				String normalizedSubject = normalizeSubject(subject);
				message.setSubject(nombreAmbiente + normalizedSubject);

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setContent(content, Constantes.CONTENT_TYPE_HTML);

				MimeMultipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);

				if (filesPath != null && filesPath.size() > 0) {
					for (String filePath : filesPath) {
						logger.info(String.format("Procesando adjunto %s", filePath));
						if (!Utility.isNullOrEmptyTrim(filePath)) {
							try {
								String normalizedPath = normalizeFilePath(filePath);
								if (normalizedPath != null) {
									logger.info(String.format("Adjunto validado: %s", normalizedPath));
									File file = new File(normalizedPath);

									messageBodyPart = new MimeBodyPart();
									FileDataSource source = new FileDataSource(normalizedPath);
									messageBodyPart.setDataHandler(new DataHandler(source));
									messageBodyPart.setFileName(file.getName());
									multipart.addBodyPart(messageBodyPart);

									logger.info(String.format("Adjunto agregado: %s", file.getName()));
								}
							} catch (Exception e) {
								logger.error("Error procesando adjunto: " + filePath, e);
							}
						}
					}
				}

				message.setContent(multipart);
				Transport.send(message);

				logger.info(String.format("Email enviado a %s", to.get(0)));
			} catch (Exception ex) {
				logger.error("Error Send Mail", ex);
			}
		}
	}
}