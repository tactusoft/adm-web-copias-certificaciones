/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

/**
 *
 *
 * @author Ernesto Luis Mosquera Heredia
 */
@Named("paymentBean")
@SessionScoped
public class PaymentBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 3774161651761374920L;
	private Cesl_tramite tramite;
    private Logger logger = LoggerFactory.getLogger(DownloadFilesBean.class);

    public PaymentBean() throws Exception {

    }

    public void cfgTramite(Cesl_tramite tramite) {
        this.tramite = tramite;
    }

    /**
     * @return the tramite
     */
    public Cesl_tramite getTramite() {
        return tramite;
    }

    /**
     * @param tramite the tramite to set
     */
    public void setTramite(Cesl_tramite tramite) {
        this.tramite = tramite;
    }

    /**
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @param logger the logger to set
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
