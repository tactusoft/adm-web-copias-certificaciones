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
import jakarta.inject.Inject;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;

@Named("confirmarPagoBean")
@ViewScoped
public class ConfirmarPagoBean extends BeanBase implements Serializable {

    private static final long serialVersionUID = 7765876811740798583L;

    private String billID;
    private String transactionState;

    @Inject
    private ProcesarPagoBean procesarPagoBean;

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

        logger.info("CONFIRMAR PAGO Metodo = " + getRequest().getMethod());
        logger.info("CONFIRMAR PAGO URL = " + getRequest().getRequestURI());
        logger.info("CONFIRMAR PAGO Map Size = " + String.valueOf(map.size()));

        LocalDateTime current = LocalDateTime.now();
        if (map.size() > 0) {
            map.entrySet().forEach((entry) -> {
                logger.info("Info " + current.toString() + " confirmarPagoBean Map -> " + entry.getKey() + " = " + entry.getValue());
            });

            // Procesar el pago si tenemos los parámetros necesarios
            if (map.containsKey("TicketId") && map.containsKey("TransactionState")) {
                try {
                    Integer ticketId = Integer.parseInt(map.get("TicketId"));
                    this.billID = String.valueOf(ticketId);

                    logger.info("Procesando pago desde confirmarPagoBean - TicketId: " + ticketId);

                    // Obtener el tipo de transacción
                    EstadoTramite estadoTransaccion = EstadoTramite.fromDescription(map.get("TransactionState"));

                    // Determinar si fue exitoso
                    if (estadoTransaccion == EstadoTramite.OK) {
                        this.transactionState = "Transacción exitosa. Su pago ha sido procesado correctamente.";
                    } else {
                        this.transactionState = "Estado de la transacción: " + map.get("TransactionState");
                    }

                    // Procesar el pago usando el mismo método que ProcesarPagoBean
                    Integer response = HttpServletResponse.SC_PRECONDITION_FAILED;
                    try (Dal dal = new Dal()) {
                        Cesl_tramite tramite = dal.getTramiteFromInvoice(ticketId);
                        if (tramite != null) {
                            if (tramite.getIdtiposolicitud() == TipoTramite.COPIAS_SIMPLES) {
                                response = procesarPagoBean.registrarPagoCopiasSimples(map);
                            } else {
                                response = procesarPagoBean.registrarPagoOtros(map);
                            }

                            if (response == HttpServletResponse.SC_OK) {
                                logger.info("Pago procesado exitosamente desde confirmarPagoBean - TicketId: " + ticketId);
                            } else {
                                logger.error("Error procesando pago desde confirmarPagoBean - Response: " + response);
                            }
                        } else {
                            logger.error("No se encontró el trámite para TicketId: " + ticketId);
                            this.AddErrorMessage("No se pudo encontrar la información del trámite.");
                        }
                    } catch (Exception e) {
                        logger.error("Error procesando pago en confirmarPagoBean", e);
                        this.AddErrorMessage("Ocurrió un error al procesar el pago: " + e.getMessage());
                    }

                } catch (NumberFormatException e) {
                    logger.error("Error convirtiendo TicketId a número", e);
                    this.AddWarningMessage("Su transacción se encuentra pendiente de confirmación. Por favor verifique el estado de la transacción en la lista de solicitudes en unos minutos.");
                }
            } else {
                logger.warn("Map contiene parámetros pero no tiene TicketId o TransactionState");
                this.AddWarningMessage("Su transacción se encuentra pendiente de confirmación. Por favor verifique el estado de la transacción en la lista de solicitudes en unos minutos.");
            }
        } else {
            logger.warn("Map vacío - No se recibieron parámetros de la pasarela de pago");
            this.AddWarningMessage("Su transacción se encuentra pendiente de confirmación. Por favor verifique el estado de la transacción en la lista de solicitudes en unos minutos.");
        }
    }
}
