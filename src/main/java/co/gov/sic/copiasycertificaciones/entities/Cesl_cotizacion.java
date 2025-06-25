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
public class Cesl_cotizacion {

    private int IdTramite;
    private Frntstco frntstco;
    private int fvalor;
    private int cantidad;
    private int totalConcepto;

    public Cesl_cotizacion() {
    }

    public Cesl_cotizacion(int IdTramite, Frntstco frntstco, int fvalor, int cantidad, int totalConcepto) {
        this.IdTramite = IdTramite;
        this.frntstco = frntstco;
        this.fvalor = fvalor;
        this.cantidad = cantidad;
        this.totalConcepto = totalConcepto;
    }

    /**
     * @return the IdTramite
     */
    public int getIdTramite() {
        return IdTramite;
    }

    /**
     * @param IdTramite the IdTramite to set
     */
    public void setIdTramite(int IdTramite) {
        this.IdTramite = IdTramite;
    }

    /**
     * @return the frntstco
     */
    public Frntstco getFrntstco() {
        return frntstco;
    }

    /**
     * @param frntstco the frntstco to set
     */
    public void setFrntstco(Frntstco frntstco) {
        this.frntstco = frntstco;
    }

    /**
     * @return the fvalor
     */
    public int getFvalor() {
        return fvalor;
    }

    /**
     * @param fvalor the fvalor to set
     */
    public void setFvalor(int fvalor) {
        this.fvalor = fvalor;
    }

    /**
     * @return the cantidad
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * @param cantidad the cantidad to set
     */
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    /**
     * @return the totalConcepto
     */
    public int getTotalConcepto() {
        return totalConcepto;
    }

    /**
     * @param totalConcepto the totalConcepto to set
     */
    public void setTotalConcepto(int totalConcepto) {
        this.totalConcepto = totalConcepto;
    }

    
    

    
    
    

}
