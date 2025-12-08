package co.gov.sic.copiasycertificaciones.rest;

public class RegistroRecaudoRequest {

	protected String usuario;

	protected String password;

	protected long idenPersConsignatario;

	protected PagoRecaudoList pagosConsignatario;

	protected ConceptoRecaudoList conceptosPagos;

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getIdenPersConsignatario() {
		return idenPersConsignatario;
	}

	public void setIdenPersConsignatario(long idenPersConsignatario) {
		this.idenPersConsignatario = idenPersConsignatario;
	}

	public PagoRecaudoList getPagosConsignatario() {
		return pagosConsignatario;
	}

	public void setPagosConsignatario(PagoRecaudoList pagosConsignatario) {
		this.pagosConsignatario = pagosConsignatario;
	}

	public ConceptoRecaudoList getConceptosPagos() {
		return conceptosPagos;
	}

	public void setConceptosPagos(ConceptoRecaudoList conceptosPagos) {
		this.conceptosPagos = conceptosPagos;
	}

}
