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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

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
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.enums.TipoReferenciaEnum;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

@Named("solicitudCopiasBean")
@ViewScoped
public class SolicitudCopiasBean extends BeanBase implements Serializable {

	private static final long serialVersionUID = -7733846022527641199L;
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
	private Integer opcionesEntrega;

	public int getNumMaxSolicitudes() {
		return numMaxSolicitudes;
	}

	public List<PersonaActo> getPersonasEjecutoria() {
		return personasEjecutoria;
	}

	public void setPersonasEjecutoria(List<PersonaActo> val) {
		personasEjecutoria = val;
	}

	public List<PersonaActo> getSelectedPersonas() {
		return selectedPersonas;
	}

	public void setSelectedPersonas(List<PersonaActo> val) {
		selectedPersonas = val;
	}

	public boolean getConstanciaEjecutoria() {
		return constanciaEjecutoria;
	}

	public void setConstanciaEjecutoria(boolean val) {
		constanciaEjecutoria = val;
	}

	public SolicitudCopiasBean() throws Exception {
		super(TipoTramite.COPIAS_SIMPLES);
		personasEjecutoria = new ArrayList<>();
		selectedPersonas = new ArrayList<>();
		listaAdjuntos = new ArrayList<>();
	}

	private void deleteAttachmentFile(String fileName) {
		Usuario usr = getDatosSesion();
		String directory = String.format("%s/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, usr.getLogin());
		Path path = FileSystems.getDefault().getPath(directory, fileName);
		try {
			Files.deleteIfExists(path);

		} catch (IOException e) {
			System.out.println(e.toString());
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

		if (totalAttachmentSize > Constantes.MAX_SIZE_ATTACHMENTS) {
			FacesMessage msg = new FacesMessage("Error",
					event.getFile().getFileName() + " no se puede adjuntar, se ha superado el tamaño maximo permitido");
			FacesContext.getCurrentInstance().addMessage("msAttachments", msg);
		}

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

			totalAttachmentSize += att.getFileSize();
			totalAttachmentSize = att.roundFileSize(totalAttachmentSize, 3);
			listaAdjuntos.add(att);

		} catch (IOException e) {
			System.out.println(e.getMessage());
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
			AddErrorMessage("La descripción de la Solicitud es requerida.");
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
			detalle.setOpcionesEntrega(this.opcionesEntrega);
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
		this.rangoFolios = null;
		this.tipoActo = null;
		this.numeActo = null;
		this.fechaActo = null;
		this.tipoCopia = null;
		this.observaciones = null;
		this.selectedPersonas = new ArrayList<>();
		this.personasEjecutoria = new ArrayList<>();
		this.constanciaEjecutoria = false;
		this.opcionesEntrega = null;
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
			Iterator<Referencia> itr = listaTiposActo.iterator();
			while (itr.hasNext()) {
				Referencia tipoActo = itr.next();
				if (tipoActo.getCodigo().equalsIgnoreCase("SE") || tipoActo.getCodigo().equalsIgnoreCase("ST")
						|| tipoActo.getCodigo().equalsIgnoreCase("SP")) {
					itr.remove();
				}
			}
		}
		return this.listaTiposActo;
	}

	@Override
	public boolean guardarSolicitud() throws Exception {
		boolean result = super.guardarSolicitud();
		setAttachmentError(false);
		if (result) {
			Radicacion radi = new Radicacion();
			Persona cuurentUser = getDatosSesion().getPersona();
			radi.setRadicador(cuurentUser);
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
					// Se debe volver a asignar por si se consulta esta propiedad mas adelante
					// el webservice no devuelve la informacion completa de la persona luego de una
					// radicacion
					radi.setRadicador(cuurentUser);
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
					MailService.Send(cuurentUser.getEmails().get(0).getDescripcion(),
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
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fullFileName));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int bytes = 0;
		while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
			baos.write(buffer, 0, bytes);
		}
		baos.close();
		bis.close();

		return baos;
	}

	public String getTipoActo() {
		return this.tipoActo;
	}

	public void setTipoActo(String val) {
		this.tipoActo = val;
	}

	public String getNumeActo() {
		String numDoc = this.numeActo;
		if (numDoc != null) {
			numDoc = numDoc.replace(".", "");
		}
		return numDoc;
	}

	public void setNumeActo(String val) {
		this.numeActo = val;
	}

	public String getTipoCopia() {
		return this.tipoCopia;
	}

	public void setTipoCopia(String val) {
		this.tipoCopia = val;
	}

	public Date getFechaActo() {
		return this.fechaActo;
	}

	public void setFechaActo(Date val) {
		this.fechaActo = val;
	}

	public String getObservaciones() {
		return this.observaciones;
	}

	public void setObservaciones(String val) {
		this.observaciones = val;
	}

	public String getRangoFolios() {
		return this.rangoFolios;
	}

	public void setRangoFolios(String val) {
		this.rangoFolios = val;
	}

	public boolean getExpedienteCompleto() {
		return this.expedienteCompleto;
	}

	public void setExpedienteCompleto(boolean val) {
		this.expedienteCompleto = val;
	}

	public String getNumeroRadicacion() {
		return this.numeroRadicacion;
	}

	public void getNumeroRadicacion(String val) {
		this.numeroRadicacion = val;
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
	 * 
	 * @return
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
