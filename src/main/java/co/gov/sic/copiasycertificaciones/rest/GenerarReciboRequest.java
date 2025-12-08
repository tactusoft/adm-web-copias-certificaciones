package co.gov.sic.copiasycertificaciones.rest;

public class GenerarReciboRequest {

	private short anioTransaccion;
	private int numTransaccion;
	private String tipoSeleccion;
	private int numRecibo;
	private String usuario;
	private boolean copia;

	public short getAnioTransaccion() {
		return anioTransaccion;
	}

	public void setAnioTransaccion(short anioTransaccion) {
		this.anioTransaccion = anioTransaccion;
	}

	public int getNumTransaccion() {
		return numTransaccion;
	}

	public void setNumTransaccion(int numTransaccion) {
		this.numTransaccion = numTransaccion;
	}

	public String getTipoSeleccion() {
		return tipoSeleccion;
	}

	public void setTipoSeleccion(String tipoSeleccion) {
		this.tipoSeleccion = tipoSeleccion;
	}

	public int getNumRecibo() {
		return numRecibo;
	}

	public void setNumRecibo(int numRecibo) {
		this.numRecibo = numRecibo;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public boolean isCopia() {
		return copia;
	}

	public void setCopia(boolean copia) {
		this.copia = copia;
	}

}