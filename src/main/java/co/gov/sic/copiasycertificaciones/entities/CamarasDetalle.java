/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.entities;

import java.util.Date;

/**
 *
 * @author emosq
 */
public class CamarasDetalle {
    
private Long iden_pers ;
private int num_decre;
private Date fech_decre;
private boolean estado;


    public CamarasDetalle() {
       estado=true;
    }

    public CamarasDetalle(Long iden_pers, int num_decre, Date fech_decre,boolean estado) {
        this.iden_pers = iden_pers;
        this.num_decre = num_decre;
        this.fech_decre = fech_decre;
        this.estado = estado;
        
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
     * @return the num_decre
     */
    public int getNum_decre() {
        return num_decre;
    }

    /**
     * @param num_decre the num_decre to set
     */
    public void setNum_decre(int num_decre) {
        this.num_decre = num_decre;
    }

    /**
     * @return the fech_decre
     */
    public Date getFech_decre() {
        return fech_decre;
    }

    /**
     * @param fech_decre the fech_decre to set
     */
    public void setFech_decre(Date fech_decre) {
        this.fech_decre = fech_decre;
    }

    /**
     * @return the estado
     */
    public boolean getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(boolean estado) {
        this.estado = estado;
    }


    
    

}
