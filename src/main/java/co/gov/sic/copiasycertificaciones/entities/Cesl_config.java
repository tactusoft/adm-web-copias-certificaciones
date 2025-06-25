/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.entities;

/**
 *
 * @author emosquera
 */
public class Cesl_config {

    private String llave;
    private String valor;
    private boolean businessDays;

    public Cesl_config() {
    }

    public Cesl_config(String llave, String valor, boolean businessDays) {
        this.llave = llave;
        this.valor = valor;
        this.businessDays = businessDays;
    }

    /**
     * @return the llave
     */
    public String getLlave() {
        return llave;
    }

    /**
     * @param llave the llave to set
     */
    public void setLlave(String llave) {
        this.llave = llave;
    }

    /**
     * @return the valor
     */
    public Integer getValor() {
        return Integer.valueOf(valor);
    }

    public String getValorString() {
        return valor;
    }

    /**
     * @param valor the valor to set
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * @return the businessDays
     */
    public boolean isBusinessDays() {
        return businessDays;
    }

    /**
     * @param businessDays the businessDays to set
     */
    public void setBusinessDays(boolean businessDays) {
        this.businessDays = businessDays;
    }

}
