package co.gov.sic.copiasycertificaciones.entities;

import java.io.Serializable;

public class PersonaEmail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	private long idenPers;
	private String nombrePersona;
	private String tipoDocumento;
	private long numeroDocumento;
	private String direEmai;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdenPers() {
		return idenPers;
	}

	public void setIdenPers(long idenPers) {
		this.idenPers = idenPers;
	}

	public String getNombrePersona() {
		return nombrePersona;
	}

	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public long getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(long numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getDireEmai() {
		return direEmai;
	}

	public void setDireEmai(String direEmai) {
		this.direEmai = direEmai;
	}

}
