package co.gov.sic.copiasycertificaciones.beans;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.enums.TipoReferenciaEnum;

@Named("certificadoCORBean")
@ViewScoped
public class CertificadoCORBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 1547205279184062049L;
    private List<Referencia> listaTiposCertificado;
    private List<Referencia> listaTiposDocumento;
    private String tipo_docu;
    private String nume_docu;
    private String tipo_certi;
    private Integer cantidad;
    private Integer rango;

    private final int numMaxSolicitudes = 20;

    public int getNumMaxSolicitudes() {
        return numMaxSolicitudes;
    }

    public CertificadoCORBean() throws Exception {
        super(TipoTramite.CERTIFICADO_SANCIONES_SIN_PAGO);
    }

    public void addRowTable() throws Exception {
        boolean isValid = true;

        // Proceso de validación
        if (Utility.isNullOrEmptyTrim(getTipo_docu())) {
            isValid = false;
            AddErrorMessage("El tipo de documento es requerido");
        }
        if (Utility.isNullOrEmptyTrim(getNume_docu())) {
            isValid = false;
            AddErrorMessage("El número de documento es requerido");
        }
        if (isValid && getTipo_docu().equals("NI") && getNume_docu().length() != 9) {
            isValid = false;
            AddErrorMessage("Cuando el tipo de documento es NIT, el número de documento debe tener exactamente 9 dígitos sin el digito de verificación.");
        }
        if (Utility.isNullOrEmptyTrim(getTipo_certi())) {
            isValid = false;
            AddErrorMessage("El tipo de certificación es requerido");
        }
        int numtotalCertificaciones = 0;
        if ((getCantidad() == null) || (getCantidad() <= 0) || (getCantidad() > 15)) {
            isValid = false;
            AddErrorMessage("La cantidad debe ser un número entero valido entre 1 y 15.");
        } else {
            numtotalCertificaciones += this.cantidad;
        }
        if ((getRango() == null) || ((getRango() <= 0))) {
            isValid = false;
            AddErrorMessage("El rango debe ser un número entero valido entre 1 y 10.");
        }

        for (Cesl_detalleSolicitud detalle : listaDetalles) {
            numtotalCertificaciones += detalle.getCantidad();
            if (Objects.equals(detalle.getTipo_docu(), getTipo_docu())
                    && Objects.equals(detalle.getNume_docu(), Long.parseLong(getNume_docu()))
                    && Objects.equals(detalle.getTipo_certifica(), this.tipo_certi)) {
                isValid = false;
                AddErrorMessage(String.format("La identificación %s %s con el tipo de certificación %s ya se encuentra agregada en la lista de solicitudes.", getTipo_docu(), this.nume_docu, getTipoCertificadoDescripcion()));
            }
        }

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
            detalle.setTipo_docu(getTipo_docu());
            detalle.setNume_docu(Long.parseLong(getNume_docu()));
            detalle.setTipo_certifica(tipo_certi);
            detalle.setTipo_certifica_descripcion(getTipoCertificadoDescripcion());
            detalle.setAnos(getRango());
            detalle.setCantidad(getCantidad());
            detalle.setValor(0D);
            setValorTotalPagar(getValorTotalPagar() + detalle.getValor());
            listaDetalles.add(detalle);
            limpiarForm();
        }
    }

    private String getTipoCertificadoDescripcion() throws Exception {
        List<Referencia> tipos = getListaTiposCertificado();
        for (Referencia referencia : tipos) {
            if (referencia.getCodigo().equals(tipo_certi)) {
                return referencia.getValor();
            }
        }
        return null;
    }

    public void limpiarForm() {
        this.tipo_certi = null;
        this.tipo_docu = null;
        this.nume_docu = null;
        this.cantidad = null;
        this.rango = null;
    }

    public List<Referencia> getTraerTipoDocumento() throws IOException {
        if (listaTiposDocumento == null) {
            listaTiposDocumento =  Utility.GetReferenciaWS(TipoReferenciaEnum.TIPO_DOCUMENTO_PERSONA);
        }
        return listaTiposDocumento;
    }

    @PostConstruct
    public void init() {
        setShowPanelAgregar(true);
        setIsSuccess(false);
    }

    public List<Referencia> getListaTiposCertificado() throws Exception {
        if (listaTiposCertificado == null) {
            listaTiposCertificado =  Utility.GetReferenciaWS("TIPOCERT_SEDELECTRO");
        }
        return listaTiposCertificado;
    }

    /**
     * @return the tipo_docu
     */
    public String getTipo_docu() {
        return tipo_docu;
    }

    /**
     * @param tipo_docu the tipo_docu to set
     */
    public void setTipo_docu(String tipo_docu) {
        this.tipo_docu = tipo_docu;
    }

    /**
     * @return the nume_docu
     */
    public String getNume_docu() {
        String numDoc = this.nume_docu;
        if (numDoc != null) {
            numDoc = numDoc.replace(".", "");
        }
        return numDoc;
    }

    /**
     * @param nume_docu the nume_docu to set
     */
    public void setNume_docu(String nume_docu) {
        this.nume_docu = nume_docu;
    }

    /**
     * @return the tipo_certi
     */
    public String getTipo_certi() {
        return tipo_certi;
    }

    /**
     * @param tipo_certi the tipo_certi to set
     */
    public void setTipo_certi(String tipo_certi) {
        this.tipo_certi = tipo_certi;
    }

    /**
     * @return the cantidad
     */
    public Integer getCantidad() {
        return cantidad;
    }

    /**
     * @param cantidad the cantidad to set
     */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * @return the rango
     */
    public Integer getRango() {
        return rango;
    }

    /**
     * @param rango the rango to set
     */
    public void setRango(Integer rango) {
        this.rango = rango;
    }
}
