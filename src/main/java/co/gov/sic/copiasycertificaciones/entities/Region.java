/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.entities;

/**
 *
 * @author emosq
 */
public class Region {



private int codi_regi;
private String codi_pais;
private String nemo_regi;
private String nomb_regi;

    public Region() {
    }

    public Region(int codi_regi, String codi_pais, String nemo_regi, String nomb_regi) {
        this.codi_regi = codi_regi;
        this.codi_pais = codi_pais;
        this.nemo_regi = nemo_regi;
        this.nomb_regi = nomb_regi;
    }

    /**
     * @return the codi_regi
     */
    public int getCodi_regi() {
        return codi_regi;
    }

    /**
     * @param codi_regi the codi_regi to set
     */
    public void setCodi_regi(int codi_regi) {
        this.codi_regi = codi_regi;
    }

    /**
     * @return the codi_pais
     */
    public String getCodi_pais() {
        return codi_pais;
    }

    /**
     * @param codi_pais the codi_pais to set
     */
    public void setCodi_pais(String codi_pais) {
        this.codi_pais = codi_pais;
    }

    /**
     * @return the nemo_regi
     */
    public String getNemo_regi() {
        return nemo_regi;
    }

    /**
     * @param nemo_regi the nemo_regi to set
     */
    public void setNemo_regi(String nemo_regi) {
        this.nemo_regi = nemo_regi;
    }

    /**
     * @return the nomb_regi
     */
    public String getNomb_regi() {
        return nomb_regi;
    }

    /**
     * @param nomb_regi the nomb_regi to set
     */
    public void setNomb_regi(String nomb_regi) {
        this.nomb_regi = nomb_regi;
    }


    

    
}
