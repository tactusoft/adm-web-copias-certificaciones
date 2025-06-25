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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Attachment;
import co.gov.sic.copiasycertificaciones.entities.Cesl_config;
import co.gov.sic.copiasycertificaciones.entities.Cesl_cotizacion;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.Frntstco;
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
import jakarta.servlet.http.HttpSession;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.ResponsableDepe;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponseResponsable;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

/**
 *
 * @author emosquera
 */
@Named("cotizacionBean")
@SessionScoped
public class CotizacionBean implements Serializable {

	private static final long serialVersionUID = 3885739537665918463L;
	protected final Logger logger = LoggerFactory.getLogger(CotizacionBean.class);
	private Cesl_config frntstco;
	private List<Frntstco> listaRentistico;
	private Short fcncpto = -1;
	private Cesl_tramite tramite;
	private boolean habilitaBtnAdd = false;
	private int fvalor;
	private int cantidad;
	private int totalConcepto;
	private List<Cesl_cotizacion> listaCotizacion;
	private int totalCotizacion = 0;

	private List<Attachment> listaAdjuntos;
	private double totalAttachmentSize;
	private String observaciones;

	public CotizacionBean() {

		Dal dal = new Dal();
		frntstco = dal.getDayConfigParameters("frntstco");
		listaRentistico = dal.getCodigosRentisticos(frntstco.getValorString());
		listaCotizacion = new ArrayList<>();
		listaAdjuntos = new ArrayList<>();
	}

	public void hacerCotizacion() {
		if (totalCotizacion > 0 && !listaCotizacion.isEmpty()) {
			InteropWSClient wsInteropClient = new InteropWSClient(Constantes.WS_INTEROP_USER,
					Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP, 500, true);
			try {
				Dal dal = new Dal();
				tramite.setValor_total((double) totalCotizacion);
				if (dal.guardarCotizacion(tramite, listaCotizacion)) {
					Radicacion radiSalida = new Radicacion();
					radiSalida.setAnio(tramite.getAno_radi());
					radiSalida.setNumero(tramite.getNume_radi());
					radiSalida.setControl(null);
					Perfil perfilRadicacion = new Perfil();
					perfilRadicacion.setActuacion((short) 344);
					perfilRadicacion.setDependencia((short) 104);
					perfilRadicacion.setEvento((short) 0);
					perfilRadicacion.setTramite((short) 362);
					radiSalida.setPerfil(perfilRadicacion);
					radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
					radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
					radiSalida.setTipoRadicacion("SA");
					radiSalida.setDependenciaDestino((short) 104);

					radiSalida.setFechaRadicacion(tramite.getFecha_creacion());
					radiSalida.setIdFuncionario(tramite.getFunc_asignado());
					Persona radicador = new Persona();
					radicador.setId(tramite.getIden_pers());
					radicador.setRetornarSoloUltimosDatos(true);
					radicador = wsInteropClient.personaConsultar(radicador).getPersona();
					radiSalida.setRadicador(radicador);

					Persona funcionario = new Persona();
					funcionario.setId(tramite.getFunc_asignado());
					funcionario.setRetornarSoloUltimosDatos(true);
					funcionario = wsInteropClient.personaConsultar(funcionario).getPersona();

					radiSalida.setTotalFolios(1);
					ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
					if (responseRadicacion.getCodigo() == 0) {
						if (dal.setCeslTramiteEstado(tramite.getIdtramite(),
								EstadoTramite.COTIZACION_ENVIADA.getValue())) {
							logger.info("se cambio el estado en sistemas de copias");
							radiSalida = responseRadicacion.getRadicacion();
							TemplateContent templateContent = new TemplateContent();

							Persona personaResponsable = new Persona();
							ResponseResponsable responseResponsable = wsInteropClient
									.dependenciaResponsable(radiSalida.getDependenciaDestino().intValue());
							ResponsableDepe responsableDepe = responseResponsable.getResponsable();

							String contentPDF = templateContent.buildCotizacionPDFTemplate(radiSalida,
									tramite.getIdtiposolicitud(), listaCotizacion, responsableDepe, funcionario,
									observaciones);
							String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());
							String fullPathRadicadoEntrada = null;

							ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
									tramite.getIdtiposolicitud().getDescripcion(), subject,
									String.format(Constantes.KEYWORDS_PDF_RADICACION,
											radiSalida.getFechaRadicacion().getYear()),
									false, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
											tramite.getIdtiposolicitud()));

							fullPathRadicadoEntrada = Functions.saveFile(radiSalida, fileContent);
							logger.info("PDF Radicacion Entrada Copias: " + fullPathRadicadoEntrada);

							radiSalida.addAdjunto(fullPathRadicadoEntrada, false);

							for (Attachment att : listaAdjuntos) {
								String path = Functions.saveFile(radiSalida, getFileContent(att.getPath()),
										att.getFileName());
								radiSalida.addAdjunto(path, false);
								deleteAttachmentFile(att.getFileName());
							}

							ResponseRadicacion responseRadicacionAdjuntos = wsInteropClient
									.radicacionAdjuntosRegistrar(radiSalida);
							if (responseRadicacionAdjuntos.getCodigo() == 0) {
								List<String> adjuntos = new ArrayList<>();

								adjuntos.add(fullPathRadicadoEntrada);
								List<String> listaEmails = this.getListaEmails(radicador.getEmails());

								String htmlEmail = templateContent.buildEmailTemplateCotizacion(tramite,
										listaEmails.get(0));

								MailService.Send(listaEmails,
										"Cotizacion - " + tramite.getAno_radi() + "-" + tramite.getNume_radi(),
										htmlEmail, adjuntos);
							}

							this.AddInfoMessage("Cotizacion enviada correctamente", "msgCotizacion");

						} else {
							logger.info("NOOOOOOOOO se cambio el estado en sistemas de copias");
							this.AddWarningMessage(
									"La actuacion fue cambiada correctamente, sin embargo no se puedo cambiar el estado en el sistema de copias ",
									"msgCotizacion");
						}

					}

				}
			} catch (Exception ex) {
				logger.error(ex.toString());
			}
		} else {
			this.AddErrorMessage("La cotizacion debe tener un valor mayor que 0", "msgCotizacion");
		}
	}

	private List<String> getListaEmails(List<Email> emails) {
		List<String> listaEmails = new ArrayList<>();
		try {
			Dal dal = new Dal();
			Cesl_detalleSolicitud tramiteDetalle = dal.getDetallesTramite(tramite.getIdtramite()).get(0);
			Direccion direccionEmail = dal.getDireccion(tramite.getIden_pers(), tramiteDetalle.getConsEmail());
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

	public void eliminarDetalleCotizacion(Cesl_cotizacion cotizacion) {
		totalCotizacion = totalCotizacion - cotizacion.getTotalConcepto();
		listaCotizacion.remove(cotizacion);
	}

	public void agregarACotizacion() {

		if (cantidad == 0 || totalConcepto == 0) {
			this.AddErrorMessage("La cotizacion debe tener un valor mayor que 0", "msgCotizacion");
			return;
		}

		Cesl_cotizacion c = new Cesl_cotizacion();
		for (Frntstco f : listaRentistico) {
			// System.out.println(" first : " + f.getFcncpto() + " - second : " + fcncpto +
			// " - resolt : " + Short.compare(f.getFcncpto(),fcncpto));

			if (Short.compare(f.getFcncpto(), fcncpto) == 0) {
				c.setFrntstco(f);
			}
		}
		c.setIdTramite(tramite.getIdtramite());
		c.setFvalor(fvalor);
		c.setCantidad(cantidad);
		c.setTotalConcepto(totalConcepto);

		listaCotizacion.add(c);
		totalCotizacion = totalCotizacion + totalConcepto;
		limpiarFormulario();
	}

	public void onChangeConcepto() {
		if (fcncpto == -1) {
			habilitaBtnAdd = false;
			return;
		}
		Dal dal = new Dal();
		fvalor = dal.getValorConcepto(frntstco.getValorString(), fcncpto);
		cantidad = 0;
		totalConcepto = 0;

	}

	public void onChangeCantidad() {
		totalConcepto = cantidad * fvalor;
	}

	public void volver() {
		fcncpto = -1;
		habilitaBtnAdd = false;
		fvalor = 0;
		cantidad = 0;
		totalConcepto = 0;
		observaciones = "";
		if (listaAdjuntos.size() > 0) {
			for (Attachment att : listaAdjuntos) {
				deleteAttachmentFile(att.getFileName());
			}
			listaAdjuntos = new ArrayList<>();
		}
	}

	public void limpiarFormulario() {
		fcncpto = -1;
		habilitaBtnAdd = false;
		fvalor = 0;
		cantidad = 0;
		totalConcepto = 0;
	}

	/**
	 * @return the listaRentistico
	 */
	public List<Frntstco> getListaRentistico() {
		return listaRentistico;
	}

	/**
	 * @param listaRentistico the listaRentistico to set
	 */
	public void setListaRentistico(List<Frntstco> listaRentistico) {
		this.listaRentistico = listaRentistico;
	}

	/**
	 * @return the fcncpto
	 */
	public Short getFcncpto() {
		return fcncpto;
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
	}

	/**
	 * @return the habilitaBtnAdd
	 */
	public boolean isHabilitaBtnAdd() {
		return habilitaBtnAdd;
	}

	/**
	 * @param habilitaBtnAdd the habilitaBtnAdd to set
	 */
	public void setHabilitaBtnAdd(boolean habilitaBtnAdd) {
		this.habilitaBtnAdd = habilitaBtnAdd;
	}

	/**
	 * @return the fvalor
	 */
	public int getFvalor() {
		return fvalor;
	}

	/**
	 * @param fvalor the fvalor to set
	 */
	public void setFvalor(int fvalor) {
		this.fvalor = fvalor;
	}

	/**
	 * @param fcncpto the fcncpto to set
	 */
	public void setFcncpto(Short fcncpto) {
		this.fcncpto = fcncpto;
	}

	/**
	 * @return the cantidad
	 */
	public int getCantidad() {
		return cantidad;
	}

	/**
	 * @param cantidad the cantidad to set
	 */
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	/**
	 * @return the totalConcepto
	 */
	public int getTotalConcepto() {
		return totalConcepto;
	}

	/**
	 * @param totalConcepto the totalConcepto to set
	 */
	public void setTotalConcepto(int totalConcepto) {
		this.totalConcepto = totalConcepto;
	}

	/**
	 * @return the listaCotizacion
	 */
	public List<Cesl_cotizacion> getListaCotizacion() {
		return listaCotizacion;
	}

	/**
	 * @param listaCotizacion the listaCotizacion to set
	 */
	public void setListaCotizacion(List<Cesl_cotizacion> listaCotizacion) {
		this.listaCotizacion = listaCotizacion;
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

	public void handleFileUpload(FileUploadEvent event) {
		Usuario usr = getDatosSesion();
		String directory = String.format("%s/%s/", Constantes.PATH_ARCHIVOS_OTROS_FORMULARIOS, usr.getLogin());

		if (getTotalAttachmentSize() > Constantes.MAX_SIZE_ATTACHMENTS) {
			this.AddErrorMessage("Error: " + event.getFile().getFileName()
					+ " no se peude adjuntar, se ha sueperado el tamaño maximo", "msgCotizacion");
			return;
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

			setTotalAttachmentSize(getTotalAttachmentSize() + att.getFileSize());
			setTotalAttachmentSize(att.roundFileSize(getTotalAttachmentSize(), 3));
			getListaAdjuntos().add(att);

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public void EliminarAdjunto(Attachment attach) {

		if (getListaAdjuntos().contains(attach)) {
			deleteAttachmentFile(attach.getFileName());
			setTotalAttachmentSize(getTotalAttachmentSize() - attach.getFileSize());
			getListaAdjuntos().remove(attach);
		}
	}

	/**
	 * @return the totalCotizacion
	 */
	public int getTotalCotizacion() {
		return totalCotizacion;
	}

	/**
	 * @param totalCotizacion the totalCotizacion to set
	 */
	public void setTotalCotizacion(int totalCotizacion) {
		this.totalCotizacion = totalCotizacion;
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

}
