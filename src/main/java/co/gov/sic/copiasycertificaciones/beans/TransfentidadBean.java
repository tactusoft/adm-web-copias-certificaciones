/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.PersonaEmail;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.MailService;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponseResponsable;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author emosquera
 */
@Named("transfentidadBean")
@SessionScoped
public class TransfentidadBean implements Serializable {

	private static final long serialVersionUID = -3893556821308988695L;
	private Cesl_tramite tramite;
	protected final Logger logger = LoggerFactory.getLogger(TransfentidadBean.class);
	private String entidad;
	private String observaciones;
	private List<PersonaEmail> listPersonaEmail;
	private PersonaEmail personaEmailSelected;

	public TransfentidadBean() {
		listPersonaEmail = new ArrayList<>();
	}

	public void buscarEntidad() {
		if (this.entidad != null && !this.entidad.isEmpty() && this.entidad.length() > 4) {
			try {
				Dal dal = new Dal();
				this.listPersonaEmail = dal.consultarPersonas(this.entidad.toLowerCase());
			} catch (Exception ex) {
				this.AddErrorMessage("La entidad es obligatorio y debe escribir mínimo 5 caracteres", "msgEntidades");
			}
		} else {
			this.AddErrorMessage("La entidad es obligatorio y debe escribir mínimo 5 caracteres", "msgEntidades");
		}
	}

	public void enviarRespuestaFinalizarSolicitud() throws Exception {
		if (this.personaEmailSelected == null) {
			this.AddErrorMessage("El campos entidad es obligatorio", "msgLoadFiles");
			return;
		}

		if (this.observaciones == null) {
			this.observaciones = " ";
		}

		Radicacion radiSalida = new Radicacion();
		radiSalida.setAnio(tramite.getAno_radi());
		radiSalida.setNumero(tramite.getNume_radi());

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);

		try {
			List<String> adjuntos = new ArrayList<>();
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 471);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radiSalida.setTipoRadicacion("SA");
			radiSalida.setDependenciaDestino((short) 104);
			radiSalida.setIdFuncionario(tramite.getFunc_asignado());

			Persona entidad = new Persona();
			entidad.setId(this.personaEmailSelected.getIdenPers());
			entidad.setRetornarSoloUltimosDatos(true);
			entidad = wsInteropClient.personaConsultar(entidad).getPersona();

			List<Email> listEmailEntidad = new ArrayList<>();
			Email emailEntidad = entidad.getEmails().get(0);
			emailEntidad.setDescripcion(this.personaEmailSelected.getDireEmai().trim());
			listEmailEntidad.add(emailEntidad);
			entidad.setEmails(listEmailEntidad);

			Persona radicador = new Persona();
			radicador.setId(tramite.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();

			radiSalida.setRadicador(entidad);
			radiSalida.setTotalFolios(1);

			Persona funcionario = new Persona();
			funcionario.setId(tramite.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			ResponseResponsable responseResponsableNotificaciones = wsInteropClient.dependenciaResponsable(104);
			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
			Dal Dal = new Dal();
			if (responseRadicacion.getCodigo() == 0) {
				radiSalida = responseRadicacion.getRadicacion();
				// Generar comunicado de salida
				TemplateContent templateContent = new TemplateContent();
				radiSalida.setRadicador(entidad);
				String contentPDF = templateContent.buildRespuestaTrasladoEntidad(radiSalida, funcionario,
						responseResponsableNotificaciones.getResponsable(), true, observaciones, radicador);
				String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());

				ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
						tramite.getIdtiposolicitud().getDescripcion(), subject,
						String.format(Constantes.KEYWORDS_PDF_RADICACION, radiSalida.getFechaRadicacion().getYear()),
						true, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
								tramite.getIdtiposolicitud()));

				String fullPathRadicadoEntradaEntidad = Functions.saveFile(radiSalida, fileContent);
				logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntradaEntidad);
				radiSalida.addAdjunto(fullPathRadicadoEntradaEntidad, false);

				Radicacion radiAdjuntos = new Radicacion();
				radiAdjuntos.setAnio(radiSalida.getAnio());
				radiAdjuntos.setNumero(radiSalida.getNumero());
				radiAdjuntos.setConsecutivo(0);
				String directoryEmail = Functions.getRadicacionFolderPath(radiAdjuntos);
				logger.info("Tactu: " + directoryEmail);
				radiAdjuntos.setControl(" ");
				String fullPathRadicadoEntradaEntidad0 = directoryEmail + Functions.getRadicacionileName(radiAdjuntos);
				System.out.println("Tactu: " + fullPathRadicadoEntradaEntidad0);

				ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient.radicacionAdjuntosRegistrar(radiSalida);
				if (responseRadicacionAdjuntos.getCodigo() == 0) {
					List<String> emailSolicitante = new ArrayList<>();
					emailSolicitante.add(this.personaEmailSelected.getDireEmai());
					adjuntos.add(fullPathRadicadoEntradaEntidad);
					adjuntos.add(fullPathRadicadoEntradaEntidad0);
					String htmlEmail = templateContent.buildEmailTemplateTrasladoEntidad(tramite,
							emailSolicitante.get(0), perfilRadicacion.getActuacion());
					MailService.Send(emailSolicitante, "Traslado por competencia de solicitud radicado #"
							+ tramite.getAno_radi() + "-" + tramite.getNume_radi(), htmlEmail, adjuntos);
				}

				adjuntos = new ArrayList<>();

				perfilRadicacion = new Perfil();
				perfilRadicacion.setActuacion((short) 440);
				perfilRadicacion.setDependencia((short) 104);
				perfilRadicacion.setEvento((short) 0);
				perfilRadicacion.setTramite((short) 362);

				radiSalida = new Radicacion();
				radiSalida.setAnio(tramite.getAno_radi());
				radiSalida.setNumero(tramite.getNume_radi());
				radiSalida.setPerfil(perfilRadicacion);
				radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
				radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
				radiSalida.setTipoRadicacion("SA");
				radiSalida.setDependenciaDestino((short) 104);
				radiSalida.setIdFuncionario(tramite.getFunc_asignado());
				radiSalida.setRadicador(radicador);
				radiSalida.setTotalFolios(1);

				responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

				if (responseRadicacion.getCodigo() == 0) {
					if (Dal.setCeslTramiteEstado(tramite.getIdtramite(), EstadoTramite.FINALIZADO.getValue())) {
						logger.info("se cambio el estado en sistemas de copias");
						this.AddInfoMessage("Solicitud enviada correctamente", "mgComplementar");
						radiSalida = responseRadicacion.getRadicacion();

						// Generar comunicado de salida
						templateContent = new TemplateContent();
						radiSalida.setRadicador(radicador);
						contentPDF = templateContent.buildRespuestaTrasladoUsuario(radiSalida, funcionario,
								responseResponsableNotificaciones.getResponsable(), true, observaciones, entidad);
						subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
						String fullPathRadicadoEntrada = null;

						fileContent = PDFGeneratorService.createPdf(contentPDF,
								tramite.getIdtiposolicitud().getDescripcion(), subject,
								String.format(Constantes.KEYWORDS_PDF_RADICACION,
										radiSalida.getFechaRadicacion().getYear()),
								true, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
										tramite.getIdtiposolicitud()));

						fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
						logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
						radiSalida.addAdjunto(fullPathRadicadoEntrada, false);
						radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

						responseRadicacionAdjuntos = wsInteropClient.radicacionAdjuntosRegistrar(radiSalida);
						if (responseRadicacionAdjuntos.getCodigo() == 0) {
							String htmlEmail = null;

							List<Email> emailSolicitanteEmail = new ArrayList<>();

							emailSolicitanteEmail = wsInteropClient.personaConsultar(tramite.getIden_pers())
									.getPersona().getEmails();
							List<String> emailSolicitante = this.getListaEmails(emailSolicitanteEmail);

							for (Email email : emailSolicitanteEmail) {
								logger.info("Procesando email : " + email.getDescripcion());
								emailSolicitante.add(email.getDescripcion());
							}

							adjuntos.add(fullPathRadicadoEntrada);
							adjuntos.add(fullPathRadicadoEntradaEntidad);

							htmlEmail = templateContent.buildEmailTemplateTrasladoEntidad(tramite,
									emailSolicitante.get(0), perfilRadicacion.getActuacion());
							MailService.Send(emailSolicitante, "Respuesta Solicitud radicado #" + tramite.getAno_radi()
									+ "-" + tramite.getNume_radi(), htmlEmail, adjuntos);
						}
					} else {
						logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
						this.AddErrorMessage(
								"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
								"mgComplementar");
					}
				} else {
					this.AddErrorMessage(responseRadicacion.getMensaje(), "msgLoadFiles");
				}
			} else {
				this.AddErrorMessage(responseRadicacion.getMensaje(), "msgLoadFiles");
			}
		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
	}
	
	private List<String> getListaEmails(List<Email> emails) {
		List<String> listaEmails = new ArrayList<>();
		try {
			Dal Dal = new Dal();
			Cesl_detalleSolicitud tramiteDetalle = Dal.getDetallesTramite(tramite.getIdtramite()).get(0);
			Direccion direccionEmail = Dal.getDireccion(tramite.getIden_pers(), tramiteDetalle.getConsEmail());
			if (direccionEmail != null && direccionEmail.getDescripcion() != null
					&& !direccionEmail.getDescripcion().isEmpty()) {
				listaEmails.add(direccionEmail.getDescripcion());
			} else {
				for (Email e : emails) {
					listaEmails.add(e.getDescripcion());
				}
			}
		} catch (Exception ex) {
			for (Email e : emails) {
				listaEmails.add(e.getDescripcion());
			}
		}
		return listaEmails;
	}

	public void limpiarForm() {

	}

	protected void AddErrorMessage(String error) {
		AddErrorMessage(error, null);
	}

	protected void AddStatusMessage(String error, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_INFO, error, error));
		}
	}

	protected void AddErrorMessage(String error, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, error, error));
		}
	}

	protected void AddWarningMessage(String message) {
		AddWarningMessage(message, null);
	}

	protected void AddWarningMessage(String message, String clientID) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientID,
					new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
		}
	}

	protected void AddInfoMessage(String message) {
		AddInfoMessage(message, null);
	}

	protected void AddInfoMessage(String message, String clientId) {
		if (FacesContext.getCurrentInstance() != null) {
			FacesContext.getCurrentInstance().addMessage(clientId,
					new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
		}
	}

	/**
	 * @return the tramite
	 */
	public Cesl_tramite getTramite() {
		return tramite;
	}

	/**
	 * @param tramite the tramite to set
	 */
	public void setTramite(Cesl_tramite tramite) {
		this.entidad = null;
		this.listPersonaEmail = new ArrayList<>();
		this.personaEmailSelected = null;
		this.tramite = tramite;
	}

	/**
	 * 
	 * @return
	 */
	public String getEntidad() {
		return entidad;
	}

	/**
	 * 
	 * @param entidad
	 */
	public void setEntidad(String entidad) {
		this.entidad = entidad;
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
	 * 
	 * @return
	 */
	public List<PersonaEmail> getListPersonaEmail() {
		return listPersonaEmail;
	}

	/**
	 * 
	 * @param listPersonaEmail
	 */
	public void setListPersonaEmail(List<PersonaEmail> listPersonaEmail) {
		this.listPersonaEmail = listPersonaEmail;
	}

	/**
	 * 
	 * @return
	 */
	public PersonaEmail getPersonaEmailSelected() {
		return personaEmailSelected;
	}

	/**
	 * 
	 * @param personaEmailSelected
	 */
	public void setPersonaEmailSelected(PersonaEmail personaEmailSelected) {
		this.personaEmailSelected = personaEmailSelected;
	}

}
