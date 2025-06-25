package co.gov.sic.copiasycertificaciones.beans;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FlowEvent;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.Sancion;
import co.gov.sic.copiasycertificaciones.entities.ws.request.RequestSignPDF;
import co.gov.sic.copiasycertificaciones.entities.ws.response.ResponseSignPDF;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoSancion;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Functions;
import co.gov.sic.copiasycertificaciones.util.PDFGeneratorService;
import co.gov.sic.copiasycertificaciones.util.TemplateContent;
import co.gov.sic.copiasycertificaciones.util.Utility;
import co.gov.sic.copiasycertificaciones.ws.client.soap.WSSignClient;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.radicacion.ResponseRadicacion;

public abstract class BeanBase {

	protected final TipoTramite tipoTramite;
	protected boolean isSuccess;
	protected Double valorUnitario;
	protected double valorTotalPagar;
	protected Integer idCurrentInvoice;
	protected Cesl_tramite tramiteSeleccionado;
	protected Boolean aceptoSolicitud;
	protected boolean containsInputFile;
	protected Perfil perfilTramite;
	private Boolean aceptoTerminos;
	private long consDire;
	private long consEmail;
	private boolean showPanelAgregar;
	protected final List<Cesl_detalleSolicitud> listaDetalles;
	protected final Logger logger = LoggerFactory.getLogger(BeanBase.class);
	private boolean attachmentError = false;
	private String attachmentErrorMsg;
	private String justificacion;

	protected boolean conApostillaje;
	protected boolean sinApostillaje;

	private String filePath;

	public boolean getShowPanelAgregar() {
		return showPanelAgregar;
	}

	public void setShowPanelAgregar(boolean val) {
		showPanelAgregar = val;
	}

	public Integer getIdCurrentInvoice() {
		return this.idCurrentInvoice;
	}

	public Cesl_tramite getTramiteSeleccionado() {
		return this.tramiteSeleccionado;
	}

	public static HttpSession getSession() {
		return (HttpSession) getExternalContext().getSession(false);
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) getExternalContext().getRequest();
	}

	public static ExternalContext getExternalContext() {
		return FacesContext.getCurrentInstance().getExternalContext();
	}

	public boolean getIsSuccess() {
		return this.isSuccess;
	}

	protected void setIsSuccess(boolean val) {
		this.isSuccess = val;
	}

	public Double getValorUnitario() {
		return this.valorUnitario;
	}

	protected void setValorUnitario(double val) {
		this.valorUnitario = val;
	}

	public double getValorTotalPagar() {
		return this.valorTotalPagar;
	}

	protected void setValorTotalPagar(double val) {
		this.valorTotalPagar = val;
	}

	public TipoTramite getTipoTramite() {
		return this.tipoTramite;
	}

	public int getCODIGO_PSE() {
		return Constantes.CODIGO_PSE;
	}

	public String getURL_SONDA() {
		return Constantes.URL_SONDA;
	}

	public String getEMAIL_SOPORTE() {
		return Constantes.EMAIL_SOPORTE;
	}

	public List<Cesl_detalleSolicitud> getListaDetalles() {
		return listaDetalles;
	}

	public Boolean getAceptoSolicitud() {
		return aceptoSolicitud;
	}

	public void setAceptoSolicitud(Boolean val) {
		this.aceptoSolicitud = val;
	}

	public Boolean getAceptoTerminos() {
		return aceptoTerminos;
	}

	public void setAceptoTerminos(Boolean val) {
		this.aceptoTerminos = val;
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getJustificacion() {
		return justificacion;
	}

	public void setJustificacion(String justificacion) {
		this.justificacion = justificacion;
	}

	public BeanBase(TipoTramite tipoTram) throws Exception {
		this.isSuccess = false;
		this.showPanelAgregar = true;
		this.tipoTramite = tipoTram;
		this.listaDetalles = new ArrayList<>();
		this.aceptoTerminos = false;
		this.consDire = -1;
		this.aceptoSolicitud = false;
		if (tipoTram != null) {
			try (Dal Dal = new Dal()) {
				perfilTramite = Dal.getPerfilCertificado(tipoTram);
				valorUnitario = Dal.getValorTramite(perfilTramite);
			}
		}
	}

	public BeanBase() throws Exception {
		this(null);
	}

	public void bindCheckAceptoTerminos() {
		Map<String, String> map = getExternalContext().getRequestParameterMap();
		if (map.containsKey("frmWizard:aceptoTerminos")) {
			this.aceptoTerminos = map.get("frmWizard:aceptoTerminos").equals("on");
		} else {
			this.aceptoTerminos = false;
		}
	}

	public void bindCheckDireccion() {
		Map<String, String> map = getExternalContext().getRequestParameterMap();
		if (map.containsKey("frmWizard:txtDirecciones_input")) {
			this.consDire = Long.parseLong(map.get("frmWizard:txtDirecciones_input"));
		} else {
			this.consDire = -1;
		}
	}

	public void bindCheckEmail() {
		Map<String, String> map = getExternalContext().getRequestParameterMap();
		if (map.containsKey("frmWizard:txtEmails_input")) {
			this.consEmail = Long.parseLong(map.get("frmWizard:txtEmails_input"));
		} else {
			this.consEmail = -1;
		}
	}

	public static String executePost(String targetURL, String urlParameters) {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "es-CO");
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if
			// Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static int stream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[Constantes.DEFAULT_SENDFILE_BUFFER_SIZE];
		int numRead = 0;
		while ((numRead = input.read(buffer)) >= 0) {
			output.write(buffer, 0, numRead);
		}
		output.close();
		return numRead;
	}

	public static String getMimeType(String name) {
		String mimeType = getExternalContext().getMimeType(name);
		if (mimeType == null) {
			mimeType = Constantes.DEFAULT_MIME_TYPE;
		}
		return mimeType;
	}

	public static String encodeURL(String string) throws UnsupportedEncodingException {
		if (string == null) {
			return null;
		}
		return URLEncoder.encode(string, UTF_8.name());
	}

	public Usuario getDatosSesion() {
		HttpSession session = getSession();
		if (session != null) {
			Object obj = session.getAttribute(Constantes.LLAVE_SESION_USUARIO);
			if (obj != null) {
				return (Usuario) obj;
			}
		}
		return null;
	}

	public Usuario getDatosSesionDireccion() {
		HttpSession session = getSession();
		if (session != null) {
			Object obj = session.getAttribute("DIRECCIONES");
			if (obj != null) {
				return (Usuario) obj;
			}
		}
		return null;
	}

	public void setDatosSesion(Usuario val, List<Direccion> direcciones, List<Direccion> correos) {
		HttpSession session = getSession();
		if (session != null) {
			session.setAttribute(Constantes.LLAVE_SESION_USUARIO, val);
			session.setAttribute("DIRECCIONES", direcciones);
			session.setAttribute("CORREOS", correos);
		}
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

	public void nextInvoiceId(Cesl_tramite tramite) throws Exception {
		this.tramiteSeleccionado = tramite;
		try (Dal Dal = new Dal()) {
			this.idCurrentInvoice = Dal.nextIdInvoice(tramite);
		}
	}

	public void reintentarPago(Cesl_tramite tramite) throws Exception {
		this.tramiteSeleccionado = tramite;
		/*
		 * try (dal Dal = new dal()) { this.idCurrentInvoice =
		 * Dal.nextIdInvoice(tramite); }
		 */
	}

	public void download(String filePath, boolean attachment) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(filePath));
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length)) {
			baos.write(bytes, 0, bytes.length);
			download(baos, new File(filePath).getName(), attachment);
		}
	}

	public void downloadFile() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(this.filePath));
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length)) {
			baos.write(bytes, 0, bytes.length);
			download(baos, new File(this.filePath).getName(), true);
		}
	}

	public void download(ByteArrayOutputStream fileContent, String fileName, boolean attachment) throws IOException {
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

	public boolean guardarSolicitud() throws Exception {
		if (!getAceptoSolicitud()) {
			AddErrorMessage("Debe confirmar que todos los datos ingresados son correctos.");
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
				// Insertar en la tabla cesl_tramite y retornamos el último id generado
				tramiteSeleccionado = Dal.insertarTramite(EstadoTramite.PRESENTADO, totalTramite, getTipoTramite(),
						datosSesion.getPersona().getId());
				// RECORREMOS LAS SOLICITUDES Y REGISTRAR EN LA BASE DE DATOS
				for (Cesl_detalleSolicitud detalle : listaDetalles) {
					detalle.setIdtramite(tramiteSeleccionado.getIdtramite());
					detalle.setConsDire(this.consDire);
					detalle.setConsEmail(this.consEmail);
					Dal.insertarDetalleTramite(detalle);
				}
			}
			return true;
		}
		return false;
	}

	public String procesoFlujoCopias(FlowEvent event) {
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
			} else if (anteriorStep.equals("personal")) {
				this.bindCheckDireccion();
				this.bindCheckEmail();
				if (this.consDire < 0) {
					AddErrorMessage("Para poder continuar, debe seleccionar una dirección");
					return anteriorStep;
				}
				if (this.consEmail < 0) {
					AddErrorMessage("Para poder continuar, debe seleccionar un correo electrónico");
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
					setIsSuccess(guardarSolicitud());
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

	public String procesoFlujo(FlowEvent event) {
		final String anteriorStep = event.getOldStep();
		final String nuevoStep = event.getNewStep();
		Boolean moveWizardSteps = null;
		try {
			PrimeFaces.current().ajax().update("frmWizard:pnlButtons");
			if (anteriorStep.equals("legal")) {
				bindCheckAceptoTerminos();
				if (aceptoTerminos == null || aceptoTerminos == false) {
					AddErrorMessage("Para poder continuar, debe aceptar los términos y condiciones expuestos.");
					return anteriorStep;
				}
			} else if (anteriorStep.equals("personal") && nuevoStep.equals("legal")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("solicitudes") && nuevoStep.equals("apostilla")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("apostilla") && nuevoStep.equals("personal")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("confirmacion")) {
				if (nuevoStep.equals("apostilla")) {
					moveWizardSteps = false;
					return nuevoStep;
				} else if (nuevoStep.equals("pago") || nuevoStep.equals("resultado")) {
					setIsSuccess(guardarSolicitud());
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
				}
			} else if (nuevoStep.equals("solicitudes") && anteriorStep.equals("apostilla")) {
				if (!conApostillaje && !sinApostillaje) {
					AddErrorMessage(
							"Para poder continuar debe especificar  si desea enviar el certificado al Ministerio de Relaciones Exteriores para que sea apostillado. .");
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

	public String procesoFlujoCertificadosSinPago(FlowEvent event) {
		final String anteriorStep = event.getOldStep();
		final String nuevoStep = event.getNewStep();
		Boolean moveWizardSteps = null;
		try {
			PrimeFaces.current().ajax().update("frmWizard:pnlButtons");
			if (anteriorStep.equals("legal")) {
				bindCheckAceptoTerminos();
				if (aceptoTerminos == null || aceptoTerminos == false) {
					AddErrorMessage("Para poder continuar, debe aceptar los términos y condiciones expuestos.");
					return anteriorStep;
				}
			} else if (anteriorStep.equals("solicitudes") && nuevoStep.equals("apostilla")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("apostilla") && nuevoStep.equals("personal")) {
				moveWizardSteps = false;
				return nuevoStep;
			} else if (anteriorStep.equals("confirmacion")) {
				if (nuevoStep.equals("apostilla")) {
					moveWizardSteps = false;
					return nuevoStep;
				} else if (nuevoStep.equals("pago") || nuevoStep.equals("resultado")) {
					this.valorUnitario = null;
					setIsSuccess(guardarSolicitud());
					if (getIsSuccess()) {
						this.registrarRadicado(tramiteSeleccionado);
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
				}
				if (this.justificacion == null || this.justificacion.isEmpty()) {
					AddErrorMessage("Para poder continuar debe agregar una justificación.");
					return anteriorStep;
				}
			} else if (nuevoStep.equals("solicitudes") && anteriorStep.equals("apostilla")) {
				if (!conApostillaje && !sinApostillaje) {
					AddErrorMessage(
							"Para poder continuar debe especificar  si desea enviar el certificado al Ministerio de Relaciones Exteriores para que sea apostillado. .");
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

	public Integer registrarRadicado(Cesl_tramite tramite) {
		try (Dal Dal = new Dal()) {

			Perfil perfilRadicacion = Dal.getPerfilCertificado(tramite.getIdtiposolicitud());
			// Perfil perfilRadicacion =
			// Dal.getPerfilCertificado(TipoTramite.COPIAS_SIMPLES);
			List<Cesl_detalleSolicitud> detallesTramite = Dal.getDetallesTramite(tramite.getIdtramite());

			Radicacion radi = new Radicacion();
			Persona radicador = new Persona();
			radicador.setId(tramite.getIden_pers());
			radicador.setRetornarSoloUltimosDatos(true);
			InteropWSClient wsInteropClient = Utility.GetWSClient();
			radicador = wsInteropClient.personaConsultar(radicador).getPersona();
			radi.setRadicador(radicador);

			radi.setPerfil(perfilRadicacion);
			radi.setTipoRadicacion(Constantes.TIPO_RADICACION_ENTRADA);
			radi.setTotalFolios(1);
			radi.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
			radi.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
			radi.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
			radi.setObservaciones("Certificados sin pago: " + this.justificacion);
			ResponseRadicacion responseRadicacion = wsInteropClient.radicacionRegistrar(radi);

			if (responseRadicacion.getCodigo() == 0) {
				radi = responseRadicacion.getRadicacion();

				tramite.setAno_radi(radi.getAnio());
				tramite.setNume_radi(radi.getNumero());
				tramite.setCont_radi(radi.getControl());
				tramite.setCons_radi(radi.getConsecutivo());
				tramite.setEstado(EstadoTramite.RADICADO_ENTRADA);
				Dal.actualizarTramite(tramite);

				TemplateContent templateContent = new TemplateContent();

				Radicacion radiSalidaTemp = new Radicacion();
				RequestSignPDF requestSign = new RequestSignPDF();
				requestSign.setPasswordCliente(Constantes.WS_SIGN_PASS);
				requestSign.setIdCliente(Constantes.WS_SIGN_USER);
				requestSign.setIdPolitica(Constantes.WS_SIGN_ID_POLITICA_SIN_ESTAMPA);
				requestSign.setStringToFind(Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
				requestSign.setNoPagina("0");
				int numeroAdjuntoInicial = 1;
				int index2 = numeroAdjuntoInicial;
				WSSignClient wsSign = new WSSignClient();
				List<String> filesToDelete = new ArrayList<String>();
				if (tramite.getEstado() == EstadoTramite.RADICADO_ENTRADA) {
					if (null != tramite.getIdtiposolicitud()) {
						for (Cesl_detalleSolicitud detalle : detallesTramite) {
							TipoSancion tipoSancion = TipoSancion.fromValue(detalle.getTipo_certifica());
							LocalDateTime finalDate = Utility.getStartDateFromTodayAnYears(detalle.getAnos(),
									radi.getFechaRadicacion());
							List<Sancion> sanciones = Dal.consultarSanciones(detalle.getTipo_docu(),
									detalle.getNume_docu(), tipoSancion, finalDate, radi.getFechaRadicacion());
							String html = templateContent.buildCertificadoSancionesPDFTemplate(detalle, tipoSancion,
									sanciones, finalDate, radi.getFechaRadicacion());
							try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(html,
									"Certificado Demandas, Investigaciones y Sanciones",
									tramite.getIdtiposolicitud().getDescripcion(), Constantes.KEYWORDS_PDF_DEMANDAS,
									true, null)) {
								for (int index = 1; index <= detalle.getCantidad(); index++) {
									String fileName = String.format(
											"%s_Certificado Demandas, Investigaciones y Sanciones_%s_%s%s_%s.%s",
											radi.getShortNumeroRadicacion(), detalle.getTipo_certifica(),
											detalle.getTipo_docu(), detalle.getNume_docu(), index,
											Constantes.PDF_EXTENSION);
									this.filePath = Functions.saveFile(radi, fileName, fileContent);

									boolean firmadoExitosamente = false;
									long startTime = System.currentTimeMillis();

									while (!firmadoExitosamente
											&& (System.currentTimeMillis() - startTime) < TimeUnit.SECONDS
													.toMillis(30)) {
										try {
											this.filePath = Functions.saveFile(radi, fileName, fileContent);
											requestSign.setFilePath(this.filePath);
											ResponseSignPDF responseSign = wsSign.Firmar(requestSign);
											byte[] filesBytes = responseSign.getDocumento();
											if (filesBytes != null) {
												this.filePath = Functions.saveFile(radi, fileName, filesBytes);
												firmadoExitosamente = true;
											} else {
												logger.error("PDF Firma Certificado: "
														+ responseSign.getRespuestaObj().getMensajes().getMensaje());
											}
										} catch (Exception e) {
											logger.error("Error durante la firma del certificado: " + e.getMessage());
										}

										if (!firmadoExitosamente) {
											Thread.sleep(TimeUnit.SECONDS.toMillis(5));
										}
									}

									if (!firmadoExitosamente) {
										logger.error("Firma del PDF fallida después de múltiples intentos.");
										return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
									}

									index2++;
									radiSalidaTemp.addAdjunto(this.filePath, false, index2);
								}
							}
						}
					}
					tramite.setEstado(EstadoTramite.GENERADO);
					Dal.actualizarTramite(tramite);
				}
				if (tramite.getEstado() == EstadoTramite.GENERADO) {
					Radicacion radiSalida = new Radicacion();
					radiSalida.setAnio(radi.getAnio());
					radiSalida.setNumero(radi.getNumero());
					radiSalida.setControl(radi.getControl());
					perfilRadicacion.setActuacion((short) 440);
					radiSalida.setPerfil(perfilRadicacion);
					radiSalida.setMedioEntrada(Constantes.WS_RADICACION_MEDIO_ENTRADA);
					radiSalida.setIdFuncionario(Constantes.WS_RADICACION_FUNCIONARIO_RADICADOR_ID);
					radiSalida.setRadicador(radicador);
					radiSalida.setTotalFolios(1);
					radiSalida.setIdTasa(Constantes.WS_RADICACION_CONS_TASA);
					radiSalida.setTipoRadicacion(Constantes.TIPO_RADICACION_SALIDA);
					responseRadicacion = wsInteropClient.radicacionRegistrar(radiSalida);
					if (responseRadicacion.getCodigo() == 0) {
						tramite.setEstado(EstadoTramite.RADICADO_SALIDA);
						Dal.actualizarTramite(tramite);

						radiSalida = responseRadicacion.getRadicacion();
						// Se debe volver a asignar por si se consulta esta propiedad mas adelante
						// el webservice no devuelve la informacion completa de la persona luego de una
						// radicacion
						radiSalida.setRadicador(radicador);
						String contentPDF = templateContent.buildRespuestaPDFTemplate(radiSalida,
								tramite.getIdtiposolicitud());
						String subject = String.format("Radicación SIC %s", radiSalida.getShortNumeroRadicacion());

						try (ByteArrayOutputStream fileContent = PDFGeneratorService.createPdf(contentPDF,
								tramite.getIdtiposolicitud().getDescripcion(), subject,
								String.format(Constantes.KEYWORDS_PDF_RADICACION,
										radiSalida.getFechaRadicacion().getYear()),
								false, Utility.getBarCode(tramite.getIdtramite(), radiSalida.getConsecutivo(),
										tramite.getIdtiposolicitud()))) {
							String fullPath = Functions.saveFile(radiSalida, fileContent);
							radiSalidaTemp.addAdjunto(fullPath, false, numeroAdjuntoInicial);
							radiSalida.setAdjuntos(radiSalidaTemp.getAdjuntos());

							ResponseRadicacion responeAdjunto = wsInteropClient.radicacionAdjuntosRegistrar(radiSalida);
							if (responeAdjunto.getCodigo() != 0) {
								String error = "Radicacion Salida adjuntos: " + responeAdjunto.getMensaje();
								logger.error(error);
								this.AddErrorMessage(error);
								logger.info("registrarRadicado OK --> Radicacion Salida adjuntos ");
								return HttpServletResponse.SC_OK;
							} else {
								for (String f : filesToDelete) {
									Path fp = Paths.get(f);
									if (Files.exists(fp, LinkOption.NOFOLLOW_LINKS)) {
										try {
											Files.delete(fp);
										} catch (Exception ex) {
											logger.error(String.format("No se pudo borrar el archivo temporal %s", f),
													ex);
										}
									}
								}
							}
						}

						tramite.setEstado(EstadoTramite.FINALIZADO);
						Dal.actualizarTramite(tramite);
						this.AddInfoMessage(
								String.format("Solicitud %s Finalizada Exitosamente", tramite.getIdtramite()));

					} else {
						String error = "Radicacion Salida registrarRadicado: " + responseRadicacion.getMensaje();
						logger.error(error);
						this.AddErrorMessage(error);
					}
				}
			} else {
				String error = "Radicacion Entrada registrarRadicado: " + responseRadicacion.getMensaje();
				logger.error(error);
				this.AddErrorMessage(error);
			}
			logger.info("registrarPagoOtros OK OK");
			return HttpServletResponse.SC_OK;
		} catch (Exception e) {
			logger.error("procesarPagoBean registrarPago", e);
			this.AddErrorMessage("procesarPagoBean registrarPago " + e.getMessage());
			return HttpServletResponse.SC_PRECONDITION_FAILED;
		}

	}

	public void eliminarDetalle(Cesl_detalleSolicitud detalle) {
		if (listaDetalles.contains(detalle)) {
			if (detalle.getValor() != null) {
				setValorTotalPagar(getValorTotalPagar() - detalle.getValor());
			}
			listaDetalles.remove(detalle);
		}
		showPanelAgregar = true;
		if (containsInputFile) {
			PrimeFaces.current().executeScript("bsCustomFileInput.init();");
		}
	}

	/**
	 * @return the attachmentError
	 */
	public boolean isAttachmentError() {
		return attachmentError;
	}

	/**
	 * @param attachmentError the attachmentError to set
	 */
	public void setAttachmentError(boolean attachmentError) {
		this.attachmentError = attachmentError;
	}

	/**
	 * @return the attachmentErrorMsg
	 */
	public String getAttachmentErrorMsg() {
		return attachmentErrorMsg;
	}

	/**
	 * @param attachmentErrorMsg the attachmentErrorMsg to set
	 */
	public void setAttachmentErrorMsg(String attachmentErrorMsg) {
		this.attachmentErrorMsg = attachmentErrorMsg;
	}

	/**
	 * @param sinApostillaje the sinApostillaje to set
	 */
	public void setSinApostillaje(boolean sinApostillaje) {
		this.sinApostillaje = sinApostillaje;
		if (sinApostillaje) {
			conApostillaje = false;
		}
	}

	public boolean getSinApostillaje() {
		return this.sinApostillaje;
	}

	public boolean getConApostillaje() {
		return conApostillaje;
	}

	public void setConApostillaje(boolean val) {
		conApostillaje = val;
		if (conApostillaje) {
			sinApostillaje = false;
		}
	}

}
