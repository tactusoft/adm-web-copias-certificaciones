package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.MailService;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

@Named("listadosInfoBean")
@ViewScoped
public class ListadosInfoBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 4899998982779486523L;
	private String tema;
	private List<Referencia> listaTemas;
    private final int numMaxSolicitudes = 1;
    private String observaciones;
    private String numeroRadicacion;

    public int getNumMaxSolicitudes() {
        return numMaxSolicitudes;
    }

    public String getTema() {
        return this.tema;
    }

    public void setTema(String val) {
        this.tema = val;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public void setObservaciones(String val) {
        this.observaciones = val;
    }

    public String getNumeroRadicacion() {
        return this.numeroRadicacion;
    }

    public void getNumeroRadicacion(String val) {
        this.numeroRadicacion = val;
    }

    public List<Referencia> getListaTemas() throws Exception {
        if (listaTemas == null) {
            listaTemas = Utility.GetReferenciaWS("TIPOSTEM_SEDELECTRO");
        }
        return listaTemas;
    }

    private String getTemaDescripcion() throws Exception {
        List<Referencia> tipos = getListaTemas();
        for (Referencia referencia : tipos) {
            if (referencia.getCodigo().equals(this.tema)) {
                return referencia.getValor();
            }
        }
        return null;
    }

    public ListadosInfoBean() throws Exception {
        super(TipoTramite.LISTADOS_INFORMACION);
    }

    public void addRowTable() throws Exception {
        boolean isValid = true;
        if (Utility.isNullOrEmptyTrim(tema)) {
            isValid = false;
            AddErrorMessage("El tema es requerido.");
        }
        if (Utility.isNullOrEmptyTrim(observaciones)) {
            isValid = false;
            AddErrorMessage("La Descripción de la Solicitud es requerida.");
        }

        int numtotalCertificaciones = 1 + listaDetalles.size();
        if (numtotalCertificaciones > numMaxSolicitudes) {
            setShowPanelAgregar(true);
            isValid = false;
            AddErrorMessage("Solo se aceptan máximo  " + numMaxSolicitudes + " certificaciones.");
        } else if (numtotalCertificaciones == numMaxSolicitudes && isValid) {
            setShowPanelAgregar(false);
        }

        // Si no hay errores
        if (isValid) {
            Cesl_detalleSolicitud detalle = new Cesl_detalleSolicitud();
            detalle.setIdtramite(listaDetalles.size());
            detalle.setTipo_certifica(tema);
            detalle.setTipo_certifica_descripcion(getTemaDescripcion());
            detalle.setObservaciones(observaciones);
            listaDetalles.add(detalle);
            limpiarForm();
        }
    }

    public void limpiarForm() {
        this.observaciones = null;
        this.tema = null;
    }

    @PostConstruct
    public void init() {
        setShowPanelAgregar(true);
        setIsSuccess(false);
    }

    @Override
    public boolean guardarSolicitud() throws Exception {
        boolean result = super.guardarSolicitud();
        if (result) {
            Radicacion radi = new Radicacion();
            Persona cuurentUser = getDatosSesion().getPersona();
            radi.setRadicador(cuurentUser);
            radi.setPerfil(perfilTramite);
            radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
            radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
            radi.setTotalFolios(1);
            radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
            radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
            InteropWSClient wsInteropClient = Utility.GetWSClient();
            ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radi);
            try (Dal Dal = new Dal()) {
                if (responseRadicacion.getCodigo() == 0) {
                    radi = responseRadicacion.getRadicacion();
                     //Se debe volver a asignar por si se consulta esta propiedad mas adelante
                    //el webservice no devuelve la informacion completa de la persona luego de una radicacion
                    radi.setRadicador(cuurentUser);
                    TemplateContent templateContent = new TemplateContent();
                    this.numeroRadicacion = radi.getShortNumeroRadicacion();
                    tramiteSeleccionado.setAno_radi(radi.getAnio());
                    tramiteSeleccionado.setNume_radi(radi.getNumero());
                    tramiteSeleccionado.setCont_radi(radi.getControl());
                    tramiteSeleccionado.setCons_radi(radi.getConsecutivo());
                    tramiteSeleccionado.setEstado(EstadoTramite.RADICADO_ENTRADA);
                    Dal.actualizarTramite(tramiteSeleccionado);

                    String contentPDF = templateContent.buildRadicacionPDFTemplate(radi, tramiteSeleccionado, listaDetalles);
                    String subject = String.format("Radicación SIC %s", radi.getShortNumeroRadicacion());

                    String fullPathRadicadoEntrada = null;
                    try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
                            tramiteSeleccionado.getIdtiposolicitud().getDescripcion(), subject,
                            String.format(Constantes.KEYWORDS_PDF_RADICACION,
                                    radi.getFechaRadicacion().getYear()), false, Utility.getBarCode(tramiteSeleccionado.getIdtramite(), radi.getConsecutivo(), tramiteSeleccionado.getIdtiposolicitud()))) {
                        fullPathRadicadoEntrada = Functions.saveFile(radi, fileContent);
                        radi.addAdjunto(fullPathRadicadoEntrada, false);
                    }

                    ResponseRadicacion responeAdjunto = wsInteropClient.radicacionAdjuntosRegistrar(radi);
                    if (responeAdjunto.getCodigo() != 0) {
                        String error = "Error radicando documento entrada: " + responeAdjunto.getMensaje();
                        logger.error(error);
                        this.AddErrorMessage(error);
                        return false;
                    } else {
                        String htmlEmail = templateContent.buildEmailTemplate(radi, tramiteSeleccionado.getIdtiposolicitud());
                        MailService.Send(radi, tramiteSeleccionado.getIdtiposolicitud().getDescripcion(), htmlEmail);
                    }
                }
            } catch (Exception e) {
                logger.error("procesoFlujo", e);
                AddErrorMessage("Ha ocurrido un error inesperado.");
                result = false;
            }
        }
        return result;
    }
}
