/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.gov.sic.copiasycertificaciones.beans;

import static co.gov.sic.copiasycertificaciones.beans.BeanBase.getSession;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.bouncycastle.util.encoders.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

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
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Asignacion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponseBase;
import sic.ws.interop.entities.response.ResponsePersona;
import sic.ws.interop.entities.response.ResponseResponsable;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author emosquera
 */
@Named("notificationProcessBean")
@SessionScoped
public class NotificationProcessBean extends NotificationManageBeanInit implements Serializable {

	private static final long serialVersionUID = 6033233867984866149L;
	private boolean solicitudAsignada = false;
	private String mensaje;
	private String observaciones;
	private Persona funcionarioAsignado;
	protected final Logger logger = LoggerFactory.getLogger(NotificationProcessBean.class);
	private boolean solicitudNoAsignada = true;
	private boolean mostrarAprobar;
	private List<Attachment> listaAdjunto;
	private Cesl_detalleSolicitud tramiteDetalle;

	public NotificationProcessBean() throws Exception {
		logger.info("NotificationProcessBean");
	}

	public void actualiarInboxTable() throws Exception {
		try (Dal Dal = new Dal()) {
			setListaSolicitudes(Dal.getPendingRequestCoordinador());
		}
	}

	public void actualizarReasignarTable() throws Exception {
		try (Dal Dal = new Dal()) {
			setListaSolicitudesActivas(Dal.getActiveCoordinador());
		}
	}

	public void setTramite(int id) throws Exception {
		for (Cesl_tramite tramite : getListaSolicitudes()) {
			if (tramite.getIdtramite() == id) {
				setTramiteSolicitud(tramite);
				this.mostrarAprobar = tramite.getEstado() == EstadoTramite.RADICADO_FIRMA_ELECTRONICA;
				this.loadListaAdjunto();
				try (Dal Dal = new Dal()) {
					this.tramiteDetalle = Dal.getDetallesTramite(tramite.getIdtramite()).get(0);
				}
				break;
			}
		}
	}

	public void setTramiteActivo(int id) throws Exception {
		for (Cesl_tramite tramite : getListaSolicitudesActivas()) {
			if (tramite.getIdtramite() == id) {
				setTramiteSolicitud(tramite);
				try (Dal Dal = new Dal()) {
					this.tramiteDetalle = Dal.getDetallesTramite(tramite.getIdtramite()).get(0);
				}
				break;
			}
		}
	}

	public void asignarFuncionario() throws IOException, Exception {
		this.asignarFuncionarioGeneral(EstadoTramite.ASIGNADA.getValue());
	}

	public void reAsignarFuncionario() throws IOException, Exception {
		this.asignarFuncionarioGeneral(getTramiteSolicitud().getEstado().getValue());
		try (Dal Dal = new Dal()) {
			Dal.setCeslTramiteEstadoReasignar(getTramiteSolicitud().getIdtramite(), this.observaciones);
		}
	}

	public void asignarFuncionarioGeneral(int estado) throws IOException, Exception {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);

		Usuario usr = getDatosSesion();

		Asignacion solicitudPorAsignar = new Asignacion();

		solicitudPorAsignar.setAnio(getTramiteSolicitud().getAno_radi());
		solicitudPorAsignar.setNumero(getTramiteSolicitud().getNume_radi());
		solicitudPorAsignar.setControl("");
		solicitudPorAsignar.setConsecutivo(0);
		solicitudPorAsignar.setIdFuncionarioAsignado(getIdFuncionario());
		solicitudPorAsignar.setIdFuncionarioQueAsigna(usr.getPersona().getId());
		solicitudPorAsignar.setIdTarea(Constantes.CODI_ACTUACION_ASIGNACION);
		solicitudPorAsignar.setObservaciones(getObservaciones());
		ResponseBase response = wsInteropClient.radicacionAsignar(solicitudPorAsignar);
		if (response.getCodigo() == 0) {
			setSolicitudAsignada(true);
		} else {
			setSolicitudAsignada(false);
		}
		setMensaje(response.getMensaje());

		try (Dal Dal = new Dal()) {
			Dal.setFuncionarioAsignado(getIdFuncionario(), getTramiteSolicitud().getIdtramite(), estado);
		}

		this.AddStatusMessage(response.getMensaje(), "msgAsignacion");
		setHabilitarAsignarFuncioanrio(false);
		enviarEmail(getTramiteSolicitud());

	}

	private void enviarEmail(Cesl_tramite tramite) {
		try {
			TemplateContent templateContent = new TemplateContent();
			String htmlEmail = templateContent.buildEmailTemplate(tramite, funcionarioAsignado);
			List<String> emailEmpresa = new ArrayList<>();

			for (Email email : funcionarioAsignado.getEmails()) {
				logger.info("Processing email address : " + email.getDescripcion());
				if (email.getDescripcion().contains("sic.gov.co")) {
					logger.info("Setting recipient email addresse to : " + email.getDescripcion());
					emailEmpresa.add(email.getDescripcion());
				}
			}
			// TODO: En dev no se envian email, sale error java.net.ConnectException:
			// Connection timed out: connect.
			MailService.Send(emailEmpresa, "Asignacion solicitud de copias y/o certificaciones", htmlEmail, null);
		} catch (MalformedURLException ex) {
			logger.error(ex.toString());
		}
	}

	public void onChangeDependencyFuncionario() throws IOException {
		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		ResponsePersona responsePersona = wsInteropClient.personaConsultar(getIdFuncionario());
		funcionarioAsignado = responsePersona.getPersona();
	}

	public void removerSolicitudAsignada() throws Exception {
		actualiarInboxTable();
		setCodigoDependencia(-1);
		setIdFuncionario(0L);
		setHabilitarAsignarFuncioanrio(false);
		setSolicitudAsignada(false);
		setSolicitudNoAsignada(true);
	}

	public void removerSolicitudReasignada() throws Exception {
		actualizarReasignarTable();
		setCodigoDependencia(-1);
		setIdFuncionario(0L);
		setHabilitarAsignarFuncioanrio(false);
		setSolicitudAsignada(false);
		setSolicitudNoAsignada(true);
	}

	public void loadListaAdjunto() {
		this.listaAdjunto = new ArrayList<>();
		File dir = new File(this.getRuta());
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile() && isPDFFile(file)) {
					Attachment attachment = new Attachment();
					attachment.setFileName(file.getName());
					attachment.setFileExtension("pdf");
					attachment.setFileSize(file.length());
					attachment.setPath(file.getAbsolutePath());
					this.listaAdjunto.add(attachment);
				}
			}
		}
	}

	private boolean isPDFFile(File file) {
		String fileName = file.getName();
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex != -1) {
			String fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
			String fileNameWithoutExtension = fileName.substring(0, dotIndex).toLowerCase();
			String numeRadiStr = String.format("%06d", this.getTramiteSolicitud().getNume_radi());
			String prefixName = this.getTramiteSolicitud().getAno_radi() + "-" + numeRadiStr + "-";
			String prefixName2 = this.getTramiteSolicitud().getAno_radi() + numeRadiStr + "-";

			return "pdf".equals(fileExtension) && !fileNameWithoutExtension.startsWith("c0p14_")
					&& !fileNameWithoutExtension.startsWith(prefixName)
					&& !fileNameWithoutExtension.startsWith(prefixName2);
		}
		return false;
	}

	private String getRuta() {
		return String.format("%s%02d/%02d-%06d/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS,
				getTramiteSolicitud().getAno_radi(), this.getTramiteSolicitud().getAno_radi(),
				this.getTramiteSolicitud().getNume_radi());
	}

	private String addPDFWatermark(String archivoPDFOriginal) throws Exception {
		String archivoPDFMarcaDeAgua = null;
		PdfReader reader = null;
		PdfStamper stamper = null;
		FileOutputStream fos = null;
		Dal Dal = null;

		try {
			archivoPDFMarcaDeAgua = this.getRuta() + "c0p14_" + archivoPDFOriginal;

			File archivoSalida = new File(archivoPDFMarcaDeAgua);
			if (archivoSalida.exists()) {
				archivoSalida.delete();
			}

			reader = new PdfReader(this.getRuta() + archivoPDFOriginal);
			int numberOfPages = reader.getNumberOfPages();
			Document document = new Document(reader.getPageSizeWithRotation(1));

			fos = new FileOutputStream(archivoPDFMarcaDeAgua);
			stamper = new PdfStamper(reader, fos);
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
			PdfContentByte content;
			float fontSize = 6;
			float pageWidth = document.right() - document.left();

			for (int i = 1; i <= numberOfPages; i++) {
				content = stamper.getOverContent(i);
				content.beginText();
				content.setFontAndSize(bf, fontSize);
				content.setColorFill(BaseColor.GRAY);

				float x = 5;
				float y = 50;

				content.setTextMatrix(0, 1, -1, 0, x, y);
				content.showText(
						"EL SUSCRITO SECRETARIO GENERAL HACE CONSTAR QUE LA PRESENTE COPIA COINCIDE CON EL DOCUMENTO ORIGINAL QUE REPOSA EN LOS ARCHIVOS DE LA SUPERINTENDENCIA DE INDUSTRIA Y COMERCIO.");

				Image image = getResourceImage(String.format("FIRMA_SECRETARIO_AD_HOC_%s.PNG",
						Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));
				image.setRotationDegrees(90);
				image.scaleAbsolute(30, 30);

				float imageY = (document.top() + document.bottom() - image.getScaledHeight()) / 2;
				image.setAbsolutePosition(x - 2, imageY);
				content.addImage(image);

				content.setTextMatrix(0, 1, -1, 0, x + 30, imageY - 10);
				content.showText(Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC);
				content.endText();

				if (i == numberOfPages) {
					content.beginText();
					content.setFontAndSize(bf, fontSize);
					content.setColorFill(BaseColor.GRAY);

					String textoAdicional = "Fecha y lugar de expedición: Bogotá D.C., " + obtenerFechaActual();
					float textWidthAdicional = bf.getWidthPoint(textoAdicional, fontSize);
					float xAdicional = (document.right() - document.left() - textWidthAdicional) / 2;
					content.setTextMatrix(1, 0, 0, 1, xAdicional, y - 10);
					content.showText(textoAdicional);

					image = getResourceImage(String.format("FIRMA_SECRETARIO_AD_HOC_%s.PNG",
							Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));
					float imageX = (pageWidth - image.getScaledWidth()) / 2;
					image.setAbsolutePosition(imageX, y - 40);
					content.addImage(image);

					textoAdicional = Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC;
					textWidthAdicional = bf.getWidthPoint(textoAdicional, fontSize);
					xAdicional = (document.right() - document.left() - textWidthAdicional) / 2;
					content.setTextMatrix(1, 0, 0, 1, xAdicional, y - 45);
					content.showText(textoAdicional);

					content.endText();

					String hash = calcularHash(content.toString());
					String numeRadiStr = String.format("%06d", this.getTramiteSolicitud().getNume_radi());
					String nombreArchivo = this.getTramiteSolicitud().getAno_radi() + "-" + numeRadiStr + "_"
							+ archivoPDFOriginal;

					String xmpMetadata = "<x:xmpmeta xmlns:x='adobe:ns:meta/'>\n"
							+ "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n"
							+ "<rdf:Description rdf:about='' xmlns:pdf='http://ns.adobe.com/pdf/1.3/'>\n"
							+ "<pdf:CustomMetadata>\n"
							+ "<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n"
							+ "<rdf:Description rdf:about='' xmlns:dc='http://purl.org/dc/elements/1.1/'>\n"
							+ "<dc:description>\n" + "<rdf:Alt>\n" + "<rdf:li xml:lang='x-default'>"
							+ this.getTramiteSolicitud().getIdtramite() + "|" + hash + "|" + nombreArchivo
							+ "</rdf:li>\n" + "</rdf:Alt>\n" + "</dc:description>\n" + "</rdf:Description>\n"
							+ "</rdf:RDF>\n" + "</pdf:CustomMetadata>\n" + "</rdf:Description>\n" + "</rdf:RDF>\n"
							+ "</x:xmpmeta>";
					byte[] xmpBytes = xmpMetadata.getBytes("UTF-8");
					stamper.getWriter().setXmpMetadata(xmpBytes);

					Dal = new Dal();
					Dal.setCeslTramiteHashPDF(getTramiteSolicitud().getIdtramite(), hash);
				}
			}

			document.close();
			return archivoPDFMarcaDeAgua;

		} catch (IOException | DocumentException e) {
			logger.error("addPDFWatermark", e);
			throw e;
		} finally {
			// ✅ Cerrar recursos en orden inverso
			if (stamper != null) {
				try {
					stamper.close();
				} catch (Exception e) {
					logger.error("Error cerrando PdfStamper", e);
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					logger.error("Error cerrando PdfReader", e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("Error cerrando FileOutputStream", e);
				}
			}
			if (Dal != null) {
				try {
					Dal.close();
				} catch (Exception e) {
					logger.error("Error cerrando Dal", e);
				}
			}
		}
	}

	public Image getResourceImage(String imageName) throws Exception {
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
				.getContext();
		String path = servletContext.getRealPath(String.format("WEB-INF/templates/img/%s", imageName));
		Image img = Image.getInstance(path);
		img.scaleAbsolute(50, 50);
		return img;
	}

	private String calcularHash(String contenido) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(contenido.getBytes());
			return new String(Hex.encode(hashBytes));
		} catch (Exception e) {
			logger.error("calcularHash", e);
			return null;
		}
	}

	private String obtenerFechaActual() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(new Date());
	}

	public void aprobarAutenticacion() throws IOException, Exception {
		for (Attachment attr : this.listaAdjunto) {
			attr.setCopyPath(this.addPDFWatermark(attr.getFileName()));
			attr.setCopyFileName("c0p14_" + attr.getFileName());
		}

		List<Attachment> combinedList = new ArrayList<>();
		for (Attachment attachment : listaAdjunto) {
			boolean isPresentInSelection = listaAdjunto.stream()
					.anyMatch(selected -> selected.getFileName().equals(attachment.getFileName()));

			Attachment combinedAttachment = new Attachment();
			combinedAttachment.setFileName(attachment.getFileName());
			combinedAttachment.setPath(isPresentInSelection ? attachment.getCopyPath() : attachment.getPath());
			combinedAttachment.setFileExtension(attachment.getFileExtension());
			combinedAttachment.setFileSize(attachment.getFileSize());
			combinedList.add(combinedAttachment);
		}

		String medioEntrada = "SL";
		if (tramiteDetalle.getOpcionesEntrega().intValue() != MedioEntrada.DIGITAL.value()) {
			medioEntrada = "CD";
		}

		InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS,
				Constantes.URL_WS_INTEROP, 500, true);
		try {
			Cesl_tramite tramite = getTramiteSolicitud();
			List<String> adjuntos = new ArrayList<>();
			Radicacion radiSalida = new Radicacion();
			radiSalida.setAnio(tramite.getAno_radi());
			radiSalida.setNumero(tramite.getNume_radi());
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

			radiSalida.setRadicador(radicador);
			radiSalida.setTotalFolios(1);

			Persona funcionario = new Persona();
			funcionario.setId(tramite.getFunc_asignado());
			funcionario.setRetornarSoloUltimosDatos(true);
			funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

			boolean observacionesObligatorias = false;
			if (this.tramiteDetalle.getObservacionesRadicado() == null
					|| this.tramiteDetalle.getObservacionesRadicado().isEmpty()) {
				observacionesObligatorias = true;
			}

			ResponseResponsable responseResponsableNotificaciones = wsInteropClient.dependenciaResponsable(104);
			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
			Dal Dal = new Dal();
			if (responseRadicacion.getCodigo() == 0) {
				if (Dal.setCeslTramiteEstado(tramite.getIdtramite(), EstadoTramite.FINALIZADO.getValue())) {
					logger.info("se cambio el estado en sistemas de copias");
					this.AddStatusMessage("Solicitud enviada correctamente", "mgComplementar");
					radiSalida = responseRadicacion.getRadicacion();

					// Generar comunicado de salida
					TemplateContent templateContent = new TemplateContent();
					radiSalida.setRadicador(radicador);
					String contentPDF = templateContent.buildRespuestaSolicitudCopias(radiSalida, funcionario,
							responseResponsableNotificaciones.getResponsable(), observacionesObligatorias,
							this.tramiteDetalle.getObservacionesRadicado());
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
					if (!combinedList.isEmpty()) {
						for (Attachment att : combinedList) {
							String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()),
									att.getFileName());
							radiSalida.addAdjunto(path, false);
							adjuntos.add(path);
							deleteAttachmentFile(att.getPath());
							if (att.getCopyFileName() != null && !att.getCopyFileName().isEmpty()) {
								deleteAttachmentFile(att.getCopyPath());
							}
						}
					}

					ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
							.radicacionAdjuntosRegistrar(radiSalida);

					if (responseRadicacionAdjuntos.getCodigo() == 0) {
						String htmlEmail = null;

						List<Email> emailSolicitanteEmail = new ArrayList<>();
						List<String> emailSolicitante = new ArrayList<>();

						emailSolicitanteEmail = wsInteropClient.personaConsultar(tramite.getIden_pers()).getPersona()
								.getEmails();

						for (Email email : emailSolicitanteEmail) {
							logger.info("Procesando email : " + email.getDescripcion());
							emailSolicitante.add(email.getDescripcion());
						}

						adjuntos.add(fullPathRadicadoEntrada);

						htmlEmail = templateContent.buildEmailTemplateRespuestaSolicitud(tramite,
								emailSolicitante.get(0));
						MailService.Send(emailSolicitante,
								"Respuesta Solicitud radicado #" + tramite.getAno_radi() + "-" + tramite.getNume_radi(),
								htmlEmail, adjuntos);
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

		} catch (IOException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

	}

	public void rechazarAutenticacion() throws IOException, Exception {
		Dal Dal = new Dal();
		Dal.setCeslTramiteEstado(getTramiteSolicitud().getIdtramite(), EstadoTramite.ASIGNADA.getValue());
		this.AddStatusMessage("Solicitud enviada correctamente", "mgComplementar");
	}

	private void deleteAttachmentFile(String fileName) {
		try {
			Path path = Paths.get(fileName);
			Files.deleteIfExists(path);
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}

	private ByteArrayOutputStream getFileContent(String fullFileName) throws FileNotFoundException, IOException {
	    byte[] buffer = new byte[4096];
	    
	    // ✅ try-with-resources cierra automáticamente bis y baos
	    try (FileInputStream fis = new FileInputStream(fullFileName);
	         BufferedInputStream bis = new BufferedInputStream(fis);
	         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
	        
	        int bytes;
	        while ((bytes = bis.read(buffer, 0, buffer.length)) > 0) {
	            baos.write(buffer, 0, bytes);
	        }
	        
	        // Retornar el contenido antes de que se cierre baos
	        return baos;
	    }
	}

	/**
	 * @return the solicitudAsignada
	 */
	public boolean isSolicitudAsignada() {
		return solicitudAsignada;
	}

	/**
	 * @param solicitudAsignada the solicitudAsignada to set
	 */
	public void setSolicitudAsignada(boolean solicitudAsignada) {
		this.solicitudAsignada = solicitudAsignada;
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

	public void resetValues() {
		setCodigoDependencia(-1);
		setIdFuncionario(-1L);
		setHabilitarAsignarFuncioanrio(false);
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

	public boolean filterByFechaRadicacion(Object value, Object filter, Locale locale) {
		if (filter == null) {
			return true;
		}

		if (value == null) {
			return false;
		}

		return DateUtils.truncatedEquals((Date) filter, (Date) value, Calendar.DATE);
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
	 * @return the solicitudNoAsignada
	 */
	public boolean isSolicitudNoAsignada() {
		return solicitudNoAsignada;
	}

	/**
	 * @param solicitudNoAsignada the solicitudNoAsignada to set
	 */
	public void setSolicitudNoAsignada(boolean solicitudNoAsignada) {
		this.solicitudNoAsignada = solicitudNoAsignada;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isMostrarAprobar() {
		return mostrarAprobar;
	}

	/**
	 * 
	 * @param mostrarAprobar
	 */
	public void setMostrarAprobar(boolean mostrarAprobar) {
		this.mostrarAprobar = mostrarAprobar;
	}

	/**
	 * 
	 * @return
	 */
	public List<Attachment> getListaAdjunto() {
		return listaAdjunto;
	}

	/**
	 * 
	 * @param listaAdjunto
	 */
	public void setListaAdjunto(List<Attachment> listaAdjunto) {
		this.listaAdjunto = listaAdjunto;
	}

}
