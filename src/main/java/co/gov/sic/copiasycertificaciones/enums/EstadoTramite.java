package co.gov.sic.copiasycertificaciones.enums;

import static co.gov.sic.copiasycertificaciones.util.Constantes.*;
import co.gov.sic.copiasycertificaciones.util.Utility;

public enum EstadoTramite {
    //Estados de la Pasarela
    UNKNOW("UNKNOW", -1),
    CREATED(ESTADO_TRAMITE_CREATED, 0, ESTADO_TRAMITE_CREATED_DESC),
    PENDING(ESTADO_TRAMITE_PENDING, 1, ESTADO_TRAMITE_PENDING_DESC),
    FAILED(ESTADO_TRAMITE_FAILED, 2, ESTADO_TRAMITE_FAILED_DESC),
    EXPIRED(ESTADO_TRAMITE_EXPIRED, 3, ESTADO_TRAMITE_EXPIRED_DESC),
    CAPTURED(ESTADO_TRAMITE_CAPTURED, 4, ESTADO_TRAMITE_CAPTURED_DESC),
    NOT_AUTHORIZED(ESTADO_TRAMITE_NOT_AUTHORIZED, 5, ESTADO_TRAMITE_NOT_AUTHORIZED_DESC),
    OK(ESTADO_TRAMITE_OK, 6, ESTADO_TRAMITE_OK_DESC),
    //Estados de la aplicacion
    PRESENTADO(ESTADO_TRAMITE_PRESENTADO, 7),
    PENDIENTE_CONFIRMACION_PAGO(ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO, 8),
    RECIBO_CAJA(ESTADO_TRAMITE_RECIBO_CAJA, 9),
    RADICADO_ENTRADA(ESTADO_TRAMITE_RADICADO_ENTRADA, 10),
    GENERADO(ESTADO_TRAMITE_CERTIFICADO_GENERADO, 11),
    RADICADO_SALIDA(ESTADO_TRAMITE_RADICADO_SALIDA, 12),
    FINALIZADO(ESTADO_TRAMITE_FINALIZADO, 13),
    ASIGNADA(ESTADO_TRAMITE_ASIGNADA, 14, ESTADO_TRAMITE_ASIGNADA),
    COMPLEMENTAR(ESTADO_TRAMITE_COMPLEMENTAR, 15, ESTADO_TRAMITE_COMPLEMENTAR),
    DESISTIDA(ESTADO_TRAMITE_DESISTIDA, 16, ESTADO_TRAMITE_DESISTIDA),
    TRASLADO(ESTADO_TRAMITE_TRASLADO_COMPETENCIA, 17, ESTADO_TRAMITE_TRASLADO_COMPETENCIA),
    RTA_SOLICITANTE(ESTADO_TRAMITE_RTA_SOLICITANTE, 18, ESTADO_TRAMITE_RTA_SOLICITANTE),
    SOL_PRORROGA(ESTADO_TRAMITE_SOL_PRORROGA, 19, ESTADO_TRAMITE_SOL_PRORROGA),
    RTA_SOL_PRORROGA(ESTADO_TRAMITE_RTA_SOL_PRORROGA, 20, ESTADO_TRAMITE_RTA_SOL_PRORROGA),
    SOL_INFO_AREA_INTERNA(ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA, 21, ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA),
    DIGITALIZACION(ESTADO_TRAMITE_RTA_SOL_DIGI_INFO, 22, ESTADO_TRAMITE_RTA_SOL_DIGI_INFO),
    COTIZACION_ENVIADA(ESTADO_TRAMITE_COTIZACION_ENVIADA, 23, ESTADO_TRAMITE_COTIZACION_ENVIADA),
    RTA_INFO_AREA_INTERNA(ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA, 24, ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA),
    PAGADO(ESTADO_TRAMITE_PAGADO, 25, ESTADO_TRAMITE_PAGADO),
	RADICADO_FIRMA_ELECTRONICA(ESTADO_TRAMITE_FIRMA_ELECTRONICA, 26, ESTADO_TRAMITE_FIRMA_ELECTRONICA);

    private final String description;

    private final String description2;

    private final int value;

    EstadoTramite(String desc, int val, String desc2) {
        this.description = desc;
        this.description2 = desc2;
        this.value = val;
    }

    EstadoTramite(String desc, int val) {
        this(desc, val, null);
    }

    public String getDescription() {
        return description;
    }

    public String getDescription2() {
        if (Utility.isNullOrEmptyTrim(description2)) {
            return description;
        } else {
            return description2;
        }
    }

    public int getValue() {
        return value;
    }

    public static EstadoTramite fromDescription(String desc) {
        if (!Utility.isNullOrEmptyTrim(desc)) {
            for (EstadoTramite c : EstadoTramite.values()) {
                if (c.description.equals(desc.trim())) {
                    return c;
                }
            }
        }
        throw new IllegalArgumentException(desc);
    }

    public static EstadoTramite fromValue(int val) {
        for (EstadoTramite c : EstadoTramite.values()) {
            if (c.getValue() == val) {
                return c;
            }
        }
        return EstadoTramite.UNKNOW;
    }
}
