package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
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

@Named("correccionCCBean")
@ViewScoped
public class CorreccionCCBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = -1623844166951178097L;
	private List<Referencia> listaCamaras;
    private List<Referencia> listaNumerosRadicacion;
    private Integer idcamaracomercio;
    private String numeroRadicacion;
    private String observaciones;
    private final int numMaxSolicitudes = 1;

    public int getNumMaxSolicitudes() {
        return numMaxSolicitudes;
    }

    public CorreccionCCBean() throws Exception {
        super(TipoTramite.CORRECCION_REPRESENTACION_CAMARAS);
    }

    @PostConstruct
    public void init() {
        setShowPanelAgregar(true);
        setIsSuccess(false);
    }

    public void addRowTable() throws Exception {
        boolean isValid = true;

        if ((getIdcamaracomercio() == null) || ((getIdcamaracomercio() <= 0))) {
            isValid = false;
            AddErrorMessage("La cámara de comercio es requerida.");
        }
        if (Utility.isNullOrEmptyTrim(numeroRadicacion)) {
            isValid = false;
            AddErrorMessage("El Número de Radicado Original es requerido.");
        }
        if (Utility.isNullOrEmptyTrim(observaciones)) {
            isValid = false;
            AddErrorMessage("Las observaciones son requeridas.");
        }
        for (Cesl_detalleSolicitud detalle : listaDetalles) {
            if (Objects.equals(detalle.getIdcamaracomercio(), getIdcamaracomercio())) {
                isValid = false;
                AddErrorMessage("Ya existe un registro para esta cámara de comercio.");
            }
        }

        int numtotalCertificaciones = 1 + listaDetalles.size();
        if (numtotalCertificaciones > numMaxSolicitudes) {
            setShowPanelAgregar(true);
            isValid = false;
            AddErrorMessage("Solo se aceptan máximo  " + numMaxSolicitudes + " solicitudes.");
        } else if (numtotalCertificaciones == numMaxSolicitudes && isValid) {
            setShowPanelAgregar(false);
        }

        // Si no hay errores
        if (isValid) {
            Cesl_detalleSolicitud detalle = new Cesl_detalleSolicitud();
            detalle.setIdtramite(listaDetalles.size());
            detalle.setIdcamaracomercio(getIdcamaracomercio());
            CamaraComercio camara = new CamaraComercio();
            camara.setNombre(getCamaraDescripcion());
            detalle.setIdcamaracomercioData(camara);
            detalle.setObservaciones(observaciones);
            detalle.setVariableAdicional1(numeroRadicacion);
            listaDetalles.add(detalle);
            this.idcamaracomercio = null;
        }
    }

    private String getCamaraDescripcion() throws Exception {
        List<Referencia> camaras = getCamaras();
        for (Referencia referencia : camaras) {
            if (referencia.getCodigo().equals(String.valueOf(getIdcamaracomercio()))) {
                return referencia.getValor();
            }
        }
        return null;
    }

    public List<Referencia> getCamaras() throws Exception {
        if (listaCamaras == null) {
            try (Dal Dal = new Dal()) {
                listaCamaras = Dal.GetCamaras();
            }
        }
        return listaCamaras;
    }

    public void onTipoCamaraChange() {
        //dummy
    }

    public List<Referencia> getListaNumerosRadicacion() throws Exception {
        listaNumerosRadicacion = new ArrayList<>();
        if (idcamaracomercio != null && idcamaracomercio > 0) {
            try (Dal Dal = new Dal()) {
                listaNumerosRadicacion = Dal.GetNumerosRadicacion(idcamaracomercio, getDatosSesion().getPersona().getId());
            }
        }
        return listaNumerosRadicacion;
    }

    public Integer getIdcamaracomercio() {
        return idcamaracomercio;
    }

    public void setIdcamaracomercio(Integer val) {
        this.idcamaracomercio = val;
    }

    public String getNumeroRadicacion() {
        return numeroRadicacion;
    }

    public void setNumeroRadicacion(String val) {
        this.numeroRadicacion = val;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String val) {
        this.observaciones = val;
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
                        logger.info("PDF Radicacion Entrada Listados: " + fullPathRadicadoEntrada);
                    }
                    //Enviar email con radicacion de entrada
                    String htmlEmail = templateContent.buildEmailTemplate(radi, tramiteSeleccionado.getIdtiposolicitud());
                    List<String> adjuntos = new ArrayList<>();
                    adjuntos.add(fullPathRadicadoEntrada);
                    MailService.Send(cuurentUser.getEmails().get(0).getDescripcion(), tramiteSeleccionado.getIdtiposolicitud().getDescripcion(), htmlEmail, adjuntos);
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
