package co.gov.sic.copiasycertificaciones.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.Cesl_config;
import co.gov.sic.copiasycertificaciones.entities.Cesl_cotizacion;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.PersonaCamara;
import co.gov.sic.copiasycertificaciones.entities.Sancion;
import co.gov.sic.copiasycertificaciones.enums.TipoSancion;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Email;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.ResponsableDepe;
import sic.ws.interop.entities.Telefono;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponseResponsable;

public class TemplateContent {

	private final TemplateEngine templateEngine;
	private final String currentURLDomain;
	protected final Logger logger = LoggerFactory.getLogger(TemplateContent.class);

	public TemplateContent() throws MalformedURLException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

        currentURLDomain = new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
                request.getContextPath()).toString();

        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L); // 1 hora
        templateResolver.setCacheable(true);
        templateResolver.setCharacterEncoding("UTF-8");

        this.templateEngine = new TemplateEngine();
        this.templateEngine.addDialect(new Java8TimeDialect());
        this.templateEngine.setTemplateResolver(templateResolver);
    }

	public String buildEmailTemplatePdfProrroga(Radicacion radi, ResponsableDepe responsableDepe, Persona funcionario)
			throws Exception {
		String nombrePdf = "Prorroga.SA";
		Perfil perfil = radi.getPerfil();
		Context context = new Context();
		// Cuadro arriba derecha
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());
		AddPersona(radi.getRadicador(), context, null);

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildComplementarPDFTemplate(Radicacion radi, TipoTramite tipoTramite,
			ResponsableDepe responsableDepe, Persona funcionario, String observaciones) throws Exception {
		String nombrePdf = "ComplementarSolicitud";
		Context context = new Context();
		AddRadicacion(radi, context);
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("observaciones", observaciones);
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());
		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}
	
	public String buildRespuestaComplementarPDFTemplate(Radicacion radi, TipoTramite tipoTramite,
			ResponsableDepe responsableDepe, Persona funcionario, String observaciones) throws Exception {
		String nombrePdf = "RespuestaComplemSol";
		Context context = new Context();
		AddRadicacion(radi, context);
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("observaciones", observaciones);
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());
		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildTrasladoPDFTemplate(Radicacion radi, TipoTramite tipoTramite,
			ResponsableDepe responsableDepeOrigen, ResponsableDepe responsableDepe, Persona funcionario,
			String observaciones) throws Exception {
		String nombrePdf = "traslado";
		Context context = new Context();
		AddRadicacion(radi, context);

		context.setVariable("nombre", responsableDepe.getNombreResponsable());
		context.setVariable("cargo", responsableDepe.getCargo());

		context.setVariable("observaciones", observaciones);
		context.setVariable("depe_resp", responsableDepeOrigen.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());
		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildCotizacionPDFTemplate(Radicacion radi, TipoTramite tipoTramite,
			List<Cesl_cotizacion> listaCotizacion, ResponsableDepe responsableDepe, Persona funcionario,
			String observaciones) throws Exception {
		String nombrePdf = "Cotizacion";
		Context context = new Context();
		AddRadicacion(radi, context);
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("asunto", tipoTramite.getDescripcion());
		context.setVariable("url", Utility.getURLConsultaRadicacionWeb(radi));

		context.setVariable("cotizaciones", listaCotizacion);
		context.setVariable("observaciones", observaciones);

		int total = 0;
		for (Cesl_cotizacion c : listaCotizacion) {
			total = total + c.getTotalConcepto();
		}
		context.setVariable("total", total);
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("func_asig", funcionario.getFullName());

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildRespuestaSolicitudCopias(Radicacion radi, Persona funcionario, ResponsableDepe responsableDepe,
			boolean obs, String observaciones) throws Exception {

		String nombrePdf = "RespuestaCopias";

		if (!obs) {
			nombrePdf = "RespuestaCopias.CUSTOM";
		}

		Context context = new Context();
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("email", radi.getRadicador().getEmails().get(0).getDescripcion());
		context.setVariable("expediente", String.valueOf(radi.getAnio()) + "-" + String.valueOf(radi.getNumero()));
		context.setVariable("tramite", radi.getPerfil().getTramite());
		context.setVariable("evento", 0);
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		context.setVariable("codi_actuacion", radi.getPerfil().getActuacion());
		context.setVariable("codi_tramite", radi.getPerfil().getTramite());
		context.setVariable("codi_evento", radi.getPerfil().getEvento());
		context.setVariable("dependencia",
				String.format("%s - %s", radi.getPerfil().getDependencia(), radi.getPerfil().getNombreDependencia()));

		context.setVariable("folios", radi.getTotalFolios());

		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("observaciones", observaciones);

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);

	}

	public String buildRespuestaSolicitudCopiasSinFirma(Radicacion radi, Persona funcionario,
			ResponsableDepe responsableDepe, String observaciones, Persona radicador) throws Exception {
		String nombrePdf = "RespuestaCopiasSinFirma.CUSTOM";

		Context context = new Context();
		AddPersona(radicador, context, null);
		context.setVariable("email", radi.getRadicador().getEmails().get(0).getDescripcion());
		context.setVariable("expediente", String.valueOf(radi.getAnio()) + "-" + String.valueOf(radi.getNumero()));
		context.setVariable("tramite", radi.getPerfil().getTramite());
		context.setVariable("evento", 0);
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		context.setVariable("codi_actuacion", radi.getPerfil().getActuacion());
		context.setVariable("codi_tramite", radi.getPerfil().getTramite());
		context.setVariable("codi_evento", radi.getPerfil().getEvento());
		context.setVariable("dependencia",
				String.format("%s - %s", radi.getPerfil().getDependencia(), radi.getPerfil().getNombreDependencia()));

		context.setVariable("folios", radi.getTotalFolios());

		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("observaciones", observaciones);

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildRespuestaTrasladoEntidad(Radicacion radi, Persona funcionario, ResponsableDepe responsableDepe,
			boolean obs, String observaciones, Persona radicador) throws Exception {

		String nombrePdf = "TrasladoEntidad";
		Context context = new Context();
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("email", radi.getRadicador().getEmails().get(0).getDescripcion());
		context.setVariable("expediente", String.valueOf(radi.getAnio()) + "-" + String.valueOf(radi.getNumero()));
		context.setVariable("tramite", radi.getPerfil().getTramite());
		context.setVariable("evento", 0);
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		context.setVariable("codi_actuacion", radi.getPerfil().getActuacion());
		context.setVariable("codi_tramite", radi.getPerfil().getTramite());
		context.setVariable("codi_evento", radi.getPerfil().getEvento());
		context.setVariable("dependencia",
				String.format("%s - %s", radi.getPerfil().getDependencia(), radi.getPerfil().getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("observaciones", observaciones);
		context.setVariable("persona", radicador.getFullName());

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);

	}

	public String buildRespuestaTrasladoUsuario(Radicacion radi, Persona funcionario, ResponsableDepe responsableDepe,
			boolean obs, String observaciones, Persona entidad) throws Exception {

		String nombrePdf = "TrasladoEntidadUsuario";
		Context context = new Context();
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("email", radi.getRadicador().getEmails().get(0).getDescripcion());
		context.setVariable("expediente", String.valueOf(radi.getAnio()) + "-" + String.valueOf(radi.getNumero()));
		context.setVariable("tramite", radi.getPerfil().getTramite());
		context.setVariable("evento", 0);
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		context.setVariable("codi_actuacion", radi.getPerfil().getActuacion());
		context.setVariable("codi_tramite", radi.getPerfil().getTramite());
		context.setVariable("codi_evento", radi.getPerfil().getEvento());
		context.setVariable("dependencia",
				String.format("%s - %s", radi.getPerfil().getDependencia(), radi.getPerfil().getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("depe_resp", responsableDepe.getNombreResponsable());
		context.setVariable("observaciones", observaciones);
		context.setVariable("entidad", entidad.getFullName());

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);

	}

	public String buildSolDigitalizacionPDFTmplate(Radicacion radi, TipoTramite tipoTramite, Persona personaResponsable,
			ResponsableDepe responsableDepe, Persona radicador, Cesl_config cfg, List<String> busnessDays,
			Persona funcionario, ResponseResponsable responseResponsableNotificaciones, String observaciones) {
		String nombrePdf = "SolDigitalizacion";
		Context context = new Context();
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		Perfil perfil = radi.getPerfil();
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_dependencia", perfil.getDependencia());
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable("nombre", personaResponsable.getFullName());
		context.setVariable("cargo", responsableDepe.getCargo());
		context.setVariable("solicitante", radicador.getFullName());
		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("coordinador", responseResponsableNotificaciones.getResponsable().getNombreResponsable());
		context.setVariable("observaciones", observaciones);
		int days = 1;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime today = radi.getFechaRadicacion();
		int count = cfg.getValor();

		if (cfg.isBusinessDays()) {
			while (days <= cfg.getValor()) {
				today = today.plusDays(1);

				if (busnessDays.contains(today.format(formatter))) {
					count++;
				}
				today.getDayOfWeek();
				today.getDayOfWeek();
				if (DayOfWeek.SATURDAY == today.getDayOfWeek()
						|| DayOfWeek.SUNDAY == today.getDayOfWeek()) {
					count++;
				}
				days++;
			}
			today = radi.getFechaRadicacion();
			today = today.plusDays(count);
		} else {
			today = today.plusDays(count);
		}

		context.setVariable("fecha_vencimiento", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(today));

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildInfoAreaInternaPDFTemplate(Radicacion radi, TipoTramite tipoTramite, Persona personaResponsable,
			ResponsableDepe responsableDepe, Persona radicador, Cesl_config cfg, List<String> busnessDays,
			Persona funcionario) throws Exception {
		String nombrePdf = "InfoAreaInterna";
		Context context = new Context();
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		Perfil perfil = radi.getPerfil();
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_dependencia", perfil.getDependencia());
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
		context.setVariable("nombre", personaResponsable.getFullName());
		context.setVariable("cargo", responsableDepe.getCargo());
		context.setVariable("solicitante", radicador.getFullName());
		context.setVariable("func_asig", funcionario.getFullName());
		context.setVariable("observaciones", radi.getObservaciones());
		context.setVariable("depe_resp", Constantes.SECRE_NOTIFICA_NAME);

		int days = 1;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDateTime today = radi.getFechaRadicacion();
		int count = cfg.getValor();

		if (cfg.isBusinessDays()) {
			while (days <= cfg.getValor()) {
				today = today.plusDays(1);

				if (busnessDays.contains(today.format(formatter))) {
					count++;
				}
				today.getDayOfWeek();
				today.getDayOfWeek();
				if (DayOfWeek.SATURDAY == today.getDayOfWeek()
						|| DayOfWeek.SUNDAY == today.getDayOfWeek()) {
					count++;
				}
				days++;
			}
			today = radi.getFechaRadicacion();
			today = today.plusDays(count);
		} else {
			today = today.plusDays(count);
		}

		context.setVariable("fecha_vencimiento", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(today));

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildRadicacionPDFTemplate(Radicacion radi, Cesl_tramite tramite,
			List<Cesl_detalleSolicitud> detalles) throws Exception {

		String nombrePdf = "";
		switch (tramite.getIdtiposolicitud()) {
		case CERTIFICADO_SANCIONES:
			nombrePdf = "RadicacionCDIS";
			break;
		case CERTIFICADO_REPRESENTACION_CAMARAS:
			nombrePdf = "RadicacionCERCC";
			break;
		case CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS:
			nombrePdf = "RadicacionFSCC";
			break;
		case COPIAS_SIMPLES:
			nombrePdf = "RadicacionSC";
			break;
		case LISTADOS_INFORMACION:
			nombrePdf = "RadicacionLI";
			break;
		case CORRECCION_REPRESENTACION_CAMARAS:
			nombrePdf = "RadicacionCCC";
			break;
		default:
			break;
		}
		Context context = new Context();
		AddRadicacion(radi, context);
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("asunto", tramite.getIdtiposolicitud().getDescripcion());
		context.setVariable("url", Utility.getURLConsultaRadicacionWeb(radi));
		Boolean conApostilla = false;
		for (Cesl_detalleSolicitud d : detalles) {
			if (!conApostilla) {
				conApostilla = d.getConApostilla();
			}
			if (d.getVariableAdicional2() == null) {
				d.setVariableAdicional2(Constantes.STR_EMPTY);
			}
			if (tramite.getIdtiposolicitud() == TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS && detalles != null
					&& detalles.size() > 0) {
				if (d.getObservaciones() != null) {
					String fileNameAndSAH1 = d.getObservaciones();
					String fileName = fileNameAndSAH1.split(Constantes.FILENAME_CHECKSUM_SEPARATOR)[0];
					d.setObservaciones(fileName);
				}
			}
		}
		context.setVariable("detalles", detalles);
		context.setVariable("conApostilla", conApostilla);
		context.setVariable("sinApostilla", !conApostilla);
		context.setVariable("fechaSolicitud", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(tramite.getFecha_creacion()));

		return this.templateEngine.process(String.format("PDF/%s.%s", nombrePdf, Constantes.PDF_EXTENSION), context);
	}

	public String buildRespuestaPDFTemplate(Radicacion radi, TipoTramite tipoTramite) throws Exception {
		Context context = new Context();
		AddRadicacion(radi, context);
		AddPersona(radi.getRadicador(), context, null);
		context.setVariable("asunto", tipoTramite.getDescripcion());
		context.setVariable("url", currentURLDomain);
		return this.templateEngine.process(String.format("PDF/Radicacion.SA.%s", Constantes.PDF_EXTENSION), context);
	}

	public String buildEmailTemplate(Radicacion radi, TipoTramite tipoTramite) throws Exception {
		Context context = new Context();
		AddRadicacion(radi, context);
		context.setVariable("asunto", tipoTramite.getDescripcion());
		context.setVariable("url", Utility.getURLConsultaRadicacionWeb(radi));
		return this.templateEngine.process(String.format("EMAIL/Radicacion.%s.EMAIL", radi.getTipoRadicacion()),
				context);
	}

	public String buildEmailTemplateCotizacion(Radicacion radi, TipoTramite tipoTramite) throws Exception {
		Context context = new Context();
		AddRadicacion(radi, context);
		context.setVariable("url", Utility.getURLConsultaRadicacionWeb(radi));
		return this.templateEngine.process(String.format("EMAIL/Cotizacion.%s.EMAIL", radi.getTipoRadicacion()),
				context);
	}

	public String buildEmailTemplateTrasLado(Cesl_tramite tramite, String email) throws Exception {

		Context context = new Context();
		context.setVariable("Solicitante", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 470);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/transladarPorCompetencias.%s.EMAIL", "CS"), context);
	}

	public String buildEmailTemplateRespuestaSolicitud(Cesl_tramite tramite, String email) throws Exception {
		Context context = new Context();
		context.setVariable("Solicitante", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 445);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/RespuestaSolicitud.%s.EMAIL", "SA"), context);

	}
	
	public String buildEmailTemplateTrasladoEntidad(Cesl_tramite tramite, String email, short actuacion) throws Exception {
		Context context = new Context();
		context.setVariable("Solicitante", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", actuacion);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/RespuestaSolicitud.%s.EMAIL", "SA"), context);

	}

	public String buildEmailTemplatePago(Radicacion tramite) throws Exception {

		Context context = new Context();
		context.setVariable("Solicitante", tramite.getRadicador().getFullName());
		context.setVariable("email", tramite.getRadicador().getEmails().get(0).getDescripcion());
		context.setVariable("expediente",
				String.valueOf(tramite.getAnio()) + "-" + String.valueOf(tramite.getNumero()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 445);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/SolicitudPagada.%s.EMAIL", "EN"), context);
	}

	public String buildEmailTemplateProrroga(Cesl_tramite tramite, String email) throws Exception {

		Context context = new Context();
		context.setVariable("Solicitante", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 445);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/Solprorroga.%s.EMAIL", "CS"), context);
	}

	public String buildEmailTemplateCotizacion(Cesl_tramite tramite, String email) throws Exception {

		Context context = new Context();
		context.setVariable("nombre", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 344);
		context.setVariable("fecha", tramite.getFecha_creacion());

		return this.templateEngine.process(String.format("EMAIL/Cotizacion.%s.EMAIL", "SA"), context);
	}

	public String buildEmailTemplateRtaSolicitante(Cesl_tramite tramite, String email) throws Exception {
		Context context = new Context();
		context.setVariable("Solicitante", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 430);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/rtacomplemento.%s.EMAIL", "CS"), context);
	}

	public String buildEmailTemplateComplementar(Cesl_tramite tramite, String email) throws Exception {

		Context context = new Context();
		context.setVariable("Solicitante", tramite.getNombreSolicitante());
		context.setVariable("email", email);
		context.setVariable("expediente",
				String.valueOf(tramite.getAno_radi()) + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("tramite", 362);
		context.setVariable("evento", 0);
		context.setVariable("actuacion", 430);
		context.setVariable("firma", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/complementar.%s.EMAIL", "CS"), context);
	}

	public String buildEmailTemplate(Cesl_tramite tramite, Persona funcionarioAsignado) {
		Context context = new Context();
		context.setVariable("nombre", funcionarioAsignado.getFullName());
		context.setVariable("nume_radi",
				tramite.getAno_radi().toString() + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("fechaasignacion", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(tramite.getFecha_creacion()));
		context.setVariable("quienAsigna", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/Asignacion.%s.EMAIL", "CS"), context);
	}

	public String buildEmailTemplateSolicitudDigitalizacion(Cesl_tramite tramite, Persona funcionarioAsignado,
			ResponsableDepe responsableDepe) {
		Context context = new Context();
		context.setVariable("nombre", funcionarioAsignado.getFullName());
		context.setVariable("nume_radi",
				tramite.getAno_radi().toString() + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("fechaasignacion", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(tramite.getFecha_creacion()));
		context.setVariable("quienAsigna", responsableDepe.getNombreResponsable());
		return this.templateEngine.process(String.format("EMAIL/Asignacion.%s.EMAIL", "CS"), context);
	}

	public String buildEmailTemplateReqAreaInterna(Cesl_tramite tramite, Persona funcionarioAsignado) {
		Context context = new Context();
		context.setVariable("nombre", funcionarioAsignado.getFullName());
		context.setVariable("nume_radi",
				tramite.getAno_radi().toString() + "-" + String.valueOf(tramite.getNume_radi()));
		context.setVariable("fechaasignacion", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(tramite.getFecha_creacion()));
		context.setVariable("quienAsigna", Constantes.SECRE_NOTIFICA_NAME);
		return this.templateEngine.process(String.format("EMAIL/Asignacion.%s.EMAIL", "CS"), context);
	}

	public String buildCertificadoSancionesPDFTemplate(Cesl_detalleSolicitud detalle, TipoSancion tipoSancion,
			List<Sancion> sanciones, LocalDateTime fechaInicial, LocalDateTime fechaRadicacion) throws Exception {
		List<Sancion> demandas = new ArrayList<>();
		List<Sancion> multas = new ArrayList<>();
		for (Sancion s : sanciones) {
			if (s.getTipo() == TipoSancion.Demanda) {
				demandas.add(s);
			} else {
				multas.add(s);
			}
		}
		boolean showMultas = !multas.isEmpty();
		boolean showDemandas = !demandas.isEmpty();

		String periodo = String.format("durante %s %s, periodo comprendido entre el %s y el %s",
				detalle.getAnos() == 1 ? "el" : "los", detalle.getAnos_descripcion().toLowerCase(),
				Utility.DD_MM_YYYY.format(fechaInicial), Utility.DD_MM_YYYY.format(fechaRadicacion));
		String tipoDocDesc = "la cédula de ciudadanía";
		if (null != detalle.getTipo_docu()) {
			switch (detalle.getTipo_docu()) {
			case "NI":
				tipoDocDesc = "el NIT";
				break;
			case "CE":
				tipoDocDesc = "la cédula de extranjeria";
				break;
			case "PA":
				tipoDocDesc = "el pasaporte";
				break;
			default:
				break;
			}
		}
		tipoDocDesc = String.format("%s número %s ", tipoDocDesc, detalle.getNume_docu_descripcion());
		String part = "Una vez revisada la información disponible en el Sistema de Trámites y en las bases de datos ";
		if (tipoSancion == TipoSancion.ProteccionCompetencia) {
			part = "del Grupo para la Protección y Promoción de la Competencia, del Grupo de Prácticas Restrictivas de la Competencia y del Grupo Élite contra Colusiones, ";
			if (showDemandas || showMultas) {
				part += String.format(
						"se encontró que %s presenta las siguientes sanciones por infracciones del régimen de libre competencia económica %s:",
						tipoDocDesc, periodo);
			} else {
				part += String.format(
						"no se encontró ninguna actuación administrativa en la que se le haya impuesto sanción al %s por infracciones del régimen de libre competencia económica %s.",
						tipoDocDesc, periodo);
			}
		} else {
			part = String.format("%s de la entidad, %s", part, tipoDocDesc);
			if (showDemandas || showMultas) {
				part += "presenta la(s) siguiente(s) " + String.valueOf(sanciones.size());
			} else {
				part += "no presenta";
			}
			String temp = " demanda(s), investigacion(es) o sancion(es)";
			if (tipoSancion == TipoSancion.Multa) {
				temp = " sancion(es)";
			} else if (tipoSancion == TipoSancion.Demanda) {
				temp = " demanda(s) y/o investigacion(es)";
			}
			part += String.format("%s en la Superintendencia de Industria y Comercio, %s%s", temp, periodo,
					(showDemandas || showMultas) ? ":" : ".");
		}

		Context context = new Context();
		context.setVariable("texto_informativo", part);
		context.setVariable("showDemandas", showDemandas);
		context.setVariable("showMultas", showMultas);
		context.setVariable("demandas", demandas);
		context.setVariable("multas", multas);
		context.setVariable("dia", String.format("%s (%s)",
				Utility.convertNumberToLetter(fechaRadicacion.getDayOfMonth()), fechaRadicacion.getDayOfMonth()));
		context.setVariable("mes", Utility.MMMMM.format(fechaRadicacion));
		context.setVariable("anio", String.format("%s (%s)", Utility.convertNumberToLetter(fechaRadicacion.getYear()),
				fechaRadicacion.getYear()));
		context.setVariable("nombre_secretario", Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
		context.setVariable("cargo_secretario", Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC);
		context.setVariable("texto_legal", Constantes.TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC);
		context.setVariable("ruta_img_firma", String.format("img/FIRMA_SECRETARIO_AD_HOC_%s.PNG",
				Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));
		return this.templateEngine.process(String.format("PDF/CertificadoDIS.%s", Constantes.PDF_EXTENSION), context);
	}

	public String buildCertificadoCamarasPDFTemplate(CamaraComercio camara, LocalDateTime fechaRadicacion)
			throws Exception {
		PersonaCamara representante = camara.getRepresentante();
		if (representante == null) {
			logger.info("No hay un rlegal activo");
			representante = new PersonaCamara();
			// representante.setFechaActaDelegacion(LocalDate.now());
		}

		String texto = String.format(
				"Que la %s identificada con el NIT %s%s, es una entidad sin ánimo de lucro creada mediante el decreto número %s de fecha %s de %s de %s.",
				camara.getNombre(), camara.getNumeroDocumentoDesc(),
				Utility.isNullOrEmptyTrim(camara.getDigitoVerificacion()) ? Constantes.STR_EMPTY
						: "-" + camara.getDigitoVerificacion(),
				camara.getNumeroDecreto(), camara.getFechaDecreto().getDayOfMonth(),
				Utility.MMMMM.format(camara.getFechaDecreto()), camara.getFechaDecreto().getYear());

		String texto2 = String.format("Que el Representante Legal de la %s es el Presidente Ejecutivo y sus suplentes.",
				camara.getNombre());
		String texto3 = "";
		if (Objects.isNull(representante)) {
			texto3 = String.format(
					"Que fue nombrado(a) como Presidente Ejecutivo de la %s a %s, identificado(a) con la Cédula de Ciudadanía número %s, mediante acta número %s de fecha %s de %s de %s.",
					camara.getNombre(), "null", "null", "null", "null", "null", "null");

		} else {
			texto3 = String.format(
					"Que fue nombrado(a) como Presidente Ejecutivo de la %s a %s, identificado(a) con la Cédula de Ciudadanía número %s, mediante acta número %s de fecha %s de %s de %s.",
					camara.getNombre(), representante.getNombre(), representante.getNumeroDocumentoDesc(),
					representante.getNumeroActaDelegacion(),
					representante.getFechaActaDelegacion() != null
							? representante.getFechaActaDelegacion().getDayOfMonth()
							: null,
					representante.getFechaActaDelegacion() != null
							? Utility.MMMMM.format(representante.getFechaActaDelegacion())
							: null,
					representante.getFechaActaDelegacion() != null ? representante.getFechaActaDelegacion().getYear()
							: null);
		}

		String texto4 = String.format(
				"Que para la %s se delegó como Presidente(s) Ejecutivo(s) Suplente(s) a la(s) siguiente(s) persona(s):",
				camara.getNombre());

		logger.info(texto3);
		Context context = new Context();
		PersonaCamara representanteJudicial = camara.getRepresentanteJudicial();

		if (Objects.nonNull(representanteJudicial)) {

			if (camara.getRepresentanteJudicial().getShowObservaciones().equals("NO")) {
				String texto5 = String.format(
						"Que fue nombrado(a) como Presidente para Asuntos Judiciales de la %s a %s, identificado(a) con la Cédula de Ciudadanía número %s, mediante acta número %s de fecha %s de %s de %s.",
						camara.getNombre(), representanteJudicial.getNombre(),
						representanteJudicial.getNumeroDocumentoDesc(), representanteJudicial.getNumeroActaDelegacion(),
						representanteJudicial.getFechaActaDelegacion().getDayOfMonth(),
						Utility.MMMMM.format(representanteJudicial.getFechaActaDelegacion()),
						representanteJudicial.getFechaActaDelegacion().getYear());
				context.setVariable("texto_informativo5", texto5);
			} else if (!camara.getRepresentanteJudicial().getShowObservaciones().isEmpty()
					&& camara.getRepresentanteJudicial().getShowObservaciones().equals("SI")) {
				context.setVariable("texto_informativo5", representanteJudicial.getObservaciones());
			}
		}

		context.setVariable("texto_informativo", texto);
		context.setVariable("texto_informativo2", texto2);
		context.setVariable("texto_informativo3", texto3);
		context.setVariable("texto_informativo4", texto4);
		context.setVariable("detalles", camara.getSuplentes());
		context.setVariable("dia", String.format("%s (%s)",
				Utility.convertNumberToLetter(fechaRadicacion.getDayOfMonth()), fechaRadicacion.getDayOfMonth()));
		context.setVariable("mes", Utility.MMMMM.format(fechaRadicacion));
		context.setVariable("anio", String.format("%s (%s)", Utility.convertNumberToLetter(fechaRadicacion.getYear()),
				fechaRadicacion.getYear()));
		context.setVariable("nombre_secretario", Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
		context.setVariable("cargo_secretario", Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC);
		context.setVariable("texto_legal", Constantes.TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC);
		context.setVariable("ruta_img_firma", String.format("img/FIRMA_SECRETARIO_AD_HOC_%s.PNG",
				Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));

		return this.templateEngine.process(String.format("PDF/CertificadoCC.%s", Constantes.PDF_EXTENSION), context);
	}

	private void AddRadicacion(Radicacion radi, Context context) {
		context.setVariable("anonimo", radi.getRadicador().getAnonimo());
		context.setVariable("expediente", radi.getFullNumeroRadicacion());
		context.setVariable("fecha", Utility.DD_MM_YYYY_HH_MM_AA_LD.format(radi.getFechaRadicacion()));
		Perfil perfil = radi.getPerfil();
		context.setVariable("codi_tramite", perfil.getTramite());
		context.setVariable("tramite", String.format("%s - %s", perfil.getTramite(), perfil.getNombreTramite()));
		context.setVariable("codi_evento", perfil.getEvento());
		context.setVariable("evento", String.format("%s - %s", perfil.getEvento(), perfil.getNombreEvento()));
		context.setVariable("codi_actuacion", perfil.getActuacion());
		context.setVariable("actuacion", String.format("%s - %s", perfil.getActuacion(), perfil.getNombreActuacion()));
		context.setVariable("codi_dependencia", perfil.getDependencia());
		context.setVariable("dependencia",
				String.format("%s - %s", perfil.getDependencia(), perfil.getNombreDependencia()));
		context.setVariable("folios", radi.getTotalFolios());
	}

	private void AddPersona(Persona pers, Context context, String sufijo) throws Exception {
		if (pers != null) {
			if (sufijo == null) {
				sufijo = Constantes.STR_EMPTY;
			}
			context.setVariable(String.format("nombre%s", sufijo), pers.getFullName());
			if (pers.getAnonimo() == null || !pers.getAnonimo()) {
				context.setVariable(String.format("identificacion%s", sufijo), String.format("%s %s",
						pers.getTipoDocumento(), Utility.tryFormatCurrencyNumber(pers.getNumeroDocumento(), false)));
				Direccion curretnDir = this.getDireccion(pers.getDirecciones());
				context.setVariable(String.format("direccion%s", sufijo),
						curretnDir != null ? curretnDir.getDescripcion() : Constantes.DEFAULT_PDF_NO_DATA_MESSAGE);

				if (curretnDir != null) {
					String region = curretnDir.getCodigoRegionDesc();
					String ciudad = curretnDir.getCodigoCiudadDesc();
					if (!Utility.isNullOrEmptyTrim(ciudad) && !Utility.isNullOrEmptyTrim(region)) {
						region = String.format("%s - %s", region, ciudad);
					} else {
						region = Constantes.STR_EMPTY;
					}
					context.setVariable(String.format("region%s", sufijo), region);
				}

				Telefono currentTel = curretnDir != null && curretnDir.getTelefonos().size() > 0
						? curretnDir.getTelefonos().get(0)
						: null;
				context.setVariable(String.format("telefono%s", sufijo),
						currentTel != null ? currentTel.getNumero() : Constantes.DEFAULT_PDF_NO_DATA_MESSAGE);
			}
			Email currentEmail = pers.getEmails().size() > 0 ? pers.getEmails().get(0) : null;
			context.setVariable(String.format("email%s", sufijo),
					currentEmail != null ? currentEmail.getDescripcion() : Constantes.DEFAULT_PDF_NO_DATA_MESSAGE);
		}
	}
	
	private Direccion getDireccion(List<Direccion> direcciones) {
		Direccion currentDir = null;
		for (Direccion direccion : direcciones) {
		    if ("PE".equals(direccion.getTipo())) {
		        currentDir = direccion;
		        break; 
		    }
		}
		if (currentDir == null && !direcciones.isEmpty()) {
		    currentDir = direcciones.get(0);
		}
		
		return currentDir;
	}
}
