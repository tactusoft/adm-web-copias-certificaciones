package co.gov.sic.copiasycertificaciones.enums;

public enum TipoAmbienteEnum {
    DESARROLLO(0), PRUEBAS(1), PRODUCCION(2);

    private final int value;

    private TipoAmbienteEnum(int v) {
        value = v;
    }

    public int value() {
        return value;
    }

    public static TipoAmbienteEnum fromValue(int v) {
        for (TipoAmbienteEnum c : TipoAmbienteEnum.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }
}
