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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sic.ws.interop.entities.Adjunto;
import sic.ws.interop.entities.radicacion.Radicacion;

public class MailService {

    protected static final Logger logger = LoggerFactory.getLogger(MailService.class);

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
            // Get system properties
            Properties properties = System.getProperties();

            // Setup mail server
            properties.setProperty("mail.smtp.host", Constantes.MAIL_HOST);

            // Get the default Session object.
            Session session = Session.getDefaultInstance(properties);

            try {
                System.setProperty("java.net.preferIPv4Stack", "true");
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Constantes.MAIL_FROM, "Superintendencia de Industria y Comercio"));
                for (int index = 0; index < to.size(); index++) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to.get(index)));
                }

                String nombreAmbiente = Constantes.STR_EMPTY;
                if (Constantes.AMBIENTE_ACTIVO != TipoAmbienteEnum.PRODUCCION) {
                    nombreAmbiente = Constantes.AMBIENTE_ACTIVO.toString() + ": ";
                } else {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(Constantes.EMAIL_SOPORTE, "Soporte Copias y Certificaciones"));
                }

                message.setSubject(nombreAmbiente + subject);

                // Create the message part
                MimeBodyPart messageBodyPart = new MimeBodyPart();

                // Now set the actual message
                messageBodyPart.setContent(content, Constantes.CONTENT_TYPE_HTML);

                // Create a multipar message
                MimeMultipart multipart = new MimeMultipart();

                // Set text message part
                multipart.addBodyPart(messageBodyPart);

                // Part two is attachment
                if (filesPath != null && filesPath.size() > 0) {
                    for (String filePath : filesPath) {
                    	logger.info(String.format("Tactu Adjunto %s", filePath));
                        if (!Utility.isNullOrEmptyTrim(filePath)) {
                        	logger.info(String.format("Tactu Adjunto %s ENTRA", filePath));
                            File file = new File(filePath);
                            if (file.exists()) {
                            	logger.info(String.format("Tactu Adjunto %s EXISTE", filePath));
                                messageBodyPart = new MimeBodyPart();
                                FileDataSource source = new FileDataSource(filePath);
                                messageBodyPart.setDataHandler(new DataHandler(source));
                                messageBodyPart.setFileName(file.getName());
                                multipart.addBodyPart(messageBodyPart);
                            }
                        }
                    }
                }
                // Send the complete message parts
                message.setContent(multipart);

                // Send message
                Transport.send(message);

                logger.info(String.format("Email enviado a %s", to.get(0)));
            } catch (Exception ex) {
                logger.error("Error Send Mail", ex);
            }
        }
    }

}
