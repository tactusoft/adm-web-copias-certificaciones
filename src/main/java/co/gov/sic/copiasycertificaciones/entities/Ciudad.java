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
public class Ciudad {
private String codi_pais;
private int codi_ciud;
private int codi_regi;
private String nomb_ciud;
private String codi_dane;
private String esta_ciud;

    public Ciudad() {
    }

    public Ciudad(String codi_pais, int codi_ciud, int codi_regi, String nomb_ciud, String codi_dane, String esta_ciud) {
        this.codi_pais = codi_pais;
        this.codi_ciud = codi_ciud;
        this.codi_regi = codi_regi;
        this.nomb_ciud = nomb_ciud;
        this.codi_dane = codi_dane;
        this.esta_ciud = esta_ciud;
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
     * @return the codi_ciud
     */
    public int getCodi_ciud() {
        return codi_ciud;
    }

    /**
     * @param codi_ciud the codi_ciud to set
     */
    public void setCodi_ciud(int codi_ciud) {
        this.codi_ciud = codi_ciud;
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
     * @return the nomb_ciud
     */
    public String getNomb_ciud() {
        return nomb_ciud;
    }

    /**
     * @param nomb_ciud the nomb_ciud to set
     */
    public void setNomb_ciud(String nomb_ciud) {
        this.nomb_ciud = nomb_ciud;
    }

    /**
     * @return the codi_dane
     */
    public String getCodi_dane() {
        return codi_dane;
    }

    /**
     * @param codi_dane the codi_dane to set
     */
    public void setCodi_dane(String codi_dane) {
        this.codi_dane = codi_dane;
    }

    /**
     * @return the esta_ciud
     */
    public String getEsta_ciud() {
        return esta_ciud;
    }

    /**
     * @param esta_ciud the esta_ciud to set
     */
    public void setEsta_ciud(String esta_ciud) {
        this.esta_ciud = esta_ciud;
    }

    


}
