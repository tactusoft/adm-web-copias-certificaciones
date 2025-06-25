package co.gov.sic.copiasycertificaciones.ws.client.soap;

import java.util.List;

public class RegistroRecaudoResponse {

    protected long codigo;

    protected String mensaje;

    protected Short anioTransaccion;

    protected Integer numeroTransaccion;

    protected List<ReciboCajaRecaudo> recibos;

    public long getCodigo() {
        return codigo;
    }

    public void setCodigo(long value) {
        this.codigo = value;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String value) {
        this.mensaje = value;
    }

    public Short getAnioTransaccion() {
        return anioTransaccion;
    }

    public void setAnioTransaccion(Short value) {
        this.anioTransaccion = value;
    }

    public Integer getNumeroTransaccion() {
        return numeroTransaccion;
    }

    public void setNumeroTransaccion(Integer value) {
        this.numeroTransaccion = value;
    }

    public List<ReciboCajaRecaudo> getRecibos() {
        return recibos;
    }

    public void setRecibos(List<ReciboCajaRecaudo> value) {
        this.recibos = value;
    }
}
