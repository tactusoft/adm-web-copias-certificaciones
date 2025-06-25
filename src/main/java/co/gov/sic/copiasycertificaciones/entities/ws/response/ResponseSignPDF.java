package co.gov.sic.copiasycertificaciones.entities.ws.response;

public class ResponseSignPDF {

    private RespuestaObjSignPDF respuestaObj;
    private Integer idTransaccion;
    private String hash;
    private String fechaFirma;
    private byte[] documento;

    public RespuestaObjSignPDF getRespuestaObj() {
        return this.respuestaObj;
    }

    public void setRespuestaObj(RespuestaObjSignPDF val) {
        this.respuestaObj = val;
    }

    public void setIdTransaccion(Integer val) {
        this.idTransaccion = val;
    }

    public Integer getIdTransaccion() {
        return this.idTransaccion;
    }

    public void setHash(String val) {
        this.hash = val;
    }

    public String getHash() {
        return this.hash;
    }

    public void setFechaFirma(String val) {
        this.fechaFirma = val;
    }

    public String getFechaFirma() {
        return this.fechaFirma;
    }

    public void setDocumento(byte[] val) {
        this.documento = val;
    }

    public byte[] getDocumento() {
        return this.documento;
    }

    public ResponseSignPDF() {
        this.respuestaObj = new RespuestaObjSignPDF();
    }
}
