package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import co.gov.sic.copiasycertificaciones.entities.ws.request.RequestSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignMensajePDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.RespuestaObjSignPDF;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.xml.soap.SOAPElement;

public class WSSignClient extends WSSoapClientBase {

    private static final String NAMESPACE = "http://ws.certicamara.com.co/";
    private static final String SOAP_ACTION = "procesarPDF";
    private RequestSignPDF currentRequest;

    public WSSignClient() {
        super(NAMESPACE, SOAP_ACTION, Constantes.URL_WS_SIGN);
    }

    public ResponseSignPDF Firmar(RequestSignPDF request) throws Exception {
        currentRequest = request;
        ResponseSignPDF response = new ResponseSignPDF();
        Document document = call();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        String val = xpath.evaluate("//documento", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            response.setDocumento(Base64.getDecoder().decode(val));
        }
        val = xpath.evaluate("//idTransaccion", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            response.setIdTransaccion(Integer.parseInt(xpath.evaluate("//idTransaccion", document)));
        }
        RespuestaObjSignPDF responseObj = response.getRespuestaObj();
        val = xpath.evaluate("//numFirmantes", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            responseObj.setNumFirmantes(Integer.valueOf(val));
        }
        val = xpath.evaluate("//verificado", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            responseObj.setVerificado(Boolean.parseBoolean(val));
        }
        ResponseSignMensajePDF responseMensaje = responseObj.getMensajes();
        val = xpath.evaluate("//codigo", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            responseMensaje.setCodigo(val);
        }
        val = xpath.evaluate("//mensaje", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            responseMensaje.setMensaje(val);
        }
        return response;
    }

    @Override
    public void fillSOAPMessageBody(SOAPElement bodyRequest) throws Exception {
        addChildElement(bodyRequest, "idCliente", currentRequest.getIdCliente(), false);
        addChildElement(bodyRequest, "passwordCliente", currentRequest.getPasswordCliente(), false);
        addChildElement(bodyRequest, "idPolitica", currentRequest.getIdPolitica(), false);
        addChildElement(bodyRequest, "stringToFind", currentRequest.getStringToFind(), false);
        addChildElement(bodyRequest, "noPagina", currentRequest.getNoPagina(), false);
        String base64File = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(currentRequest.getFilePath())));
        addChildElement(bodyRequest, "pdf", base64File, false);
    }
}
