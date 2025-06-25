package co.gov.sic.copiasycertificaciones.util;

import java.io.IOException;
import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("navegaUsuarioMB")
@SessionScoped
public class NavegaUsuarioMB implements Serializable {

	private static final long serialVersionUID = -2296150042934311803L;

	public NavegaUsuarioMB() {
	}

	public String autenticacion(boolean withContextPath, String contextPath) {
		String path = "/view/login.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	public String adminHome(boolean withContextPath, String contextPath) {
		String path = "/view/notification/inboxnotify.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	public String responsableHome(boolean withContextPath, String contextPath) {
		String path = "/view/requests/missolicitudes.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	public String userHomeHome(boolean withContextPath, String contextPath) {
		String path = "/view/tramites/menu.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	public String redirectToHome(boolean withContextPath, String contextPath) {
		String path = "/view/tramites/menu.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	public String redirectToNotificationInbox(boolean withContextPath, String contextPath) {
		String path = "/view/tramites/menu.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	public String redirectToNotificationHome(boolean withContextPath, String contextPath) {
		String path = "/view/admin/ccparametros.xhtml?faces-redirect=true";
		return getRedirect(withContextPath, path, contextPath);
	}

	private String getRedirect(boolean withContextPath, String path, String contextPath) {
		if (withContextPath) {
			return contextPath.concat(path);
		} else {
			return path;
		}
	}

	public String redireccionServiciosLinea(boolean isNewUser, boolean isForgetPassword) throws IOException {
		if (isNewUser) {
			return Constantes.URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO;
		} else if (isForgetPassword) {
			return Constantes.URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO;
		} else {
			return Constantes.URL_WEB_SERVICIOS_EN_LINEA;
		}
	}
}