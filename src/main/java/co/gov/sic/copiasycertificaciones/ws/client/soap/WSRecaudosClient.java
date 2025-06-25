package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.xml.soap.SOAPElement;

public class WSRecaudosClient extends WSSoapClientBase {

    private static final String NAMESPACE = "RegistrarRecaudosWSDL";
    private static final String SOAP_ACTION = "Registrar";
    private RegistroRecaudoRequest currentRequest;

    public WSRecaudosClient() {
        super(NAMESPACE, SOAP_ACTION, Constantes.URL_WS_RECAUDOS);
    }

    public RegistroRecaudoResponse Registrar(RegistroRecaudoRequest request) throws Exception {
        currentRequest = request;
        RegistroRecaudoResponse response = new RegistroRecaudoResponse();
        Document document = call();
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        response.setCodigo(Long.valueOf(xpath.evaluate("//Codigo", document)));
        response.setMensaje(xpath.evaluate("//Mensaje", document));
        String val = xpath.evaluate("//AnioTransaccion", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            response.setAnioTransaccion(Short.valueOf(val));
        }
        val = xpath.evaluate("//NumeroTransaccion", document);
        if (!Utility.isNullOrEmptyTrim(val)) {
            response.setNumeroTransaccion(Integer.valueOf(val));
        }
        NodeList expre = (NodeList) xpath.compile("//item").evaluate(document, XPathConstants.NODESET);
        if (expre.getLength() > 0) {
            List<ReciboCajaRecaudo> recibos = new ArrayList<>();
            for (int i = 0; i < expre.getLength(); i++) {
                ReciboCajaRecaudo r = new ReciboCajaRecaudo();
                val = xpath.evaluate("//NumeroRecibo", expre.item(i));
                if (!Utility.isNullOrEmptyTrim(val)) {
                    r.setNumeroRecibo(Integer.valueOf(val));
                }
                val = xpath.evaluate("//AnioRadicacion", expre.item(i));
                if (!Utility.isNullOrEmptyTrim(val)) {
                    r.setAnioRadicacion(Short.valueOf(val));
                }
                recibos.add(r);
            }
            response.setRecibos(recibos);
        }
        return response;
    }

    @Override
    public void fillSOAPMessageBody(SOAPElement bodyRequest) throws Exception {
        SOAPElement soapBodyElem1 = addChildElement(bodyRequest, "request", null, true);

        if (currentRequest.getUsuario() != null) {
            addChildElement(soapBodyElem1, "Usuario", currentRequest.getUsuario(), true);
        }
        if (currentRequest.getPassword() != null) {
            addChildElement(soapBodyElem1, "Password", currentRequest.getPassword(), true);
        }
        addChildElement(soapBodyElem1, "IdenPersConsignatario", currentRequest.getIdenPersConsignatario(), true);

        if (currentRequest.getPagosConsignatario() != null && currentRequest.getPagosConsignatario().size() > 0) {
            SOAPElement soapBodyElem5 = addChildElement(soapBodyElem1, "PagosConsignatario", null, true);
            for (PagoRecaudo pago : currentRequest.getPagosConsignatario()) {
                SOAPElement soapBodyElem6 = addChildElement(soapBodyElem5, "Pago", null, true);
                if (pago.getTipoPago() != null) {
                    addChildElement(soapBodyElem6, "TipoPago", pago.getTipoPago(), true);
                }
                if (pago.getCodigoBanco() != null) {
                    addChildElement(soapBodyElem6, "CodigoBanco", pago.getCodigoBanco(), true);
                }
                if (pago.getCodigoSucursal() != null) {
                    addChildElement(soapBodyElem6, "CodigoSucursal", pago.getCodigoSucursal(), true);
                }
                if (pago.getNumeroCuenta() != null) {
                    addChildElement(soapBodyElem6, "NumeroCuenta", pago.getNumeroCuenta(), true);
                }
                addChildElement(soapBodyElem6, "NumeroPago", pago.getNumeroPago(), true);
                addChildElement(soapBodyElem6, "FechaPago", pago.getFechaPago(), true);
                addChildElement(soapBodyElem6, "ValorPago", pago.getValorPago(), true);
            }
        }
        if (currentRequest.getConceptosPagos() != null && currentRequest.getConceptosPagos().size() > 0) {
            SOAPElement soapBodyElem14 = addChildElement(soapBodyElem1, "ConceptosPagos", null, true);
            for (ConceptoRecaudo concepto : currentRequest.getConceptosPagos()) {
                SOAPElement soapBodyElem15 = addChildElement(soapBodyElem14, "Concepto", null, true);
                if (concepto.getCodigoRentistico() != null) {
                    addChildElement(soapBodyElem15, "CodigoRentistico", concepto.getCodigoRentistico(), true);
                }
                addChildElement(soapBodyElem15, "CodigoConcepto", concepto.getCodigoConcepto(), true);
                addChildElement(soapBodyElem15, "CantidadRecibos", concepto.getCantidadRecibos(), true);
                addChildElement(soapBodyElem15, "UnidadesXRecibo", concepto.getUnidadesXRecibo(), true);
                addChildElement(soapBodyElem15, "ValorUnitario", concepto.getValorUnitario(), true);
                addChildElement(soapBodyElem15, "NumeroMulta", concepto.getNumeroMulta(), true);
                addChildElement(soapBodyElem15, "ConsecutivoRecibo", concepto.getConsecutivoRecibo(), true);
            }
        }
    }
}
