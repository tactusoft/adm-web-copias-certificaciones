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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Attachment;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.PersonaActo;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.MailService;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.Telefono;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.enums.TipoReferenciaEnum;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponsePersona;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author elmos
 */
@Named("overTheCounterBean")
@SessionScoped
public class OverTheCounterBean extends BeanBase implements Serializable {

	private static final long serialVersionUID = -6711066212654428066L;
	protected final Logger logger = LoggerFactory.getLogger(OverTheCounterBean.class);
	private List<Referencia> listaTipoDocumentos;
	private String tipoDocumento;
	private Long numeroIdentificacion;
	private Persona persona;
	private List<Email> userMails;
	private List<Direccion> direcciones;
	private String email;
	private String direccion;
	private String telefono;
	private String codigoRegionDesc;
	private String codigoCiudadDesc;

	private Boolean aceptoTerminos = true;
	private List<Referencia> listaTiposSolicitudes;
	private List<Referencia> listaTiposActo;
	private String tipoActo;
	private String numeActo;
	private Date fechaActo;
	private String tipoCopia;
	private String observaciones;
	private boolean expedienteCompleto;
	private boolean constanciaEjecutoria;
	private String rangoFolios;
	private String numeroRadicacion;
	private List<PersonaActo> personasEjecutoria;
	private List<PersonaActo> selectedPersonas;
	private List<Attachment> listaAdjuntos;
	private double totalAttachmentSize;
	private final int numMaxSolicitudes = 10;
	private String fullName;
	private boolean userExist;

	public OverTheCounterBean() throws Exception {
		super(TipoTramite.COPIAS_SIMPLES);
		userExist = false;
		try {
			listaTipoDocumentos = new ArrayList<>();
			listaTipoDocumentos = Utility.GetReferenciaWS("DOCUMENTO");

			personasEjecutoria = new ArrayList<>();
			selectedPersonas = new ArrayList<>();
			listaAdjuntos = new ArrayList<>();

		} catch (IOException ex) {
			// Logger.getLogger(OverTheCounterBean.class.getName()).log(Level.SEVERE, null,
			// ex);
			logger.error(ex.toString());
		}

	}

	public void consultarPersona() {

	}

	public void onChangeTipoDocumento() {

	}

	public String procesoFlujoOTC(FlowEvent event) {
		final String anteriorStep = event.getOldStep();
		final String nuevoStep = event.getNewStep();
		Boolean moveWizardSteps = null;
		try {
			PrimeFaces.current().ajax().update("frmWizard:pnlButtons");
			if (anteriorStep.equals("legal")) {
				if (nuevoStep.equals("consulta")) {
					moveWizardSteps = false;
					return nuevoStep;
				}
				bindCheckAceptoTerminos();
				if (aceptoTerminos == null || aceptoTerminos == false) {
					AddErrorMessage("Para poder continuar, debe aceptar los términos y condiciones expuestos.");
					return anteriorStep;
				}
			} else if (anteriorStep.equals("personal") && nuevoStep.equals("solicitudes")) {
				if (!userExist) {
					AddErrorMessage(
							"No se ha selleccionado un usuario solicitante valido. Si el usuario solcitante no está registrado por favor registrelo antes de continuar");
					return anteriorStep;
				}
			} else if (anteriorStep.equals("personal") && nuevoStep.equals("legal")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("solicitudes") && nuevoStep.equals("personal")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("confirmacion")) {
				if (nuevoStep.equals("solicitudes")) {
					moveWizardSteps = false;
					return nuevoStep;
				} else if (nuevoStep.equals("pago") || nuevoStep.equals("resultado")) {
					setIsSuccess(salvarSolicitud());
					if (getIsSuccess()) {
						moveWizardSteps = true;
						return nuevoStep;
					} else {
						return anteriorStep;
					}
				}
			} else if (nuevoStep.equals("confirmacion") && anteriorStep.equals("solicitudes")) {
				if (listaDetalles.isEmpty()) {
					AddErrorMessage("Para poder continuar debe agregar al menos una solicitud.");
					return anteriorStep;
				} else if (isAttachmentError()) {
					AddErrorMessage(getAttachmentErrorMsg());
					return anteriorStep;
				}

			}
			moveWizardSteps = true;
			return nuevoStep;
		} catch (Exception e) {
			logger.error("procesoFlujo", e);
			AddErrorMessage("Ha ocurrido un error inesperado.");
			moveWizardSteps = null;
			return event.getOldStep();
		} finally {
			if (moveWizardSteps != null) {
				PrimeFaces.current().executeScript("onmoveWizard(" + String.valueOf(moveWizardSteps) + ");");
				if (containsInputFile) {
					PrimeFaces.current().executeScript("bsCustomFileInput.init();");
				}
			}
			PrimeFaces.current().executeScript("BindToolTips();");
		}
	}

	public void onChangeDocumentNumber() {
		limpiarForm();
		userExist = false;
		try {
			InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
					Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
			persona = new Persona();
			persona.setNumeroDocumento(numeroIdentificacion);
			persona.setTipoDocumento(tipoDocumento);
			persona.setRetornarSoloUltimosDatos(true);
			ResponsePersona responsePersona = wsInteropClient.personaConsultar(persona);

			if (responsePersona.getCodigo() != 0) {
				AddErrorMessage(responsePersona.getMensaje(), "msgUserData");
				return;
			}

			persona = responsePersona.getPersona();

			if (persona == null) {
				AddInfoMessage("El usuario solicitante no esta registrado", "msgUserData");
				return;
			}

			setUserMails(persona.getEmails());
			setDirecciones(persona.getDirecciones());
			for (Email mail : getUserMails()) {

				if (persona.getTipoPersona().equals("FU")) {
					if (mail.getTipo().equals("EM")) {
						email = mail.getDescripcion();
					} else if (mail.getTipo().equals("PE")) {
						email = mail.getDescripcion();
					}

				} else {
					if (mail.getTipo().equals("EM")) {
						email = mail.getDescripcion();
					} else if (mail.getTipo().equals("PE")) {
						email = mail.getDescripcion();
					}
				}

			}
			List<Telefono> telf = new ArrayList<>();
			for (Direccion dir : getDirecciones()) {
				direccion = dir.getDescripcion();
				codigoRegionDesc = dir.getCodigoRegionDesc();
				codigoCiudadDesc = dir.getCodigoCiudadDesc();
				telf = dir.getTelefonos();
			}

			for (Telefono tel : telf) {
				telefono = tel.getNumero();
				break;
			}

			if (persona.getTipoPersona().equals("NA")) {
				fullName = persona.getNatural().getFullName();
			} else if (persona.getTipoPersona().equals("FU")) {
				fullName = persona.getNatural().getFullName();
			}
			userExist = true;

		} catch (IOException ex) {
			// Logger.getLogger(OverTheCounterBean.class.getName()).log(Level.SEVERE, null,
			// ex);
			logger.error(ex.toString());

		}
	}

	private void deleteAttachmentFile(String fileName) {
		Usuario usr = getDatosSesion();
		String directory = String.format("%s/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, usr.getLogin());
		Path path = FileSystems.getDefault().getPath(directory, fileName);
		try {
			Files.deleteIfExists(path);

		} catch (IOException e) {
			logger.error("deleteAttachmentFile", e);
		}

	}

	public void EliminarAdjunto(Attachment attach) {

		if (listaAdjuntos.contains(attach)) {
			deleteAttachmentFile(attach.getFileName());
			totalAttachmentSize -= attach.getFileSize();
			listaAdjuntos.remove(attach);
		}
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
			getListaAdjuntos().add(att);

		} catch (IOException e) {
			logger.error("handleFileUpload", e);
			this.AddErrorMessage("Error al guardar el archivo", "msgCotizacion");
		}
	}

	public void addRowTable() throws Exception {
		boolean isValid = true;

		// Proceso de validación
		if (Utility.isNullOrEmptyTrim(this.tipoCopia)) {
			isValid = false;
			AddErrorMessage("El tipo de copia es requerido.");
		}
		if (Utility.isNullOrEmptyTrim(this.observaciones)) {
			isValid = false;
			AddErrorMessage("La Descripción de la Solicitud es requerida.");
		}
		String personasSeleccionadas = "";
		if (constanciaEjecutoria) {
			isValid = false;
			if (selectedPersonas != null && selectedPersonas.size() > 0) {
				isValid = true;
				for (PersonaActo pers : selectedPersonas) {
					personasSeleccionadas = String.format("%s\n%s %s - %s", personasSeleccionadas,
							pers.getTipoDocumento(), pers.getNumeroDocumento(), pers.getNombrePersona());
				}
			}
			if (!isValid) {
				AddErrorMessage("Debe seleccionar por lo menos una persona para emitir ejecutoria.");
			}
		}
		int numtotalCertificaciones = 1 + listaDetalles.size();
		if (numtotalCertificaciones > numMaxSolicitudes) {
			setShowPanelAgregar(true);
			isValid = false;
			AddErrorMessage("Solo se aceptan máximo  " + numMaxSolicitudes + " solicitudes.");
		} else if (numtotalCertificaciones == numMaxSolicitudes && isValid) {
			setShowPanelAgregar(false);
		}

		// Si no hay errores
		if (isValid) {
			Cesl_detalleSolicitud detalle = new Cesl_detalleSolicitud();
			detalle.setIdtramite(listaDetalles.size());
			detalle.setTipo_certifica(tipoCopia);
			detalle.setTipo_certifica_descripcion(getTipoCopiaDescripcion());
			detalle.setVariableAdicional1(getTipoActoDescripcion());
			if (!Utility.isNullOrEmptyTrim(getNumeActo())) {
				detalle.setVariableAdicional2(getNumeActo());
			}
			detalle.setFechaAdicional1(Utility.convertToLocalDateViaInstant(this.fechaActo));
			if (this.expedienteCompleto) {
				this.observaciones += ".\nSe solicita copia de todo el expediente.";
			} else if (!Utility.isNullOrEmptyTrim(this.rangoFolios)) {
				this.observaciones += ".\nSe solicita copia del rango de folios: " + this.rangoFolios + ".";
			}
			if (constanciaEjecutoria && !Utility.isNullOrEmptyTrim(personasSeleccionadas)) {
				this.observaciones += ".\nPersonas a emitir Ejecutoria:" + personasSeleccionadas;
			}
			detalle.setObservaciones(this.observaciones);
			listaDetalles.add(detalle);
			limpiarForm();
		}
	}

	private String getTipoCopiaDescripcion() throws Exception {
		List<Referencia> tipos = getListaTiposSolicitudes();
		for (Referencia referencia : tipos) {
			if (referencia.getCodigo().equals(this.tipoCopia)) {
				return referencia.getValor();
			}
		}
		return null;
	}

	public void onTipoCopiaChange() {
		try {
			personasEjecutoria.clear();
			constanciaEjecutoria = tipoCopia != null && (tipoCopia.equals("AC") || tipoCopia.equals("SC"));
			if (constanciaEjecutoria) {
				if (!Utility.isNullOrEmptyTrim(tipoActo) && fechaActo != null
						&& !Utility.isNullOrEmptyTrim(getNumeActo())) {
					int acto = Integer.parseInt(getNumeActo());
					try (Dal Dal = new Dal()) {
						personasEjecutoria = Dal.getPartesActo(tipoActo, acto, Utility.convertUtilToSql(fechaActo));
					}
				}
			}
		} catch (Exception ex) {
			super.AddErrorMessage("No se pudo consultar la lista de personas vinculadas al acto administrativo.");
			logger.error("onTipoCopiaChange", ex);
		}
	}

	private String getTipoActoDescripcion() throws Exception {
		List<Referencia> tipos = getListaTiposActo();
		for (Referencia referencia : tipos) {
			if (referencia.getCodigo().equals(this.tipoActo)) {
				return referencia.getValor();
			}
		}
		return null;
	}

	public void limpiarForm() {
		this.tipoActo = null;
		this.numeActo = null;
		this.fechaActo = null;
		this.tipoCopia = null;
		this.observaciones = null;
		this.selectedPersonas = new ArrayList<>();
		this.personasEjecutoria = new ArrayList<>();
		this.constanciaEjecutoria = false;
		this.email = "";
		this.fullName = "";
		this.direccion = "";
		this.telefono = "";
		this.codigoCiudadDesc = "";
	}

	@PostConstruct
	public void init() {
		setShowPanelAgregar(true);
		setIsSuccess(false);
	}

	public List<Referencia> getListaTiposSolicitudes() throws Exception {
		if (listaTiposSolicitudes == null) {
			listaTiposSolicitudes = Utility.GetReferenciaWS("TIPOSOL_SEDELECTRO");
		}
		return listaTiposSolicitudes;
	}

	public List<Referencia> getListaTiposActo() throws Exception {
		if (this.listaTiposActo == null) {
			listaTiposActo = Utility.GetReferenciaWS(TipoReferenciaEnum.TIPO_ACTO);
		}
		return this.listaTiposActo;
	}

	public boolean salvarSolicitud() throws Exception {
		boolean result = false;
		if (!getAceptoSolicitud()) {
			AddErrorMessage("Debe confirmar que todos los datos ingresados son correctos.");
			result = false;
		} else {
			Usuario datosSesion = this.getDatosSesion();
			try (Dal Dal = new Dal()) {
				Double totalTramite = null;
				// Calculamos es total de solicitudes * el previo o valor del trámite
				if (valorUnitario != null) {
					totalTramite = 0d;
					for (Cesl_detalleSolicitud detalle : listaDetalles) {
						detalle.setValor(detalle.getCantidad() * this.valorUnitario);
						totalTramite += detalle.getValor();
					}
				}
				// PROCEDER A INSERTAR EN LA TABLA DE cesl_tramite y retornamos el último id
				// gnerado
				tramiteSeleccionado = Dal.insertarTramite(EstadoTramite.PRESENTADO, totalTramite, getTipoTramite(),
						persona.getId());
				// RECORREMOS LAS SOLICITUDES Y REGISTRAR EN LA BASE DE DATOS
				for (Cesl_detalleSolicitud detalle : listaDetalles) {
					detalle.setIdtramite(tramiteSeleccionado.getIdtramite());
					Dal.insertarDetalleTramite(detalle);
				}
			}
			result = true;
		}

		setAttachmentError(false);
		if (result) {
			Radicacion radi = new Radicacion();
			// Persona cuurentUser = getDatosSesion().getPersona();
			radi.setRadicador(persona);
			radi.setPerfil(perfilTramite);
			radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
			radi.setTotalFolios(1);
			radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
			InteropWSClient wsInteropClient = Utility.GetWSClient();
			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radi);
			try (Dal Dal = new Dal()) {
				if (responseRadicacion.getCodigo() == 0) {
					radi = responseRadicacion.getRadicacion();
					TemplateContent templateContent = new TemplateContent();
					this.numeroRadicacion = radi.getShortNumeroRadicacion();
					tramiteSeleccionado.setAno_radi(radi.getAnio());
					tramiteSeleccionado.setNume_radi(radi.getNumero());
					tramiteSeleccionado.setCont_radi(radi.getControl());
					tramiteSeleccionado.setCons_radi(radi.getConsecutivo());
					tramiteSeleccionado.setEstado(EstadoTramite.RADICADO_ENTRADA);
					Dal.actualizarTramite(tramiteSeleccionado);

					String contentPDF = templateContent.buildRadicacionPDFTemplate(radi, tramiteSeleccionado,
							listaDetalles);
					String subject = String.format("Radicación SIC %s", radi.getShortNumeroRadicacion());

					String fullPathRadicadoEntrada = null;

					try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
							tramiteSeleccionado.getIdtiposolicitud().getDescripcion(), subject,
							String.format(Constantes.KEYWORDS_PDF_RADICACION, radi.getFechaRadicacion().getYear()),
							false, Utility.getBarCode(tramiteSeleccionado.getIdtramite(), radi.getConsecutivo(),
									tramiteSeleccionado.getIdtiposolicitud()))) {
						fullPathRadicadoEntrada = Functions.saveFile(radi, fileContent);
						logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);
					}

					radi.addAdjunto(fullPathRadicadoEntrada, false);

					for (Attachment att : listaAdjuntos) {
						String path = Functions.saveFile(radi, getFileContent(att.getPath()), att.getFileName());
						radi.addAdjunto(path, false);
						deleteAttachmentFile(att.getFileName());
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient.radicacionAdjuntosRegistrar(radi);
					// if (responseRadicacionAdjuntos.getCodigo() == 0) {
					// Enviar email con radicacion de entrada
					String htmlEmail = templateContent.buildEmailTemplate(radi,
							tramiteSeleccionado.getIdtiposolicitud());
					List<String> adjuntos = new ArrayList<>();
					adjuntos.add(fullPathRadicadoEntrada);
					MailService.Send(persona.getEmails().get(0).getDescripcion(),
							tramiteSeleccionado.getIdtiposolicitud().getDescripcion(), htmlEmail, adjuntos);
					/*
					 * } else { setAttachmentErrorMsg("Radicacion Entrada adjuntos: " +
					 * responseRadicacionAdjuntos.getMensaje());
					 * logger.error(getAttachmentErrorMsg()); setAttachmentError(true); }
					 */

				}
			} catch (Exception e) {
				logger.error("procesoFlujo", e);
				AddErrorMessage("Ha ocurrido un error inesperado.");
				result = false;
			}
		}
		return result;
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

	//// Setters and getters/////////////////////////////////
	/**
	 * @return the listaTipoDocumentos
	 */
	public List<Referencia> getListaTipoDocumentos() {
		return listaTipoDocumentos;
	}

	/**
	 * @param listaTipoDocumentos the listaTipoDocumentos to set
	 */
	public void setListaTipoDocumentos(List<Referencia> listaTipoDocumentos) {
		this.listaTipoDocumentos = listaTipoDocumentos;
	}

	/**
	 * @return the tipoDocumento
	 */
	public String getTipoDocumento() {
		return tipoDocumento;
	}

	/**
	 * @param tipoDocumento the tipoDocumento to set
	 */
	public void setTipoDocumento(String tipoDocumento) {

		this.tipoDocumento = tipoDocumento;
	}

	/**
	 * @return the numeroIdentificacion
	 */
	public Long getNumeroIdentificacion() {
		return numeroIdentificacion;
	}

	/**
	 * @param numeroIdentificacion the numeroIdentificacion to set
	 */
	public void setNumeroIdentificacion(Long numeroIdentificacion) {

		this.numeroIdentificacion = numeroIdentificacion;
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
	 * @return the userMails
	 */
	public List<Email> getUserMails() {
		return userMails;
	}

	/**
	 * @param userMails the userMails to set
	 */
	public void setUserMails(List<Email> userMails) {
		this.userMails = userMails;
	}

	/**
	 * @return the direcciones
	 */
	public List<Direccion> getDirecciones() {
		return direcciones;
	}

	/**
	 * @param direcciones the direcciones to set
	 */
	public void setDirecciones(List<Direccion> direcciones) {
		this.direcciones = direcciones;
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
	 * @return the codigoRegionDesc
	 */
	public String getCodigoRegionDesc() {
		return codigoRegionDesc;
	}

	/**
	 * @param codigoRegionDesc the codigoRegionDesc to set
	 */
	public void setCodigoRegionDesc(String codigoRegionDesc) {
		this.codigoRegionDesc = codigoRegionDesc;
	}

	/**
	 * @return the codigoCiudadDesc
	 */
	public String getCodigoCiudadDesc() {
		return codigoCiudadDesc;
	}

	/**
	 * @param codigoCiudadDesc the codigoCiudadDesc to set
	 */
	public void setCodigoCiudadDesc(String codigoCiudadDesc) {
		this.codigoCiudadDesc = codigoCiudadDesc;
	}

	/**
	 * @return the tipoActo
	 */
	public String getTipoActo() {
		return tipoActo;
	}

	/**
	 * @param tipoActo the tipoActo to set
	 */
	public void setTipoActo(String tipoActo) {
		this.tipoActo = tipoActo;
	}

	/**
	 * @return the numeActo
	 */
	public String getNumeActo() {
		return numeActo;
	}

	/**
	 * @param numeActo the numeActo to set
	 */
	public void setNumeActo(String numeActo) {
		this.numeActo = numeActo;
	}

	/**
	 * @return the fechaActo
	 */
	public Date getFechaActo() {
		return fechaActo;
	}

	/**
	 * @param fechaActo the fechaActo to set
	 */
	public void setFechaActo(Date fechaActo) {
		this.fechaActo = fechaActo;
	}

	/**
	 * @return the tipoCopia
	 */
	public String getTipoCopia() {
		return tipoCopia;
	}

	/**
	 * @param tipoCopia the tipoCopia to set
	 */
	public void setTipoCopia(String tipoCopia) {
		this.tipoCopia = tipoCopia;
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
	 * @return the expedienteCompleto
	 */
	public boolean isExpedienteCompleto() {
		return expedienteCompleto;
	}

	/**
	 * @param expedienteCompleto the expedienteCompleto to set
	 */
	public void setExpedienteCompleto(boolean expedienteCompleto) {
		this.expedienteCompleto = expedienteCompleto;
	}

	/**
	 * @return the constanciaEjecutoria
	 */
	public boolean isConstanciaEjecutoria() {
		return constanciaEjecutoria;
	}

	/**
	 * @param constanciaEjecutoria the constanciaEjecutoria to set
	 */
	public void setConstanciaEjecutoria(boolean constanciaEjecutoria) {
		this.constanciaEjecutoria = constanciaEjecutoria;
	}

	/**
	 * @return the rangoFolios
	 */
	public String getRangoFolios() {
		return rangoFolios;
	}

	/**
	 * @param rangoFolios the rangoFolios to set
	 */
	public void setRangoFolios(String rangoFolios) {
		this.rangoFolios = rangoFolios;
	}

	/**
	 * @return the numeroRadicacion
	 */
	public String getNumeroRadicacion() {
		return numeroRadicacion;
	}

	/**
	 * @param numeroRadicacion the numeroRadicacion to set
	 */
	public void setNumeroRadicacion(String numeroRadicacion) {
		this.numeroRadicacion = numeroRadicacion;
	}

	/**
	 * @return the personasEjecutoria
	 */
	public List<PersonaActo> getPersonasEjecutoria() {
		return personasEjecutoria;
	}

	/**
	 * @param personasEjecutoria the personasEjecutoria to set
	 */
	public void setPersonasEjecutoria(List<PersonaActo> personasEjecutoria) {
		this.personasEjecutoria = personasEjecutoria;
	}

	/**
	 * @return the selectedPersonas
	 */
	public List<PersonaActo> getSelectedPersonas() {
		return selectedPersonas;
	}

	/**
	 * @param selectedPersonas the selectedPersonas to set
	 */
	public void setSelectedPersonas(List<PersonaActo> selectedPersonas) {
		this.selectedPersonas = selectedPersonas;
	}

	/**
	 * @return the listaAdjuntos
	 */
	public List<Attachment> getListaAdjuntos() {
		return listaAdjuntos;
	}

	/**
	 * @param listaAdjuntos the listaAdjuntos to set
	 */
	public void setListaAdjuntos(List<Attachment> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	/**
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
	 * @return the numMaxSolicitudes
	 */
	public int getNumMaxSolicitudes() {
		return numMaxSolicitudes;
	}

	/**
	 * @return the aceptoTerminos
	 */
	public Boolean getAceptoTerminos() {
		return aceptoTerminos;
	}

	/**
	 * @param aceptoTerminos the aceptoTerminos to set
	 */
	public void setAceptoTerminos(Boolean aceptoTerminos) {
		this.aceptoTerminos = aceptoTerminos;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return the userExist
	 */
	public boolean isUserExist() {
		return userExist;
	}

	/**
	 * @param userExist the userExist to set
	 */
	public void setUserExist(boolean userExist) {
		this.userExist = userExist;
	}

}
