/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import static co.gov.sic.copiasycertificaciones.beans.BeanBase.encodeURL;
import static co.gov.sic.copiasycertificaciones.beans.BeanBase.getExternalContext;
import static co.gov.sic.copiasycertificaciones.beans.BeanBase.getMimeType;
import static co.gov.sic.copiasycertificaciones.beans.BeanBase.getSession;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Attachment;
import co.gov.sic.copiasycertificaciones.entities.Cesl_config;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.Dependencia;
import co.gov.sic.copiasycertificaciones.entities.Obse_Radi;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.MailService;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Adjunto;
import sic.ws.interop.entities.AdscritoDepe;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.ResponsableDepe;
import sic.ws.interop.entities.Telefono;
import sic.ws.interop.entities.Tramite;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.request.RequestTramite;
import sic.ws.interop.entities.response.ResponseAdjuntoConsultar;
import sic.ws.interop.entities.response.ResponseAdscritos;
import sic.ws.interop.entities.response.ResponsePersona;
import sic.ws.interop.entities.response.ResponseResponsable;
import sic.ws.interop.entities.response.ResponseTramite;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author emosquera
 */
@Named("requestManagementBean")
@SessionScoped
public class RequestManagementBean implements Serializable {

	private static final long serialVersionUID = 1154251012490903254L;
	private List<Cesl_tramite> listaSolicitudes;
	private Cesl_tramite tramiteSolicitud;
	private Persona persona;
	private String mensaje;
	private String direccion;
	private String telefono;
	private String email;
	private List<Adjunto> listaAdjuntos;
	protected final Logger logger = LoggerFactory.getLogger(RequestManagementBean.class);
	private StreamedContent download;
	private Integer codigoDependencia = -1;
	private boolean tipoTraslado;
	private List<Dependencia> listaDependencias;
	private boolean habilitaTrasLado = false;
	private List<AdscritoDepe> listaAdscritos;
	private String observaciones;
	private Referencia tipoCopia;
	private List<Referencia> listaTipoCopia;
	private List<Obse_Radi> listaComentarios;
	private String infoAdicional;
	private boolean solicitudTrasladada = false;
	private List<Attachment> listaAdjuntoCompletarSolicitud;
	private double totalAttachmentSize;
	private List<Tramite> listaExpedientes;
	private List<Referencia> listaTiposSolicitudes;
	private boolean habilitaComplemanatrCiudadano = true;
	private boolean habilitaNota = true;

	public RequestManagementBean() throws Exception {

		listaSolicitudes = new ArrayList<>();
		tipoTraslado = false;
		listaAdscritos = new ArrayList<>();
		listaAdjuntoCompletarSolicitud = new ArrayList<>();
		listaAdjuntos = new ArrayList<>();
		listaExpedientes = new ArrayList<>();
		listaComentarios = new ArrayList<>();
		getMyAssignedRequest();
		try (Dal Dal = new Dal()) {
			listaDependencias = Dal.getAllDependencies();
		}

	}

	@PostConstruct
	public void init() {

	}

	public List<Referencia> getListaTiposSolicitudes() throws Exception {
		if (listaTiposSolicitudes == null) {
			listaTiposSolicitudes = Utility.GetReferenciaWS("TIPOSOL_SEDELECTRO");
		}

		return listaTiposSolicitudes;
	}

	public void complementarInformacionSolicitante(Cesl_tramite tramite) {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		try {

			setTramiteSolicitud(tramite);

			ResponsePersona responsePersona = wsInteropClient.personaConsultar(tramite.getIden_pers());

			persona = new Persona();

			persona.setId(tramiteSolicitud.getIden_pers());
			persona.setRetornarSoloUltimosDatos(true);
			persona = wsInteropClient.personaConsultar(persona).getPersona();
			persona = responsePersona.getPersona();

			tramiteSolicitud.setNombreSolicitante(persona.getFullName());
			tramiteSolicitud.setTipoDcoumento(persona.getTipoDocumento());
			tramiteSolicitud.setNumeroIdentificacion(persona.getNumeroDocumento().toString());
			List<String> emails = this.getListaEmails(persona.getEmails());
			List<Direccion> direcciones = persona.getDirecciones();
			email = emails.get(0);

			List<Telefono> tel = new ArrayList<>();
			for (Direccion dir : direcciones) {
				direccion = dir.getDescripcion();
				tel = dir.getTelefonos();
				break;
			}

			for (Telefono numTelefonos : tel) {
				if (numTelefonos.getTipo().equals("CE") && !numTelefonos.getNumero().isEmpty()
						&& !numTelefonos.getNumero().equals("null")) {
					this.telefono = numTelefonos.getNumero();
					break;
				}

			}

			RequestTramite requestTramite;
			requestTramite = new RequestTramite();
			requestTramite.setAnio(tramiteSolicitud.getAno_radi());
			requestTramite.setNumero(tramiteSolicitud.getNume_radi());
			requestTramite.setNumeroDocumento(null);
			requestTramite.setTipoDocumento(null);
			requestTramite.setExcluirDependencias(false);
			ResponseTramite responseTramite = wsInteropClient.radicacionConsultar(requestTramite);
			listaExpedientes = responseTramite.getExpedientes();

			Dal Dal = new Dal();
			setListaComentarios(
					Dal.getObservacionesPorRadicado(tramiteSolicitud.getAno_radi(), tramiteSolicitud.getNume_radi()));
			if (listaComentarios.size() > 0) {
				observaciones = listaComentarios.get(0).getText_obse();
			}
		} catch (IOException ex) {
			logger.error(ex.toString());
		}

	}

	public void onChangeDependencySolInfo() throws IOException, Exception {

		if (codigoDependencia != -1) {
			setHabilitaTrasLado(true);

		} else {
			setHabilitaTrasLado(false);
		}
	}

	public void onChangeDependency() throws IOException {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		getListaAdscritos().clear();
		ResponseAdscritos adscritos = wsInteropClient.dependenciaAdscritos(codigoDependencia);
		setListaAdscritos(adscritos.getAdscritos());
		setHabilitaTrasLado(true);

	}

	public void setRadicado(int cons_radi) {
		try {
			InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
					Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
			Radicacion radi = new Radicacion();
			radi.setAnio(tramiteSolicitud.getAno_radi());
			radi.setNumero(tramiteSolicitud.getNume_radi());
			radi.setSecuenciaEvento((short) cons_radi);

			ResponseAdjuntoConsultar adjResponse = wsInteropClient.radicacionAdjuntosConsultar(radi);
			if (adjResponse.getCodigo() == 0) {
				Dal Dal = new Dal();
				setListaComentarios(Dal.getObservacionesPorRadicado(tramiteSolicitud.getAno_radi(),
						tramiteSolicitud.getNume_radi()));
				List<Radicacion> radis = adjResponse.getRadicaciones();
				for (Radicacion radicacion : radis) {
					if (radicacion.getConsecutivo() == cons_radi) {
						getListaAdjuntos().clear();
						listaAdjuntos = radicacion.getAdjuntos();
					}
				}
			} else {
				getListaAdjuntos().clear();
			}

		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

	}

	public void setTramite(int id) {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);

		getListaSolicitudes().stream().filter(tramiteLocal -> (tramiteLocal.getIdtramite() == id))
				.forEachOrdered(tramiteLocal -> {

					try {

						setTramiteSolicitud(tramiteLocal);
						ResponsePersona responsePersona = wsInteropClient.personaConsultar(tramiteLocal.getIden_pers());

						persona = new Persona();

						persona.setId(tramiteSolicitud.getIden_pers());
						persona.setRetornarSoloUltimosDatos(true);
						persona = wsInteropClient.personaConsultar(persona).getPersona();
						persona = responsePersona.getPersona();
						List<String> emails = this.getListaEmails(persona.getEmails());
						email = emails.get(0);
						List<Direccion> direcciones = persona.getDirecciones();

						List<Telefono> tel = new ArrayList<>();
						for (Direccion dir : direcciones) {
							direccion = dir.getDescripcion();
							tel = dir.getTelefonos();
							break;
						}

						for (Telefono numTelefonos : tel) {
							if (numTelefonos.getTipo().equals("CE") && numTelefonos.getNumero() != null
									&& !numTelefonos.getNumero().isEmpty()
									&& !numTelefonos.getNumero().equals("null")) {
								this.telefono = numTelefonos.getNumero();
								break;
							}

						}

						reloadListaExpedientes();
						if (tramiteLocal.getNotificacion()) {
							actualizarNotificacion(id);
						}

					} catch (IOException ex) {
						logger.error(ex.toString());
					}

				});
	}

	private void reloadListaExpedientes() {

		try {
			InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
					Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);

			RequestTramite requestTramite;
			requestTramite = new RequestTramite();
			requestTramite.setAnio(tramiteSolicitud.getAno_radi());
			requestTramite.setNumero(tramiteSolicitud.getNume_radi());
			requestTramite.setNumeroDocumento(null);
			requestTramite.setTipoDocumento(null);
			requestTramite.setExcluirDependencias(false);
			ResponseTramite responseTramite = wsInteropClient.radicacionConsultar(requestTramite);
			listaExpedientes = responseTramite.getExpedientes();
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(RequestManagementBean.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void actualizarNotificacion(Integer id) {
		Dal dal = new Dal();
		dal.updateNotificacionByTramite(id, "N");
	}

	public void limpiarForm() throws Exception {
		observaciones = "";
		observaciones = new String();
		setListaComentarios(new ArrayList<>());
		codigoDependencia = -1;
		setInfoAdicional("");
		tipoTraslado = false;
		totalAttachmentSize = 0;
		habilitaComplemanatrCiudadano = true;
		setHabilitaTrasLado(false);
		if (listaAdjuntoCompletarSolicitud.size() > 0) {
			for (Attachment att : listaAdjuntoCompletarSolicitud) {
				deleteAttachmentFile(att.getFileName());
			}
			listaAdjuntoCompletarSolicitud = new ArrayList<>();
		}
		reloadListaExpedientes();

	}

	public void solDigitalizacionInformacion() {

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);

		try {
			Dal Dal = new Dal();
			Radicacion radiSalida = new Radicacion();
			TemplateContent templateContent = new TemplateContent();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 431);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radiSalida.setTipoRadicacion("TR");
			radiSalida.setDependenciaDestino(codigoDependencia.shortValue());
			// radiSalida.setObservaciones(observaciones);

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();

			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());
			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);

			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {

				if (Dal.setCeslTramiteEstado(getTramiteSolicitud().getIdtramite(),
						EstadoTramite.DIGITALIZACION.getValue())) {
					this.AddInfoMessage("Solicitud enviada correctamente", "msgSolInfo");

					radiSalida = responseRadicacion.getRadicacion();
					Persona funcionario = new Persona();
					funcionario.setId(tramiteSolicitud.getFunc_asignado());
					funcionario.setRetornarSoloUltimosDatos(true);
					funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

					Persona personaResponsable = new Persona();
					ResponseResponsable responseResponsable = wsInteropClient
							.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());

					Persona personaResponsableNotificaciones = new Persona();
					ResponseResponsable responseResponsableNotificaciones = wsInteropClient.dependenciaResponsable(104);

					ResponsableDepe responsableDepe = responseResponsable.getResponsable();

					personaResponsable.setId(Long.valueOf(responsableDepe.getIdenPers()));
					ResponsePersona responsePersona = wsInteropClient.personaConsultar(personaResponsable);
					personaResponsable = responsePersona.getPersona();

					Cesl_config cfg = Dal.getDayConfigParameters("internal_days");
					List<String> busnessDays = wsInteropClient.utilFestivos(25);

					String contentPDF = templateContent.buildSolDigitalizacionPDFTmplate(radiSalida,
							getTramiteSolicitud().getIdtiposolicitud(), personaResponsable, responsableDepe, radicador,
							cfg, busnessDays, funcionario, responseResponsableNotificaciones, observaciones);
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							true, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radiSalida.getConsecutivo(),
									tramiteSolicitud.getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

					radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()), att.getFileName());
						radiSalida.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);
					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);
						String htmlEmail = templateContent.buildEmailTemplateReqAreaInterna(getTramiteSolicitud(),
								personaResponsable);
						List<String> listaEmails = new ArrayList<>();

						for (Email email : personaResponsable.getEmails()) {
							listaEmails.add(email.getDescripcion());
						}
						MailService.Send(listaEmails, "Solicitud de digitalización  - "
								+ getTramiteSolicitud().getAno_radi() + "-" + getTramiteSolicitud().getNume_radi(),
								htmlEmail, adjuntos);
					} else {
						logger.info("No se pudo adjuntar los archivos al expediente");
						this.AddErrorMessage("No fue posible adjuntar los archivos al expediente.", "msgSolInfo");
					}

				} else {
					logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
					this.AddErrorMessage(
							"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
							"msgSolInfo");
				}

			} else {
				setMensaje(responseRadicacion.getMensaje());
				this.AddErrorMessage(responseRadicacion.getMensaje(), "msgSolInfo");
			}

		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

	}

	public void solInfoAreaExterna() {
		if (observaciones.isEmpty()) { // || listaAdjuntoCompletarSolicitud.isEmpty()
			this.AddErrorMessage("No se ha diligenciado el campo observaciones o no se ha adjuntado ningun archivo",
					"msgSolInfo");
			return;
		}

		observaciones = StringEscapeUtils.escapeJava(StringUtils.chomp(observaciones));

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);

		try {

			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 431);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radiSalida.setTipoRadicacion("TR");
			radiSalida.setDependenciaDestino(codigoDependencia.shortValue());
			radiSalida.setObservaciones(observaciones.replace("\\n", " "));

			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();

			Persona funcionario = new Persona();
			funcionario.setId(tramiteSolicitud.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);

			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {

				Dal Dal = new Dal();
				if (Dal.setCeslTramiteEstado(getTramiteSolicitud().getIdtramite(),
						EstadoTramite.SOL_INFO_AREA_INTERNA.getValue())) {
					this.AddInfoMessage("Solicitud enviada correctamente", "msgSolInfo");
					// Todo crear pdf template anexar al email.

					radiSalida = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent();

					Persona personaResponsable = new Persona();
					ResponseResponsable responseResponsable = wsInteropClient
							.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());

					ResponsableDepe responsableDepe = responseResponsable.getResponsable();

					personaResponsable.setId(Long.valueOf(responsableDepe.getIdenPers()));
					ResponsePersona responsePersona = wsInteropClient.personaConsultar(personaResponsable);
					personaResponsable = responsePersona.getPersona();

					Cesl_config cfg = Dal.getDayConfigParameters("internal_days");
					List<String> busnessDays = wsInteropClient.utilFestivos(25);

					String contentPDF = templateContent.buildInfoAreaInternaPDFTemplate(radiSalida,
							getTramiteSolicitud().getIdtiposolicitud(), personaResponsable, responsableDepe, radicador,
							cfg, busnessDays, funcionario);
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							getTramiteSolicitud().getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(getTramiteSolicitud().getIdtramite(), radiSalida.getConsecutivo(),
									getTramiteSolicitud().getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

					radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()), att.getFileName());
						radiSalida.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);

					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);

						String htmlEmail = templateContent.buildEmailTemplateReqAreaInterna(getTramiteSolicitud(),
								personaResponsable);
						List<String> listaEmails = new ArrayList<>();

						for (Email email : personaResponsable.getEmails()) {
							listaEmails.add(email.getDescripcion());
						}
						MailService.Send(listaEmails, "Requerimiento Interno - " + getTramiteSolicitud().getAno_radi()
								+ "-" + getTramiteSolicitud().getNume_radi(), htmlEmail, adjuntos);
					}

				} else {
					logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
					this.AddErrorMessage(
							"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
							"msgSolInfo");
				}

			} else {
				setMensaje(responseRadicacion.getMensaje());
				this.AddErrorMessage(responseRadicacion.getMensaje(), "msgSolInfo");
			}

		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

	}

	// Crea un nuevo tramite
	private void nuevoTrasladoCmpetencias() {
		InteropWSClient wsInteropClient = Utility.GetWSClient();

		try {
			Dal Dal = new Dal();
			Radicacion radi = new Radicacion();
			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();
			radi.setRadicador(radicador);
			Perfil perfilRadicacion = Dal.getPerfilCertificado(tramiteSolicitud.getIdtiposolicitud());
			radi.setPerfil(perfilRadicacion);
			radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			// radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
			radi.setDependenciaDestino(codigoDependencia.shortValue());
			radi.setTotalFolios(1);
			radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radi);
			if (responseRadicacion.getCodigo() == 0) {
				radi = responseRadicacion.getRadicacion();
				TemplateContent templateContent = new TemplateContent();
				if (Dal.setCeslTramiteEstado(tramiteSolicitud.getIdtramite(), EstadoTramite.TRASLADO.getValue())) {
					String contentPDF = templateContent.buildRadicacionPDFTemplate(radi, tramiteSolicitud,
							tramiteSolicitud.getDetalles());
					String subject = String.format("Radicación SIC %s", radi.getShortNumeroRadicacion());

					String fullPathRadicadoEntrada = null;

					try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION, radi.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radi.getConsecutivo(),
									tramiteSolicitud.getIdtiposolicitud()))) {
						fullPathRadicadoEntrada = Functions.saveFile(radi, fileContent);
						logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
					}

					radi.addAdjunto(fullPathRadicadoEntrada, false);

					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radi, getFileContent(att.getPath()), att.getFileName());
						radi.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient.radicacionAdjuntosRegistrar(radi);
					ResponseResponsable responseResponsable = wsInteropClient.dependenciaResponsable(codigoDependencia);
					ResponsableDepe responsableDepe = responseResponsable.getResponsable();
					Persona responsableArea = new Persona();
					responsableArea.setId(Long.valueOf(responseResponsable.getResponsable().getIdenPers()));
					responsableArea.setRetornarSoloUltimosDatos(true);
					responsableArea = wsInteropClient.personaConsultar(responsableArea).getPersona();
					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);
						List<String> listaEmails = new ArrayList<>();

						for (Email e : responsableArea.getEmails()) {
							listaEmails.add(e.getDescripcion());
						}

						String htmlEmail = templateContent.buildEmailTemplateCotizacion(tramiteSolicitud,
								listaEmails.get(0));

						MailService.Send(listaEmails,
								"Traslado - " + tramiteSolicitud.getAno_radi() + "-" + tramiteSolicitud.getNume_radi(),
								htmlEmail, adjuntos);
						this.AddInfoMessage("Solicitud trasaladada correctamente", "msgTraslado");
					} else {
						this.AddErrorMessage(responseRadicacionAdjuntos.getMensaje(), "msgTraslado");
					}

				}
			} else {
				this.AddErrorMessage(responseRadicacion.getMensaje(), "msgTraslado");
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}
	}

	public void transladarPorCompetencias() {

		if (!tipoTraslado) {
			nuevoTrasladoCmpetencias();
			return;
		}

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		ResponseResponsable responseResponsable;
		ResponseResponsable responseResponsableOrigen;
		ResponsableDepe responsableDepe;
		ResponsableDepe responsableDepeOrigen;

		try {
			Dal Dal = new Dal();
			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 470);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_SALIDA);

			radiSalida.setTipoRadicacion("TR");
			radiSalida.setDependenciaDestino(codigoDependencia.shortValue());

			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();
			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);
			observaciones = StringEscapeUtils.escapeJava(StringUtils.chomp(observaciones));
			radiSalida.setObservaciones(observaciones.replace("\\n", ""));

			Persona funcionario = new Persona();
			funcionario.setId(tramiteSolicitud.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			responseResponsable = wsInteropClient.dependenciaResponsable(codigoDependencia);
			responsableDepe = responseResponsable.getResponsable();

			responseResponsableOrigen = wsInteropClient.dependenciaResponsable(104);
			responsableDepeOrigen = responseResponsableOrigen.getResponsable();

			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {
				boolean changeEstado;
				changeEstado = Dal.setCeslTramiteEstado(tramiteSolicitud.getIdtramite(),
						EstadoTramite.FINALIZADO.getValue());
				if (changeEstado) {
					radiSalida = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent();

					String contentPDF = templateContent.buildTrasladoPDFTemplate(radiSalida,
							tramiteSolicitud.getIdtiposolicitud(), responsableDepeOrigen, responsableDepe, funcionario,
							observaciones);

					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;
					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radiSalida.getConsecutivo(),
									tramiteSolicitud.getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

					radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()), att.getFileName());
						radiSalida.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);
					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);
						List<String> listaEmails = this.getListaEmails(radicador.getEmails());

						String htmlEmail = templateContent.buildEmailTemplateCotizacion(tramiteSolicitud,
								listaEmails.get(0));

						MailService.Send(listaEmails,
								"Traslado - " + tramiteSolicitud.getAno_radi() + "-" + tramiteSolicitud.getNume_radi(),
								htmlEmail, adjuntos);
						this.AddInfoMessage("Solicitud trasaladada correctamente", "msgTraslado");
					} else {
						this.AddErrorMessage(responseRadicacionAdjuntos.getMensaje(), "msgTraslado");
					}

				} else {
					this.AddErrorMessage("No fue posible cambioar el estado en el sistema de copias", "msgTraslado");
				}

			} else {

				this.AddErrorMessage(responseRadicacion.getMensaje(), "msgTraslado");
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}

	}

	private void enviarEmail(Cesl_tramite tramite, int actuacion) throws MalformedURLException, Exception {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);

		TemplateContent templateContent = new TemplateContent();
		String htmlEmail = null;

		List<Email> emailSolicitanteEmail = new ArrayList<>();

		List<Email> emailFuncionarioFull = new ArrayList<>();
		List<String> emailFuncionario = new ArrayList<>();

		emailSolicitanteEmail = wsInteropClient.personaConsultar(tramite.getIden_pers()).getPersona().getEmails();
		emailFuncionarioFull = wsInteropClient.personaConsultar(tramite.getFunc_asignado()).getPersona().getEmails();

		List<String> emailSolicitante = this.getListaEmails(emailSolicitanteEmail);

		for (Email email : emailSolicitanteEmail) {
			logger.info("Procesando email : " + email.getDescripcion());
			emailSolicitante.add(email.getDescripcion());
		}

		for (Email email : emailFuncionarioFull) {
			logger.info("Procesando email : " + email.getDescripcion());
			emailFuncionario.add(email.getDescripcion());
		}

		if (actuacion == 451) {
			ResponseResponsable responsable = wsInteropClient.dependenciaResponsable(0);

		}

		switch (actuacion) {
		case 445:
			htmlEmail = templateContent.buildEmailTemplateProrroga(tramite, emailSolicitante.get(0));
			MailService.Send(emailSolicitante,
					"Solicitud de prorroga radicado #" + tramite.getAno_radi() + "-" + tramite.getNume_radi(),
					htmlEmail, null);
			break;
		case 430:
			htmlEmail = templateContent.buildEmailTemplateComplementar(tramite, emailSolicitante.get(0));
			MailService.Send(emailSolicitante,
					"Solicitud de complemento radicado #" + tramite.getAno_radi() + "-" + tramite.getNume_radi(),
					htmlEmail, null);
			break;
		case 444:
			htmlEmail = templateContent.buildEmailTemplateRtaSolicitante(tramite, emailSolicitante.get(0));
			MailService.Send(emailSolicitante, "Respuesta a requerimiento solicitante radicado #"
					+ tramite.getAno_radi() + "-" + tramite.getNume_radi(), htmlEmail, null);
			MailService.Send(emailFuncionario, "Respuesta a requerimiento solicitante radicado #"
					+ tramite.getAno_radi() + "-" + tramite.getNume_radi(), htmlEmail, null);
			break;
		case 470:
			htmlEmail = templateContent.buildEmailTemplateTrasLado(tramite, emailSolicitante.get(0));
			MailService.Send(emailSolicitante,
					"Traslado por competencias radicado #" + tramite.getAno_radi() + "-" + tramite.getNume_radi(),
					htmlEmail, null);
			break;
		default:
			break;
		}

	}

	private void download(ByteArrayOutputStream fileContent, String fileName, boolean attachment) throws IOException {
		ExternalContext externalContext = getExternalContext();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
		int fileSize = fileContent.size();
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		externalContext.setResponseHeader("Content-Disposition",
				String.format(Constantes.SENDFILE_HEADER, (attachment ? "attachment" : "inline"), encodeURL(fileName)));
		response.setHeader("Content-Length", String.valueOf(fileSize));
		response.setContentType(getMimeType(fileName));
		response.setBufferSize(Constantes.DEFAULT_SENDFILE_BUFFER_SIZE);
		response.setContentLength(fileSize);
		try (OutputStream os = response.getOutputStream()) {
			fileContent.writeTo(os);
		}
		FacesContext.getCurrentInstance().responseComplete();
	}

	public void downloadFile(ByteArrayOutputStream fileContent, String fileName, boolean attachment)
			throws IOException {
		ExternalContext externalContext = getExternalContext();
		HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
		int fileSize = fileContent.size();
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		externalContext.setResponseHeader("Content-Disposition",
				String.format(Constantes.SENDFILE_HEADER, (attachment ? "attachment" : "inline"), encodeURL(fileName)));
		response.setHeader("Content-Length", String.valueOf(fileSize));
		response.setContentType(getMimeType(fileName));
		response.setBufferSize(Constantes.DEFAULT_SENDFILE_BUFFER_SIZE);
		response.setContentLength(fileSize);
		try (OutputStream os = response.getOutputStream()) {
			fileContent.writeTo(os);
		}
		FacesContext.getCurrentInstance().responseComplete();
	}

	public String showAdjuntoENVisor() {
		try {
			if (tramiteSolicitud == null)
				return "";

			String ano = String.valueOf(tramiteSolicitud.getAno_radi());
			String nume = String.valueOf(tramiteSolicitud.getNume_radi());
			String cons = String.valueOf(tramiteSolicitud.getCons_radi() != null ? tramiteSolicitud.getCons_radi() : 0);

			String trama = obtenerTramaSoap(ano, nume, cons);

			if (StringUtils.isBlank(trama))
				return "";

			return trama.trim();

		} catch (Exception e) {
			logger.error("Error construyendo URL del visor por trama SOAP", e);
			return "";
		}
	}

	private String obtenerTramaSoap(String anoRadi, String numeRadi, String consRadi) throws Exception {

		String soapBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:bean=\"http://beanService/\">"
				+ "<soapenv:Header/><soapenv:Body><bean:encrypt>" + "<anoRadi>" + anoRadi + "</anoRadi>" + "<numeRadi>"
				+ numeRadi + "</numeRadi>" + "<contRadi></contRadi>" + "<consRadi>" + consRadi + "</consRadi>"
				+ "<idenPers></idenPers>" + "<codiSist>Int3rOp.Int_Vis0r</codiSist>"
				+ "</bean:encrypt></soapenv:Body></soapenv:Envelope>";

		java.net.URL url = new java.net.URL(Constantes.URL_VISOR);
		java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(10000);
		conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

		try (OutputStream os = conn.getOutputStream()) {
			os.write(soapBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		}

		String response;
		try (InputStream is = conn.getInputStream()) {
			response = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		} finally {
			conn.disconnect();
		}

		// Extraer <trama>
		javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc = factory.newDocumentBuilder()
				.parse(new org.xml.sax.InputSource(new java.io.StringReader(response)));

		org.w3c.dom.NodeList list = doc.getElementsByTagName("trama");
		return list.getLength() > 0 ? list.item(0).getTextContent() : "";
	}

	public void descargarAdjunto(String pathToFind, String fileName) {
		try {
			logger.info("Descargando adjunto");
			if (pathToFind.isEmpty()) {
				logger.info("No se ha especificado una ruta valida");
				return;
			}
			ByteArrayOutputStream fileInMemory = getFileContent(pathToFind);
			downloadFile(fileInMemory, fileName, true);
		} catch (IOException ex) {
			logger.error("File Not found");
			logger.error(ex.getLocalizedMessage());
		}
	}

	private ByteArrayOutputStream getFileContent(String fullFileName) throws FileNotFoundException, IOException {
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (FileInputStream fis = new FileInputStream(fullFileName);
				BufferedInputStream bis = new BufferedInputStream(fis)) {

			int bytes;
			while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
				baos.write(buffer, 0, bytes);
			}
		}

		return baos;
	}

	public void getMyAssignedRequest() throws Exception {
		getListaSolicitudes().clear();
		try (Dal Dal = new Dal()) {
			setListaSolicitudes(Dal.getMyAssignedRequest(getDatosSesion().getId()));
		}

		this.habilitaNota = false;
		for (Cesl_tramite tramite : listaSolicitudes) {
			if (tramite.getNotificacion()) {
				this.habilitaNota = true;
				break;
			}
		}
	}

	private Usuario getDatosSesion() {
		HttpSession session = getSession();
		if (session != null) {
			Object obj = session.getAttribute(Constantes.LLAVE_SESION_USUARIO);
			if (obj != null) {
				return (Usuario) obj;
			}
		}
		return null;
	}

	/**
	 * @return the listaSolicitudes
	 */
	public List<Cesl_tramite> getListaSolicitudes() {
		return listaSolicitudes;
	}

	/**
	 * @param listaSolicitudes the listaSolicitudes to set
	 */
	public void setListaSolicitudes(List<Cesl_tramite> listaSolicitudes) {
		this.listaSolicitudes = listaSolicitudes;
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
	 * @return the persona
	 */
	public Persona getPersona() {
		return persona;
	}

	/**
	 * @param persona the persona to set
	 */
	public void setPersona(Persona persona) {
		this.persona = persona;
	}

	/**
	 * @return the direccion
	 */
	public String getDireccion() {
		return direccion;
	}

	/**
	 * @param direccion the direccion to set
	 */
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	/**
	 * @return the telefono
	 */
	public String getTelefono() {
		return telefono;
	}

	/**
	 * @param telefono the telefono to set
	 */
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the adjuntos
	 */
	public List<Adjunto> getAdjuntos() {
		return getListaAdjuntos();
	}

	/**
	 * @param adjuntos the adjuntos to set
	 */
	public void setAdjuntos(List<Adjunto> adjuntos) {
		this.setListaAdjuntos(adjuntos);
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
	 * @return the download
	 */
	public StreamedContent getDownload() {
		return download;
	}

	/**
	 * @param download the download to set
	 */
	public void setDownload(StreamedContent download) {
		this.download = download;
	}

	/**
	 * @return the codigoDependencia
	 */
	public Integer getCodigoDependencia() {
		return codigoDependencia;
	}

	/**
	 * @param codigoDependencia the codigoDependencia to set
	 */
	public void setCodigoDependencia(Integer codigoDependencia) {
		this.codigoDependencia = codigoDependencia;
	}

	/**
	 * @return the listaDependencias
	 */
	public List<Dependencia> getListaDependencias() {
		return listaDependencias;
	}

	/**
	 * @param listaDependencias the listaDependencias to set
	 */
	public void setListaDependencias(List<Dependencia> listaDependencias) {
		this.listaDependencias = listaDependencias;
	}

	/**
	 * @return the habilitaTrasLado
	 */
	public boolean isHabilitaTrasLado() {
		return habilitaTrasLado;
	}

	/**
	 * @param habilitaTrasLado the habilitaTrasLado to set
	 */
	public void setHabilitaTrasLado(boolean habilitaTrasLado) {
		this.habilitaTrasLado = habilitaTrasLado;
	}

	/**
	 * @return the listaAdscritos
	 */
	public List<AdscritoDepe> getListaAdscritos() {
		return listaAdscritos;
	}

	/**
	 * @param listaAdscritos the listaAdscritos to set
	 */
	public void setListaAdscritos(List<AdscritoDepe> listaAdscritos) {
		this.listaAdscritos = listaAdscritos;
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
	public Referencia getTipoCopia() {
		return tipoCopia;
	}

	/**
	 * 
	 * @param tipoCopia
	 */
	public void setTipoCopia(Referencia tipoCopia) {
		this.tipoCopia = tipoCopia;
	}

	/**
	 * 
	 * @return
	 */
	public List<Referencia> getListaTipoCopia() {
		if (listaTipoCopia == null) {
			listaTipoCopia = new ArrayList<>();
			listaTipoCopia.add(new Referencia(1, "Simple"));
			listaTipoCopia.add(new Referencia(2, "Autenticación digital"));
		}
		return listaTipoCopia;
	}

	/**
	 * 
	 * @param listaTipoCopia
	 */
	public void setListaTipoCopia(List<Referencia> listaTipoCopia) {
		this.listaTipoCopia = listaTipoCopia;
	}

	/**
	 * @return the solicitudTrasladada
	 */
	public boolean isSolicitudTrasladada() {
		return solicitudTrasladada;
	}

	/**
	 * @param solicitudTrasladada the solicitudTrasladada to set
	 */
	public void setSolicitudTrasladada(boolean solicitudTrasladada) {
		this.solicitudTrasladada = solicitudTrasladada;
	}

	/**
	 * @return the mensaje
	 */
	public String getMensaje() {
		return mensaje;
	}

	/**
	 * @param mensaje the mensaje to set
	 */
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public void handleFileUpload(FileUploadEvent event) {
		Usuario usr = getDatosSesion();
		String directory = String.format("%s/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, usr.getLogin());

		if (getTotalAttachmentSize() > Constantes.MAX_SIZE_ATTACHMENTS) {
			this.AddErrorMessage("Error: " + event.getFile().getFileName()
					+ " no se puede adjuntar, se ha superado el tamaño máximo", "msgCotizacion");
			return;
		}

		UploadedFile file = event.getFile();
		String originalFileName = file.getFileName();

		// Validar nombre de archivo
		if (originalFileName == null || originalFileName.trim().isEmpty()) {
			this.AddErrorMessage("Error: Nombre de archivo inválido", "msgCotizacion");
			return;
		}

		// Prevenir path traversal
		String sanitizedFileName = originalFileName.trim();
		if (sanitizedFileName.contains("..") || sanitizedFileName.contains("/") || sanitizedFileName.contains("\\")
				|| sanitizedFileName.contains("~")) {
			this.AddErrorMessage("Error: Nombre de archivo contiene caracteres no permitidos", "msgCotizacion");
			logger.warn("Intento de path traversal detectado: " + originalFileName);
			return;
		}

		// Solo permitir caracteres seguros en el nombre
		if (!sanitizedFileName.matches("^[a-zA-Z0-9._-]+$")) {
			this.AddErrorMessage("Error: Nombre de archivo contiene caracteres inválidos", "msgCotizacion");
			return;
		}

		Path targetLocation = Paths.get(directory);

		try {
			if (!Files.exists(targetLocation)) {
				Files.createDirectories(targetLocation);
			}

			// Normalizar y validar path final
			Path targetFile = targetLocation.resolve(sanitizedFileName).normalize();

			// Verificar que no salga del directorio permitido
			if (!targetFile.startsWith(targetLocation.normalize())) {
				this.AddErrorMessage("Error: Operación de archivo no permitida", "msgCotizacion");
				logger.error("Path traversal detectado después de normalización: " + originalFileName);
				return;
			}

			// Copiar archivo con try-with-resources para cerrar el stream
			try (InputStream input = file.getInputStream()) {
				Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING);
			}

			Attachment att = new Attachment(sanitizedFileName, file.getContentType(), file.getSize(),
					targetFile.toString());

			setTotalAttachmentSize(getTotalAttachmentSize() + att.getFileSize());
			setTotalAttachmentSize(att.roundFileSize(getTotalAttachmentSize(), 3));
			getListaAdjuntoCompletarSolicitud().add(att);

		} catch (IOException e) {
			logger.error("handleFileUpload", e);
			this.AddErrorMessage("Error al guardar el archivo", "msgCotizacion");
		}
	}

	private void deleteAttachmentFile(String fileName) {
		Usuario usr = getDatosSesion();
		String directory = String.format("%s/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, usr.getLogin());
		Path path = FileSystems.getDefault().getPath(directory, fileName);
		try {
			Files.deleteIfExists(path);

		} catch (IOException e) {
			logger.error(e.toString());
		}

	}

	public void EliminarAdjunto(Attachment attach) {

		if (getListaAdjuntoCompletarSolicitud().contains(attach)) {
			deleteAttachmentFile(attach.getFileName());
			setTotalAttachmentSize(getTotalAttachmentSize() - attach.getFileSize());
			getListaAdjuntoCompletarSolicitud().remove(attach);
		}
	}

	/**
	 *
	 * /
	 *
	 **
	 * @return the totalAttachmentSize
	 */
	public double getTotalAttachmentSize() {
		return totalAttachmentSize;
	}

	/**
	 * @param totalAttachmentSize the totalAttachmentSize to set
	 */
	public void setTotalAttachmentSize(double totalAttachmentSize) {
		this.totalAttachmentSize = totalAttachmentSize;
	}

	/**
	 * @return the listaAdjuntoCompletarSolicitud
	 */
	public List<Attachment> getListaAdjuntoCompletarSolicitud() {
		return listaAdjuntoCompletarSolicitud;
	}

	/**
	 * @param listaAdjuntoCompletarSolicitud the listaAdjuntoCompletarSolicitud to
	 *                                       set
	 */
	public void setListaAdjuntoCompletarSolicitud(List<Attachment> listaAdjuntoCompletarSolicitud) {
		this.listaAdjuntoCompletarSolicitud = listaAdjuntoCompletarSolicitud;
	}

	public void rtaSolicitante() {
		if (infoAdicional.isEmpty()) {
			this.AddErrorMessage("No se ha diligenciado el campo Información Adicional", "msgRtaSolicitante");
			return;
		}

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		try {
			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 444);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radiSalida.setTipoRadicacion("EN");
			radiSalida.setDependenciaDestino((short) 104);
			radiSalida.setObservaciones(infoAdicional);

			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();

			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);
			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {
				Dal Dal = new Dal();
				if (Dal.setCeslTramiteEstado(getTramiteSolicitud().getIdtramite(),
						EstadoTramite.RTA_SOLICITANTE.getValue())) {

					// Actualizar notificación para indicarle que fue pagado
					Dal.updateNotificacionByTramite(tramiteSolicitud.getIdtramite(), "S");
					radiSalida = responseRadicacion.getRadicacion();

					ResponseResponsable responseResponsable = wsInteropClient
							.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());
					ResponsableDepe responsableDepe = responseResponsable.getResponsable();

					Persona funcionario = new Persona();
					funcionario.setId(tramiteSolicitud.getFunc_asignado());
					funcionario.setRetornarSoloUltimosDatos(true);
					funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

					TemplateContent templateContent = new TemplateContent();
					String contentPDF = templateContent.buildRespuestaComplementarPDFTemplate(radiSalida,
							tramiteSolicitud.getIdtiposolicitud(), responsableDepe, funcionario, infoAdicional);
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radiSalida.getConsecutivo(),
									tramiteSolicitud.getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					String fileName = String.format("%s.%s", radiSalida.getFullNumeroRadicacion(),
							Constantes.PDF_EXTENSION);
					String base64EncodedPdf = Base64.getEncoder().encodeToString(fileContent.toByteArray());
					logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
					radiSalida.addAdjunto(base64EncodedPdf, fileName, true);

					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()), att.getFileName());
						radiSalida.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);

					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						logger.info(getTramiteSolicitud().getNume_radi().toString());
						this.AddInfoMessage("Solicitud enviada correctamente", "msgRtaSolicitante");
						enviarEmail(getTramiteSolicitud(), 444); // 444
					}
				} else {
					logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
					this.AddErrorMessage(
							"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
							"msgRtaSolicitante");
				}

			} else {
				setMensaje(responseRadicacion.getMensaje());
				this.AddErrorMessage("Se presento un error, por favor contacte al administrador", "msgRtaSolicitante");
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
			Cesl_detalleSolicitud tramiteDetalle = Dal.getDetallesTramite(tramiteSolicitud.getIdtramite()).get(0);
			Direccion direccionEmail = Dal.getDireccion(tramiteSolicitud.getIden_pers(), tramiteDetalle.getConsEmail());
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

	public void complementarSolicitud() {
		if (observaciones.isEmpty()) {
			this.AddErrorMessage("No se ha diligenciado el campo observaciones", "mgComplementar");
			return;
		}
		observaciones = StringEscapeUtils.escapeJava(StringUtils.chomp(observaciones));

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		ResponseTramite responseTramite = null;
		try {

			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 430);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radiSalida.setTipoRadicacion("SA");
			radiSalida.setDependenciaDestino((short) 104);
			radiSalida.setObservaciones(observaciones.replace("\\n", " "));
			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();
			radiSalida.setRadicador(radicador);

			Persona funcionario = new Persona();
			funcionario.setId(tramiteSolicitud.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			radiSalida.setTotalFolios(1);

			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {
				Dal Dal = new Dal();
				if (Dal.setCeslTramiteEstado(getTramiteSolicitud().getIdtramite(),
						EstadoTramite.COMPLEMENTAR.getValue())) {
					radiSalida = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent();

					Persona personaResponsable = new Persona();
					ResponseResponsable responseResponsable = wsInteropClient
							.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());
					ResponsableDepe responsableDepe = responseResponsable.getResponsable();
					radiSalida.setRadicador(radicador);
					String contentPDF = templateContent.buildComplementarPDFTemplate(radiSalida,
							tramiteSolicitud.getIdtiposolicitud(), responsableDepe, funcionario, observaciones);
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSolicitud.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramiteSolicitud.getIdtramite(), radiSalida.getConsecutivo(),
									tramiteSolicitud.getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

					radiSalida.addAdjunto(fullPathRadicadoEntrada, false);
					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()), att.getFileName());
						radiSalida.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);

					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);

						List<String> listaEmails = this.getListaEmails(radicador.getEmails());
						String htmlEmail = templateContent.buildEmailTemplateComplementar(tramiteSolicitud,
								listaEmails.get(0));

						MailService.Send(
								listaEmails, "Solicitud de complemento de información - "
										+ tramiteSolicitud.getAno_radi() + "-" + tramiteSolicitud.getNume_radi(),
								htmlEmail, adjuntos);
					}
					this.AddInfoMessage("Solicitud enviada correctamente", "mgComplementar");
					habilitaComplemanatrCiudadano = false;

					// enviarEmail(getTramiteSolicitud(), 430);//430
				} else {
					logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
					this.AddInfoMessage(
							"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
							"mgComplementar");
				}

			} else {
				setMensaje(responseRadicacion.getMensaje());
				this.AddErrorMessage("Se presento un error, por favor contacte al administrador", "mgComplementar");
			}

		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

	}

	public void ProrrogaSolicitud() {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		try {

			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramiteSolicitud.getAno_radi());
			radiSalida.setNumero(tramiteSolicitud.getNume_radi());
			radiSalida.setControl(null);
			Perfil perfilRadicacion = new Perfil();
			perfilRadicacion.setActuacion((short) 445);
			perfilRadicacion.setDependencia((short) 104);
			perfilRadicacion.setEvento((short) 0);
			perfilRadicacion.setTramite((short) 362);
			radiSalida.setPerfil(perfilRadicacion);
			radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radiSalida.setTipoRadicacion("SA");
			radiSalida.setDependenciaDestino((short) 104);

			radiSalida.setIdFuncionario(tramiteSolicitud.getFunc_asignado());

			Persona radicador = new Persona();
			radicador.setId(tramiteSolicitud.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();

			Persona funcionario = new Persona();
			funcionario.setId(tramiteSolicitud.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);

			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);

			if (responseRadicacion.getCodigo() == 0) {

				Dal Dal = new Dal();
				if (Dal.setCeslTramiteEstado(getTramiteSolicitud().getIdtramite(),
						EstadoTramite.SOL_PRORROGA.getValue())) {
					logger.info("se cambio el estado en sistemas de copias");
					this.AddInfoMessage("Solicitud enviada correctamente", "msgProrroga");

					radiSalida = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent();

					ResponseResponsable responseResponsable = wsInteropClient
							.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());
					ResponsableDepe responsableDepe = responseResponsable.getResponsable();

					String contentPDF = templateContent.buildEmailTemplatePdfProrroga(radiSalida, responsableDepe,
							funcionario);
					String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
					String fullPathRadicadoEntrada = null;

					ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							getTramiteSolicitud().getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION,
									radiSalida.getFechaRadicacion().getYear()),
							true, Utility.getBarCode(getTramiteSolicitud().getIdtramite(), radiSalida.getConsecutivo(),
									getTramiteSolicitud().getIdtiposolicitud()));

					fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
					logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

					radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

					for (Attachment att : listaAdjuntoCompletarSolicitud) {
						String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()), att.getFileName());
						radiSalida.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);

					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						List<String> adjuntos = new ArrayList<>();

						adjuntos.add(fullPathRadicadoEntrada);
						List<String> listaEmails = this.getListaEmails(radicador.getEmails());

						String htmlEmail = templateContent.buildEmailTemplateProrroga(getTramiteSolicitud(),
								listaEmails.get(0));

						MailService.Send(listaEmails, "Requerimiento Interno - " + getTramiteSolicitud().getAno_radi()
								+ "-" + getTramiteSolicitud().getNume_radi(), htmlEmail, adjuntos);
					}
				} else {
					logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
					this.AddInfoMessage(
							"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
							"msgProrroga");
				}

			} else {
				setMensaje(responseRadicacion.getMensaje());
				this.AddErrorMessage(responseRadicacion.getMensaje(), "msgProrroga");
			}

		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
	}

	/**
	 * @return the listaExpedientes
	 */
	public List<Tramite> getListaExpedientes() {
		return listaExpedientes;
	}

	/**
	 * @param listaExpedientes the listaExpedientes to set
	 */
	public void setListaExpedientes(List<Tramite> listaExpedientes) {
		this.listaExpedientes = listaExpedientes;
	}

	/**
	 * @return the listaAdjuntos
	 */
	public List<Adjunto> getListaAdjuntos() {
		return listaAdjuntos;
	}

	/**
	 * @param listaAdjuntos the listaAdjuntos to set
	 */
	public void setListaAdjuntos(List<Adjunto> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	/**
	 * @return the infoAdicional
	 */
	public String getInfoAdicional() {
		return infoAdicional;
	}

	/**
	 * @param infoAdicional the infoAdicional to set
	 */
	public void setInfoAdicional(String infoAdicional) {
		this.infoAdicional = infoAdicional;
	}

	/**
	 * @return the listaComentarios
	 */
	public List<Obse_Radi> getListaComentarios() {
		return listaComentarios;
	}

	/**
	 * @param listaComentarios the listaComentarios to set
	 */
	public void setListaComentarios(List<Obse_Radi> listaComentarios) {
		this.listaComentarios = listaComentarios;
	}

	/**
	 * @return the tipoTraslado
	 */
	public boolean isTipoTraslado() {
		return tipoTraslado;
	}

	/**
	 * @param tipoTraslado the tipoTraslado to set
	 */
	public void setTipoTraslado(boolean tipoTraslado) {
		this.tipoTraslado = tipoTraslado;
	}

	/**
	 * @return the habilitaComplemanatrCiudadano
	 */
	public boolean isHabilitaComplemanatrCiudadano() {
		return habilitaComplemanatrCiudadano;
	}

	/**
	 * @param habilitaComplemanatrCiudadano the habilitaComplemanatrCiudadano to set
	 */
	public void setHabilitaComplemanatrCiudadano(boolean habilitaComplemanatrCiudadano) {
		this.habilitaComplemanatrCiudadano = habilitaComplemanatrCiudadano;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isHabilitaNota() {
		return habilitaNota;
	}

	/**
	 * 
	 * @param habilitaNota
	 */
	public void setHabilitaNota(boolean habilitaNota) {
		this.habilitaNota = habilitaNota;
	}

}
