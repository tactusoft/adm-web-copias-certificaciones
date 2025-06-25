package co.gov.sic.copiasycertificaciones.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import sic.ws.interop.entities.Referencia;

@Named("certificadoCCBean")
@ViewScoped
public class CertificadoCCBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 1L;
	private List<Referencia> listaCamaras;
    private Integer idcamaracomercio;
    private Integer cantidad;
    private final int numMaxSolicitudes = 1;

    public int getNumMaxSolicitudes() {
        return numMaxSolicitudes;
    }

    public CertificadoCCBean() throws Exception {
        super(TipoTramite.CERTIFICADO_REPRESENTACION_CAMARAS);
    }

    @PostConstruct
    public void init() {
        setShowPanelAgregar(true);
        setIsSuccess(false);
    }

    public void addRowTable() throws Exception {
        boolean isValid = true;
        int numtotalCertificaciones = 1;
        if ((getCantidad() == null) || (getCantidad() <= 0) || (getCantidad() > 10)) {
            isValid = false;
            AddErrorMessage("La cantidad debe ser un número entero valido entre 1 y 10.");
        }
        if ((getIdcamaracomercio() == null) || ((getIdcamaracomercio() <= 0))) {
            isValid = false;
            AddErrorMessage("La cámara de comercio es requerida.");
        }
        for (Cesl_detalleSolicitud detalle : listaDetalles) {
            if (Objects.equals(detalle.getIdcamaracomercio(), getIdcamaracomercio())) {
                isValid = false;
                AddErrorMessage("Ya existe un registro para esta cámara de comercio.");
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
            detalle.setIdcamaracomercio(getIdcamaracomercio());
            CamaraComercio camara = new CamaraComercio();
            camara.setNombre(getCamaraDescripcion());
            detalle.setIdcamaracomercioData(camara);
            detalle.setCantidad(getCantidad());
            detalle.setValor(getCantidad() * this.valorUnitario);
            setValorTotalPagar(getValorTotalPagar() + detalle.getValor());
            listaDetalles.add(detalle);
            this.idcamaracomercio = null;
            this.cantidad = null;
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

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getIdcamaracomercio() {
        return idcamaracomercio;
    }

    public void setIdcamaracomercio(Integer idcamaracomercio) {
        this.idcamaracomercio = idcamaracomercio;
    }
}
