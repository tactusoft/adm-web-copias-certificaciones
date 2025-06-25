package co.gov.sic.copiasycertificaciones.entities;

import java.sql.Date;
import java.time.LocalDate;

public class Cesl_detalleSolicitud {

	private Integer iddetallesolicitud;
	private String tipo_certifica;
	private String tipo_certifica_descripcion;
	private String tipo_docu;
	private String tipo_docu_descripcion;
	private Long nume_docu;
	private String nume_docu_descripcion;
	private Integer cantidad;
	private Integer anos;
	private String anos_descripcion;
	private Double valor;
	private Integer idcamaracomercio;
	private CamaraComercio idcamaracomercioData;
	private String observaciones;
	private Integer idtramite;
	private String variableAdicional1;
	private String variableAdicional2;
	private String idCodigoApostilla;
	private LocalDate fechaAdicional1;
	private Boolean conApostilla;
	private Integer opcionesEntrega;
	private String copiaAutenticada;
	private String observacionesRadicado;
	private String hashPdf;
	private long consDire;
	private long consEmail;
	private String rutaMemo;

	/**
	 * @return the iddetallesolicitud
	 */
	public Integer getIddetallesolicitud() {
		return iddetallesolicitud;
	}

	/**
	 * @param iddetallesolicitud the iddetallesolicitud to set
	 */
	public void setIddetallesolicitud(Integer iddetallesolicitud) {
		this.iddetallesolicitud = iddetallesolicitud;
	}

	public String getTipo_certifica() {
		return tipo_certifica;
	}

	public void setTipo_certifica(String tipo_certifica) {
		this.tipo_certifica = tipo_certifica;
	}

	public String getTipo_certifica_descripcion() {
		return tipo_certifica_descripcion;
	}

	public void setTipo_certifica_descripcion(String val) {
		this.tipo_certifica_descripcion = val;
	}

	public CamaraComercio getIdcamaracomercioData() {
		return idcamaracomercioData;
	}

	public void setIdcamaracomercioData(CamaraComercio val) {
		this.idcamaracomercioData = val;
	}

	/**
	 * @return the tipo_docu
	 */
	public String getTipo_docu() {
		return tipo_docu;
	}

	/**
	 * @param tipo_docu the tipo_docu to set
	 */
	public void setTipo_docu(String tipo_docu) {
		this.tipo_docu = tipo_docu;
	}

	public String getTipo_docu_descripcion() {
		return this.tipo_docu_descripcion;
	}

	public void setTipo_docu_descripcion(String val) {
		this.tipo_docu_descripcion = val;
	}

	public String getNume_docu_descripcion() {
		return this.nume_docu_descripcion;
	}

	public void setNume_docu_descripcion(String val) {
		this.nume_docu_descripcion = val;
	}

	/**
	 * @return the nume_docu
	 */
	public Long getNume_docu() {
		return nume_docu;
	}

	/**
	 * @param nume_docu the nume_docu to set
	 */
	public void setNume_docu(Long nume_docu) {
		this.nume_docu = nume_docu;
	}

	/**
	 * @return the cantidad
	 */
	public Integer getCantidad() {
		return cantidad;
	}

	/**
	 * @param cantidad the cantidad to set
	 */
	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	/**
	 * @return the anos
	 */
	public Integer getAnos() {
		return anos;
	}

	/**
	 * @param anos the anos to set
	 */
	public void setAnos(Integer anos) {
		this.anos = anos;
	}

	public String getAnos_descripcion() {
		return anos_descripcion;
	}

	public void setAnos_descripcion(String val) {
		this.anos_descripcion = val;
	}

	/**
	 * @return the valor
	 */
	public Double getValor() {
		return valor;
	}

	/**
	 * @param valor the valor to set
	 */
	public void setValor(Double valor) {
		this.valor = valor;
	}

	/**
	 * @return the idcamaracomercio
	 */
	public Integer getIdcamaracomercio() {
		return idcamaracomercio;
	}

	/**
	 * @param idcamaracomercio the idcamaracomercio to set
	 */
	public void setIdcamaracomercio(Integer idcamaracomercio) {
		this.idcamaracomercio = idcamaracomercio;
	}

	/**
	 * @return the observaciones
	 */
	public String getObservaciones() {
		return observaciones;
	}

	/**
	 * @param observaciones the observaciones to set
	 */
	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	/**
	 * @return the idtramite
	 */
	public Integer getIdtramite() {
		return idtramite;
	}

	/**
	 * @param idtramite the idtramite to set
	 */
	public void setIdtramite(Integer idtramite) {
		this.idtramite = idtramite;
	}

	public String getVariableAdicional1() {
		return this.variableAdicional1;
	}

	public void setVariableAdicional1(String val) {
		this.variableAdicional1 = val;
	}

	public String getVariableAdicional2() {
		return this.variableAdicional2;
	}

	public void setVariableAdicional2(String val) {
		this.variableAdicional2 = val;
	}

	public LocalDate getFechaAdicional1() {
		return this.fechaAdicional1;
	}

	public void setFechaAdicional1(LocalDate val) {
		this.fechaAdicional1 = val;
	}

	public void setFechaAdicional1(Date val) {
		if (val != null) {
			this.fechaAdicional1 = val.toLocalDate();
		} else {
			this.fechaAdicional1 = null;
		}
	}

	public Boolean getConApostilla() {
		conApostilla = getVariableAdicional1() != null;
		return conApostilla;
	}

	public String getIdCodigoApostilla() {
		idCodigoApostilla = String.format("T%sD%s", this.idtramite, this.iddetallesolicitud);
		return idCodigoApostilla;
	}

	public Integer getOpcionesEntrega() {
		return opcionesEntrega;
	}

	public void setOpcionesEntrega(Integer opcionesEntrega) {
		this.opcionesEntrega = opcionesEntrega;
	}

	public String getCopiaAutenticada() {
		return copiaAutenticada;
	}

	public void setCopiaAutenticada(String copiaAutenticada) {
		this.copiaAutenticada = copiaAutenticada;
	}

	public String getObservacionesRadicado() {
		return observacionesRadicado;
	}

	public void setObservacionesRadicado(String observacionesRadicado) {
		this.observacionesRadicado = observacionesRadicado;
	}

	public String getHashPdf() {
		return hashPdf;
	}

	public void setHashPdf(String hashPdf) {
		this.hashPdf = hashPdf;
	}

	public long getConsDire() {
		return consDire;
	}

	public void setConsDire(long consDire) {
		this.consDire = consDire;
	}

	public long getConsEmail() {
		return consEmail;
	}

	public void setConsEmail(long consEmail) {
		this.consEmail = consEmail;
	}

	public String getRutaMemo() {
		return rutaMemo;
	}

	public void setRutaMemo(String rutaMemo) {
		this.rutaMemo = rutaMemo;
	}

}
