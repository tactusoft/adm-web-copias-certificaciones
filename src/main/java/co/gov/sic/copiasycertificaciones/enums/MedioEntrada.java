package co.gov.sic.copiasycertificaciones.enums;

public enum MedioEntrada {
	FISICO(1), MAGNETICO(2), DIGITAL(3);

	private final int value;

	private MedioEntrada(int v) {
		value = v;
	}

	public int value() {
		return value;
	}

	public static MedioEntrada fromValue(int v) {
		for (MedioEntrada c : MedioEntrada.values()) {
			if (c.value == v) {
				return c;
			}
		}
		throw new IllegalArgumentException(String.valueOf(v));
	}
}
