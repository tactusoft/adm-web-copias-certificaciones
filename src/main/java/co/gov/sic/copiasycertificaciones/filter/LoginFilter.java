package co.gov.sic.copiasycertificaciones.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.beans.LoginBean;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.NavegaUsuarioMB;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/view/*")
public class LoginFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

	@Inject
	private LoginBean loginBean; // Inyectar el LoginBean usando CDI

	@Inject
	private NavegaUsuarioMB navegaUsuarioMB; // Inyectar el NavegaUsuarioMB usando CDI

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String path = httpRequest.getRequestURI();

		boolean validatePath = !path.contains("/jakarta.faces.resource/") && !path.contains("/css/")
				&& !path.contains("/img/") && !path.contains("/js/") && !path.contains("/fonts/")
				&& !path.endsWith(".pdf") && !path.contains("/login.xhtml") && !path.contains("/procesarPago.xhtml")
				&& !path.contains("/confirmarPago.xhtml")
				&& !path.contains("/admin/pendientes.xhtml");

		boolean validAdminPath = path.contains("/notification/validaciontramite.xhtml")
				|| path.contains("/notification/inboxnotify.xhtml") || path.contains("/notification/reasignar.xhtml")
				|| path.contains("/notification/reasignardetalle.xhtml") || path.contains("/admin/parametros.xhtml")
				|| path.contains("/tramites/CertificadoCOR.xhtml") || path.contains("/notification/memorandos.xhtml");

		boolean validOtcPath = path.contains("/otc/overthecounter.xhtml") || path.contains("/otc/fetchuserdata.xhtml")
				|| path.contains("/errors/csrf.xhtml") || path.contains("/errors/pagenotfound.xhtml");

		boolean validResponsablePath = path.contains("/requests/complementar.xhtml")
				|| path.contains("/requests/cotizacion.xhtml") || path.contains("/requests/detalleadjuntos.xhtml")
				|| path.contains("/requests/detalletramite.xhtml") || path.contains("/requests/digitalizacion.xhtml")
				|| path.contains("/requests/infoareaex.xhtml") || path.contains("/requests/loadfiles.xhtml")
				|| path.contains("/requests/transfentidad.xhtml") || path.contains("/requests/missolicitudes.xhtml")
				|| path.contains("/requests/prorroga.xhtml") || path.contains("/errors/csrf.xhtml")
				|| path.contains("/errors/pagenotfound.xhtml") || path.contains("/requests/traslado.xhtml");

		boolean validUserPath = path.contains("/tramites/CertificadoDIS.xhtml")
				|| path.contains("/tramites/ListadosInf.xhtml") || path.contains("/tramites/SolicitudCopias.xhtml")
				|| path.contains("/tramites/listado.xhtml")
				|| path.contains("/tramites/menu.xhtml") || path.contains("/errors/csrf.xhtml")
				|| path.contains("/tramites/detalleadjuntosSolicitante.xhtml")
				|| path.contains("/tramites/downloadfiles.xhtml") || path.contains("/tramites/payment.xhtml")
				|| path.contains("/errors/rtacomplemento.xhtml") || path.contains("/errors/pagenotfound.xhtml")
				|| path.contains("/requester/detalleadjuntosSolicitante.xhtml")
				|| path.contains("/requester/downloadfiles.xhtml") || path.contains("/requester/payment.xhtml")
				|| path.contains("/requester/rtacomplemento.xhtml");

		String contextPath = httpRequest.getContextPath();
		try {
			if (validatePath && (loginBean == null || !loginBean.getLoggedIn())) {
				String redirectPath = navegaUsuarioMB.autenticacion(false, contextPath);
				httpResponse.sendRedirect(contextPath + redirectPath);
			} else {
				if (loginBean == null || !loginBean.getLoggedIn()) {
					logger.debug("No hay una sesión activa");
				} else {
					if (loginBean.getNombreRol().equals("COORDINADOR_SCC") && !validAdminPath) {
						String redirectPath = navegaUsuarioMB.adminHome(false, contextPath);
						httpResponse.sendRedirect(redirectPath);
					} else if (loginBean.getNombreRol().equals("RESPONSABLE_SCC") && !validResponsablePath) {
						String redirectPath = navegaUsuarioMB.responsableHome(false, contextPath);
						httpResponse.sendRedirect(redirectPath);
					} else if (loginBean.getNombreRol().equals("VENTANILLA_SE") && !validOtcPath) {
						String redirectPath = navegaUsuarioMB.adminHome(false, contextPath);
						httpResponse.sendRedirect(redirectPath);
					} else if (loginBean.getNombreRol().equals("SOLICITANTE_SCC") && !validUserPath) {
						String redirectPath = navegaUsuarioMB.userHomeHome(false, contextPath);
						httpResponse.sendRedirect(redirectPath);
					}
				}
				chain.doFilter(request, response);
			}
		} catch (IOException | ServletException | NullPointerException e) {
			logger.error(e.toString());

			if (loginBean == null) {
				logger.debug("No hay una sesión activa");
			} else {
				if (loginBean.getNombreRol().equals("COORDINADOR_SCC")) {
					String redirectPath = navegaUsuarioMB.adminHome(false, contextPath);
					httpResponse.sendRedirect(redirectPath);
				} else if (loginBean.getNombreRol().equals("RESPONSABLE_SCC")) {
					String redirectPath = navegaUsuarioMB.responsableHome(true, contextPath);
					httpResponse.sendRedirect(redirectPath);
				} else if (loginBean.getNombreRol().equals("VENTANILLA_SE")) {
					String redirectPath = navegaUsuarioMB.adminHome(false, contextPath);
					httpResponse.sendRedirect(redirectPath);
				} else if (loginBean.getNombreRol().equals("SOLICITANTE_SCC")) {
					String redirectPath = navegaUsuarioMB.userHomeHome(false, contextPath);
					httpResponse.sendRedirect(redirectPath);
				}
			}
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		new Constantes();
	}

	@Override
	public void destroy() {
		// Nothing to do here!
	}
}