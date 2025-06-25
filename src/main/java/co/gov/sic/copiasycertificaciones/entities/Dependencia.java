/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.entities;

import java.time.LocalDateTime;

/**
 *
 * @author emosquera
 */
public class Dependencia {

    private int codi_depe;
    private String nomb_depe;
    private String noab_depe;
    private String esta_depe;
    private LocalDateTime fech_vige;
    private LocalDateTime fech_crea;
    private String tipo_acto;
    private int nume_acto;
    private LocalDateTime fech_acto;
    private String tipo_acti;
    private int nume_acti;
    private LocalDateTime fech_acti;
    private int iden_resp;
    private int iden_firm;
    private String carg_firm;
    private String depe_nomi;

    public Dependencia() {
    }

    public Dependencia(int codi_depe, String nomb_depe, String noab_depe, String esta_depe, LocalDateTime fech_vige, LocalDateTime fech_crea, String tipo_acto, int nume_acto, LocalDateTime fech_acto, String tipo_acti, int nume_acti, LocalDateTime fech_acti, int iden_resp, int iden_firm, String carg_firm, String depe_nomi) {
        this.codi_depe = codi_depe;
        this.nomb_depe = nomb_depe;
        this.noab_depe = noab_depe;
        this.esta_depe = esta_depe;
        this.fech_vige = fech_vige;
        this.fech_crea = fech_crea;
        this.tipo_acto = tipo_acto;
        this.nume_acto = nume_acto;
        this.fech_acto = fech_acto;
        this.tipo_acti = tipo_acti;
        this.nume_acti = nume_acti;
        this.fech_acti = fech_acti;
        this.iden_resp = iden_resp;
        this.iden_firm = iden_firm;
        this.carg_firm = carg_firm;
        this.depe_nomi = depe_nomi;
    }
    
    

    /**
     * @return the codi_depe
     */
    public int getCodi_depe() {
        return codi_depe;
    }

    /**
     * @param codi_depe the codi_depe to set
     */
    public void setCodi_depe(int codi_depe) {
        this.codi_depe = codi_depe;
    }

    /**
     * @return the nomb_depe
     */
    public String getNomb_depe() {
        return nomb_depe;
    }

    /**
     * @param nomb_depe the nomb_depe to set
     */
    public void setNomb_depe(String nomb_depe) {
        this.nomb_depe = nomb_depe;
    }

    /**
     * @return the noab_depe
     */
    public String getNoab_depe() {
        return noab_depe;
    }

    /**
     * @param noab_depe the noab_depe to set
     */
    public void setNoab_depe(String noab_depe) {
        this.noab_depe = noab_depe;
    }

    /**
     * @return the esta_depe
     */
    public String getEsta_depe() {
        return esta_depe;
    }

    /**
     * @param esta_depe the esta_depe to set
     */
    public void setEsta_depe(String esta_depe) {
        this.esta_depe = esta_depe;
    }

    /**
     * @return the fech_vige
     */
    public LocalDateTime getFech_vige() {
        return fech_vige;
    }

    /**
     * @param fech_vige the fech_vige to set
     */
    public void setFech_vige(LocalDateTime fech_vige) {
        this.fech_vige = fech_vige;
    }

    /**
     * @return the fech_crea
     */
    public LocalDateTime getFech_crea() {
        return fech_crea;
    }

    /**
     * @param fech_crea the fech_crea to set
     */
    public void setFech_crea(LocalDateTime fech_crea) {
        this.fech_crea = fech_crea;
    }

    /**
     * @return the tipo_acto
     */
    public String getTipo_acto() {
        return tipo_acto;
    }

    /**
     * @param tipo_acto the tipo_acto to set
     */
    public void setTipo_acto(String tipo_acto) {
        this.tipo_acto = tipo_acto;
    }

    /**
     * @return the nume_acto
     */
    public int getNume_acto() {
        return nume_acto;
    }

    /**
     * @param nume_acto the nume_acto to set
     */
    public void setNume_acto(int nume_acto) {
        this.nume_acto = nume_acto;
    }

    /**
     * @return the fech_acto
     */
    public LocalDateTime getFech_acto() {
        return fech_acto;
    }

    /**
     * @param fech_acto the fech_acto to set
     */
    public void setFech_acto(LocalDateTime fech_acto) {
        this.fech_acto = fech_acto;
    }

    /**
     * @return the tipo_acti
     */
    public String getTipo_acti() {
        return tipo_acti;
    }

    /**
     * @param tipo_acti the tipo_acti to set
     */
    public void setTipo_acti(String tipo_acti) {
        this.tipo_acti = tipo_acti;
    }

    /**
     * @return the nume_acti
     */
    public int getNume_acti() {
        return nume_acti;
    }

    /**
     * @param nume_acti the nume_acti to set
     */
    public void setNume_acti(int nume_acti) {
        this.nume_acti = nume_acti;
    }

    /**
     * @return the fech_acti
     */
    public LocalDateTime getFech_acti() {
        return fech_acti;
    }

    /**
     * @param fech_acti the fech_acti to set
     */
    public void setFech_acti(LocalDateTime fech_acti) {
        this.fech_acti = fech_acti;
    }

    /**
     * @return the iden_resp
     */
    public int getIden_resp() {
        return iden_resp;
    }

    /**
     * @param iden_resp the iden_resp to set
     */
    public void setIden_resp(int iden_resp) {
        this.iden_resp = iden_resp;
    }

    /**
     * @return the iden_firm
     */
    public int getIden_firm() {
        return iden_firm;
    }

    /**
     * @param iden_firm the iden_firm to set
     */
    public void setIden_firm(int iden_firm) {
        this.iden_firm = iden_firm;
    }

    /**
     * @return the carg_firm
     */
    public String getCarg_firm() {
        return carg_firm;
    }

    /**
     * @param carg_firm the carg_firm to set
     */
    public void setCarg_firm(String carg_firm) {
        this.carg_firm = carg_firm;
    }

    /**
     * @return the depe_nomi
     */
    public String getDepe_nomi() {
        return depe_nomi;
    }

    /**
     * @param depe_nomi the depe_nomi to set
     */
    public void setDepe_nomi(String depe_nomi) {
        this.depe_nomi = depe_nomi;
    }

}
