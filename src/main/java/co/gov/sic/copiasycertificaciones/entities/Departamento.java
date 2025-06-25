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
public class Departamento {

    private String codi_depa;
    private int codi_regi;
    private String codi_pais;
    private String nomb_depa;

    public Departamento() {
    }

    public Departamento(String codi_depa, int codi_regi, String codi_pais, String nomb_depa) {
        this.codi_depa = codi_depa;
        this.codi_regi = codi_regi;
        this.codi_pais = codi_pais;
        this.nomb_depa = nomb_depa;
    }

    /**
     * @return the codi_depa
     */
    public String getCodi_depa() {
        return codi_depa;
    }

    /**
     * @param codi_depa the codi_depa to set
     */
    public void setCodi_depa(String codi_depa) {
        this.codi_depa = codi_depa;
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
     * @return the nomb_depa
     */
    public String getNomb_depa() {
        return nomb_depa;
    }

    /**
     * @param nomb_depa the nomb_depa to set
     */
    public void setNomb_depa(String nomb_depa) {
        this.nomb_depa = nomb_depa;
    }
    
    
    
}
