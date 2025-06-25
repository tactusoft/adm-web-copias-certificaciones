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

import java.time.LocalDate;
public class Cesl_PersonaCamara {
    
    private int idsecrecamaras;
    private String tipo_docu;
    private int nume_docu;
    private String nomb_perso;
    private Long iden_pers; //idcamara
    private LocalDate fecha_acto;
    private LocalDate fecha_retiro;
    private String cargo;
    private String docu_nombra;
    private String nume_acto;
    private String rol;
    private boolean  isEnabled;
    private String estado;

    public Cesl_PersonaCamara(int idsecrecamaras, String tipo_docu, int nume_docu, String nomb_perso, Long iden_pers, LocalDate fecha_acto, LocalDate fecha_retiro, String cargo, String docu_nombra, String nume_acto, String rol, boolean isEnabled, String estado) {
        this.idsecrecamaras = idsecrecamaras;
        this.tipo_docu = tipo_docu;
        this.nume_docu = nume_docu;
        this.nomb_perso = nomb_perso;
        this.iden_pers = iden_pers;
        this.fecha_acto = fecha_acto;
        this.fecha_retiro = fecha_retiro;
        this.cargo = cargo;
        this.docu_nombra = docu_nombra;
        this.nume_acto = nume_acto;
        this.rol = rol;
        this.isEnabled = isEnabled;
        this.estado = estado;
    }

  

    
    public Cesl_PersonaCamara() {
        isEnabled=false;
    }

    /**
     * @return the idsecrecamaras
     */
    public int getIdsecrecamaras() {
        return idsecrecamaras;
    }

    /**
     * @param idsecrecamaras the idsecrecamaras to set
     */
    public void setIdsecrecamaras(int idsecrecamaras) {
        this.idsecrecamaras = idsecrecamaras;
    }

    /**
     * @return the tipo_docu
     */
    public String getTipo_docu() {
        return tipo_docu;
    }

    /**
     * @param tipo_docu the tipo_docu to set
     */
    public void setTipo_docu(String tipo_docu) {
        this.tipo_docu = tipo_docu;
    }

    /**
     * @return the nume_docu
     */
    public int getNume_docu() {
        return nume_docu;
    }

    /**
     * @param nume_docu the nume_docu to set
     */
    public void setNume_docu(int nume_docu) {
        this.nume_docu = nume_docu;
    }

    /**
     * @return the nomb_perso
     */
    public String getNomb_perso() {
        return nomb_perso;
    }

    /**
     * @param nomb_perso the nomb_perso to set
     */
    public void setNomb_perso(String nomb_perso) {
        this.nomb_perso = nomb_perso;
    }

    /**
     * @return the iden_pers
     */
    public Long getIden_pers() {
        return iden_pers;
    }

    /**
     * @param iden_pers the iden_pers to set
     */
    public void setIden_pers(Long iden_pers) {
        this.iden_pers = iden_pers;
    }

    /**
     * @return the fecha_acto
     */
    public LocalDate getFecha_acto() {
        return fecha_acto;
    }

    /**
     * @param fecha_acto the fecha_acto to set
     */
    public void setFecha_acto(LocalDate fecha_acto) {
        this.fecha_acto = fecha_acto;
    }

    /**
     * @return the fecha_retiro
     */
    public LocalDate getFecha_retiro() {
        return fecha_retiro;
    }

    /**
     * @param fecha_retiro the fecha_retiro to set
     */
    public void setFecha_retiro(LocalDate fecha_retiro) {
        this.fecha_retiro = fecha_retiro;
    }

    /**
     * @return the cargo
     */
    public String getCargo() {
        return cargo;
    }

    /**
     * @param cargo the cargo to set
     */
    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    /**
     * @return the docu_nombra
     */
    public String getDocu_nombra() {
        return docu_nombra;
    }

    /**
     * @param docu_nombra the docu_nombra to set
     */
    public void setDocu_nombra(String docu_nombra) {
        this.docu_nombra = docu_nombra;
    }

    /**
     * @return the nume_acto
     */
    public String getNume_acto() {
        return nume_acto;
    }

    /**
     * @param nume_acto the nume_acto to set
     */
    public void setNume_acto(String nume_acto) {
        this.nume_acto = nume_acto;
    }

    /**
     * @return the rol
     */
    public String getRol() {
        return rol;
    }

    /**
     * @param rol the rol to set
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * @return the isEnabled
     */
    public boolean isIsEnabled() {
        return isEnabled;
    }
    
    /**
     * @param isEnabled the isEnabled to set
     */
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * @return the estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    
    
}
