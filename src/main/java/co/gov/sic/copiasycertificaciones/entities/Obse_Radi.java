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
public class Obse_Radi {

   
    private int ano_radi;
    private int nume_radi;
    private int cons_radi;
    private String cont_radi;
    private int cons_obse;
    private String text_obse;

    public Obse_Radi() {
    }

    public Obse_Radi(int ano_radi, int nume_radi, int cons_radi, String cont_radi, int cons_obse, String text_obse) {
        this.ano_radi = ano_radi;
        this.nume_radi = nume_radi;
        this.cons_radi = cons_radi;
        this.cont_radi = cont_radi;
        this.cons_obse = cons_obse;
        this.text_obse = text_obse;
    }
     /**
     * @return the ano_radi
     */
    public int getAno_radi() {
        return ano_radi;
    }

    /**
     * @param ano_radi the ano_radi to set
     */
    public void setAno_radi(int ano_radi) {
        this.ano_radi = ano_radi;
    }

    /**
     * @return the nume_radi
     */
    public int getNume_radi() {
        return nume_radi;
    }

    /**
     * @param nume_radi the nume_radi to set
     */
    public void setNume_radi(int nume_radi) {
        this.nume_radi = nume_radi;
    }

    /**
     * @return the cons_radi
     */
    public int getCons_radi() {
        return cons_radi;
    }

    /**
     * @param cons_radi the cons_radi to set
     */
    public void setCons_radi(int cons_radi) {
        this.cons_radi = cons_radi;
    }

    /**
     * @return the cont_radi
     */
    public String getCont_radi() {
        return cont_radi;
    }

    /**
     * @param cont_radi the cont_radi to set
     */
    public void setCont_radi(String cont_radi) {
        this.cont_radi = cont_radi;
    }

    /**
     * @return the cons_obse
     */
    public int getCons_obse() {
        return cons_obse;
    }

    /**
     * @param cons_obse the cons_obse to set
     */
    public void setCons_obse(int cons_obse) {
        this.cons_obse = cons_obse;
    }

    /**
     * @return the text_obse
     */
    public String getText_obse() {
        return text_obse;
    }

    /**
     * @param text_obse the text_obse to set
     */
    public void setText_obse(String text_obse) {
        this.text_obse = text_obse;
    }
    
    
}
