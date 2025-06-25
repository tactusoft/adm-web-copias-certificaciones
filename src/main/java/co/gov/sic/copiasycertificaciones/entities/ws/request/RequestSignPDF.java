package co.gov.sic.copiasycertificaciones.entities.ws.request;

public class RequestSignPDF {

    private String idCliente;
    private String passwordCliente;
    private Integer idPolitica;
    private String stringToFind;
    private String filePath;
    private String noPagina;

    public String getIdCliente() {
        return this.idCliente;
    }

    public void setIdCliente(String val) {
        this.idCliente = val;
    }

    public void setPasswordCliente(String val) {
        this.passwordCliente = val;
    }

    public String getPasswordCliente() {
        return this.passwordCliente;
    }

    public void setIdPolitica(Integer val) {
        this.idPolitica = val;
    }

    public Integer getIdPolitica() {
        return this.idPolitica;
    }

    public void setStringToFind(String val) {
        this.stringToFind = val;
    }

    public String getStringToFind() {
        return this.stringToFind;
    }

    public void setFilePath(String val) {
        this.filePath = val;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setNoPagina(String val) {
        this.noPagina = val;
    }

    public String getNoPagina() {
        return this.noPagina;
    }
}
