package co.gov.sic.copiasycertificaciones.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.beans.BeanBase;
import co.gov.sic.copiasycertificaciones.enums.TipoAmbienteEnum;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

@Singleton
@Startup
public class Constantes {

	// Parametros cambiar dependiendo del ambiente
	// DESARROLLO
	/**
	 *
	 */
	protected final Logger logger = LoggerFactory.getLogger(BeanBase.class);
	public static String AMBIENTE;

	public static TipoAmbienteEnum AMBIENTE_ACTIVO;// = TipoAmbienteEnum.DESARROLLO

	public static long WS_RADICACION_FUNCIONARIO_RADICADOR_ID;
	public static String WS_INTEROP_USER;
	public static String WS_INTEROP_PASS;
	public static String WS_RECAUDOS_PASS;
	public static String WS_RECAUDOS_USER;
	public static String WS_SIGN_USER;
	public static String WS_SIGN_PASS;
	public static String URL_WS_INTEROP;
	public static String URL_RECAUDOS_BASE;
	public static String URL_WEB_SERVICIOS_EN_LINEA;
	public static String URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO;
	public static String URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO;
	public static String URL_WEB_CONSULTA_TRAMITE;
	public static String URL_WS_SIGN;
	public static int WS_SIGN_ID_POLITICA_CON_ESTAMPA;
	public static int WS_SIGN_ID_POLITICA_SIN_ESTAMPA;
	public static String WS_SIGN_NOMBRE_SECRETARIO_AD_HOC;
	public static String TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC;

	public static String WS_SIGN_CARGO_SECRETARIO_AD_HOC;
	public static String WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC;
	public static String URL_SONDA;
	public static String PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE;

	public static String PATH_ARCHIVOS_OTROS_FORMULARIOS;

	public static String MAIL_HOST;
	public static String MAIL_FROM;
	public static boolean SEND_MAIL_ENABLE;

	public static double MAX_SIZE_ATTACHMENTS;

	public static String UTF_8;
	public static String CONNECTION_STRING_JNDI;
	public static String TIPO_RADICACION_ENTRADA;
	public static String TIPO_RADICACION_SALIDA;
	public static String COD_SISTEMA_SERVICIOS_LINEA;
	public static String MEDIO_RESPUESTA_ELECTRONICO;
	public static String LLAVE_SESION_USUARIO;
	public static String DEFAULT_MIME_TYPE;
	public static String PDF_MIME_TYPE;
	public static String SENDFILE_HEADER;
	// Extensiones, deben ser mayusculas, sino, el servidor de apache no encuentra
	// los archivos por que es case sensitive
	public static String PDF_EXTENSION;
	public static String ZIP_EXTENSION;

	public static int DEFAULT_SENDFILE_BUFFER_SIZE;
	public static String ROL_REPRESENTANTE_LEGAL;
	public static String ROL_SUPLENTE;
	public static String ROL_SECRETARIO;
	public static String ROL_REPRESENTANTE_JUDICIAL;

	public static String TIPO_PERSONA_EMPRESA;
	public static String TIPO_PERSONA_NATURAL;
	public static String TIPO_PERSONA_FUNCIONARIO;
	public static int ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE;

	public static char ENCODE_PAD_CHAR1;
	public static char ENCODE_PAD_CHAR2;
	public static char ENCODE_PAD_CHAR3;

	// Formatos de Fecha
	public static String DATE_TIME_FORMAT_DD_MM_YYYY;
	public static String DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM;
	public static String DATE_TIME_FORMAT_YYYY_MM_DD;
	public static String DATE_TIME_FORMAT_YYYYMMDD;
	public static String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS;
	public static String DATE_TIME_FORMAT_DD_MMM_YYYY;
	public static String DATE_TIME_FORMAT_D_MMM_YYYY;
	public static String DATE_TIME_FORMAT_DD_MMMM_YYYY;
	public static String DATE_TIME_FORMAT_D_MMMM_YYYY;
	public static String DATE_TIME_FORMAT_MMMM;
	// Nombres Tramites
	public static String NOMBRE_TRAMITE_CERTIFICADO_SANCIONES;
	public static String NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS;
	public static String NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS;
	public static String NOMBRE_TRAMITE_COPIAS_SIMPLES;
	public static String NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS;
	public static String NOMBRE_TRAMITE_LISTADOS_INFORMACION;
	// Localizacion
	public static String ES_CO;
	public static Locale LOCALE_ES_CO;

	// Estados de la pasarela
	public static String ESTADO_TRAMITE_CREATED;
	public static String ESTADO_TRAMITE_PENDING;
	public static String ESTADO_TRAMITE_OK;
	public static String ESTADO_TRAMITE_FAILED;
	public static String ESTADO_TRAMITE_EXPIRED;
	public static String ESTADO_TRAMITE_CAPTURED;
	public static String ESTADO_TRAMITE_NOT_AUTHORIZED;
	public static String ESTADO_TRAMITE_CREATED_DESC;
	public static String ESTADO_TRAMITE_PENDING_DESC;
	public static String ESTADO_TRAMITE_OK_DESC;
	public static String ESTADO_TRAMITE_FAILED_DESC;
	public static String ESTADO_TRAMITE_EXPIRED_DESC;
	public static String ESTADO_TRAMITE_CAPTURED_DESC;
	public static String ESTADO_TRAMITE_NOT_AUTHORIZED_DESC;
	// Estados de la aplicacion
	public static String ESTADO_TRAMITE_PRESENTADO;
	public static String ESTADO_TRAMITE_RADICADO_ENTRADA;
	public static String ESTADO_TRAMITE_RADICADO_SALIDA;
	public static String ESTADO_TRAMITE_CERTIFICADO_GENERADO;
	public static String ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO;
	public static String ESTADO_TRAMITE_RECIBO_CAJA;
	public static String ESTADO_TRAMITE_FINALIZADO;
	public static String ESTADO_TRAMITE_ASIGNADA;
	public static String ESTADO_TRAMITE_COMPLEMENTAR;
	public static String ESTADO_TRAMITE_DESISTIDA;
	public static String ESTADO_TRAMITE_TRASLADO_COMPETENCIA;
	public static String ESTADO_TRAMITE_RTA_SOLICITANTE;
	public static String ESTADO_TRAMITE_SOL_PRORROGA;
	public static String ESTADO_TRAMITE_RTA_SOL_PRORROGA;
	public static String ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA;
	public static String ESTADO_TRAMITE_RTA_SOL_DIGI_INFO;
	public static String ESTADO_TRAMITE_COTIZACION_ENVIADA;
	public static String ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA;
	public static String ESTADO_TRAMITE_PAGADO;
	public static String ESTADO_TRAMITE_FIRMA_ELECTRONICA;
	public static Short CODI_ACTUACION_ASIGNACION;
	public static Short CODI_ACTUACION_TRASLADO;
	// PSE
	public static int CODIGO_PSE;
	public static String RECAUDOS_TIPO_PAGO_PSE;
	public static String RECAUDOS_COD_BANCO_DE_BOGOTA;
	public static String RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL;
	public static String RECAUDOS_NUMERO_CUENTA;

	public static int WS_RADICACION_CONS_TASA;
	public static String WS_RADICACION_MEDIO_ENTRADA;
	public static String WS_RADICACION_MEDIO_SALIDA;
	public static String CONTENT_TYPE_JSON;
	public static String CONTENT_TYPE_HTML;
	public static String HTTP_METHOD_GET;
	public static String HTTP_METHOD_POST;
	public static String STR_EMPTY = "";

	public static String DEFAULT_PDF_NO_DATA_MESSAGE;
	public static String AUTHOR;
	public static String KEYWORDS_PDF_RADICACION;
	public static String KEYWORDS_PDF_DEMANDAS;
	public static String KEYWORDS_PDF_CAMARAS;
	public static String KEYWORDS_PDF_FIRMA_SECRETARIO;
	public static String URL_WS_RECAUDOS;

	public static String URL_DOWNLOAD_RECIBO_RECAUDOS;

	public static String[] UNIDADES = { "", "Un ", "Dos ", "Tres ", "Cuatro ", "Cinco ", "Seis ", "Siete ", "Ocho ",
			"Nueve ", "Diez ", "Once ", "Doce ", "Trece ", "Catorce ", "Quince ", "Dieciséis", "Diecisiete",
			"Dieciocho", "Diecinueve", "Veinte" };
	public static String[] DECENAS = { "Veinti", "Treinta ", "Cuarenta ", "Cincuenta ", "Sesenta ", "Setenta ",
			"Ochenta ", "Noventa ", "Cien " };
	public static String[] CENTENAS = { "Ciento ", "Doscientos ", "Trescientos ", "Cuatrocientos ", "Quinientos ",
			"Seiscientos ", "Setecientos ", "Ochocientos ", "Novecientos " };

	public static String URL_PROTOCOL_HTTPS;
	public static String URL_SIC;
	public static String URL_DATOS_PERSONALES;
	public static String URL_ENCUESTA;

	public static String CERTIFICATE_SIGNER_CC_FIELD_NAME;
	public static String CERTIFICATE_SIGNER_NIT_FIELD_NAME;

	public static String COORDINADOR_SCC;
	public static String RESPONSABLE_SCC;
	public static String VENTANILLA_SE;
	public static String SECRE_NOTIFICA_NAME;
	public static String FILENAME_CHECKSUM_SEPARATOR;
	public static String EMAIL_SOPORTE;

	public static String WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL;
	public static String WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL;
	public static float PDF_FOOTER_FONT_SIZE;
	public static String PDF_APOSTILLE_SUFIX;

	public static String URL_VISOR;
	
	public Constantes() {
		
	}

	@PostConstruct
    public void init() {
		FileInputStream fis = null;
		try {

			fis = new FileInputStream(new File(
					System.getProperty("jboss.server.config.dir") + "/copiascertificaciones/Constans.properties"));
			// fis = new FileInputStream(new File("C:\\Works\\SIC\\COPIAS
			// PRODUCCION\\adm-web-copias-certificaciones\\Constans.DEV.properties"));
			Properties props = new Properties();
			props.load(new InputStreamReader(fis, Charset.forName("UTF-8")));
			AMBIENTE = props.getProperty("AMBIENTE_ACTIVO");

			if ("DE".equals(AMBIENTE)) {
				AMBIENTE_ACTIVO = TipoAmbienteEnum.DESARROLLO;
			} else if ("QA".equals(AMBIENTE)) {
				AMBIENTE_ACTIVO = TipoAmbienteEnum.PRUEBAS;
			} else {
				AMBIENTE_ACTIVO = TipoAmbienteEnum.PRODUCCION;
			}

			WS_RADICACION_FUNCIONARIO_RADICADOR_ID = Long
					.parseLong(props.getProperty("WS_RADICACION_FUNCIONARIO_RADICADOR_ID"));
			WS_INTEROP_USER = props.getProperty("WS_INTEROP_USER");
			WS_INTEROP_PASS = props.getProperty("WS_INTEROP_PASS");
			WS_RECAUDOS_PASS = props.getProperty("WS_RECAUDOS_PASS");
			WS_RECAUDOS_USER = props.getProperty("WS_RECAUDOS_USER");
			WS_SIGN_USER = props.getProperty("WS_SIGN_USER");
			WS_SIGN_PASS = props.getProperty("WS_SIGN_PASS");
			URL_WS_INTEROP = props.getProperty("URL_WS_INTEROP");
			URL_RECAUDOS_BASE = props.getProperty("URL_RECAUDOS_BASE");
			URL_WEB_SERVICIOS_EN_LINEA = props.getProperty("URL_WEB_SERVICIOS_EN_LINEA");
			URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO = props.getProperty("URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO");
			URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO = props
					.getProperty("URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO");
			URL_WEB_CONSULTA_TRAMITE = props.getProperty("URL_WEB_CONSULTA_TRAMITE");
			URL_WS_SIGN = props.getProperty("URL_WS_SIGN");
			WS_SIGN_ID_POLITICA_CON_ESTAMPA = Integer.parseInt(props.getProperty("WS_SIGN_ID_POLITICA_CON_ESTAMPA"));
			WS_SIGN_ID_POLITICA_SIN_ESTAMPA = Integer.parseInt(props.getProperty("WS_SIGN_ID_POLITICA_SIN_ESTAMPA"));
			WS_SIGN_NOMBRE_SECRETARIO_AD_HOC = props.getProperty("WS_SIGN_NOMBRE_SECRETARIO_AD_HOC");
			TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC = props
					.getProperty("TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC");
			WS_SIGN_CARGO_SECRETARIO_AD_HOC = props.getProperty("WS_SIGN_CARGO_SECRETARIO_AD_HOC");
			WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC = props
					.getProperty("WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC");
			URL_SONDA = props.getProperty("URL_SONDA");
			PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE = String.format(
					props.getProperty("PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE"),
					AMBIENTE_ACTIVO.toString().substring(0, 4));

			PATH_ARCHIVOS_OTROS_FORMULARIOS = String.format(props.getProperty("PATH_ARCHIVOS_OTROS_FORMULARIOS"),
					AMBIENTE_ACTIVO.toString().substring(0, 4));

			MAIL_HOST = props.getProperty("MAIL_HOST");
			MAIL_FROM = props.getProperty("MAIL_FROM");
			SEND_MAIL_ENABLE = Boolean.parseBoolean(props.getProperty("SEND_MAIL_ENABLE"));
			MAX_SIZE_ATTACHMENTS = Double.parseDouble(props.getProperty("MAX_SIZE_ATTACHMENTS"));

			UTF_8 = props.getProperty("UTF_8");

			CONNECTION_STRING_JNDI = props.getProperty("CONNECTION_STRING_JNDI");
			TIPO_RADICACION_ENTRADA = props.getProperty("TIPO_RADICACION_ENTRADA");
			TIPO_RADICACION_SALIDA = props.getProperty("TIPO_RADICACION_SALIDA");
			COD_SISTEMA_SERVICIOS_LINEA = props.getProperty("COD_SISTEMA_SERVICIOS_LINEA");
			MEDIO_RESPUESTA_ELECTRONICO = props.getProperty("MEDIO_RESPUESTA_ELECTRONICO");
			LLAVE_SESION_USUARIO = props.getProperty("LLAVE_SESION_USUARIO");
			DEFAULT_MIME_TYPE = props.getProperty("DEFAULT_MIME_TYPE");
			PDF_MIME_TYPE = props.getProperty("PDF_MIME_TYPE");
			SENDFILE_HEADER = props.getProperty("SENDFILE_HEADER") + UTF_8 + props.getProperty("SENDFILE_HEADER_END");

			PDF_EXTENSION = props.getProperty("PDF_EXTENSION");
			ZIP_EXTENSION = props.getProperty("ZIP_EXTENSION");

			DEFAULT_SENDFILE_BUFFER_SIZE = Integer.parseInt(props.getProperty("DEFAULT_SENDFILE_BUFFER_SIZE"));
			ROL_REPRESENTANTE_LEGAL = props.getProperty("ROL_REPRESENTANTE_LEGAL");
			ROL_SUPLENTE = props.getProperty("ROL_SUPLENTE");
			ROL_SECRETARIO = props.getProperty("ROL_SECRETARIO");
			ROL_REPRESENTANTE_JUDICIAL = props.getProperty("ROL_REPRESENTANTE_JUDICIAL");

			TIPO_PERSONA_EMPRESA = props.getProperty("TIPO_PERSONA_EMPRESA");
			TIPO_PERSONA_NATURAL = props.getProperty("TIPO_PERSONA_NATURAL");
			TIPO_PERSONA_FUNCIONARIO = props.getProperty("TIPO_PERSONA_FUNCIONARIO");
			ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE = Integer
					.parseInt(props.getProperty("ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE"));

			ENCODE_PAD_CHAR1 = props.getProperty("ENCODE_PAD_CHAR1").charAt(0);
			ENCODE_PAD_CHAR2 = props.getProperty("ENCODE_PAD_CHAR2").charAt(0);
			ENCODE_PAD_CHAR3 = props.getProperty("ENCODE_PAD_CHAR3").charAt(0);

			DATE_TIME_FORMAT_DD_MM_YYYY = props.getProperty("DATE_TIME_FORMAT_DD_MM_YYYY");
			DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM = props.getProperty("DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM");
			DATE_TIME_FORMAT_YYYY_MM_DD = props.getProperty("DATE_TIME_FORMAT_YYYY_MM_DD");
			DATE_TIME_FORMAT_YYYYMMDD = props.getProperty("DATE_TIME_FORMAT_YYYYMMDD");
			DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS = props.getProperty("DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS");
			DATE_TIME_FORMAT_DD_MMM_YYYY = props.getProperty("DATE_TIME_FORMAT_DD_MMM_YYYY");
			DATE_TIME_FORMAT_D_MMM_YYYY = props.getProperty("DATE_TIME_FORMAT_D_MMM_YYYY");
			DATE_TIME_FORMAT_DD_MMMM_YYYY = props.getProperty("DATE_TIME_FORMAT_DD_MMMM_YYYY");
			DATE_TIME_FORMAT_D_MMMM_YYYY = props.getProperty("DATE_TIME_FORMAT_D_MMMM_YYYY");
			DATE_TIME_FORMAT_MMMM = props.getProperty("DATE_TIME_FORMAT_MMMM");

			NOMBRE_TRAMITE_CERTIFICADO_SANCIONES = props.getProperty("NOMBRE_TRAMITE_CERTIFICADO_SANCIONES");
			NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS = props
					.getProperty("NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS");
			NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS = props
					.getProperty("NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS");
			NOMBRE_TRAMITE_COPIAS_SIMPLES = props.getProperty("NOMBRE_TRAMITE_COPIAS_SIMPLES");
			NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS = props
					.getProperty("NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS");
			NOMBRE_TRAMITE_LISTADOS_INFORMACION = props.getProperty("NOMBRE_TRAMITE_LISTADOS_INFORMACION");

			ES_CO = props.getProperty("ES_CO");
			LOCALE_ES_CO = Locale.forLanguageTag(ES_CO);

			ESTADO_TRAMITE_CREATED = props.getProperty("ESTADO_TRAMITE_CREATED");
			ESTADO_TRAMITE_PENDING = props.getProperty("ESTADO_TRAMITE_PENDING");
			ESTADO_TRAMITE_OK = props.getProperty("ESTADO_TRAMITE_OK");
			ESTADO_TRAMITE_FAILED = props.getProperty("ESTADO_TRAMITE_FAILED");
			ESTADO_TRAMITE_EXPIRED = props.getProperty("ESTADO_TRAMITE_EXPIRED");
			ESTADO_TRAMITE_CAPTURED = props.getProperty("ESTADO_TRAMITE_CAPTURED");
			ESTADO_TRAMITE_NOT_AUTHORIZED = props.getProperty("ESTADO_TRAMITE_NOT_AUTHORIZED");
			ESTADO_TRAMITE_CREATED_DESC = props.getProperty("ESTADO_TRAMITE_CREATED_DESC");
			ESTADO_TRAMITE_PENDING_DESC = props.getProperty("ESTADO_TRAMITE_PENDING_DESC");
			ESTADO_TRAMITE_OK_DESC = props.getProperty("ESTADO_TRAMITE_OK_DESC");
			ESTADO_TRAMITE_FAILED_DESC = props.getProperty("ESTADO_TRAMITE_FAILED_DESC");
			ESTADO_TRAMITE_EXPIRED_DESC = props.getProperty("ESTADO_TRAMITE_EXPIRED_DESC");
			ESTADO_TRAMITE_CAPTURED_DESC = props.getProperty("ESTADO_TRAMITE_CAPTURED_DESC");
			ESTADO_TRAMITE_NOT_AUTHORIZED_DESC = props.getProperty("ESTADO_TRAMITE_NOT_AUTHORIZED_DESC");
			ESTADO_TRAMITE_PRESENTADO = props.getProperty("ESTADO_TRAMITE_PRESENTADO");
			ESTADO_TRAMITE_RADICADO_ENTRADA = props.getProperty("ESTADO_TRAMITE_RADICADO_ENTRADA");
			ESTADO_TRAMITE_RADICADO_SALIDA = props.getProperty("ESTADO_TRAMITE_RADICADO_SALIDA");
			ESTADO_TRAMITE_CERTIFICADO_GENERADO = props.getProperty("ESTADO_TRAMITE_CERTIFICADO_GENERADO");
			ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO = props
					.getProperty("ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO");
			ESTADO_TRAMITE_RECIBO_CAJA = props.getProperty("ESTADO_TRAMITE_RECIBO_CAJA");
			ESTADO_TRAMITE_FINALIZADO = props.getProperty("ESTADO_TRAMITE_FINALIZADO");
			ESTADO_TRAMITE_ASIGNADA = props.getProperty("ESTADO_TRAMITE_ASIGNADA");
			ESTADO_TRAMITE_COMPLEMENTAR = props.getProperty("ESTADO_TRAMITE_COMPLEMENTAR");
			ESTADO_TRAMITE_DESISTIDA = props.getProperty("ESTADO_TRAMITE_DESISTIDA");
			ESTADO_TRAMITE_TRASLADO_COMPETENCIA = props.getProperty("ESTADO_TRAMITE_TRASLADO_COMPETENCIA");
			ESTADO_TRAMITE_RTA_SOLICITANTE = props.getProperty("ESTADO_TRAMITE_RTA_SOLICITANTE");
			ESTADO_TRAMITE_SOL_PRORROGA = props.getProperty("ESTADO_TRAMITE_SOL_PRORROGA");
			ESTADO_TRAMITE_RTA_SOL_PRORROGA = props.getProperty("ESTADO_TRAMITE_RTA_SOL_PRORROGA");
			ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA = props.getProperty("ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA");
			ESTADO_TRAMITE_RTA_SOL_DIGI_INFO = props.getProperty("ESTADO_TRAMITE_RTA_SOL_DIGI_INFO");
			ESTADO_TRAMITE_COTIZACION_ENVIADA = props.getProperty("ESTADO_TRAMITE_COTIZACION_ENVIADA");
			ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA = props.getProperty("ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA");
			ESTADO_TRAMITE_PAGADO = props.getProperty("ESTADO_TRAMITE_PAGADO");
			ESTADO_TRAMITE_FIRMA_ELECTRONICA = props.getProperty("ESTADO_TRAMITE_FIRMA_ELECTRONICA");
			CODI_ACTUACION_ASIGNACION = Short.parseShort(props.getProperty("CODI_ACTUACION_ASIGNACION"));
			CODI_ACTUACION_TRASLADO = Short.parseShort(props.getProperty("CODI_ACTUACION_TRASLADO"));

			CODIGO_PSE = Integer.parseInt(props.getProperty("CODIGO_PSE"));
			RECAUDOS_TIPO_PAGO_PSE = props.getProperty("RECAUDOS_TIPO_PAGO_PSE");
			RECAUDOS_COD_BANCO_DE_BOGOTA = props.getProperty("RECAUDOS_COD_BANCO_DE_BOGOTA");
			RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL = props
					.getProperty("RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL");
			RECAUDOS_NUMERO_CUENTA = props.getProperty("RECAUDOS_NUMERO_CUENTA");

			WS_RADICACION_CONS_TASA = Integer.parseInt(props.getProperty("WS_RADICACION_CONS_TASA"));
			WS_RADICACION_MEDIO_ENTRADA = props.getProperty("WS_RADICACION_MEDIO_ENTRADA");
			WS_RADICACION_MEDIO_SALIDA = props.getProperty("WS_RADICACION_MEDIO_SALIDA");
			CONTENT_TYPE_JSON = props.getProperty("CONTENT_TYPE_JSON") + UTF_8;
			CONTENT_TYPE_HTML = props.getProperty("CONTENT_TYPE_HTML") + UTF_8;
			HTTP_METHOD_GET = props.getProperty("HTTP_METHOD_GET");
			HTTP_METHOD_POST = props.getProperty("HTTP_METHOD_POST");

			DEFAULT_PDF_NO_DATA_MESSAGE = props.getProperty("DEFAULT_PDF_NO_DATA_MESSAGE");
			AUTHOR = props.getProperty("AUTHOR");
			KEYWORDS_PDF_RADICACION = props.getProperty("KEYWORDS_PDF_RADICACION");
			KEYWORDS_PDF_DEMANDAS = props.getProperty("KEYWORDS_PDF_DEMANDAS");
			KEYWORDS_PDF_CAMARAS = props.getProperty("KEYWORDS_PDF_CAMARAS");
			KEYWORDS_PDF_FIRMA_SECRETARIO = props.getProperty("KEYWORDS_PDF_FIRMA_SECRETARIO");
			URL_WS_RECAUDOS = URL_RECAUDOS_BASE + props.getProperty("URL_WS_RECAUDOS");

			URL_DOWNLOAD_RECIBO_RECAUDOS = URL_RECAUDOS_BASE + props.getProperty("URL_DOWNLOAD_RECIBO_RECAUDOS_1")
					+ WS_RECAUDOS_USER + props.getProperty("URL_DOWNLOAD_RECIBO_RECAUDOS_2") + WS_RECAUDOS_PASS;

			URL_PROTOCOL_HTTPS = props.getProperty("URL_PROTOCOL_HTTPS");
			URL_SIC = URL_PROTOCOL_HTTPS + props.getProperty("URL_SIC");
			URL_DATOS_PERSONALES = URL_SIC + props.getProperty("URL_DATOS_PERSONALES");
			URL_ENCUESTA = props.getProperty("URL_ENCUESTA");

			CERTIFICATE_SIGNER_CC_FIELD_NAME = props.getProperty("CERTIFICATE_SIGNER_CC_FIELD_NAME");
			CERTIFICATE_SIGNER_NIT_FIELD_NAME = props.getProperty("CERTIFICATE_SIGNER_NIT_FIELD_NAME");

			COORDINADOR_SCC = props.getProperty("COORDINADOR_SCC");
			RESPONSABLE_SCC = props.getProperty("RESPONSABLE_SCC");
			VENTANILLA_SE = props.getProperty("VENTANILLA_SE");
			SECRE_NOTIFICA_NAME = props.getProperty("SECRE_NOTIFICA_NAME");
			FILENAME_CHECKSUM_SEPARATOR = props.getProperty("FILENAME_CHECKSUM_SEPARATOR");
			EMAIL_SOPORTE = props.getProperty("EMAIL_SOPORTE");

			WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL = props
					.getProperty("WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL");
			WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL = props.getProperty("WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL");
			PDF_FOOTER_FONT_SIZE = Float.parseFloat(props.getProperty("PDF_FOOTER_FONT_SIZE"));
			PDF_APOSTILLE_SUFIX = props.getProperty("PDF_APOSTILLE_SUFIX");
			URL_VISOR = Objects.isNull(props.getProperty("URL_VISOR"))
					? "http://visordocs.sic.gov.co:8080/consultaDocs/visor.jsf?ano_radi=%s&nume_radi=%s&cont_radi=%s&cons_radi=%s"
					: props.getProperty("URL_VISOR");

			logger.info(AMBIENTE);
			logger.info(String.valueOf(WS_RADICACION_FUNCIONARIO_RADICADOR_ID));
			logger.info(WS_INTEROP_USER);
			logger.info(WS_INTEROP_PASS);
			logger.info(WS_RECAUDOS_PASS);
			logger.info(WS_RECAUDOS_USER);
			logger.info(WS_SIGN_USER);
			logger.info(WS_SIGN_PASS);
			logger.info(URL_WS_INTEROP);
			logger.info(URL_RECAUDOS_BASE);
			logger.info(URL_WEB_SERVICIOS_EN_LINEA);
			logger.info(URL_WEB_SERVICIOS_EN_LINEA_CREAR_USUARIO);
			logger.info(URL_WEB_SERVICIOS_EN_LINEA_RECORDAR_USUARIO);
			logger.info(URL_WEB_CONSULTA_TRAMITE);
			logger.info(URL_WS_SIGN);
			logger.info(String.valueOf(WS_SIGN_ID_POLITICA_CON_ESTAMPA));
			logger.info(String.valueOf(WS_SIGN_ID_POLITICA_SIN_ESTAMPA));
			logger.info(WS_SIGN_NOMBRE_SECRETARIO_AD_HOC);
			logger.info(TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC);
			logger.info(WS_SIGN_CARGO_SECRETARIO_AD_HOC);
			logger.info(WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC);
			logger.info(URL_SONDA);
			logger.info(PATH_ARCHIVOS_FORMULARIO_COPIAS_DRIVE);
			logger.info(PATH_ARCHIVOS_OTROS_FORMULARIOS);
			logger.info(MAIL_HOST);
			logger.info(MAIL_FROM);
			logger.info(String.valueOf(SEND_MAIL_ENABLE));
			logger.info(String.valueOf(MAX_SIZE_ATTACHMENTS));
			logger.info(UTF_8);
			logger.info(CONNECTION_STRING_JNDI);
			logger.info(TIPO_RADICACION_ENTRADA);
			logger.info(TIPO_RADICACION_SALIDA);
			logger.info(COD_SISTEMA_SERVICIOS_LINEA);
			logger.info(MEDIO_RESPUESTA_ELECTRONICO);
			logger.info(LLAVE_SESION_USUARIO);
			logger.info(DEFAULT_MIME_TYPE);
			logger.info(PDF_MIME_TYPE);
			logger.info(SENDFILE_HEADER);
			logger.info(PDF_EXTENSION);
			logger.info(ZIP_EXTENSION);
			logger.info(String.valueOf(DEFAULT_SENDFILE_BUFFER_SIZE));
			logger.info(ROL_REPRESENTANTE_LEGAL);
			logger.info(ROL_REPRESENTANTE_JUDICIAL);
			logger.info(ROL_SUPLENTE);
			logger.info(ROL_SECRETARIO);
			logger.info(TIPO_PERSONA_EMPRESA);
			logger.info(TIPO_PERSONA_NATURAL);
			logger.info(TIPO_PERSONA_FUNCIONARIO);
			logger.info(String.valueOf(ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE));
			logger.info(String.valueOf(ENCODE_PAD_CHAR1));
			logger.info(String.valueOf(ENCODE_PAD_CHAR2));
			logger.info(String.valueOf(ENCODE_PAD_CHAR3));
			logger.info(DATE_TIME_FORMAT_DD_MM_YYYY);
			logger.info(DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM);
			logger.info(DATE_TIME_FORMAT_YYYY_MM_DD);
			logger.info(DATE_TIME_FORMAT_YYYYMMDD);
			logger.info(DATE_TIME_FORMAT_YYYY_MM_DD_HH_MM_SS);
			logger.info(DATE_TIME_FORMAT_DD_MMM_YYYY);
			logger.info(DATE_TIME_FORMAT_D_MMM_YYYY);
			logger.info(DATE_TIME_FORMAT_DD_MMMM_YYYY);
			logger.info(DATE_TIME_FORMAT_D_MMMM_YYYY);
			logger.info(DATE_TIME_FORMAT_MMMM);
			logger.info(NOMBRE_TRAMITE_CERTIFICADO_SANCIONES);
			logger.info(NOMBRE_TRAMITE_CERTIFICADO_REPRESENTACION_CAMARAS);
			logger.info(NOMBRE_TRAMITE_CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS);
			logger.info(NOMBRE_TRAMITE_COPIAS_SIMPLES);
			logger.info(NOMBRE_TRAMITE_CORRECCION_REPRESENTACION_CAMARAS);
			logger.info(NOMBRE_TRAMITE_LISTADOS_INFORMACION);
			logger.info(ES_CO);
			logger.info(ESTADO_TRAMITE_CREATED);
			logger.info(ESTADO_TRAMITE_PENDING);
			logger.info(ESTADO_TRAMITE_OK);
			logger.info(ESTADO_TRAMITE_FAILED);
			logger.info(ESTADO_TRAMITE_EXPIRED);
			logger.info(ESTADO_TRAMITE_CAPTURED);
			logger.info(ESTADO_TRAMITE_NOT_AUTHORIZED);
			logger.info(ESTADO_TRAMITE_CREATED_DESC);
			logger.info(ESTADO_TRAMITE_PENDING_DESC);
			logger.info(ESTADO_TRAMITE_OK_DESC);
			logger.info(ESTADO_TRAMITE_FAILED_DESC);
			logger.info(ESTADO_TRAMITE_EXPIRED_DESC);
			logger.info(ESTADO_TRAMITE_CAPTURED_DESC);
			logger.info(ESTADO_TRAMITE_NOT_AUTHORIZED_DESC);
			logger.info(ESTADO_TRAMITE_PRESENTADO);
			logger.info(ESTADO_TRAMITE_RADICADO_ENTRADA);
			logger.info(ESTADO_TRAMITE_RADICADO_SALIDA);
			logger.info(ESTADO_TRAMITE_CERTIFICADO_GENERADO);
			logger.info(ESTADO_TRAMITE_PENDIENTE_CONFIRMACION_PAGO);
			logger.info(ESTADO_TRAMITE_RECIBO_CAJA);
			logger.info(ESTADO_TRAMITE_FINALIZADO);
			logger.info(ESTADO_TRAMITE_ASIGNADA);
			logger.info(ESTADO_TRAMITE_COMPLEMENTAR);
			logger.info(ESTADO_TRAMITE_DESISTIDA);
			logger.info(ESTADO_TRAMITE_TRASLADO_COMPETENCIA);
			logger.info(ESTADO_TRAMITE_RTA_SOLICITANTE);
			logger.info(ESTADO_TRAMITE_SOL_PRORROGA);
			logger.info(ESTADO_TRAMITE_RTA_SOL_PRORROGA);
			logger.info(ESTADO_TRAMITE_RTA_SOL_INFO_AREA_INTERNA);
			logger.info(ESTADO_TRAMITE_RTA_SOL_DIGI_INFO);
			logger.info(ESTADO_TRAMITE_COTIZACION_ENVIADA);
			logger.info(ESTADO_TRAMITE_RTA_INFO_AREA_INTERNA);
			logger.info(ESTADO_TRAMITE_PAGADO);
			logger.info(String.valueOf(CODI_ACTUACION_ASIGNACION));
			logger.info(String.valueOf(CODI_ACTUACION_TRASLADO));
			logger.info(String.valueOf(CODIGO_PSE));
			logger.info(RECAUDOS_TIPO_PAGO_PSE);
			logger.info(RECAUDOS_COD_BANCO_DE_BOGOTA);
			logger.info(RECAUDOS_COD_SUCURSAL_CENTRO_INTERNACIONAL);
			logger.info(RECAUDOS_NUMERO_CUENTA);
			logger.info(String.valueOf(WS_RADICACION_CONS_TASA));
			logger.info(WS_RADICACION_MEDIO_ENTRADA);
			logger.info(WS_RADICACION_MEDIO_SALIDA);
			logger.info(CONTENT_TYPE_JSON);
			logger.info(CONTENT_TYPE_HTML);
			logger.info(HTTP_METHOD_GET);
			logger.info(HTTP_METHOD_POST);
			logger.info(DEFAULT_PDF_NO_DATA_MESSAGE);
			logger.info(AUTHOR);
			logger.info(KEYWORDS_PDF_RADICACION);
			logger.info(KEYWORDS_PDF_DEMANDAS);
			logger.info(KEYWORDS_PDF_CAMARAS);
			logger.info(KEYWORDS_PDF_FIRMA_SECRETARIO);
			logger.info(URL_WS_RECAUDOS);
			logger.info(URL_DOWNLOAD_RECIBO_RECAUDOS);
			logger.info(URL_PROTOCOL_HTTPS);
			logger.info(URL_SIC);
			logger.info(URL_DATOS_PERSONALES);
			logger.info(URL_ENCUESTA);
			logger.info(CERTIFICATE_SIGNER_CC_FIELD_NAME);
			logger.info(CERTIFICATE_SIGNER_NIT_FIELD_NAME);
			logger.info(COORDINADOR_SCC);
			logger.info(RESPONSABLE_SCC);
			logger.info(VENTANILLA_SE);
			logger.info(SECRE_NOTIFICA_NAME);
			logger.info(FILENAME_CHECKSUM_SEPARATOR);
			logger.info(EMAIL_SOPORTE);
			logger.info(WS_CANCILLERIA_CODIGO_CERTIFICADO_EXISTENCIA_REPRESENTACION_LEGAL);
			logger.info(WS_CANCILLERIA_NOMBRE_CAMPO_FIRMA_DIGITAL);
			logger.info(String.valueOf(PDF_FOOTER_FONT_SIZE));
			logger.info(PDF_APOSTILLE_SUFIX);
			logger.info(URL_VISOR);

		} catch (FileNotFoundException ex) {
			logger.error("Archivo no encontrado: {}", ex.getMessage(), ex);
			logger.error(ex.getLocalizedMessage());
		} catch (IOException ex) {
			logger.error("Error de I/O: {}", ex.getMessage(), ex);
		} finally {
			try {
				fis.close();
			} catch (IOException ex) {
				logger.error("Error al cerrar el FileInputStream: {}", ex.getMessage(), ex);
			}
		}

	}

}
