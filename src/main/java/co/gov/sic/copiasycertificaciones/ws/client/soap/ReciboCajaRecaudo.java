package co.gov.sic.copiasycertificaciones.ws.client.soap;

public class ReciboCajaRecaudo {

    protected int numeroRecibo;
    protected short anioRadicacion;
    protected Integer numeroRadicacion;
    protected String controlRadicacion;
    protected Short consecutivoRadicacion;

    public int getNumeroRecibo() {
        return this.numeroRecibo;
    }

    public void setNumeroRecibo(int val) {
        this.numeroRecibo = val;
    }

    public short getAnioRadicacion() {
        return this.anioRadicacion;
    }

    public void setAnioRadicacion(short val) {
        this.anioRadicacion = val;
    }

    public Integer getNumeroRadicacion() {
        return this.numeroRadicacion;
    }

    public void setNumeroRadicacion(Integer val) {
        this.numeroRadicacion = val;
    }

    public String getControlRadicacion() {
        return this.controlRadicacion;
    }

    public void setControlRadicacion(String val) {
        this.controlRadicacion = val;
    }

    public Short getConsecutivoRadicacion() {
        return this.consecutivoRadicacion;
    }

    public void setConsecutivoRadicacion(Short val) {
        this.consecutivoRadicacion = val;
    }
}
