package co.gov.sic.copiasycertificaciones.rest;

public class RegistroRecaudoResponse {

	protected Long codigo;

	protected String mensaje;

	protected Short anioTransaccion;

	protected Integer numeroTransaccion;

	protected ReciboCajaRecaudoList recibos;

	public Long getCodigo() {
		return codigo;
	}

	public void setCodigo(Long value) {
		this.codigo = value;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String value) {
		this.mensaje = value;
	}

	public Short getAnioTransaccion() {
		return anioTransaccion;
	}

	public void setAnioTransaccion(Short value) {
		this.anioTransaccion = value;
	}

	public Integer getNumeroTransaccion() {
		return numeroTransaccion;
	}

	public void setNumeroTransaccion(Integer value) {
		this.numeroTransaccion = value;
	}

	public ReciboCajaRecaudoList getRecibos() {
		return recibos;
	}

	public void setRecibos(ReciboCajaRecaudoList value) {
		this.recibos = value;
	}
}
