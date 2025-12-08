package co.gov.sic.copiasycertificaciones.rest;

public class ReciboCajaRecaudo {

	protected Integer numeroRecibo;
	protected Short anioRadicacion;
	protected Integer numeroRadicacion;
	protected String controlRadicacion;
	protected Short consecutivoRadicacion;

	public Integer getNumeroRecibo() {
		return numeroRecibo;
	}

	public void setNumeroRecibo(Integer numeroRecibo) {
		this.numeroRecibo = numeroRecibo;
	}

	public Short getAnioRadicacion() {
		return anioRadicacion;
	}

	public void setAnioRadicacion(Short anioRadicacion) {
		this.anioRadicacion = anioRadicacion;
	}

	public Integer getNumeroRadicacion() {
		return numeroRadicacion;
	}

	public void setNumeroRadicacion(Integer numeroRadicacion) {
		this.numeroRadicacion = numeroRadicacion;
	}

	public String getControlRadicacion() {
		return controlRadicacion;
	}

	public void setControlRadicacion(String controlRadicacion) {
		this.controlRadicacion = controlRadicacion;
	}

	public Short getConsecutivoRadicacion() {
		return consecutivoRadicacion;
	}

	public void setConsecutivoRadicacion(Short consecutivoRadicacion) {
		this.consecutivoRadicacion = consecutivoRadicacion;
	}

}
