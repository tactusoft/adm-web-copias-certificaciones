package co.gov.sic.copiasycertificaciones.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;

public class Cesl_tramite implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer idtramite;
	private Short ano_radi;
	private Integer nume_radi;
	private String stringNume_radi;
	private String cont_radi;
	private Integer cons_radi;
	private EstadoTramite estado;
	private Double valor_total;
	private LocalDateTime fecha_creacion;
	private LocalDateTime fecha_modificacion;
	private String medio_respuesta;
	private TipoTramite idtiposolicitud;
	private Long iden_pers;
	private String tipoDcoumento;
	private String numeroIdentificacion;
	private String nombreSolicitante;
	private Short ano_recibo;
	private Integer nume_recibo;
	private List<Cesl_detalleSolicitud> detalles;
	private Long func_asignado;
	private String nombreFuncionario;
	private Boolean notificacion;
	private Integer opcionesEntrega;

	public Cesl_tramite() {
		this.detalles = new ArrayList<>();
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

	/**
	 * @return the ano_radi
	 */
	public Short getAno_radi() {
		return ano_radi;
	}

	/**
	 * @param ano_radi the ano_radi to set
	 */
	public void setAno_radi(Short ano_radi) {
		this.ano_radi = ano_radi;
	}

	/**
	 * @return the nume_radi
	 */
	public Integer getNume_radi() {
		return nume_radi;
	}

	/**
	 * @param nume_radi the nume_radi to set
	 */
	public void setNume_radi(Integer nume_radi) {
		this.nume_radi = nume_radi;
	}

	/**
	 * @return the cont_radi
	 */
	public String getCont_radi() {
		return cont_radi;
	}

	/**
	 * @param cont_radi the cont_radi to set
	 */
	public void setCont_radi(String cont_radi) {
		this.cont_radi = cont_radi;
	}

	/**
	 * @return the cons_radi
	 */
	public Integer getCons_radi() {
		return cons_radi;
	}

	/**
	 * @param cons_radi the cons_radi to set
	 */
	public void setCons_radi(Integer cons_radi) {
		this.cons_radi = cons_radi;
	}

	/**
	 * @return the estado
	 */
	public EstadoTramite getEstado() {
		return estado;
	}

	/**
	 * @param estado the estado to set
	 */
	public void setEstado(EstadoTramite estado) {
		this.estado = estado;
	}

	public void setEstado(int estado) {
		this.estado = EstadoTramite.fromValue(estado);
	}

	/**
	 * @return the valor_total
	 */
	public Double getValor_total() {
		return valor_total;
	}

	/**
	 * @param valor_total the valor_total to set
	 */
	public void setValor_total(Double valor_total) {
		this.valor_total = valor_total;
	}

	/**
	 * @return the fecha_creacion
	 */
	public LocalDateTime getFecha_creacion() {
		return fecha_creacion;
	}

	/**
	 * @param fecha_creacion the fecha_creacion to set
	 */
	public void setFecha_creacion(LocalDateTime fecha_creacion) {
		this.fecha_creacion = fecha_creacion;
	}

	/**
	 * @return the fecha_modificacion
	 */
	public LocalDateTime getFecha_modificacion() {
		return fecha_modificacion;
	}

	/**
	 * @param fecha_modificacion the fecha_modificacion to set
	 */
	public void setFecha_modificacion(LocalDateTime fecha_modificacion) {
		this.fecha_modificacion = fecha_modificacion;
	}

	/**
	 * @return the medio_respuesta
	 */
	public String getMedio_respuesta() {
		return medio_respuesta;
	}

	/**
	 * @param medio_respuesta the medio_respuesta to set
	 */
	public void setMedio_respuesta(String medio_respuesta) {
		this.medio_respuesta = medio_respuesta;
	}

	/**
	 * @return the idtiposolicitud
	 */
	public TipoTramite getIdtiposolicitud() {
		return idtiposolicitud;
	}

	public void setIdtiposolicitud(TipoTramite val) {
		this.idtiposolicitud = val;
	}

	public void setIdtiposolicitud(int val) {
		this.idtiposolicitud = TipoTramite.fromValue(val);
	}

	/**
	 * @return the iden_pers
	 */
	public Long getIden_pers() {
		return iden_pers;
	}

	/**
	 * @param iden_pers the iden_pers to set
	 */
	public void setIden_pers(Long iden_pers) {
		this.iden_pers = iden_pers;
	}

	public Short getAno_recibo() {
		return ano_recibo;
	}

	public void setAno_recibo(Short val) {
		this.ano_recibo = val;
	}

	public Integer getNume_recibo() {
		return this.nume_recibo;
	}

	public void setNume_recibo(Integer val) {
		this.nume_recibo = val;
	}

	public List<Cesl_detalleSolicitud> getDetalles() {
		return this.detalles;
	}

	public void setDetalles(List<Cesl_detalleSolicitud> val) {
		this.detalles = val;
	}

	/**
	 * @return the tipoDcoumento
	 */
	public String getTipoDcoumento() {
		return tipoDcoumento;
	}

	/**
	 * @param tipoDcoumento the tipoDcoumento to set
	 */
	public void setTipoDcoumento(String tipoDcoumento) {
		this.tipoDcoumento = tipoDcoumento;
	}

	/**
	 * @return the numeroIdentificacion
	 */
	public String getNumeroIdentificacion() {
		return numeroIdentificacion;
	}

	/**
	 * @param numeroIdentificacion the numeroIdentificacion to set
	 */
	public void setNumeroIdentificacion(String numeroIdentificacion) {
		this.numeroIdentificacion = numeroIdentificacion;
	}

	/**
	 * @return the nombreSolicitante
	 */
	public String getNombreSolicitante() {
		return nombreSolicitante;
	}

	/**
	 * @param nombreSolicitante the nombreSolicitante to set
	 */
	public void setNombreSolicitante(String nombreSolicitante) {
		this.nombreSolicitante = nombreSolicitante;
	}

	/**
	 * @return the func_asignado
	 */
	public Long getFunc_asignado() {
		return func_asignado;
	}

	/**
	 * @param func_asignado the func_asignado to set
	 */
	public void setFunc_asignado(Long func_asignado) {
		this.func_asignado = func_asignado;
	}

	/**
	 * 
	 * @return
	 */
	public String getNombreFuncionario() {
		return nombreFuncionario;
	}

	/**
	 * 
	 * @param nombreFuncionario
	 */
	public void setNombreFuncionario(String nombreFuncionario) {
		this.nombreFuncionario = nombreFuncionario;
	}

	/**
	 * @return the stringNume_radi
	 */
	public String getStringNume_radi() {
		return stringNume_radi;
	}

	/**
	 * @param stringNume_radi the stringNume_radi to set
	 */
	public void setStringNume_radi(String stringNume_radi) {
		this.stringNume_radi = stringNume_radi;
	}

	/*
	 * @return the notificacion
	 */
	public Boolean getNotificacion() {
		return notificacion;
	}

	/*
	 * @param notificacion the notificacion to set
	 * 
	 */
	public void setNotificacion(Boolean notificacion) {
		this.notificacion = notificacion;
	}

	/**
	 */
	public Integer getOpcionesEntrega() {
		return opcionesEntrega;
	}

	/**
	 * 
	 * @param opcionesEntrega
	 */
	public void setOpcionesEntrega(Integer opcionesEntrega) {
		this.opcionesEntrega = opcionesEntrega;
	}

}
