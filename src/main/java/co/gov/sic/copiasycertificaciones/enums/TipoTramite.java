package co.gov.sic.copiasycertificaciones.enums;

import static co.gov.sic.copiasycertificaciones.util.Constantes.*;

public enum TipoTramite {

    CERTIFICADO_SANCIONES(1, NOMBRE_TRAMITE_CERTIFICADO_SANCIONES),
    CERTIFICADO_REPRESENTACION_CAMARAS(2, NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS),
    CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS(3, NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS),
    CORRECCION_REPRESENTACION_CAMARAS(4, NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS),
    COPIAS_SIMPLES(5, NOMBRE_TRAMITE_COPIAS_SIMPLES),
    LISTADOS_INFORMACION(6, NOMBRE_TRAMITE_LISTADOS_INFORMACION),
	CERTIFICADO_SANCIONES_SIN_PAGO(7, NOMBRE_TRAMITE_CERTIFICADO_SANCIONES);

    private final int value;
    private final String descripcion;

    private TipoTramite(int val, String desc) {
        this.value = val;
        this.descripcion = desc;
    }

    public int getValue() {
        return value;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public static TipoTramite fromValue(int v) {
        for (TipoTramite c : TipoTramite.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }
}
