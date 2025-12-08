package co.gov.sic.copiasycertificaciones.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class CSRFProtectionFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(CSRFProtectionFilter.class);

	// Páginas que NO requieren validación CSRF (ej: login, recursos estáticos)
	private static final List<String> EXCLUDED_PATHS = Arrays.asList("/view/login.xhtml", "/jakarta.faces.resource/",
			"/css/", "/js/", "/img/", "/fonts/", ".pdf", ".css", ".js", ".png", ".jpg", ".gif", ".ico");

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("CSRFProtectionFilter inicializado");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String requestURI = httpRequest.getRequestURI();
		String method = httpRequest.getMethod();

		// Verificar si la ruta está excluida
		boolean isExcluded = EXCLUDED_PATHS.stream().anyMatch(requestURI::contains);

		// Solo validar en POST requests a páginas protegidas
		if ("POST".equalsIgnoreCase(method) && !isExcluded) {

			// ====== VALIDACIÓN #1: Verificar Referer Header ======
			String referer = httpRequest.getHeader("Referer");
			String host = httpRequest.getHeader("Host");

			if (referer == null || !referer.contains(host)) {
				logger.warn("CSRF: Referer inválido o ausente. URI: {}, Referer: {}", requestURI, referer);
				httpResponse.sendRedirect(httpRequest.getContextPath() + "/view/errors/csrf.xhtml");
				return;
			}

			// ====== VALIDACIÓN #2: Verificar Origin Header ======
			String origin = httpRequest.getHeader("Origin");
			if (origin != null && !origin.contains(host)) {
				logger.warn("CSRF: Origin inválido. URI: {}, Origin: {}", requestURI, origin);
				httpResponse.sendRedirect(httpRequest.getContextPath() + "/view/errors/csrf.xhtml");
				return;
			}

			// ====== VALIDACIÓN #3: Verificar token de JSF (jakarta.faces.ViewState) ======
			// JSF automáticamente valida este token en protected-views
			// Este filtro es una capa adicional de defensa

			HttpSession session = httpRequest.getSession(false);
			if (session == null) {
				logger.warn("CSRF: Sesión inválida o expirada. URI: {}", requestURI);
				httpResponse.sendRedirect(httpRequest.getContextPath() + "/view/login.xhtml");
				return;
			}

			// ====== VALIDACIÓN #4: Verificar que la solicitud viene de un formulario JSF
			// ======
			String facesRequest = httpRequest.getParameter("jakarta.faces.partial.ajax");
			String viewState = httpRequest.getParameter("jakarta.faces.ViewState");

			// Si es una solicitud JSF, debe tener ViewState
			if (requestURI.contains(".xhtml") && viewState == null && facesRequest == null) {
				logger.warn("CSRF: ViewState ausente en solicitud JSF. URI: {}", requestURI);
				// No redirigir aquí porque podría ser una solicitud legítima no-JSF
				// Solo registrar para auditoría
			}
		}

		// Continuar con la cadena de filtros
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		logger.info("CSRFProtectionFilter destruido");
	}
}