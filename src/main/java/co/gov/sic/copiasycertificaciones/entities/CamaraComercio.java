package co.gov.sic.copiasycertificaciones.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CamaraComercio {

    private Integer idenPers;
    private String nombre;
    private String tipoDocumento;
    private Long numeroDocumento;
    private String numeroDocumentoDesc;
    private String digitoVerificacion;
    private Integer numeroDecreto;
    private LocalDate fechaDecreto;
    private PersonaCamara representante;
    private List<PersonaCamara> suplentes;
    private List<PersonaCamara> secretarios;
    private PersonaCamara representanteJudicial;

    public CamaraComercio() {
        suplentes = new ArrayList<>();
        secretarios = new ArrayList<>();
    }

    public Integer getIdenPers() {
        return idenPers;
    }

    public void setIdenPers(Integer val) {
        idenPers = val;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String val) {
        nombre = val;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String val) {
        tipoDocumento = val;
    }

    public Long getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(Long val) {
        numeroDocumento = val;
    }

    public String getDigitoVerificacion() {
        return digitoVerificacion;
    }

    public void setDigitoVerificacion(String val) {
        digitoVerificacion = val;
    }

    public Integer getNumeroDecreto() {
        return numeroDecreto;
    }

    public void setNumeroDecreto(Integer val) {
        numeroDecreto = val;
    }

    public LocalDate getFechaDecreto() {
        return fechaDecreto;
    }

    public void setFechaDecreto(LocalDate val) {
        fechaDecreto = val;
    }

    public String getNumeroDocumentoDesc() {
        return this.numeroDocumentoDesc;
    }

    public void setNumeroDocumentoDesc(String val) {
        this.numeroDocumentoDesc = val;
    }

    public PersonaCamara getRepresentante() {
        return this.representante;
    }

    public void setRepresentante(PersonaCamara val) {
        this.representante = val;
    }

    public List<PersonaCamara> getSuplentes() {
        return this.suplentes;
    }

    public void setSuplentes(List<PersonaCamara> val) {
        this.suplentes = val;
    }

    public List<PersonaCamara> getSecretarios() {
        return this.secretarios;
    }

    public void setSecretarios(List<PersonaCamara> val) {
        this.secretarios = val;
    }

    /**
     * @return the representanteJudicial
     */
    public PersonaCamara getRepresentanteJudicial() {
        return representanteJudicial;
    }

    /**
     * @param representanteJudicial the representanteJudicial to set
     */
    public void setRepresentanteJudicial(PersonaCamara representanteJudicial) {
        this.representanteJudicial = representanteJudicial;
    }
    
    
}
