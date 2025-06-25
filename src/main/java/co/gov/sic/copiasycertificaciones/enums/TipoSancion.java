package co.gov.sic.copiasycertificaciones.enums;

public enum TipoSancion {

    Multa("SA", "Sanciones y/o Multas"),
    Demanda("DI", "Demandas y/o Investigaciones"),
    ProteccionCompetencia("PC", "Infracciones Protección y Promoción de la Competencia");

    private final String value;
    private final String descripcion;

    private TipoSancion(String val, String desc) {
        this.value = val;
        this.descripcion = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public static TipoSancion fromValue(String v) {
        for (TipoSancion c : TipoSancion.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
