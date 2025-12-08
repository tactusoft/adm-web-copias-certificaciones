package co.gov.sic.copiasycertificaciones.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class SameSiteCookieFilter implements Filter {

    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String SAME_SITE_STRICT = "; SameSite=Strict";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Usar un wrapper para interceptar los Set-Cookie headers
        SameSiteResponseWrapper wrappedResponse = new SameSiteResponseWrapper(httpResponse);
        
        // Continuar con la cadena
        chain.doFilter(request, wrappedResponse);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
    
    /**
     * Wrapper que intercepta los headers Set-Cookie y agrega SameSite
     */
    private static class SameSiteResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        
        public SameSiteResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        
        @Override
        public void addHeader(String name, String value) {
            if (SET_COOKIE_HEADER.equalsIgnoreCase(name)) {
                value = addSameSiteAttribute(value);
            }
            super.addHeader(name, value);
        }
        
        @Override
        public void setHeader(String name, String value) {
            if (SET_COOKIE_HEADER.equalsIgnoreCase(name)) {
                value = addSameSiteAttribute(value);
            }
            super.setHeader(name, value);
        }
        
        /**
         * Agrega SameSite=Strict a la cookie si no lo tiene ya
         */
        private String addSameSiteAttribute(String cookieValue) {
            // Si ya tiene SameSite, no modificar
            if (cookieValue.toLowerCase().contains("samesite=")) {
                return cookieValue;
            }
            
            // Decidir entre Strict o Lax según el tipo de cookie
            // Para JSESSIONID y cookies de sesión: Strict
            // Para otras cookies: puede ser Lax
            if (cookieValue.toUpperCase().contains("JSESSIONID") || 
                cookieValue.toUpperCase().contains("SESSIONID")) {
                return cookieValue + SAME_SITE_STRICT;
            } else {
                // Para otras cookies, usar Strict también (más seguro)
                return cookieValue + SAME_SITE_STRICT;
            }
        }
    }
}