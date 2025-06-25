/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.Serializable;

import co.gov.sic.copiasycertificaciones.util.NavegaUsuarioMB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.inject.Named;

/**
 *
 * @author c.ibello
 */
@Named("tramitesMB")
@SessionScoped
public class TramitesMB implements Serializable {

    private static final long serialVersionUID = -4035010625499621229L;
	//Management Bean
    @ManagedProperty(value = "#{navegaUsuarioMB}")
    private NavegaUsuarioMB navegaUsuarioMB;

    /**
     * Creates a new instance of TramitesMB
     */
    public TramitesMB() {
    }

    /**
     * @return the navegaUsuarioMB
     */
    public NavegaUsuarioMB getNavegaUsuarioMB() {
        return navegaUsuarioMB;
    }

    /**
     * @param navegaUsuarioMB the navegaUsuarioMB to set
     */
    public void setNavegaUsuarioMB(NavegaUsuarioMB navegaUsuarioMB) {
        this.navegaUsuarioMB = navegaUsuarioMB;
    }
}
