/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.Dependencia;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.AdscritoDepe;
import sic.ws.interop.entities.response.ResponseAdscritos;

/**
 *
 * @author emosquera
 */
public abstract class NotificationManageBeanInit {

	private List<Cesl_tramite> listaSolicitudes;
	private List<Cesl_tramite> listaSolicitudesActivas;
	private List<Cesl_tramite> listaMemorando;
	private Cesl_tramite tramiteSolicitud;
	private List<Dependencia> listaDependencias;
	private Integer codigoDependencia = -1;
	private Long idFuncionario = 0L;
	private List<AdscritoDepe> listaAdscritos;
	private boolean habilitarAsignarFuncioanrio = false;
	protected final Logger logger = LoggerFactory.getLogger(NotificationManageBeanInit.class);

	public NotificationManageBeanInit() throws Exception {
		listaSolicitudes = new ArrayList<>();
		listaSolicitudesActivas = new ArrayList<>();
		listaMemorando = new ArrayList<>();
		listaAdscritos = new ArrayList<>();
		// getPendingRequest();

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		listaAdscritos.clear();

		ResponseAdscritos adscritos = wsInteropClient.dependenciaAdscritos(104);
		listaAdscritos = adscritos.getAdscritos();
		logger.info("NotificationManageBeanInit");

	}

	@PostConstruct
	public void init() {

	}

	private List<Dependencia> getAllDependencies() throws Exception {
		try (Dal Dal = new Dal()) {
			listaDependencias = Dal.getAllDependencies();
		}
		return listaDependencias;

	}

	private List<Cesl_tramite> getPendingRequest() throws Exception {
		listaSolicitudes.clear();
		try (Dal Dal = new Dal()) {
			setListaSolicitudes(Dal.getPendingRequestCoordinador());
		}
		return listaSolicitudes;

	}
	
	private List<Cesl_tramite> getActiveRequest() throws Exception {
		listaSolicitudesActivas.clear();
		try (Dal Dal = new Dal()) {
			setListaSolicitudesActivas(Dal.getActiveCoordinador());
		}
		return listaSolicitudesActivas;
	}

	private List<Cesl_tramite> getMemo() throws Exception {
		listaMemorando.clear();
		try (Dal Dal = new Dal()) {
			setListaMemorando(Dal.getMemo());
		}
		return listaMemorando;

	}

	/**
	 * @return the listaSolicitudes
	 */
	public List<Cesl_tramite> getListaSolicitudes() throws Exception {
		if (listaSolicitudes.isEmpty()) {
			getPendingRequest();
		}
		return listaSolicitudes;
	}

	public List<Cesl_tramite> getListaMemorando() throws Exception {
		if (listaMemorando.isEmpty()) {
			getMemo();
		}
		return listaMemorando;
	}

	public void setListaMemorando(List<Cesl_tramite> listaMemorando) {
		this.listaMemorando = listaMemorando;
	}

	/**
	 * @return the tramiteSolicitud
	 */
	public Cesl_tramite getTramiteSolicitud() {
		return tramiteSolicitud;
	}

	/**
	 * @param tramiteSolicitud the tramiteSolicitud to set
	 */
	public void setTramiteSolicitud(Cesl_tramite tramiteSolicitud) {
		this.tramiteSolicitud = tramiteSolicitud;
	}

	/**
	 * @return the listaDependencias
	 */
	public List<Dependencia> getListaDependencias() throws Exception {
		return getAllDependencies();
	}

	public void onChangeDependency() throws IOException {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		listaAdscritos.clear();

		ResponseAdscritos adscritos = wsInteropClient.dependenciaAdscritos(codigoDependencia);
		listaAdscritos = adscritos.getAdscritos();

		setHabilitarAsignarFuncioanrio(false);
	}

	public void descargarMemo(Cesl_tramite tramite) {
		Dal Dal = new Dal();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		try {
			Cesl_detalleSolicitud tramiteDetalle = Dal.getDetallesTramite(tramite.getIdtramite()).get(0);
			String rutaMemo = tramiteDetalle.getRutaMemo();
			if (rutaMemo != null && !rutaMemo.isEmpty()) {
				rutaMemo = rutaMemo.trim();
				File file = new File(rutaMemo);
				HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

				response.reset();
				response.setHeader("Content-Type", "application/octet-stream");
				response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");

				try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
						OutputStream output = response.getOutputStream()) {

					byte[] buffer = new byte[1024];
					int length;
					while ((length = input.read(buffer)) > 0) {
						output.write(buffer, 0, length);
					}
					output.flush();
				}
				facesContext.responseComplete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			AddErrorMessage("Error al descargar el archivo: " + e.getMessage());
		}
	}

	/**
	 * @return the codigoDependencia
	 */
	public int getCodigoDependencia() {
		return codigoDependencia;
	}

	/**
	 * @param codigoDependencia the codigoDependencia to set
	 */
	public void setCodigoDependencia(int codigoDependencia) {
		this.codigoDependencia = codigoDependencia;
	}

	/**
	 * @return the listaAdscritos
	 */
	public List<AdscritoDepe> getListaAdscritos() {
		return listaAdscritos;
	}

	/**
	 * @return the idFuncionario
	 */
	public Long getIdFuncionario() {
		return idFuncionario;
	}

	/**
	 * @param idFuncionario the idFuncionario to set
	 */
	public void setIdFuncionario(Long idFuncionario) {
		this.idFuncionario = idFuncionario;
	}

	/**
	 * @return the habilitarAsignarFuncioanrio
	 */
	public boolean isHabilitarAsignarFuncioanrio() {
		return habilitarAsignarFuncioanrio;
	}

	/**
	 * @param habilitarAsignarFuncioanrio the habilitarAsignarFuncioanrio to set
	 */
	public void setHabilitarAsignarFuncioanrio(boolean habilitarAsignarFuncioanrio) {
		this.habilitarAsignarFuncioanrio = habilitarAsignarFuncioanrio;
	}

	protected void AddErrorMessage(String error) {
		AddErrorMessage(error, null);
	}

	protected void AddErrorMessage(String error, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, error, error));
		}
	}

	protected void AddStatusMessage(String error, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_INFO, error, error));
		}
	}

	/**
	 * @param listaSolicitudes the listaSolicitudes to set
	 */
	public void setListaSolicitudes(List<Cesl_tramite> listaSolicitudes) {
		this.listaSolicitudes = listaSolicitudes;
	}

	public List<Cesl_tramite> getListaSolicitudesActivas() throws Exception {
		if (listaSolicitudesActivas.isEmpty()) {
			getActiveRequest();
		}
		return listaSolicitudesActivas;
	}

	public void setListaSolicitudesActivas(List<Cesl_tramite> listaSolicitudesActivas) {
		this.listaSolicitudesActivas = listaSolicitudesActivas;
	}

}
