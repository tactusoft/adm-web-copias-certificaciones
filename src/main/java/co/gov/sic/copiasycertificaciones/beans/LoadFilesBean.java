/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Attachment;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.MedioEntrada;
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
@Named("loadFilesBean")
@SessionScoped
public class LoadFilesBean implements Serializable {

	private static final long serialVersionUID = -2939845650224883108L;
	private List<Attachment> listaAdjunto;
	private Cesl_tramite tramite;
	private Cesl_detalleSolicitud tramiteDetalle;
	protected final Logger logger = LoggerFactory.getLogger(LoadFilesBean.class);
	private String observaciones;
	private String tipoCopias;
	private boolean observacionesObligatorias;
	private ByteArrayOutputStream fileContentSinFirma;

	public LoadFilesBean() {
		listaAdjunto = new ArrayList<>();
		observacionesObligatorias = true;
	}

	private String getRuta() {
		return String.format("%s%02d/%02d-%06d/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, tramite.getAno_radi(),
				tramite.getAno_radi(), tramite.getNume_radi());
	}

	public void handleFileUpload(FileUploadEvent event) {
		String directory = getRuta();
		UploadedFile file = event.getFile();
		Path targetLocation = Paths.get(directory);

		try {
			if (!Files.exists(targetLocation)) {
				Files.createDirectories(targetLocation);
			}

			InputStream input = file.getInputStream();
			Files.copy(input, new File(directory, file.getFileName()).toPath());
			Attachment att = new Attachment(file.getFileName(), file.getContentType(), file.getSize(),
					directory + file.getFileName());
			getListaAdjunto().add(att);

		} catch (IOException e) {
			logger.error("handleFileUpload", e);
		}
	}

	private void deleteAttachmentFile(String fileName) {
		String directory = getRuta();
		Path path = FileSystems.getDefault().getPath(directory, fileName);
		try {
			Files.deleteIfExists(path);

		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	public void enviarRespuestaFinalizarSolicitud() {
		if ((!observacionesObligatorias && observaciones.isEmpty()) || tipoCopias == null) {
			this.AddErrorMessage("Los campos observaciones y tipos de copias son obligatorios", "msgLoadFiles");
			return;
		}

		Radicacion radiSalida = new Radicacion();
		radiSalida.setAnio(tramite.getAno_radi());
		radiSalida.setNumero(tramite.getNume_radi());

		Direccion direccionFisica = null;

		if (tipoCopias.equals("S")) {
			InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
					Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
			try {
				Dal dal = new Dal();
				String medioEntrada = "SL";
				if (tramiteDetalle.getOpcionesEntrega() != null && tramiteDetalle.getOpcionesEntrega().intValue() != 0
						&& tramiteDetalle.getOpcionesEntrega().intValue() != MedioEntrada.DIGITAL.value()) {
					medioEntrada = "CD";
				}

				List<String> adjuntos = new ArrayList<>();
				radiSalida.setControl(null);
				Perfil perfilRadicacion = new Perfil();
				perfilRadicacion.setActuacion((short) 440);
				perfilRadicacion.setDependencia((short) 104);
				perfilRadicacion.setEvento((short) 0);
				perfilRadicacion.setTramite((short) 362);
				radiSalida.setPerfil(perfilRadicacion);
				radiSalida.setMedioEntrada(medioEntrada);
				radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
				radiSalida.setTipoRadicacion("SA");
				radiSalida.setDependenciaDestino((short) 104);
				// radiSalida.setObservaciones(observaciones);

				radiSalida.setIdFuncionario(tramite.getFunc_asignado());

				Persona radicador = new Persona();
				radicador.setId(tramite.getIden_pers());
				radicador.setRetornarSoloUltimosDatos(true);
				radicador = wsInteropClient.personaConsultar(radicador).getPersona();

				if (medioEntrada.equals("CD")) {
					direccionFisica = dal.getDireccion(tramite.getIden_pers(), tramiteDetalle.getConsDire());
					if (direccionFisica == null) {
						this.AddErrorMessage("El usuario no tiene una dirección física asociada", "msgLoadFiles");
						return;
					}
				}

				if (radicador.getDirecciones().isEmpty() && medioEntrada.equals("CD")) {
					this.AddErrorMessage("El usuario no tiene direcciones fisicas asociadas", "mgComplementar");
				} else {
					radiSalida.setRadicador(radicador);
					radiSalida.setTotalFolios(1);

					Persona funcionario = new Persona();
					funcionario.setId(tramite.getFunc_asignado());
					funcionario.setRetornarSoloUltimosDatos(true);
					funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

					ResponseResponsable responseResponsableNotificaciones = wsInteropClient.dependenciaResponsable(104);
					ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
					if (responseRadicacion.getCodigo() == 0) {
						if (dal.setCeslTramiteEstado(tramite.getIdtramite(), EstadoTramite.FINALIZADO.getValue())) {
							logger.info("se cambio el estado en sistemas de copias");
							this.AddInfoMessage("Solicitud enviada correctamente", "mgComplementar");
							radiSalida = responseRadicacion.getRadicacion();

							// Generar comunicado de salida
							TemplateContent templateContent = new TemplateContent();
							radiSalida.setRadicador(radicador);
							String contentPDF = templateContent.buildRespuestaSolicitudCopias(radiSalida, funcionario,
									responseResponsableNotificaciones.getResponsable(), observacionesObligatorias,
									observaciones);
							String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
							String fullPathRadicadoEntrada = null;

							ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
									tramite.getIdtiposolicitud().getDescripcion(), subject,
									String.format(Constantes.KEYWORDS_PDF_RADICACION,
											radiSalida.getFechaRadicacion().getYear()),
									true, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
											tramite.getIdtiposolicitud()));

							fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
							logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
							radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

							// Agregar adjuntos
							if (!listaAdjunto.isEmpty()) {
								for (Attachment att : listaAdjunto) {
									if ("application/pdf".equals(att.getFileExtension())) {
										String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()),
												att.getFileName());
										radiSalida.addAdjunto(path, false);
										adjuntos.add(path);
										deleteAttachmentFile(att.getFileName());
									}
								}
							}

							ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
									.radicacionAdjuntosRegistrar(radiSalida);

							if (responseRadicacionAdjuntos.getCodigo() == 0) {
								String htmlEmail = null;

								List<Email> emailSolicitanteEmail = new ArrayList<>();

								emailSolicitanteEmail = wsInteropClient.personaConsultar(tramite.getIden_pers())
										.getPersona().getEmails();

								List<String> emailSolicitante = this.getListaEmails(emailSolicitanteEmail);

								adjuntos.add(fullPathRadicadoEntrada);

								htmlEmail = templateContent.buildEmailTemplateRespuestaSolicitud(tramite,
										emailSolicitante.get(0));
								MailService.Send(emailSolicitante, "Respuesta Solicitud radicado #"
										+ tramite.getAno_radi() + "-" + tramite.getNume_radi(), htmlEmail, adjuntos);
							}

							if (medioEntrada.equals("CD")) {
								List<Direccion> listDireccion = new ArrayList<>();
								listDireccion.add(direccionFisica);
								radicador.setDirecciones(listDireccion);

								contentPDF = templateContent.buildRespuestaSolicitudCopiasSinFirma(radiSalida,
										funcionario, responseResponsableNotificaciones.getResponsable(), observaciones,
										radicador);
								subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
								this.fileContentSinFirma = PDFGeneratorService.createPdf(contentPDF,
										tramite.getIdtiposolicitud().getDescripcion(), subject,
										String.format(Constantes.KEYWORDS_PDF_RADICACION,
												radiSalida.getFechaRadicacion().getYear()),
										true, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
												tramite.getIdtiposolicitud()));

								String fullPathMemo = Functions.saveFile(radiSalida, this.fileContentSinFirma);
								logger.info("Memo: " + fullPathMemo);
								dal.setCeslTramiteRutaMemo(tramiteDetalle.getIdtramite(), fullPathMemo);
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
				}
			} catch (IOException ex) {
				logger.error(ex.toString());
			} catch (Exception ex) {
				logger.error(ex.toString());
			}
		} else {
			if (!listaAdjunto.isEmpty()) {
				Dal Dal = new Dal();
				if (Dal.setCeslTramiteEstado(tramite.getIdtramite(),
						EstadoTramite.RADICADO_FIRMA_ELECTRONICA.getValue())) {
					if (Dal.setCeslTramiteEstadoFirmaElectronica(tramite.getIdtramite(), this.observaciones)) {
						this.AddInfoMessage("Solicitud enviada correctamente", "mgComplementar");
					}
				}
			} else {
				this.AddWarningMessage("De agregar mínimo un adjunto");
			}
		}
	}

	private List<String> getListaEmails(List<Email> emails) {
		List<String> listaEmails = new ArrayList<>();
		try {
			Dal Dal = new Dal();
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

	public void descargarRespuesta() throws IOException {
		new DownloadFilesBean().downloadFile(this.fileContentSinFirma, "RespuestaSinFirma.pdf", false);
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

	public void limpiarForm() {

	}

	public void EliminarAdjunto(Attachment attach) {
		if (getListaAdjunto().contains(attach)) {
			deleteAttachmentFile(attach.getFileName());
			getListaAdjunto().remove(attach);
		}
	}

	/**
	 * @return the listaAdjunto
	 */
	public List<Attachment> getListaAdjunto() {
		return listaAdjunto;
	}

	/**
	 * @param listaAdjunto the listaAdjunto to set
	 */
	public void setListaAdjunto(List<Attachment> listaAdjunto) {
		this.listaAdjunto = listaAdjunto;
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

	public void setObservacionesObligatoriasTrue() {
		this.observacionesObligatorias = false;
		this.tipoCopias = "S";
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
		this.tramite = tramite;
		this.fileContentSinFirma = null;
		Dal Dal = new Dal();
		try {
			this.tramiteDetalle = Dal.getDetallesTramite(tramite.getIdtramite()).get(0);
			if (tramiteDetalle.getOpcionesEntrega() != null && tramiteDetalle.getOpcionesEntrega().intValue() != 0
					&& tramiteDetalle.getOpcionesEntrega().intValue() != MedioEntrada.DIGITAL.value()) {
				this.observacionesObligatorias = false;
			}
		} catch (Exception e) {
			logger.error("setTramite", e);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Cesl_detalleSolicitud getTramiteDetalle() {
		return tramiteDetalle;
	}

	/**
	 * 
	 * @param tramiteDetalle
	 */
	public void setTramiteDetalle(Cesl_detalleSolicitud tramiteDetalle) {
		this.tramiteDetalle = tramiteDetalle;
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
	 * @return the observacionesObligatorias
	 */
	public boolean isObservacionesObligatorias() {
		return observacionesObligatorias;
	}

	/**
	 * @param observacionesObligatorias the observacionesObligatorias to set
	 */
	public void setObservacionesObligatorias(boolean observacionesObligatorias) {
		this.observacionesObligatorias = observacionesObligatorias;
	}

	/**
	 * 
	 * @return
	 */
	public String getTipoCopias() {
		return tipoCopias;
	}

	/**
	 * 
	 * @param tipoCopias
	 */
	public void setTipoCopias(String tipoCopias) {
		this.tipoCopias = tipoCopias;
	}

	/**
	 * 
	 * @return
	 */
	public ByteArrayOutputStream getFileContentSinFirma() {
		return fileContentSinFirma;
	}

	/**
	 * 
	 * @param fileContentSinFirma
	 */
	public void setFileContentSinFirma(ByteArrayOutputStream fileContentSinFirma) {
		this.fileContentSinFirma = fileContentSinFirma;
	}

}
