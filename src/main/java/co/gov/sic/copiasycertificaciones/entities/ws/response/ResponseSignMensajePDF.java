package co.gov.sic.copiasycertificaciones.entities.ws.response;

public class ResponseSignMensajePDF {

    private String codigo;
    private String mensaje;

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String val) {
        this.codigo = val;
    }

    public void setMensaje(String val) {
        this.mensaje = val;
    }

    public String getMensaje() {
        return this.mensaje;
    }
}
