package co.gov.sic.copiasycertificaciones.beans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.itextpdf.text.pdf.PdfReader;

import co.gov.sic.copiasycertificaciones.dataaccess.Dal;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.EncriptacionUtil;
import co.gov.sic.copiasycertificaciones.util.NavegaUsuarioMB;
import co.gov.sic.copiasycertificaciones.util.Utility;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Usuario;
import sic.ws.interop.entities.response.ResponseAutenticar;

@Named("loginBean")
@SessionScoped
public class LoginBean extends BeanBase implements Serializable {

	@Inject
	private NavegaUsuarioMB navegaUsuarioMB;

	private static final long serialVersionUID = 7765876811740798583L;
	private final List<Cesl_tramite> tramitesActivos;
	private String logi_usua;
	private String pass_usua;
	private String codigo1;
	private String codigo2;
	private String codigo3;
	private boolean loggenIn;
	private boolean isNotoficationRoll = false;
	private int codigoRol = 0;
	private String nombreRol;
	private final Logger logger = LoggerFactory.getLogger(LoginBean.class);
	private List<Direccion> listDirecciones;
	private List<Direccion> listEmails;
	private long consDire;
	private long consEmail;
	private String userCiudad;
	private String userRegion;
	private String captchaSiteKey;
	private String captchaSecret;

	public LoginBean() throws Exception {
		super();
		try (Dal DAL = new Dal()) {
			tramitesActivos = DAL.getTramitesActivos();
		}
	}

	public boolean getNotificationRoll() {
		return isNotoficationRoll;
	}

	public String getCodigo1() {
		return codigo1;
	}

	public void setCodigo1(String val) {
		codigo1 = val;
	}

	public String getCodigo3() {
		return codigo3;
	}

	public void setCodigo3(String val) {
		codigo3 = val;
	}

	public String getCodigo2() {
		return codigo2;
	}

	public void setCodigo2(String val) {
		codigo2 = val;
	}

	public List<Cesl_tramite> getTramitesActivos() {
		return tramitesActivos;
	}

	public void validarCodigo() {
		recuperarCodigo(false);
	}

	public void recuperarCodigo(boolean showEstado) {
		try {
			if (!Utility.isNullOrEmptyTrim(codigo1) && !Utility.isNullOrEmptyTrim(codigo2)) {
				int idTramite = Utility.decryptBarCode(codigo1, Constantes.ENCODE_PAD_CHAR1);
				int tipoTram = Utility.decryptBarCode(codigo2, Constantes.ENCODE_PAD_CHAR2);
				int consRadi = Utility.decryptBarCode(codigo3, Constantes.ENCODE_PAD_CHAR3);
				try (Dal Dal = new Dal()) {
					Cesl_tramite tram = Dal.consultarTramite(idTramite, tipoTram, consRadi);
					if (tram != null) {
						if (showEstado) {
							this.AddInfoMessage(String.format("El estado actual del tramite de %s es \"%s\".",
									tram.getIdtiposolicitud().getDescripcion(), tram.getEstado().getDescription()),
									"msgCodigo");
						} else {
							this.AddInfoMessage(String.format("El código del tramite de %s es valido.",
									tram.getIdtiposolicitud().getDescripcion()), "msgCodigo");
						}
					} else {
						this.AddErrorMessage("El código introducido no es valido.", "msgCodigo");
					}
				}
			} else {
				this.AddErrorMessage("El código completo es requerido.", "msgCodigo");
			}
		} catch (Exception e) {
			logger.error("recuperarCodigo", e);
			this.AddErrorMessage("El código introducido no es valido.", "msgCodigo");
		}
	}

	public void consultarEstado() {
		recuperarCodigo(true);
	}

	public void validar() {
		try {
			Usuario user = new Usuario();
			user.setPassword(pass_usua);
			user.setLogin(logi_usua);
			user.setSistema(Constantes.COD_SISTEMA_SERVICIOS_LINEA);
			user.setRetornarPersona(true);
			user.setRetornarSoloUltimosDatos(true);
			user.setUsarHashContrasena(true);
			InteropWSClient wsInteropClient = Utility.GetWSClient();
			if (wsInteropClient.testIsOnline()) {
				ResponseAutenticar response = wsInteropClient.usuarioAutenticar(user);
				if (response.getCodigo() == 0) {

					this.consDire = -1;
					this.consEmail = -1;
					this.getListDireccions();
					setDatosSesion(response.getUsuario(), this.listDirecciones, this.listEmails);
					setLoggenIn(true);
					logger.info("Rol : " + response.getUsuario().getCodigoRol());

					if (response.getUsuario().getCodigoRol().equals(Constantes.COORDINADOR_SCC)) {
						codigoRol = Integer.parseInt(Constantes.COORDINADOR_SCC);
						nombreRol = "COORDINADOR_SCC";
					} else if (response.getUsuario().getCodigoRol().equals(Constantes.RESPONSABLE_SCC)) {
						codigoRol = Integer.parseInt(Constantes.RESPONSABLE_SCC);
						nombreRol = "RESPONSABLE_SCC";
					} else if (response.getUsuario().getCodigoRol().equals(Constantes.VENTANILLA_SE)) {
						codigoRol = Integer.parseInt(Constantes.VENTANILLA_SE);
						nombreRol = "VENTANILLA_SE";
					} else {
						nombreRol = "SOLICITANTE_SCC";
					}
				} else {
					this.AddErrorMessage("Credenciales Invalidas. " + response.getMensaje(), "msgLogin");
					setLoggenIn(false);
				}
			} else {
				this.AddErrorMessage("El servicio no se encuentra disponible", "msgLogin");
			}
		} catch (Exception e) {
			logger.error("Login", e);
			this.AddErrorMessage("Error general: No se pudo realizar la operacion.", "msgLogin");
		}
	}

	private void getListDireccions() throws IOException {
		Usuario user = new Usuario();
		user.setPassword(pass_usua);
		user.setLogin(logi_usua);
		user.setSistema(Constantes.COD_SISTEMA_SERVICIOS_LINEA);
		user.setRetornarPersona(true);
		user.setRetornarSoloUltimosDatos(false);
		user.setUsarHashContrasena(true);
		InteropWSClient wsInteropClient = Utility.GetWSClient();
		if (wsInteropClient.testIsOnline()) {
			ResponseAutenticar response = wsInteropClient.usuarioAutenticar(user);
			this.listDirecciones = response.getUsuario().getPersona().getDirecciones().stream()
					.filter(direccion -> "PE".equals(direccion.getTipo())).collect(Collectors.toList());
			this.listEmails = response.getUsuario().getPersona().getDirecciones().stream()
					.filter(direccion -> "EL".equals(direccion.getTipo())).collect(Collectors.toList());

		} else {
			this.listDirecciones = new ArrayList<>();
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		try {
			byte[] fileContent = uploadedFile.getContent();
			String calculatedHash = calculateHash(fileContent);
			if (calculatedHash != null) {
				try (Dal DAL = new Dal()) {
					String[] partes = calculatedHash.replaceAll("\\n", "").split("\\|");
					int idSolicitud = Integer.parseInt(partes[0]);
					String hash = partes[1];
					boolean validateHash = DAL.obtenerHash(idSolicitud, hash);
					if (validateHash) {
						this.AddInfoMessage("¡El archivo es una copia auténtica!", "msgCodigo");
					} else {
						this.AddErrorMessage("El archivo no es una copia auténtica.", "msgCodigo");
					}
				}
			} else {
				this.AddErrorMessage("El archivo no es una copia auténtica.", "msgCodigo");
			}
		} catch (Exception e) {
			logger.error("Error al manejar la carga del archivo", e);
			this.AddErrorMessage("Ocurrió un error al procesar el archivo.", "msgCodigo");
		}
	}

	private String calculateHash(byte[] data) throws NoSuchAlgorithmException {
		try {
			PdfReader reader = new PdfReader(data);
			byte[] xmpData = reader.getMetadata();
			String xmpString = new String(xmpData, StandardCharsets.UTF_8);
			return obtenerHashDesdeXMP(xmpString);
		} catch (Exception e) {
			logger.error("calculateHash", e);
		}
		return null;
	}

	public String obtenerHashDesdeXMP(String xmpString) {
		try {
			// Configuración segura del DocumentBuilderFactory
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder builder = factory.newDocumentBuilder();

			// Parsear el XML de forma segura
			Document document = builder.parse(new ByteArrayInputStream(xmpString.getBytes(StandardCharsets.UTF_8)));

			// Procesar el contenido del XML para obtener el hash
			NodeList dcDescriptions = document.getElementsByTagName("dc:description");
			if (dcDescriptions.getLength() > 0) {
				Node dcDescription = dcDescriptions.item(0);
				String hash = dcDescription.getTextContent();
				return hash;
			}
		} catch (Exception e) {
			logger.error("obtenerHashDesdeXMP", e);
		}
		return null;
	}

	public void onChangeDireccion() {
		List<Direccion> list = this.listDirecciones.stream().filter(direccion -> direccion.getId() == this.consDire)
				.collect(Collectors.toList());
		if (list.isEmpty()) {
			this.userCiudad = "";
			this.userRegion = "";
		} else {
			Direccion direcciones = list.get(0);
			this.userCiudad = direcciones.getCodigoCiudadDesc();
			this.userRegion = direcciones.getCodigoRegionDesc();
		}
	}

	public String rolCoordinador() {
		return Constantes.COORDINADOR_SCC;
	}

	public String rolFuncionario() {
		return Constantes.RESPONSABLE_SCC;
	}

	public String rolVentanilla() {
		return Constantes.VENTANILLA_SE;
	}

	/**
	 * @return the logi_usua
	 */
	public String getLogi_usua() {
		return logi_usua;
	}

	/**
	 * @param logi_usua the logi_usua to set
	 */
	public void setLogi_usua(String logi_usua) {
		this.logi_usua = logi_usua;
	}

	/**
	 * @return the pass_usua
	 */
	public String getPass_usua() {
		return pass_usua;
	}

	public String cerrarSesion() {
		HttpSession session = getSession();
		session.invalidate();
		setDatosSesion(null, null, null);
		this.setLoggenIn(false);

		return "/view/login.xhtml?faces-redirect=true";
	}

	/**
	 * @param pass_usua the pass_usua to set
	 */
	public void setPass_usua(String pass_usua) {
		this.pass_usua = pass_usua;
	}

	public boolean getLoggedIn() {
		return this.loggenIn;
	}

	public String getUserTelefono() {
		if (getDatosSesion().getPersona() != null && getDatosSesion().getPersona().getDirecciones() != null
				&& getDatosSesion().getPersona().getDirecciones().size() > 0
				&& getDatosSesion().getPersona().getDirecciones().get(0).getTelefonos() != null
				&& getDatosSesion().getPersona().getDirecciones().get(0).getTelefonos().size() > 0) {
			return getDatosSesion().getPersona().getDirecciones().get(0).getTelefonos().get(0).getNumero();
		} else {
			return null;
		}
	}

	public String getUserDireccion() {
		if (getDatosSesion().getPersona() != null && getDatosSesion().getPersona().getDirecciones() != null
				&& getDatosSesion().getPersona().getDirecciones().size() > 0) {
			return getDatosSesion().getPersona().getDirecciones().get(0).getDescripcion();
		} else {
			return null;
		}
	}

	public String getUserEmail() {
		if (getDatosSesion().getPersona() != null && getDatosSesion().getPersona().getEmails() != null
				&& getDatosSesion().getPersona().getEmails().size() > 0) {
			return getDatosSesion().getPersona().getEmails().get(0).getDescripcion();
		} else {
			return null;
		}
	}

	public String getRefPasarelaEncriptado() {
		String valor = "";
		try {
			valor = EncriptacionUtil.encriptarRefPasarela();
			logger.info("RefPasarela: " + valor);
		} catch (Exception e) {
			logger.error("Error generando RefPasarela", e);
		}
		return valor;
	}

	/**
	 * @return the codigoRol
	 */
	public int getCodigoRol() {
		return codigoRol;
	}

	/**
	 * @return the nombreRol
	 */
	public String getNombreRol() {
		return nombreRol;
	}

	/**
	 * @param loggenIn the loggenIn to set
	 */
	public void setLoggenIn(boolean loggenIn) {
		this.loggenIn = loggenIn;
	}

	public List<Direccion> getListDirecciones() {
		return listDirecciones;
	}

	public void setListDirecciones(List<Direccion> listDirecciones) {
		this.listDirecciones = listDirecciones;
	}

	public List<Direccion> getListEmails() {
		return listEmails;
	}

	public void setListEmails(List<Direccion> listEmails) {
		this.listEmails = listEmails;
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

	public String getUserCiudad() {
		return userCiudad;
	}

	public void setUserCiudad(String userCiudad) {
		this.userCiudad = userCiudad;
	}

	public String getUserRegion() {
		return userRegion;
	}

	public void setUserRegion(String userRegion) {
		this.userRegion = userRegion;
	}

	public String getCaptchaSiteKey() {
		return captchaSiteKey;
	}

	public void setCaptchaSiteKey(String captchaSiteKey) {
		this.captchaSiteKey = captchaSiteKey;
	}

	public String getCaptchaSecret() {
		return captchaSecret;
	}

	public void setCaptchaSecret(String captchaSecret) {
		this.captchaSecret = captchaSecret;
	}

}
