/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author emosquera
 */
public abstract class ConfigurationBeanInit {
	
	protected final Logger logger = LoggerFactory.getLogger(LoadFilesBean.class);
    
    public ConfigurationBeanInit() {
    	logger.info("Inicio del bean de configuracion");
    }
    
}
