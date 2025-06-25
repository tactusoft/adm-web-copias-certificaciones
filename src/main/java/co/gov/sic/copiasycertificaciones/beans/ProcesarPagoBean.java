package co.gov.sic.copiasycertificaciones.beans;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.Cesl_cotizacion;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.Sancion;
import co.gov.sic.copiasycertificaciones.entities.ws.request.RequestSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignPDF;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoSancion;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.MailService;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import co.gov.sic.copiasycertificaciones.ws.client.soap.ConceptoRecaudo;
import co.gov.sic.copiasycertificaciones.ws.client.soap.PagoRecaudo;
import co.gov.sic.copiasycertificaciones.ws.client.soap.ReciboCajaRecaudo;
import co.gov.sic.copiasycertificaciones.ws.client.soap.RegistroRecaudoRequest;
import co.gov.sic.copiasycertificaciones.ws.client.soap.RegistroRecaudoResponse;
import co.gov.sic.copiasycertificaciones.ws.client.soap.WSRecaudosClient;
import co.gov.sic.copiasycertificaciones.ws.client.soap.WSSignClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.ReciboCaja;
import sic.ws.interop.entities.Tramite;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.request.RequestTramite;
import sic.ws.interop.entities.response.ResponseTramite;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

@Named("procesarPagoBean")
@ViewScoped
public class ProcesarPagoBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 7765876811740798583L;

    private String billID;
    private String transactionState;

    public String getBillID() {
        return this.billID;
    }

    public void setBillID(String val) {
        this.billID = val;
    }

    public String getTransactionState() {
        return this.transactionState;
    }

    public void setTransactionState(String val) {
        this.transactionState = val;
    }

    public ProcesarPagoBean() throws Exception {
        super();
    }

    @PostConstruct
    public void init() {
        Map<String, String> map = getExternalContext().getRequestParameterMap();
        registrarPago(map, false);
    }

    public void simularPago(Cesl_tramite tramite) throws Exception {
        nextInvoiceId(tramite);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("TransactionState", EstadoTramite.OK.getDescription());
        map.put("TrazabilityCode", "123456789");
        map.put("ReturnCode", "SUCCESS");
        Integer billId = null;
        try (Dal Dal = new Dal()) {
            billId = Dal.getInvoiceId(tramite.getIdtramite(), tramite.getIden_pers());
        }
        map.put("TicketId", String.valueOf(billId));
        map.put("Banco", "Bancolombia");
        map.put("Ref4", "");
        map.put("TransactionValue", "8400");
        map.put("BankProcessDate", "23/08/2021 12:00:00 a. m.");
        map.put("TRANGUID", "123456789");
        logger.info(String.format("procesarPagoBean SIMULAR pago idtramite = %s", tramite.getIdtramite()));
        registrarPago(map, true);
    }

    public Integer registrarPagoOtros(Map<String, String> map) {
        if (map != null && map.size() > 0 && map.containsKey("TransactionState")) {
            // if (Constantes.AMBIENTE_ACTIVO != TipoAmbienteEnum.PRODUCCION) {
            map.entrySet().forEach((entry) -> {
                logger.info("procesarPagoBean Map -> " + entry.getKey() + " = " + entry.getValue());
            });
            //}

            try (Dal Dal = new Dal()) {
                EstadoTramite TransactionState = EstadoTramite.fromDescription(map.get("TransactionState"));
                //cambiar en base de datos el tipo de TrazabilityCode a Varchar, ya que se detecto que puede ser no un entero
                String val = map.get("TrazabilityCode");
                Integer TrazabilityCode = null;
                int TicketId = Integer.parseInt(map.get("TicketId"));
                Cesl_tramite tramite = Dal.getTramiteFromInvoice(TicketId);
                if (tramite.getEstado() != EstadoTramite.FINALIZADO) {
                    if (tramite.getEstado().getValue() <= EstadoTramite.PENDIENTE_CONFIRMACION_PAGO.getValue()) {
                        //Lo primero que se debe actualizar es el estado en la base de datos
                        Dal.updateInvoice(TicketId, TransactionState, tramite.getIdtramite(), TrazabilityCode, null, null);
                        tramite.setEstado(TransactionState);
                        Dal.actualizarTramite(tramite);
                    }
                    Perfil perfilRadicacion = Dal.getPerfilCertificado(tramite.getIdtiposolicitud());
                    List<Cesl_detalleSolicitud> detallesTramite = Dal.getDetallesTramite(tramite.getIdtramite());

                    //Si fue pagado correctamente, generar recibo de caja, radicar y generar certificado
                    if (TransactionState == EstadoTramite.OK || (tramite.getEstado().getValue() >= EstadoTramite.RECIBO_CAJA.getValue() && tramite.getEstado().getValue() <= EstadoTramite.RADICADO_SALIDA.getValue())) {

                    	// Actualizar notificación para indicarle que fue pagado
                    	Dal.updateNotificacionByTramite(tramite.getIdtramite(), "S");
                    	
                        RegistroRecaudoResponse responseRecaudo = new RegistroRecaudoResponse();
                        if (tramite.getEstado() == EstadoTramite.OK) {
                            String[] codigosRecaudos = Dal.getRentisticoyConcepto(perfilRadicacion);
                            RegistroRecaudoRequest requestRecaudo = new RegistroRecaudoRequest();
                            requestRecaudo.setUsuario(Constantes.WS_RECAUDOS_USER);
                            requestRecaudo.setPassword(Constantes.WS_RECAUDOS_PASS);
                            requestRecaudo.setIdenPersConsignatario(tramite.getIden_pers());
                            List<PagoRecaudo> pagos = new ArrayList<>();
                            PagoRecaudo pago = new PagoRecaudo();
                            pago.setCodigoBanco(Constantes.RECAUDOS_COD_BANCO_DE_BOGOTA);
                            pago.setCodigoSucursal(Constantes.RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL);
                            pago.setFechaPago(LocalDate.now());
                            pago.setNumeroCuenta(Constantes.RECAUDOS_NUMERO_CUENTA);
                            pago.setNumeroPago(TicketId);
                            pago.setTipoPago(Constantes.RECAUDOS_TIPO_PAGO_PSE);
                            pago.setValorPago(tramite.getValor_total());
                            pagos.add(pago);
                            requestRecaudo.setPagosConsignatario(pagos);

                            List<ConceptoRecaudo> conceptos = new ArrayList<>();
                            int cantidadTotal = 0;
                            for (Cesl_detalleSolicitud detalle : detallesTramite) {
                                cantidadTotal += detalle.getCantidad();
                            }
                            ConceptoRecaudo conceptoCapital = new ConceptoRecaudo();
                            conceptoCapital.setCantidadRecibos(1);
                            conceptoCapital.setUnidadesXRecibo(cantidadTotal);
                            conceptoCapital.setCodigoConcepto(Long.parseLong(codigosRecaudos[1]));
                            conceptoCapital.setCodigoRentistico(codigosRecaudos[0]);
                            conceptoCapital.setValorUnitario(tramite.getValor_total() / cantidadTotal);
                            conceptos.add(conceptoCapital);
                            requestRecaudo.setConceptosPagos(conceptos);
                            WSRecaudosClient wsReca = new WSRecaudosClient();
                            responseRecaudo = wsReca.Registrar(requestRecaudo);
                        }
                        ReciboCajaRecaudo reciboCaja = null;
                        if (responseRecaudo.getRecibos() != null && responseRecaudo.getRecibos() != null && responseRecaudo.getRecibos().size() > 0) {
                            reciboCaja = responseRecaudo.getRecibos().get(0);
                        }
                        if (responseRecaudo.getCodigo() == 0) {
                            if (tramite.getEstado() == EstadoTramite.OK) {
                                tramite.setEstado(EstadoTramite.RECIBO_CAJA);
                                tramite.setAno_recibo(responseRecaudo.getAnioTransaccion());
                                tramite.setNume_recibo(reciboCaja.getNumeroRecibo());
                                Dal.actualizarTramite(tramite);
                                Dal.updateInvoice(TicketId, TransactionState, tramite.getIdtramite(), TrazabilityCode, responseRecaudo.getAnioTransaccion(), responseRecaudo.getNumeroTransaccion());
                            } else {
                                responseRecaudo.setAnioTransaccion(tramite.getAno_recibo());
                                reciboCaja = new ReciboCajaRecaudo();
                                reciboCaja.setNumeroRecibo(tramite.getNume_recibo());
                            }
                            Radicacion radi = new Radicacion();
                            Persona radicador = new Persona();
                            radicador.setId(tramite.getIden_pers());
                            radicador.setRetornarSoloUltimosDatos(true);
                            InteropWSClient wsInteropClient = Utility.GetWSClient();
                            radicador = wsInteropClient.personaConsultar(radicador).getPersona();
                            radi.setRadicador(radicador);
                            radi.setPerfil(perfilRadicacion);
                            radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
                            radi.setTotalFolios(1);
                            radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
                            radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
                            radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
                            ResponseRadicacion responseRadicacion = new ResponseRadicacion();
                            if (tramite.getEstado() == EstadoTramite.RECIBO_CAJA) {
                                if (reciboCaja.getNumeroRadicacion() != null) {
                                    radi.setAnio(reciboCaja.getAnioRadicacion());
                                    radi.setNumero(reciboCaja.getNumeroRadicacion());
                                }
                                ReciboCaja reciboRecaudos = new ReciboCaja();
                                reciboRecaudos.setAnio(responseRecaudo.getAnioTransaccion());
                                reciboRecaudos.setNumero(reciboCaja.getNumeroRecibo());
                                radi.getRecibosCaja().add(reciboRecaudos);
                                radi.setObservaciones(String.format("Recibo Caja %s-%s", responseRecaudo.getAnioTransaccion(), reciboCaja.getNumeroRecibo()));
                                responseRadicacion = wsInteropClient.radicacionRegistrar(radi);
                            }
                            if (responseRadicacion.getCodigo() == 0) {
                                TemplateContent templateContent = new TemplateContent();
                                if (tramite.getEstado() == EstadoTramite.RECIBO_CAJA) {
                                    radi = responseRadicacion.getRadicacion();
                                    //Se debe volver a asignar por si se consulta esta propiedad mas adelante
                                    //el webservice no devuelve la informacion completa de la persona luego de una radicacion
                                    radi.setRadicador(radicador);
                                    tramite.setAno_radi(radi.getAnio());
                                    tramite.setNume_radi(radi.getNumero());
                                    tramite.setCont_radi(radi.getControl());
                                    tramite.setCons_radi(radi.getConsecutivo());
                                    tramite.setEstado(EstadoTramite.RADICADO_ENTRADA);
                                    Dal.actualizarTramite(tramite);
                                } else {
                                    RequestTramite request = new RequestTramite();
                                    request.setAnio(tramite.getAno_radi());
                                    request.setNumero(tramite.getNume_radi());
                                    request.setExcluirDependencias(false);
                                    ResponseTramite response = wsInteropClient.radicacionConsultar(request);
                                    for (Tramite tram : response.getExpedientes()) {
                                        if (tram.getConsecutivo() == 0) {
                                            radi.setAnio(tram.getAnio());
                                            radi.setNumero(tram.getNumero());
                                            radi.setPerfil(tram.getPerfil());
                                            radi.setConsecutivo(tram.getConsecutivo());
                                            radi.setControl(tram.getControl());
                                            radi.setFechaRadicacion(tram.getFechaRadicacion());
                                            break;
                                        }
                                    }
                                }

                                Path fullPathRadicadoEntrada = Functions.getRadicacionFullPathFileName(radi);
                                if (!Files.exists(fullPathRadicadoEntrada)) {
                                    String contentPDF = templateContent.buildRadicacionPDFTemplate(radi, tramite, detallesTramite);
                                    String subject = String.format("Radicación SIC %s", radi.getShortNumeroRadicacion());

                                    try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
                                            tramite.getIdtiposolicitud().getDescripcion(), subject,
                                            String.format(Constantes.KEYWORDS_PDF_RADICACION,
                                                    radi.getFechaRadicacion().getYear()), false, Utility.getBarCode(tramite.getIdtramite(), radi.getConsecutivo(), tramite.getIdtiposolicitud()))) {
                                        String pathRadicadoEntrada = Functions.saveFile(radi, fileContent);
                                        radi.addAdjunto(pathRadicadoEntrada, false);
                                    }

                                    //Descargar Recibo de Caja
                                    URL url = new URL(String.format(Constantes.URL_DOWNLOAD_RECIBO_RECAUDOS, responseRecaudo.getAnioTransaccion(), reciboCaja.getNumeroRecibo()));

                                    try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
                                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                            byte[] buffer = new byte[1024];
                                            int count = 0;
                                            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                                                baos.write(buffer, 0, count);
                                            }
                                            bis.close();
                                            String fileName = String.format("%s_ReciboCaja_%s-%s.%s", radi.getShortNumeroRadicacion(), tramite.getAno_recibo(), tramite.getNume_recibo(), Constantes.PDF_EXTENSION);
                                            String fullPathReciboCaja = Functions.saveFile(radi, fileName, baos);
                                            radi.addAdjunto(fullPathReciboCaja, false);
                                        }
                                    } catch (Exception ex) {
                                        //Para el recibo de caja, si no se pude descraga rno hay problema, se reporta en el log y se continua cin el proceso
                                        //no es un bloqueante
                                        logger.error(String.format("Descargando Recibo de Caja Tramite %s", tramite.getIdtramite()), ex);
                                    }

                                    ResponseRadicacion responeAdjunto = wsInteropClient.radicacionAdjuntosRegistrar(radi);
                                    if (responeAdjunto.getCodigo() != 0) {
                                        String error = "Radicacion Entrada adjuntos: " + responeAdjunto.getMensaje();
                                        logger.error(error);
                                        this.AddErrorMessage(error);
                                        Files.deleteIfExists(fullPathRadicadoEntrada);
                                        logger.info("registrarPagoOtros OK --> Radicacion Entrada adjuntos");
                                        return HttpServletResponse.SC_OK;
                                    } else {                                     //Enviar email con radicacion de entrada
                                        String htmlEmail = templateContent.buildEmailTemplate(radi, tramite.getIdtiposolicitud());
                                        MailService.Send(radi, tramite.getIdtiposolicitud().getDescripcion(), htmlEmail);
                                    }
                                }

                                Radicacion radiSalidaTemp = new Radicacion();
                                RequestSignPDF requestSign = new RequestSignPDF();
                                requestSign.setPasswordCliente(Constantes.WS_SIGN_PASS);
                                requestSign.setIdCliente(Constantes.WS_SIGN_USER);
                                requestSign.setIdPolitica(Constantes.WS_SIGN_ID_POLITICA_SIN_ESTAMPA);
                                requestSign.setStringToFind(Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
                                requestSign.setNoPagina("0");
                                int numeroAdjuntoInicial = 1;
                                int index2 = numeroAdjuntoInicial;
                                WSSignClient wsSign = new WSSignClient();
                                List<String> filesToDelete = new ArrayList<String>();
                                if (tramite.getEstado() == EstadoTramite.RADICADO_ENTRADA) {
                                    if (null != tramite.getIdtiposolicitud()) {
                                        switch (tramite.getIdtiposolicitud()) {
                                            case CERTIFICADO_SANCIONES:

                                                for (Cesl_detalleSolicitud detalle : detallesTramite) {
                                                    TipoSancion tipoSancion = TipoSancion.fromValue(detalle.getTipo_certifica());
                                                    LocalDateTime finalDate = Utility.getStartDateFromTodayAnYears(detalle.getAnos(), radi.getFechaRadicacion());
                                                    List<Sancion> sanciones = Dal.consultarSanciones(detalle.getTipo_docu(), detalle.getNume_docu(), tipoSancion, finalDate, radi.getFechaRadicacion());
                                                    String html = templateContent.buildCertificadoSancionesPDFTemplate(detalle, tipoSancion, sanciones, finalDate, radi.getFechaRadicacion());
                                                    try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(html, "Certificado Demandas, Investigaciones y Sanciones", tramite.getIdtiposolicitud().getDescripcion(), Constantes.KEYWORDS_PDF_DEMANDAS, true, null)) {
                                                        for (int index = 1; index <= detalle.getCantidad(); index++) {
                                                            String fileName = String.format("%s_Certificado Demandas, Investigaciones y Sanciones_%s_%s%s_%s.%s", radi.getShortNumeroRadicacion(), detalle.getTipo_certifica(), detalle.getTipo_docu(), detalle.getNume_docu(), index, Constantes.PDF_EXTENSION);
                                                            String fullPath = Functions.saveFile(radi, fileName, fileContent);

                                                            requestSign.setFilePath(fullPath);
                                                            
                                                            int attempts = 0;
                                                            boolean success = false;
                                                            ResponseSignPDF responseSign = null;

                                                            while (attempts < 6 && !success) {
                                                                try {
                                                                    responseSign = wsSign.Firmar(requestSign);
                                                                    if (responseSign != null && responseSign.getDocumento() != null) {
                                                                        success = true;
                                                                    } else {
                                                                        throw new Exception("Error en la firma: Documento no disponible.");
                                                                    }

                                                                } catch (Exception ex) {
                                                                    attempts++;
                                                                    logger.error("Intento " + attempts + " fallido al firmar el PDF: " + ex.getMessage());

                                                                    if (attempts >= 6) {
                                                                        logger.error("Fallo la firma del PDF tras 6 intentos.");
                                                                        throw new Exception("Error tras varios intentos al firmar el documento.");
                                                                    }

                                                                    try {
                                                                        Thread.sleep(10000);
                                                                    } catch (InterruptedException e) {
                                                                        logger.error("Error al dormir el hilo entre intentos de firma: " + e.getMessage());
                                                                    }
                                                                }
                                                            }
                                                            
                                                            if (responseSign != null && responseSign.getDocumento() != null) {
                                                                byte[] filesBytes = responseSign.getDocumento();
                                                                if (filesBytes != null) {
                                                                    fullPath = Functions.saveFile(radi, fileName, filesBytes);
                                                                } else {
                                                                    logger.error("PDF Firma Certificado: " + responseSign.getRespuestaObj().getMensajes().getMensaje());
                                                                    return HttpServletResponse.SC_OK;
                                                                }
                                                            }
                                                            
                                                            index2++;
                                                            radiSalidaTemp.addAdjunto(fullPath, false, index2);
                                                        }
                                                    }
                                                }
                                                break;
                                            case CERTIFICADO_REPRESENTACION_CAMARAS:
                                                for (Cesl_detalleSolicitud detalle : detallesTramite) {
                                                    CamaraComercio camara = detalle.getIdcamaracomercioData();
                                                    String html = templateContent.buildCertificadoCamarasPDFTemplate(camara, radi.getFechaRadicacion());
                                                    try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(html, String.format("Certificado %s", camara.getNombre()), tramite.getIdtiposolicitud().getDescripcion(), Constantes.KEYWORDS_PDF_CAMARAS, true, null)) {
                                                        for (int index = 1; index <= detalle.getCantidad(); index++) {
                                                            String fileName = String.format("%s_CertificadoCamaras_%s%s_%s.%s", radi.getShortNumeroRadicacion(), camara.getTipoDocumento(), camara.getNumeroDocumento(), index, Constantes.PDF_EXTENSION);
                                                            String fullPath = Functions.saveFile(radi, fileName, fileContent);

                                                            requestSign.setFilePath(fullPath);
                                                            ResponseSignPDF responseSign = wsSign.Firmar(requestSign);
                                                            byte[] filesBytes = responseSign.getDocumento();
                                                            if (filesBytes != null) {
                                                                Functions.saveFile(radi, fileName, filesBytes);
                                                            } else {
                                                                logger.error("PDF Firma Camara: " + responseSign.getRespuestaObj().getMensajes().getMensaje());
                                                                logger.info("registrarPagoOtros OK --> PDF Firma Certificado 2 ");
                                                                return HttpServletResponse.SC_OK;
                                                            }
                                                            index2++;
                                                            radiSalidaTemp.addAdjunto(fullPath, false, index2);
                                                        }
                                                    }
                                                }
                                                break;
                                            case CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS:
                                                for (Cesl_detalleSolicitud detalle : detallesTramite) {

                                                    String fileNameAndSAH1 = detalle.getObservaciones();
                                                    String fileName = fileNameAndSAH1.split(Constantes.FILENAME_CHECKSUM_SEPARATOR)[0];
                                                    String directoryTemp = Functions.getAdjuntosFolderPath(tramite.getIden_pers());
                                                    directoryTemp = String.format("%s/%s", directoryTemp, fileName);
                                                    String directoryRadi = Functions.getRadicacionFolderPath(radi);
                                                    String fileNameSigned = String.format("%s_%s_%s%s.%s", radi.getShortNumeroRadicacion(), fileName.replace(".pdf", Constantes.STR_EMPTY).replace(".PDF", Constantes.STR_EMPTY), detalle.getConApostilla() ? "Con" : "Sin", Constantes.PDF_APOSTILLE_SUFIX, Constantes.PDF_EXTENSION);
                                                    String fullPath = String.format("%s/%s", directoryRadi, fileNameSigned);

                                                    PDFGeneratorService.AddSignPage(directoryTemp, fullPath, detalle, radi.getRadicador());

                                                    requestSign.setFilePath(fullPath);
                                                    requestSign.setIdPolitica(Constantes.WS_SIGN_ID_POLITICA_SIN_ESTAMPA);
                                                    ResponseSignPDF responseSign = wsSign.Firmar(requestSign);
                                                    byte[] filesBytes = responseSign.getDocumento();
                                                    if (filesBytes != null) {
                                                        fullPath = Functions.saveFile(radi, fileNameSigned, filesBytes);
                                                    } else {
                                                        logger.error("PDF Firma Secretario: " + responseSign.getRespuestaObj().getMensajes().getMensaje());
                                                        logger.info("registrarPagoOtros OK --> PDF Firma Secretario ");
                                                        return HttpServletResponse.SC_OK;
                                                    }

                                                    String dest2 = String.format("%s/%s_%s", directoryRadi, radi.getShortNumeroRadicacion(), fileName);
                                                    Files.move(Paths.get(directoryTemp), Paths.get(dest2), StandardCopyOption.REPLACE_EXISTING);
                                                    index2++;
                                                    radiSalidaTemp.addAdjunto(dest2, false, index2);
                                                    index2++;
                                                    radiSalidaTemp.addAdjunto(fullPath, false, index2);
                                                    filesToDelete.add(directoryTemp);

                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    tramite.setEstado(EstadoTramite.GENERADO);
                                    Dal.actualizarTramite(tramite);
                                }
                                if (tramite.getEstado() == EstadoTramite.GENERADO) {
                                    Radicacion radiSalida = new Radicacion();
                                    radiSalida.setAnio(radi.getAnio());
                                    radiSalida.setNumero(radi.getNumero());
                                    radiSalida.setControl(radi.getControl());
                                    perfilRadicacion.setActuacion((short) 440);
                                    radiSalida.setPerfil(perfilRadicacion);
                                    radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
                                    radiSalida.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
                                    radiSalida.setRadicador(radicador);
                                    radiSalida.setTotalFolios(1);
                                    radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
                                    radiSalida.setTipoRadicacion(Constantes.TIPO_RADICACION_SALIDA);
                                    responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
                                    if (responseRadicacion.getCodigo() == 0) {
                                        tramite.setEstado(EstadoTramite.RADICADO_SALIDA);
                                        Dal.actualizarTramite(tramite);

                                        radiSalida = responseRadicacion.getRadicacion();
                                        //Se debe volver a asignar por si se consulta esta propiedad mas adelante
                                        //el webservice no devuelve la informacion completa de la persona luego de una radicacion
                                        radiSalida.setRadicador(radicador);
                                        String contentPDF = templateContent.buildRespuestaPDFTemplate(radiSalida, tramite.getIdtiposolicitud());
                                        String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());

                                        try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
                                                tramite.getIdtiposolicitud().getDescripcion(), subject,
                                                String.format(Constantes.KEYWORDS_PDF_RADICACION,
                                                        radiSalida.getFechaRadicacion().getYear()), false, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(), tramite.getIdtiposolicitud()))) {
                                            String fullPath = Functions.saveFile(radiSalida, fileContent);
                                            radiSalidaTemp.addAdjunto(fullPath, false, numeroAdjuntoInicial);
                                            radiSalida.setAdjuntos(radiSalidaTemp.getAdjuntos());

                                            ResponseRadicacion responeAdjunto = wsInteropClient.radicacionAdjuntosRegistrar(radiSalida);
                                            if (responeAdjunto.getCodigo() != 0) {
                                                String error = "Radicacion Salida adjuntos: " + responeAdjunto.getMensaje();
                                                logger.error(error);
                                                this.AddErrorMessage(error);
                                                logger.info("registrarPagoOtros OK --> Radicacion Salida adjuntos ");
                                                return HttpServletResponse.SC_OK;
                                            } else {
                                                for (String f : filesToDelete) {
                                                    Path fp = Paths.get(f);
                                                    if (Files.exists(fp, LinkOption.NOFOLLOW_LINKS)) {
                                                        try {
                                                            Files.delete(fp);
                                                        } catch (Exception ex) {
                                                            logger.error(String.format("No se pudo borrar el archivo temporal %s", f), ex);
                                                        }
                                                    }
                                                }
                                            }

                                            //Enviar email con radicacion de entrada
                                            String htmlEmail = templateContent.buildEmailTemplate(radiSalida, tramite.getIdtiposolicitud());
                                            MailService.Send(radiSalida, tramite.getIdtiposolicitud().getDescripcion(), htmlEmail);
                                        }

                                        tramite.setEstado(EstadoTramite.FINALIZADO);
                                        Dal.actualizarTramite(tramite);
                                        this.AddInfoMessage(String.format("Solicitud %s Finalizada Exitosamente", tramite.getIdtramite()));

                                    } else {
                                        String error = "Radicacion Salida registrarPago: " + responseRadicacion.getMensaje();
                                        logger.error(error);
                                        this.AddErrorMessage(error);
                                    }
                                }

                            } else {
                                String error = "Radicacion Entrada registrarPago: " + responseRadicacion.getMensaje();
                                logger.error(error);
                                this.AddErrorMessage(error);
                            }
                        } else {
                            String error = "Recuados registrarPago: " + responseRecaudo.getMensaje();
                            logger.error(error);
                            this.AddErrorMessage(error);
                        }
                    }

                }
                logger.info("registrarPagoOtros OK OK");
                return HttpServletResponse.SC_OK;
            } catch (Exception e) {
                logger.error("procesarPagoBean registrarPago", e);
                this.AddErrorMessage("procesarPagoBean registrarPago " + e.getMessage());
                return HttpServletResponse.SC_PRECONDITION_FAILED;
            }
        } else {
            logger.error("procesarPagoBean registrarPago - Mapa de Request Vacio");
            return HttpServletResponse.SC_PRECONDITION_FAILED;
        }
    }

    public Integer registrarPagoCopiasSimples(Map<String, String> map) {
        map.entrySet().forEach((entry) -> {
            logger.info("procesarPagoBean Map -> " + entry.getKey() + " = " + entry.getValue());
        });
        try (Dal Dal = new Dal()) {
            EstadoTramite TransactionState = EstadoTramite.fromDescription(map.get("TransactionState"));
            String val = map.get("TrazabilityCode");
            Integer TrazabilityCode = null;
            if (!Utility.isNullOrEmptyTrim(val)) {
                TrazabilityCode = Integer.parseInt(val);
            }
            String ReturnCode = map.get("ReturnCode");
            int TicketId = Integer.parseInt(map.get("TicketId"));
            String Banco = map.get("Banco");
            String Ref4 = map.get("Ref4");
            double TransactionValue = Double.parseDouble(map.get("TransactionValue"));
            String BankProcessDate = map.get("BankProcessDate");// 16/05/2020 12:00:00 a. m.
            int TRANGUID = Integer.parseInt(map.get("TRANGUID"));
            Cesl_tramite tramite = Dal.getTramiteFromInvoice(TicketId);
            if (tramite.getEstado() != EstadoTramite.FINALIZADO) {
                if (tramite.getEstado().getValue() <= EstadoTramite.PENDIENTE_CONFIRMACION_PAGO.getValue()) {
                    //Lo primero que se debe actualizar es el estado en la base de datos
                    Dal.updateInvoice(TicketId, TransactionState, tramite.getIdtramite(), TrazabilityCode, null, null);
                    tramite.setEstado(TransactionState);
                    Dal.actualizarTramite(tramite);
                }
                Perfil perfilRadicacion = Dal.getPerfilCertificado(tramite.getIdtiposolicitud());
                List<Cesl_detalleSolicitud> detallesTramite = Dal.getDetallesTramite(tramite.getIdtramite());
                //Si fue pagado correctamente, generear recibo de caja, radicar y generar certificado
                if (TransactionState == EstadoTramite.OK) {
                	// Actualizar notificación para indicarle que fue pagado
                	Dal.updateNotificacionByTramite(tramite.getIdtramite(), "S");
                	
                    RegistroRecaudoResponse responseRecaudo = new RegistroRecaudoResponse();
                    if (tramite.getEstado() == EstadoTramite.OK) {
                        perfilRadicacion.setTramite((short) 362);
                        String[] codigosRecaudos = Dal.getRentisticoyConcepto(perfilRadicacion);
                        RegistroRecaudoRequest requestRecaudo = new RegistroRecaudoRequest();
                        requestRecaudo.setUsuario(Constantes.WS_RECAUDOS_USER);
                        requestRecaudo.setPassword(Constantes.WS_RECAUDOS_PASS);
                        requestRecaudo.setIdenPersConsignatario(tramite.getIden_pers());
                        List<PagoRecaudo> pagos = new ArrayList<>();
                        PagoRecaudo pago = new PagoRecaudo();
                        pago.setCodigoBanco(Constantes.RECAUDOS_COD_BANCO_DE_BOGOTA);
                        pago.setCodigoSucursal(Constantes.RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL);
                        pago.setFechaPago(LocalDate.now());
                        pago.setNumeroCuenta(Constantes.RECAUDOS_NUMERO_CUENTA);
                        pago.setNumeroPago(TicketId);
                        pago.setTipoPago(Constantes.RECAUDOS_TIPO_PAGO_PSE);
                        pago.setValorPago(tramite.getValor_total());
                        pagos.add(pago);
                        requestRecaudo.setPagosConsignatario(pagos);

                        List<ConceptoRecaudo> conceptos = new ArrayList<>();
                        List<Cesl_cotizacion> listaCotizacion = Dal.getCotizacionByIdTramite(tramite.getIdtramite());
                        for (Cesl_cotizacion c : listaCotizacion) {
                            ConceptoRecaudo conceptoCapital = new ConceptoRecaudo();
                            conceptoCapital.setCantidadRecibos(1);
                            conceptoCapital.setUnidadesXRecibo(c.getCantidad());
                            conceptoCapital.setCodigoConcepto(c.getFrntstco().getFcncpto());
                            conceptoCapital.setCodigoRentistico(Dal.getDayConfigParameters("frntstco").getValorString());
                            conceptoCapital.setValorUnitario(Double.valueOf(c.getFvalor()));
                            conceptos.add(conceptoCapital);
                        }
                        requestRecaudo.setConceptosPagos(conceptos);
                        WSRecaudosClient wsReca = new WSRecaudosClient();
                        responseRecaudo = wsReca.Registrar(requestRecaudo);
                    }
                    ReciboCajaRecaudo reciboCaja = null;
                    if (responseRecaudo.getRecibos() != null && responseRecaudo.getRecibos() != null && responseRecaudo.getRecibos().size() > 0) {
                        reciboCaja = responseRecaudo.getRecibos().get(0);
                    }
                    if (responseRecaudo.getCodigo() == 0) {

                        if (tramite.getEstado() == EstadoTramite.OK) {
                            tramite.setEstado(EstadoTramite.RECIBO_CAJA);
                            tramite.setAno_recibo(responseRecaudo.getAnioTransaccion());
                            tramite.setNume_recibo(reciboCaja.getNumeroRecibo());
                            Dal.actualizarTramite(tramite);
                            Dal.updateInvoice(TicketId, TransactionState, tramite.getIdtramite(), TrazabilityCode, responseRecaudo.getAnioTransaccion(), responseRecaudo.getNumeroTransaccion());

                        } else {
                            responseRecaudo.setAnioTransaccion(tramite.getAno_recibo());
                            reciboCaja = new ReciboCajaRecaudo();
                            reciboCaja.setNumeroRecibo(tramite.getNume_recibo());
                        }

                        Radicacion radi = new Radicacion();
                        Persona radicador = new Persona();
                        radicador.setId(tramite.getIden_pers());
                        radicador.setRetornarSoloUltimosDatos(true);
                        InteropWSClient wsInteropClient = Utility.GetWSClient();
                        radicador = wsInteropClient.personaConsultar(radicador).getPersona();
                        radi.setRadicador(radicador);

                        ResponseRadicacion responseRadicacion = new ResponseRadicacion();
                        if (tramite.getEstado() == EstadoTramite.RECIBO_CAJA) {
                            radi.setAnio(tramite.getAno_radi());
                            radi.setNumero(tramite.getNume_radi());
                            perfilRadicacion.setActuacion((short) 406);
                            perfilRadicacion.setDependencia((short) 104);
                            perfilRadicacion.setEvento((short) 0);
                            perfilRadicacion.setTramite((short) 362);

                            radi.setPerfil(perfilRadicacion);
                            radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
                            radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);

                            radi.setTotalFolios(1);
                            radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
                            radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
                            ReciboCaja reciboRecaudos = new ReciboCaja();
                            reciboRecaudos.setAnio(responseRecaudo.getAnioTransaccion());
                            reciboRecaudos.setNumero(reciboCaja.getNumeroRecibo());
                            radi.getRecibosCaja().add(reciboRecaudos);
                            radi.setObservaciones(String.format("Recibo Caja %s-%s", responseRecaudo.getAnioTransaccion(), reciboCaja.getNumeroRecibo()));
                            responseRadicacion = wsInteropClient.radicacionRegistrar(radi);
                        }
                        if (responseRadicacion.getCodigo() == 0) {
                            TemplateContent templateContent = new TemplateContent();
                            if (tramite.getEstado() == EstadoTramite.RECIBO_CAJA) {
                                radi = responseRadicacion.getRadicacion();
                                radi.setRadicador(radicador);

                                String fullPathReciboCaja = null;
                                //Descargar Recibo de Caja
                                URL url = new URL(String.format(Constantes.URL_DOWNLOAD_RECIBO_RECAUDOS, responseRecaudo.getAnioTransaccion(), reciboCaja.getNumeroRecibo()));

                                try (BufferedInputStream bis = new BufferedInputStream(url.openStream())) {
                                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                        byte[] buffer = new byte[1024];
                                        int count = 0;
                                        while ((count = bis.read(buffer, 0, 1024)) != -1) {
                                            baos.write(buffer, 0, count);
                                        }
                                        bis.close();
                                        String fileName = String.format("%s_ReciboCaja_%s-%s.%s", radi.getShortNumeroRadicacion(), tramite.getAno_recibo(), tramite.getNume_recibo(), Constantes.PDF_EXTENSION);
                                        fullPathReciboCaja = Functions.saveFile(radi, fileName, baos);
                                        radi.addAdjunto(fullPathReciboCaja, false);
                                    }
                                } catch (Exception ex) {
                                    logger.error(String.format("Descargando Recibo de Caja Tramite %s", tramite.getIdtramite()), ex);
                                }

                                ResponseRadicacion responeAdjunto = wsInteropClient.radicacionAdjuntosRegistrar(radi);
                                if (responeAdjunto.getCodigo() != 0) {
                                    if (responeAdjunto.getCodigo() != 0) {
                                        String error = "Radicacion Entrada adjuntos: " + responeAdjunto.getMensaje();
                                        logger.error(error);
                                        this.AddErrorMessage(error);
                                    }
                                }
                                String htmlEmail;
                                htmlEmail = templateContent.buildEmailTemplatePago(radi);
                                tramite.setEstado(EstadoTramite.PAGADO);
                                Dal.actualizarTramite(tramite);
                                List<String> listaAdjunto = new ArrayList<>();
                                listaAdjunto.add(fullPathReciboCaja);

                                List<String> listaEmails = new ArrayList<>();
                                for (Email e : radicador.getEmails()) {
                                    listaEmails.add(e.getDescripcion());
                                }

                                MailService.Send(listaEmails, tramite.getIdtiposolicitud().getDescripcion(), htmlEmail, listaAdjunto);

                            }

                        }

                    }
                }
            }
            logger.info("Finaliza metodo registrarPagoCopiasSimples OK");
            return HttpServletResponse.SC_OK;
        } catch (Exception e) {
            logger.error("procesarPagoBean registrarPago", e);
            this.AddErrorMessage("procesarPagoBean registrarPago " + e.getMessage());
            return HttpServletResponse.SC_PRECONDITION_FAILED;
        }

    }

    public void registrarPago(Map<String, String> map, Boolean simular) {
        System.out.println("==> registrarPago");
        Integer response = HttpServletResponse.SC_PRECONDITION_FAILED;
        Integer TicketId = 0;
        if (map != null && map.size() > 0 && map.containsKey("TicketId")) {
            TicketId = Integer.parseInt(map.get("TicketId"));
            try (Dal Dal = new Dal()) {
                Cesl_tramite tramite = Dal.getTramiteFromInvoice(TicketId);
                if (tramite.getIdtiposolicitud() == TipoTramite.COPIAS_SIMPLES) {
                    response = registrarPagoCopiasSimples(map);
                } else {
                    response = registrarPagoOtros(map);
                }
            } catch (Exception e) {

            }

        } else {
            logger.error("procesarPagoBean registrarPago - Mapa de Request Vacio");
        }

        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.setResponseStatus(response);

            logger.error("Rta Sonda " + response + " Id " + TicketId);
            // Serializar json
            String codError = "";//Esta variable la debemos alimentar donde se genera el error.
            String desError = "";//Esta variable la debemos alimentar donde se genera el error.
            Date fechaHora = new Date();
            // Si el verdadero
            if (simular) {
                return;
            }

            Boolean rta = false;

            if (response == 200) {
                codError = "null";
                desError = "null";
            } else {
                rta = true;
                codError = "" + response;
                desError = "";
            }
            String json = "{"
                    + "\"error\": \"" + rta + "\","
                    + "\"codigoError\": \"" + codError + "\","
                    + "\"descripcionError\": \"" + desError + "\","
                    + "\"fechaHora\": \"" + fechaHora + "\""
                    + "}";
            logger.info(json);
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();
            ec.responseReset();
            ec.setResponseContentType("application/json");
            ec.setResponseCharacterEncoding("UTF-8");
            try (PrintWriter p = new PrintWriter(ec.getResponseOutputStream())) {
                p.println(json);
            } catch (IOException e) {
                logger.error("Error json respuesta ", e);
            }

            fc.responseComplete();
        } catch (java.lang.IllegalStateException e) {
        }

    }
}
