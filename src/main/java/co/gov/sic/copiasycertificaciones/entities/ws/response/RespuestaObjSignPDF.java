package co.gov.sic.copiasycertificaciones.entities.ws.response;

public class RespuestaObjSignPDF {

    private int numFirmantes;
    private boolean verificado;
    private ResponseSignMensajePDF mensajes;

    public int getNumFirmantes() {
        return this.numFirmantes;
    }

    public void setNumFirmantes(int val) {
        this.numFirmantes = val;
    }

    public void setVerificado(boolean val) {
        this.verificado = val;
    }

    public boolean getVerificado() {
        return this.verificado;
    }

    public void setMensajes(ResponseSignMensajePDF val) {
        this.mensajes = val;
    }

    public ResponseSignMensajePDF getMensajes() {
        return this.mensajes;
    }

    public RespuestaObjSignPDF() {
        this.mensajes = new ResponseSignMensajePDF();
    }
}
