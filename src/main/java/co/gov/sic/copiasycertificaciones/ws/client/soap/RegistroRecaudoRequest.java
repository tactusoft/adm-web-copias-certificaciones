package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.util.List;

public class RegistroRecaudoRequest {

    protected String usuario;

    protected String password;

    protected long idenPersConsignatario;

    protected List<PagoRecaudo> pagosConsignatario;

    protected List<ConceptoRecaudo> conceptosPagos;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String value) {
        this.usuario = value;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public long getIdenPersConsignatario() {
        return idenPersConsignatario;
    }

    public void setIdenPersConsignatario(long value) {
        this.idenPersConsignatario = value;
    }

    public List<PagoRecaudo> getPagosConsignatario() {
        return pagosConsignatario;
    }

    public void setPagosConsignatario(List<PagoRecaudo> value) {
        this.pagosConsignatario = value;
    }

    public List<ConceptoRecaudo> getConceptosPagos() {
        return conceptosPagos;
    }

    public void setConceptosPagos(List<ConceptoRecaudo> value) {
        this.conceptosPagos = value;
    }
}
