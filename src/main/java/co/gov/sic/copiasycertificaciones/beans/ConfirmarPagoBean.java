/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named("confirmarPagoBean")
@ViewScoped
public class ConfirmarPagoBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 7765876811740798583L;

    private String billID;
    private String transactionState;

    public String getBillID() {
        return this.billID;
    }

    public void setBillID(String val) {
        this.billID = val;
    }

    public String getTransactionState() {
        return this.transactionState;
    }

    public void setTransactionState(String val) {
        this.transactionState = val;
    }

    public ConfirmarPagoBean() throws Exception {
        super();
        this.transactionState = EstadoTramite.PENDIENTE_CONFIRMACION_PAGO.getDescription2();
    }

    @PostConstruct
    public void init() {
        Map<String, String> map = getExternalContext().getRequestParameterMap();
        // if (Constantes.AMBIENTE_ACTIVO != TipoAmbienteEnum.PRODUCCION) {

        logger.info("CONFIRMAR PAGO Metodo = " + getRequest().getMethod());
        logger.info("CONFIRMAR PAGO URL = " + getRequest().getRequestURI());
        logger.info("CONFIRMAR PAGO Map Size = " + String.valueOf(map.size()));
        //}
        LocalDateTime current = LocalDateTime.now();
        if (map.size() > 0) {
            // if (Constantes.AMBIENTE_ACTIVO != TipoAmbienteEnum.PRODUCCION) {
            map.entrySet().forEach((entry) -> {
                logger.info("Info " + current.toString() + " confirmarPagoBean Map -> " + entry.getKey() + " = " + entry.getValue());
            });
            //}
        } else {
            this.AddWarningMessage("Su transacción se encuentra pendiente de confirmación. Por favor verifique el estado de la transacción en la lista de solicitudes en unos minutos.");
        }
    }
}
