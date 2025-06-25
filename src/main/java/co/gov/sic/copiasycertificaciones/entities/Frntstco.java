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
public class Frntstco {
    private Short fcncpto;
    private String nomb_conc;

    public Frntstco() {
    }

    public Frntstco(Short fcncpto, String nomb_conc) {
        this.fcncpto = fcncpto;
        this.nomb_conc = nomb_conc;
    }

    /**
     * @return the fcncpto
     */
    public Short getFcncpto() {
        return fcncpto;
    }

    /**
     * @param fcncpto the fcncpto to set
     */
    public void setFcncpto(Short fcncpto) {
        this.fcncpto = fcncpto;
    }

    /**
     * @return the nomb_conc
     */
    public String getNomb_conc() {
        return nomb_conc;
    }

    /**
     * @param nomb_conc the nomb_conc to set
     */
    public void setNomb_conc(String nomb_conc) {
        this.nomb_conc = nomb_conc;
    }
    
    
    
}
