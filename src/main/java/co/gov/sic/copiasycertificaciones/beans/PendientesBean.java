package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.ws.request.RequestSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignPDF;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.MailService;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import co.gov.sic.copiasycertificaciones.ws.client.soap.WSSignClient;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Tramite;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.request.RequestTramite;
import sic.ws.interop.entities.response.ResponseTramite;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

@Named("pendientesBean")
@ViewScoped
public class PendientesBean extends ProcesarPagoBean implements Serializable {

    private static final long serialVersionUID = -7211875418173836017L;

	public PendientesBean() throws Exception {
        super();
    }

    @PostConstruct
    public void init() {

        logger.info("init pendientesBean");

    }

    public void reintentarPago(Cesl_tramite tramite) throws Exception {
        try {
            if (tramite.getIdtiposolicitud() == TipoTramite.LISTADOS_INFORMACION
                    || tramite.getIdtiposolicitud() == TipoTramite.CORRECCION_REPRESENTACION_CAMARAS) {
                InteropWSClient wsInteropClient = Utility.GetWSClient();
                RequestTramite request = new RequestTramite();
                request.setAnio(tramite.getAno_radi());
                request.setNumero(tramite.getNume_radi());
                request.setExcluirDependencias(false);
                ResponseTramite response = wsInteropClient.radicacionConsultar(request);
                Tramite tram = response.getExpedientes().get(0);

                Radicacion radi = new Radicacion();
                radi.setAnio(tram.getAnio());
                radi.setNumero(tram.getNumero());
                radi.setControl(tram.getControl());
                radi.setConsecutivo(tram.getConsecutivo());
                radi.setFechaRadicacion(tram.getFechaRadicacion());
                radi.setPerfil(tram.getPerfil());
                radi.setTotalFolios(1);
                Persona radicador = new Persona();
                radicador.setId(tramite.getIden_pers());
                radicador.setRetornarSoloUltimosDatos(true);
                radicador = wsInteropClient.personaConsultar(radicador).getPersona();
                radi.setRadicador(radicador);
                radi.setTipoRadicacion(tram.getTipoRadicacion());

                if (tramite.getIdtiposolicitud() == TipoTramite.LISTADOS_INFORMACION) {
                    TemplateContent templateContent = new TemplateContent();

                    List<Cesl_detalleSolicitud> detalles = null;
                    try (Dal Dal = new Dal()) {
                        detalles = Dal.getDetallesTramite(tramite.getIdtramite());
                    }
                    String contentPDF = templateContent.buildRadicacionPDFTemplate(radi, tramite, detalles);
                    String subject = String.format("Radicación SIC %s", radi.getShortNumeroRadicacion());

                    String fullPathRadicadoEntrada = null;

                    try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
                            tramite.getIdtiposolicitud().getDescripcion(), subject,
                            String.format(Constantes.KEYWORDS_PDF_RADICACION,
                                    radi.getFechaRadicacion().getYear()), false, Utility.getBarCode(tramite.getIdtramite(), radi.getConsecutivo(), tramite.getIdtiposolicitud()))) {
                        fullPathRadicadoEntrada = Functions.saveFile(radi, fileContent);
                        radi.addAdjunto(fullPathRadicadoEntrada, false);
                    }
                    ResponseRadicacion responeAdjunto = wsInteropClient.radicacionAdjuntosRegistrar(radi);
                    if (responeAdjunto.getCodigo() != 0) {
                        String error = "Error radicando documento entrada: " + responeAdjunto.getMensaje();
                        logger.error(error);
                        this.AddErrorMessage(error);
                    } else {
                        String htmlEmail = templateContent.buildEmailTemplate(radi, tramite.getIdtiposolicitud());
                        MailService.Send(radi, tramite.getIdtiposolicitud().getDescripcion(), htmlEmail);
                        this.AddInfoMessage(String.format("Solicitud %s Finalizada Exitosamente", tramite.getIdtramite()));
                    }

                } else if (tramite.getIdtiposolicitud() == TipoTramite.CORRECCION_REPRESENTACION_CAMARAS) {
                    TemplateContent templateContent = new TemplateContent();
                    try (Dal Dal = new Dal()) {
                        List<Cesl_detalleSolicitud> detallesTramite = Dal.getDetallesTramite(tramite.getIdtramite());
                        for (Cesl_detalleSolicitud detalle : detallesTramite) {
                            String numRadicacion = detalle.getVariableAdicional1();
                            if (!Utility.isNullOrEmptyTrim(numRadicacion)) {
                                String[] parts = numRadicacion.split("-");
                                Integer anoRadi = Integer.parseInt(parts[0].trim());
                                Integer numeRadi = Integer.parseInt(parts[1].trim());
                                Cesl_tramite tramiteOriginal = Dal.getTramiteFromNumRadicacion(anoRadi, numeRadi);
                                List<Cesl_detalleSolicitud> detallesTramiteOriginal = Dal.getDetallesTramite(tramiteOriginal.getIdtramite());

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
                                if (tramite.getEstado() == EstadoTramite.RADICADO_ENTRADA) {
                                    for (Cesl_detalleSolicitud detalleOriginal : detallesTramiteOriginal) {
                                        CamaraComercio camara = detalleOriginal.getIdcamaracomercioData();
                                        String html = templateContent.buildCertificadoCamarasPDFTemplate(camara, radi.getFechaRadicacion());
                                        try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(html, String.format("Certificado %s", camara.getNombre()), tramiteOriginal.getIdtiposolicitud().getDescripcion(), Constantes.KEYWORDS_PDF_CAMARAS, true, null)) {
                                            for (int index = 1; index <= detalleOriginal.getCantidad(); index++) {
                                                String fileName = String.format("%s_CertificadoCamaras_%s%s_%s.%s", radi.getShortNumeroRadicacion(), camara.getTipoDocumento(), camara.getNumeroDocumento(), index, Constantes.PDF_EXTENSION);
                                                String fullPath = Functions.saveFile(radi, fileName, fileContent);

                                                requestSign.setFilePath(fullPath);
                                                ResponseSignPDF responseSign = wsSign.Firmar(requestSign);
                                                byte[] filesBytes = responseSign.getDocumento();
                                                if (filesBytes != null) {
                                                    Functions.saveFile(radi, fileName, filesBytes);
                                                } else {
                                                    logger.error("PDF Firma Camara Correccion: " + responseSign.getRespuestaObj().getMensajes().getMensaje());
                                                    return;
                                                }
                                                index2++;
                                                radiSalidaTemp.addAdjunto(fullPath, false, index2);
                                            }
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
                                    tram.getPerfil().setActuacion((short) 440);
                                    radiSalida.setPerfil(tram.getPerfil());
                                    radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
                                    radiSalida.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
                                    radiSalida.setRadicador(radicador);
                                    radiSalida.setTotalFolios(1);
                                    radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
                                    radiSalida.setTipoRadicacion(Constantes.TIPO_RADICACION_SALIDA);
                                    ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
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
                                                tramiteOriginal.getIdtiposolicitud().getDescripcion(), subject,
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
                                                return;
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
                                this.AddErrorMessage("El numero de radicacion original para correccion es nulo.");
                            }
                        }
                    }
                }
            } else {
                Map<String, String> map = new LinkedHashMap<>();

                try (Dal Dal = new Dal()) {
                    Integer billId = Dal.getInvoiceId(tramite.getIdtramite(), tramite.getIden_pers());
                    map.put("TicketId", String.valueOf(billId));
                    map.put("TransactionState", tramite.getEstado().getDescription());
                }

                logger.info(String.format("reintentarPago idtramite = %s", tramite.getIdtramite()));
                registrarPago(map, false);
            }
        } catch (Exception e) {
            logger.error("pendientesoBean reintentarPago", e);
            this.AddErrorMessage(e.getMessage());
        }
    }

    public List<Cesl_tramite> getListaTramitesUsua() throws Exception {
        try (Dal Dal = new Dal()) {
            List<Cesl_tramite> temp = Dal.consultarTramitesPendientes();
            return temp;
        }
    }

    public void descargarExpediente(Cesl_tramite tramite) {
        try {
            String folderPath = Functions.getRadicacionFolderPath(tramite.getAno_radi(), tramite.getNume_radi());
            Path folder = Paths.get(folderPath);
            Path folderName = folder.getFileName();
            String fileZipName = String.format("%s.%s", folderName, Constantes.ZIP_EXTENSION);

            //Se arma zip con todos los documentos generados en el expediente
            ByteArrayOutputStream zipMemory = Utility.zipDirectory(folderPath);

            download(zipMemory, fileZipName, true);
        } catch (Exception ex) {
            logger.error("descargarExpediente", ex);
            this.AddErrorMessage("No se pudo descargar el archivo:" + ex.getMessage());
        };
    }
}
