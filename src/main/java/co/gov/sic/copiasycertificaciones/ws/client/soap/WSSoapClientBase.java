package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;

public abstract class WSSoapClientBase {

	private final String NAMESPACE_PREFIX = "reg";
	private final Logger LOGGER = LoggerFactory.getLogger(WSSoapClientBase.class);
	private final String NAMESPACE_URI;
	private final String SOAP_ACTION_NAME;
	private final String SOAP_ACTION;
	private final String WS_URL;

	public WSSoapClientBase(String nameSpaceURI, String soapActionName, String wsUrl) {
		NAMESPACE_URI = nameSpaceURI;
		SOAP_ACTION_NAME = soapActionName;
		WS_URL = wsUrl;
		SOAP_ACTION = String.format("%s#%s", NAMESPACE_URI, SOAP_ACTION_NAME);
		// LOGGER.info("SOAP_ACTION " + SOAP_ACTION);
	}

	public String extractValidXmlResponse(String xmlResponse) {
		// Buscar el inicio del XML válido
		int startIndex = xmlResponse.indexOf("<?xml");
		if (startIndex == -1) {
			// Si no se encuentra el encabezado XML, lanzar una excepción o manejar el error
			// según sea necesario
			throw new RuntimeException("No se encontró el encabezado XML en la respuesta.");
		}

		// Extraer el contenido XML desde el inicio hasta el final de la respuesta
		return xmlResponse.substring(startIndex).trim();
	}

	public Document call() throws Exception {
		SOAPConnection soapConnection = null;
		Document document = null;
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();

			SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), WS_URL);
			String xmlResponse;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				soapResponse.writeTo(out);
				xmlResponse = new String(out.toByteArray());
			}
			// LOGGER.info("Response SOAP " + xmlResponse);
			String validXmlResponse = extractValidXmlResponse(xmlResponse);
			InputSource source = new InputSource(new StringReader(validXmlResponse));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(source);
		} finally {
			if (soapConnection != null) {
				soapConnection.close();
			}
		}
		return document;
	}

	private SOAPElement createSoapEnvelope(SOAPMessage soapMessage) throws Exception {
		SOAPPart soapPart = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URI);
		SOAPBody soapBody = envelope.getBody();
		return addChildElement(soapBody, SOAP_ACTION_NAME, null, true);
	}

	protected SOAPElement addChildElement(SOAPElement target, String nombre, Object valor, boolean usePrefix)
			throws SOAPException {
		SOAPElement soapBodyElem1 = usePrefix ? target.addChildElement(nombre, NAMESPACE_PREFIX)
				: target.addChildElement(nombre);
		if (valor != null) {
			soapBodyElem1.addTextNode(valor.toString());
		}
		return soapBodyElem1;
	}

	protected abstract void fillSOAPMessageBody(SOAPElement bodyRequest) throws Exception;

	private SOAPMessage createSOAPRequest() throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPElement bodyRequest = createSoapEnvelope(soapMessage);
		fillSOAPMessageBody(bodyRequest);
		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("SOAPAction", SOAP_ACTION);
		soapMessage.saveChanges();

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			soapMessage.writeTo(out);
			String xmlRequest = new String(out.toByteArray());
			// LOGGER.info("Request SOAP " + xmlRequest);
		}
		return soapMessage;
	}
}
