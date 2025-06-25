package co.gov.sic.copiasycertificaciones.dataaccess;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.gov.sic.copiasycertificaciones.entities.CamaraComercio;
import co.gov.sic.copiasycertificaciones.entities.CamarasDetalle;
import co.gov.sic.copiasycertificaciones.entities.Cesl_PersonaCamara;
import co.gov.sic.copiasycertificaciones.entities.Cesl_config;
import co.gov.sic.copiasycertificaciones.entities.Cesl_cotizacion;
import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.entities.Cesl_tramite;
import co.gov.sic.copiasycertificaciones.entities.Ciudad;
import co.gov.sic.copiasycertificaciones.entities.Dependencia;
import co.gov.sic.copiasycertificaciones.entities.Frntstco;
import co.gov.sic.copiasycertificaciones.entities.Obse_Radi;
import co.gov.sic.copiasycertificaciones.entities.PersonaActo;
import co.gov.sic.copiasycertificaciones.entities.PersonaCamara;
import co.gov.sic.copiasycertificaciones.entities.PersonaEmail;
import co.gov.sic.copiasycertificaciones.entities.Region;
import co.gov.sic.copiasycertificaciones.entities.Sancion;
import co.gov.sic.copiasycertificaciones.entities.Tasa;
import co.gov.sic.copiasycertificaciones.enums.EstadoTramite;
import co.gov.sic.copiasycertificaciones.enums.TipoSancion;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import co.gov.sic.copiasycertificaciones.util.Constantes;
import co.gov.sic.copiasycertificaciones.util.Utility;

import sic.ws.interop.entities.Direccion;
import sic.ws.interop.entities.Perfil;
import sic.ws.interop.entities.Persona;
import sic.ws.interop.entities.Referencia;

public class Dal implements AutoCloseable {

	private Connection _myConn;
	protected final Logger logger = LoggerFactory.getLogger(Dal.class);
	private static final String SQL_SELECT_MAX_ID_TRAMITE = "SELECT MAX(idtramite) AS idtramite FROM cesl_tramite WHERE iden_pers = ?";
	private static final String SQL_SELECT_TRAMITE = "SELECT idtramite, idtiposolicitud, ano_radi, nume_radi, cont_radi, cons_radi, estado, valor_total, fecha_creacion, fecha_modificacion, medio_respuesta, iden_pers, ano_recibo, num_recibo, func_asignado FROM cesl_tramite WHERE ";
	private static final String SQL_INSERT_TRAMITE = "INSERT INTO cesl_tramite (estado, valor_total, fecha_creacion, idtiposolicitud, iden_pers) VALUES(?, ?, CURRENT year to second, ?, ?)";
	private static final String SQL_SELECT_FIRST_PAGO = "select FIRST 1 1 from pagos_pse_reg where id_element = ? and estado in (0, 1, 7) and sistema = ?";
	private static final String SQL_INSERT_PAGO = "insert into pagos_pse_reg (service_code, trans_value, trans_tax_value, trans_payed_value, iden_pers, estado, id_element, fecha_creacion, fecha_actualizacion, sistema) values(?, ?, ?, ?, ?, ?, ?, CURRENT year to second, CURRENT year to second, ?)";
	private static final String SQL_UPDATE_TRAMITE_ESTADO = "UPDATE cesl_tramite SET estado = ?,fecha_modificacion=current WHERE idtramite = ?";
	private static final String SQL_UPDATE_TRAMITE_ESTADO_FIRMA_ELECTRONICA = "UPDATE cesl_detallesolicitud SET copia_autenticada = 'S', observaciones_radicado = ? WHERE idtramite = ?";
	private static final String SQL_UPDATE_TRAMITE_ESTADO_REASIGNAR = "UPDATE cesl_detallesolicitud SET observaciones_reasig = ? WHERE idtramite = ?";
	private static final String SQL_UPDATE_TRAMITE_HASH = "UPDATE cesl_detallesolicitud SET hash_pdf = ? WHERE idtramite = ?";
	private static final String SQL_UPDATE_TRAMITE_RUTAMEMO = "UPDATE cesl_detallesolicitud SET ruta_memo = ? WHERE idtramite = ?";
	private static final String SQL_SELECT_TRAMITE_HASH = "SELECT DISTINCT 1 FROM cesl_detallesolicitud WHERE idtramite = ? and hash_pdf = ?";
	private static final String SQL_SELECT_PAGO = "select estado, id_element, trans_payed_value, trans_value, trans_tax_value from pagos_pse_reg where sistema = ? and invoice = ?;";
	private static final String SQL_SELECT_TRAMITE_FROM_NUM_RADICACION = "select idtramite FROM cesl_tramite WHERE ano_radi = ? AND nume_radi = ? ANd cons_radi = 0 AND cont_radi = '' AND estado = 13;";
	private static final String SQL_SELECT_INVOICE = "select MAX(invoice) as invoice from pagos_pse_reg where sistema = ? and id_element = ? and service_code = ? and iden_pers = ?;";
	private static final String SQL_UPDATE_PAGO = "update pagos_pse_reg set estado = ?, trazability_code = ?, fecha_actualizacion = CURRENT year to second, ano_tran_recibo = ?, nume_tran_recibo = ? where sistema = ? and invoice = ? and id_element = ?;";
	private static final String SQL_UPDATE_TRAMITE = "UPDATE cesl_tramite SET ano_radi = ?, nume_radi = ?, cons_radi = ?, cont_radi = ?, estado = ?, fecha_modificacion = CURRENT year to second, ano_recibo = ?, num_recibo = ? WHERE idtramite = ?";
	private static final String SQL_SELECT_DETALLE_TRAMITE = "SELECT p.idtiposolicitud, iddetallesolicitud, tipo_certifica, tipo_docu, nume_docu, cantidad, anos, valor, idcamaracomercio, observaciones, variable_adicional_2, variable_adicional_1, fecha_adicional_1, opciones_entrega, copia_autenticada, observaciones_radicado, hash_pdf, cons_dire, cons_email, ruta_memo FROM cesl_detallesolicitud c INNER JOIN cesl_tramite p on p.idtramite = c.idtramite WHERE c.idtramite = ?";
	private static final String SQL_SELECT_DIRECCION = "SELECT a.cons_dire, a.domi_dire, tipo_dire, a.codi_regi, b.nomb_regi, a.codi_ciud, c.nomb_ciud FROM informix.direccion a JOIN informix.region b on a.codi_regi = b.codi_regi JOIN informix.ciudades c ON a.codi_regi = c.codi_regi AND a.codi_ciud = c.codi_ciud WHERE a.iden_pers = ? AND a.cons_dire = ?";
	private static final String SQL_PERFIL_TRAMITE = "SELECT codi_depe, codi_tram, codi_even, codi_actu FROM cesl_tiposolicitud WHERE idtiposolicitud = ?";
	private static final String SQL_TRAMITE_DESCRIPCION = "SELECT idtiposolicitud FROM cesl_tiposolicitud WHERE estado = 'AC'";
	private static final String SQL_CAMARA = "select trim(e.nomb_empr) nomb_empr, e.digi_veri, c.num_decre, c.fech_decre , p.nume_docu, p.tipo_docu from empresa e inner join persona p on p.iden_pers = e.iden_pers inner join cesl_camaras_detalle c on c.iden_pers = p.iden_pers where e.iden_pers = ?";
	private static final String SQL_RENTISTICO_CONCEPTO_TRAMITE = "SELECT ta.fvlor, tc.fcncpto codigo_concepto, trim(tc.frntstco) cod_rentistico  FROM fttrfa ta INNER JOIN tarifas_copias tc ON tc.fcncpto = ta.fcncpto AND tc.ffchavgn = ta.ffchavgn AND year(tc.ffchavgn) = year(CURRENT) AND tc.codi_tram = ? AND tc.codi_even = ? AND tc.codi_actu = ? AND tc.codi_depe = ?";
	private static final String SQL_INSERT_DETALLE_TRAMITE = "INSERT INTO cesl_detallesolicitud (tipo_certifica, tipo_docu, nume_docu, cantidad, anos, valor, idcamaracomercio, observaciones, idtramite, variable_adicional_1, variable_adicional_2, fecha_adicional_1,opciones_entrega, cons_dire, cons_email) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	// private static final String SQL_INSERT_DETALLE_TRAMITE = "INSERT INTO
	// cesl_detallesolicitud (tipo_certifica, tipo_docu, nume_docu, cantidad, anos,
	// valor, idcamaracomercio, observaciones, idtramite, variable_adicional_1,
	// variable_adicional_2, fecha_adicional_1) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
	// ?, ?)";
	private static final String SQL_SELECT_SANCIONES_1 = "SELECT m.fech_acto, m.tipo_acto, m.nume_acto, r.ano_radi, r.nume_radi, EXISTS (SELECT 'x' FROM pers_acto a "
			+ " WHERE  a.tipo_acto = m.tipo_acto AND a.nume_acto = m.nume_acto "
			+ " AND    a.fech_acto = m.fech_acto AND a.iden_pers = m.iden_pers "
			+ " AND    a.noti_indi is not null  AND a.ejec_indi is not null "
			+ " AND    a.fech_cons is not null AND a.cons_ejec = 'CE') AND NOT EXISTS (SELECT 'x' "
			+ " FROM   move_mult v WHERE v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU') "
			+ " AND    v.valo_movi = m.valo_mult) as firmeza FROM acto_radi r, multa m, persona p"
			+ " WHERE  r.tipo_acto = m.tipo_acto AND r.nume_acto = m.nume_acto"
			+ " AND    r.fech_acto = m.fech_acto AND m.iden_pers = p.iden_pers"
			+ " AND    m.esta_mult in ('AC','FN') AND m.fech_acto >= '1998-01-01'"
			+ " AND    m.cons_mult >= 2925 AND p.tipo_docu = ? AND p.nume_docu = ?"
			+ " AND    m.fech_acto BETWEEN ? AND ? AND m.cons_mult NOT IN (SELECT c.cons_mult FROM cont_mult c) AND NOT EXISTS (SELECT 'x' FROM   move_mult v  WHERE  v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU'))";

	private static final String SQL_SELECT_SANCIONES_3 = "SELECT m.fech_acto, m.tipo_acto, m.nume_acto, r.ano_radi, r.nume_radi, EXISTS (SELECT 'x' FROM pers_acto a"
			+ "			WHERE  a.tipo_acto = m.tipo_acto AND a.nume_acto = m.nume_acto"
			+ "			AND    a.fech_acto = m.fech_acto AND a.iden_pers = m.iden_pers"
			+ "			AND    a.noti_indi is not null  AND a.ejec_indi is not null"
			+ "			AND    a.fech_cons is not null AND a.cons_ejec = 'CE') AND NOT EXISTS (SELECT 'x'"
			+ "			FROM   move_mult v WHERE  v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU')"
			+ "			AND    v.valo_movi = m.valo_mult) as firmeza FROM acto_radi r"
			+ "			INNER JOIN multa m ON r.tipo_acto = m.tipo_acto AND r.nume_acto = m.nume_acto AND r.fech_acto = m.fech_acto"
			+ "			INNER JOIN persona p ON m.iden_pers = p.iden_pers"
			+ "			INNER JOIN acto_subc a3 on a3.tipo_acto = r.tipo_acto and a3.nume_acto = r.nume_acto and r.fech_acto = a3.fech_acto"
			+ "			INNER JOIN radicacion r2 on r.ano_radi = r2.ano_radi and r.nume_radi = r2.nume_radi and r.cont_radi = r2.cont_radi and r.cons_radi = r2.cons_radi"
			+ "			WHERE  m.esta_mult in ('AC','FN') AND m.fech_acto >= '1998-01-01'"
			+ "			AND    m.cons_mult >= 2925 AND p.tipo_docu = ? AND p.nume_docu = ?"
			+ "			AND    m.fech_acto BETWEEN ? AND ? AND ("
			+ "				(a3.codi_clas = 5 AND a3.codi_subc in (54,24,28,64)) OR"
			+ "				(a3.codi_clas = 18 AND a3.codi_subc = 39) OR"
			+ "				(r2.codi_tram IN (355, 114, 304) AND ((a3.codi_clas = 18 AND a3.codi_subc = 40) OR a3.codi_clas = 12 AND a3.codi_subc IN (34,2))) OR"
			+ "				(a3.codi_clas = 12 AND a3.codi_subc = 30) OR (a3.codi_clas in (64, 1) AND a3.codi_subc = 1)"
			+ "			)"
			+ "			AND m.cons_mult NOT IN (SELECT c.cons_mult FROM cont_mult c) AND NOT EXISTS (SELECT 'x' FROM   move_mult v  WHERE  v.cons_mult = m.cons_mult AND v.tipo_movi in ('MM','RV', 'NU'))";

	private static final String SQL_SELECT_SANCIONES_2 = "select r.ano_radi, r.nume_Radi from partes pa "
			+ " inner join radicacion r on pa.ano_radi = r.ano_radi and pa.nume_radi = r.nume_radi and pa.cont_radi = r.cont_radi"
			+ " inner join acto_radi a2 on a2.ano_radi = r.ano_radi and a2.nume_radi = r.nume_radi and a2.cont_radi = r.cont_radi and a2.cons_radi = r.cons_radi"
			+ " inner join persona p on p.iden_pers = pa.iden_pers"
			+ " inner join acto_subc a3 on a3.tipo_acto = a2.tipo_acto and a3.nume_acto = a2.nume_acto and a3.fech_acto = a2.fech_acto"
			+ " where p.tipo_docu = ? AND p.nume_docu = ? AND pa.esta_regi = 'AC' AND pa.rol = 'IN' AND ("
			+ "	(a3.codi_clas = 5 AND (a3.codi_subc in (24,28,64) OR (a3.codi_subc = 54 and r.codi_tram in (400,385)))) OR"
			+ "	(a3.codi_clas = 18 AND a3.codi_subc in(39,40)) OR"
			+ "	(a3.codi_clas = 12 AND a3.codi_subc in(2,30,34)) OR	(a3.codi_clas in (64, 1) AND a3.codi_subc = 1) OR"
			+ "	(r.codi_tram in (228,328,383,391,350,351,342,187,381,356,367) AND r.codi_Actu in (460,653,706,707,500)) OR"
			+ "	(r.codi_tram = 384 AND r.codi_even in (328,330) AND r.codi_Actu in (460,653,706,707)) OR"
			+ "	(r.codi_tram in (388,389,390,105,414) AND r.codi_even in (356,357,358,327,325,328) AND r.codi_Actu in (460,653,706,707,500))"
			+ " ) AND a2.fech_acto BETWEEN ? AND ? group by r.ano_radi, r.nume_Radi";

	private static final String SQL_SELECT_SANCIONES_4 = "select r.ano_radi, r.nume_Radi from pers_acto pa "
			+ "  inner join acto_radi a2 on a2.tipo_acto = pa.tipo_acto and a2.nume_acto = pa.nume_acto and a2.fech_acto = pa.fech_acto"
			+ "	 inner join radicacion r on a2.ano_radi = r.ano_radi and a2.nume_radi = r.nume_radi and a2.cont_radi = r.cont_radi and a2.cons_radi = r.cons_radi "
			+ " inner join persona p on p.iden_pers = pa.iden_pers"
			+ " inner join acto_subc a3 on a3.tipo_acto = a2.tipo_acto and a3.nume_acto = a2.nume_acto and a3.fech_acto = a2.fech_acto"
			+ " where p.tipo_docu = ? AND p.nume_docu = ? AND  pa.tipo_vinc = 'IN' AND ("
			+ "	(a3.codi_clas = 5 AND (a3.codi_subc in (24,28,64) OR (a3.codi_subc = 54 and r.codi_tram in (400,385)))) OR"
			+ "	(a3.codi_clas = 18 AND a3.codi_subc in(39,40)) OR"
			+ "	(a3.codi_clas = 12 AND a3.codi_subc in(2,30,34)) OR (a3.codi_clas in (64, 1) AND a3.codi_subc = 1) OR"
			+ "	(r.codi_tram in (228,328,383,391,350,351,342,187,381,356,367) AND r.codi_Actu in (460,653,706,707,500)) OR"
			+ "	(r.codi_tram = 384 AND r.codi_even in (328,330) AND r.codi_Actu in (460,653,706,707)) OR"
			+ "	(r.codi_tram in (388,389,390,105,414) AND r.codi_even in (356,357,358,327,325,328) AND r.codi_Actu in (460,653,706,707,500))"
			+ " ) AND a2.fech_acto BETWEEN ? AND ? group by r.ano_radi, r.nume_Radi";

	private static final String SQL_SELECT_SANCIONES_5 = "select r.ano_radi, r.nume_Radi from tgpersona pa "
			+ " inner join radicacion r on pa.pitranor = r.ano_radi and pa.pitrnura = r.nume_radi and pa.control = r.cont_radi"
			+ " inner join acto_radi a2 on a2.ano_radi = r.ano_radi and a2.nume_radi = r.nume_radi and a2.cont_radi = r.cont_radi and a2.cons_radi = r.cons_radi"
			+ " inner join persona p on p.iden_pers = pa.ide_per"
			+ " inner join acto_subc a3 on a3.tipo_acto = a2.tipo_acto and a3.nume_acto = a2.nume_acto and a3.fech_acto = a2.fech_acto"
			+ " where p.tipo_docu = ? AND p.nume_docu = ? and pa.rol = 'IN' AND ("
			+ "	(a3.codi_clas = 5 AND (a3.codi_subc in (24,28,64) OR (a3.codi_subc = 54 and r.codi_tram in (400,385)))) OR"
			+ "	(a3.codi_clas = 18 AND a3.codi_subc in(39,40)) OR"
			+ "	(a3.codi_clas = 12 AND a3.codi_subc in(2,30,34)) OR	(a3.codi_clas in (64, 1) AND a3.codi_subc = 1) OR"
			+ "	(r.codi_tram in (228,328,383,391,350,351,342,187,381,356,367) AND r.codi_Actu in (460,653,706,707,500)) OR"
			+ "	(r.codi_tram = 384 AND r.codi_even in (328,330) AND r.codi_Actu in (460,653,706,707)) OR"
			+ "	(r.codi_tram in (388,389,390,105,414) AND r.codi_even in (356,357,358,327,325,328) AND r.codi_Actu in (460,653,706,707,500))"
			+ " ) AND a2.fech_acto BETWEEN ? AND ? group by r.ano_radi, r.nume_Radi";

	private static final String SQL_SELECT_CAMARAS = "SELECT trim(e.nomb_empr) nomb_empr, e.iden_pers FROM empresa e INNER JOIN pers_grup p ON p.iden_pers = e.iden_pers AND p.codi_grup = 1 and p.esta_rela = 'AC' ORDER BY e.nomb_empr ASC";
	private static final String SQL_GET_ID_CAMARA_BY_NIT = "SELECT p.iden_pers FROM persona p INNER JOIN pers_grup p2 ON p2.iden_pers = p.iden_pers AND p2.codi_grup = 1 and p2.esta_rela = 'AC' where p.nume_docu  = ?";
	private static final String SQL_SELECT_PERSONAS_CAMARAS = "SELECT idsecrecamaras, tipo_docu, nume_docu, nomb_perso, fecha_acto, cargo, docu_nombra, nume_acto, rol,show_obs,observaciones from cesl_persocamaras where iden_pers = ? and fecha_retiro is null ORDER BY rol";
	private static final String SQL_SELECT_DETALLE_CERTIFICADO_APOSTILLA = "select t.ano_radi , t.nume_radi, d.observaciones from cesl_detallesolicitud d inner join cesl_tramite t on t.idtramite = d.idtramite where d.iddetallesolicitud = ? and DATE(t.fecha_creacion) = ? and t.estado = ? and t.idtramite = ? and t.idtiposolicitud = ?";
	private static final String SQL_SELECT_NUMEROS_RADICACION = "select ano_radi, nume_radi from cesl_tramite t inner join cesl_detallesolicitud d on t.idtramite = d.idtramite where t.idtiposolicitud  = ? and t.estado = ? and t.iden_pers = ? and d.idcamaracomercio = ? order by t.idtramite desc";
	private static final String SQL_PARTES_ACTO = "SELECT distinct pa.iden_pers, trim(bp.nomb_pers) nomb_pers, pa.ejec_indi, pa.noti_indi, pa.cons_ejec, pa.fech_cons, p.tipo_docu, p.nume_docu, pa.ano_radi, pa.nume_radi FROM pers_acto pa INNER JOIN busc_pers bp ON pa.iden_pers = bp.iden_pers INNER JOIN persona p on p.iden_pers = bp.iden_pers WHERE pa.tipo_acto = ? AND pa.nume_acto =? AND pa.fech_acto = ?";

	private static final String SQL_PENDING_REQUEST = "SELECT DISTINCT tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu " + "FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "where  tramite.idtiposolicitud=5 and tramite.estado=? " + "order by idtramite asc";

	private static final String SQL_PENDING_REQUEST_COORDINADOR = "SELECT DISTINCT tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "where  tramite.idtiposolicitud=5 and tramite.estado in (10,26) " + "order by idtramite asc";

	private static final String SQL_ACTIVE_REQUEST_COORDINADOR = "SELECT DISTINCT tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado, busc_pers1.nomb_pers nom_func_asignado"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "left join busc_pers busc_pers1 on (tramite.func_asignado = busc_pers1.iden_pers)"
			+ "where  tramite.idtiposolicitud=5 and tramite.estado in (14,15,16,17,18,19,20,21,22,23,24) "
			+ "and tramite.ano_radi is not null and tramite.nume_radi is not null " + "order by idtramite asc";

	private static final String SQL_REQUEST_BY_PERSON = "SELECT DISTINCT tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado,tramite.notificacion"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu " + "FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "where  tramite.idtiposolicitud=5 " + "and tramite.estado not in (13,16,26) and tramite.func_asignado =? "
			+ "order by idtramite asc";

	private static final String SQL_PENDING_REQUEST_MEMO = "SELECT DISTINCT tramite.idtramite,tramite.ano_radi,tramite.nume_radi,tramite.cont_radi,tramite.cons_radi"
			+ ",tramite.estado,tramite.valor_total,tramite.fecha_creacion,tramite.fecha_modificacion"
			+ ",tramite.medio_respuesta,tramite.idtiposolicitud,tramite.iden_pers"
			+ ",tramite.ano_recibo,tramite.num_recibo,tramite.func_asignado"
			+ ",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu FROM cesl_tramite tramite "
			+ "inner join busc_pers busc_pers on (tramite.iden_pers = busc_pers.iden_pers) "
			+ "inner join persona persona on (persona.iden_pers = tramite.iden_pers) "
			+ "inner join cesl_detallesolicitud detalle on (detalle.idtramite = tramite.idtramite) "
			+ "where tramite.idtiposolicitud=5 and detalle.opciones_entrega = 1 and tramite.estado = 13 and detalle.ruta_memo is not null order by idtramite asc";

	private static final String SQL_UPDATE_NOTIFICACION_BY_TRAMITE = "UPDATE cesl_tramite SET notificacion=? WHERE idtramite=?";

	private static final String SQL_GET_DEPENDENCIES = "SELECT codi_depe" + ",nomb_depe, noab_depe,esta_depe"
			+ ",fech_vige,fech_crea,tipo_acto" + ",nume_acto,fech_acto,tipo_acti" + ",nume_acti,fech_acti,iden_resp"
			+ ",iden_firm,carg_firm,depe_nomi " + "FROM dependencia where esta_depe = \"AC\"";

	private static final String SQL_GET_ALL_PARAMETERS = "SELECT llave" + ",valor " + "FROM cesl_config";

	private static final String SQL_UPDATE_DAY_PARAMETERS = "UPDATE cesl_config SET valor=? WHERE llave=?";
	private static final String SQL_UPDATE_USER_ROL = "UPDATE usuario SET codi_rol=? WHERE iden_pers=? and codi_sist='SL'";
	private static final String SQL_GET_OBSER_RADICADO = "SELECT ano_radi, nume_radi, cons_radi, cont_radi, cons_obse, text_obse FROM obse_radi where ano_radi=? and nume_radi=? order by cons_radi desc";
	private static final String SQL_GET_TASAS = "SELECT id,nomb_tasa, tasa_desc ,tasa from cesl_tasa";
	private static final String SQL_UPDATE_TASA = "UPDATE cesl_tasa SET tasa=? WHERE nomb_tasa=?";

	private static final String SQL_UPDATE_CESL_TRAMITE = "UPDATE cesl_tramite SET valor_total=? WHERE idtramite=?";

	private static final String SQL_GET_ALL_CAMARAS = "SELECT c.iden_pers,trim(e.nomb_empr) nomb_empr,p.tipo_docu,p.nume_docu,c.num_decre,c.fech_decre from empresa e inner join persona p on p.iden_pers = e.iden_pers inner join cesl_camaras_detalle c on c.iden_pers = p.iden_pers";
	private static final String SQL_UPDATE__PERSONAS_CAMARAS = "update cesl_persocamaras \n" + "set tipo_docu =?, \n"
			+ "nume_docu =?, \n" + "nomb_perso =?, \n" + "fecha_acto =?, \n" + "fecha_retiro=?, \n" + "cargo =?, \n"
			+ "docu_nombra =?, \n" + "nume_acto =?, \n" + "rol=? \n" + "where iden_pers =? and idsecrecamaras =?;";

	private static final String SQL_GET_CIUDADES = "SELECT codi_pais, codi_ciud, codi_regi, nomb_ciud, codi_dane, esta_ciud\n"
			+ "FROM ciudades where codi_pais = \"CO\" and esta_ciud=\"AC\" and codi_regi=?";

	private static final String SQL_GET_DEPARTAMENTOS = "SELECT codi_regi, codi_pais, nemo_regi, nomb_regi FROM region where codi_pais =\"CO\"";

	private static final String SQL_GET_PARAMETERS_BY_KEY = "SELECT llave" + ",valor "
			+ "FROM cesl_config where llave=?";

	private static final String SQL_GET_FRNTSTCO = "SELECT fcncpto, nomb_conc from fttrfahoy where frntstco = ?  and ffchavgn >= TODAY  order by fcncpto;";
	private static final String SQL_GET_FRNTSTCO_VALOR = "SELECT FIRST 1 f1.fvlor from fttrfa f1 where f1.ffchavgn >= TODAY and f1.frntstco = ? and f1.fcncpto = ?";
	private static final String SQL_INSERT_COTIZACION = "INSERT INTO cesl_cotizacion(idtramite, fcncpto, nomb_conc, fvalor, cantidad, totalconcepto) VALUES(?,?,?,?,?,?)";
	private static final String SQL_SELECT_COTIZACION_BY_IDTRAMITE = "SELECT idcotizacion, idtramite, fcncpto, nomb_conc, fvalor, cantidad, totalconcepto FROM cesl_cotizacion where idtramite=?";
	private static final String SQL_UPDATE_CESL_TRAMITE_FUNCIONARIO_ASIGNADO = "UPDATE CESL_TRAMITE SET func_asignado=?,estado=?,fecha_modificacion=current WHERE idtramite=?";
	private static final String SQL_VERIFY_CHECKSUM = "SELECT FIRST 1 1 FROM cesl_detallesolicitud d inner join cesl_tramite t on t.idtramite = d.idtramite where d.observaciones like ? and t.estado not in (?, ?, ?, ?) and t.idtiposolicitud = ?";
	private static final String SQL_SELECT_BUSC_PERS = "SELECT FIRST 1 b.nomb_pers, p.tipo_docu, p.nume_docu FROM busc_pers b inner join persona p on p.iden_pers = b.iden_pers where b.iden_pers = ?";
	private static final String SQL_SELECT_BUSC_PERS_NOMB = "SELECT b.iden_pers, b.nomb_pers, p.tipo_docu, p.nume_docu, e.dire_emai FROM busc_pers b inner join persona p on p.iden_pers = b.iden_pers inner join email e on p.iden_pers = e.iden_pers where e.esta_emai = 'AC' AND trim(e.dire_emai) <> '' AND lower(b.nomb_pers) LIKE ?";

	private static final String SQL_INSERT_DETALLE_CAMARA = "INSERT INTO cesl_camaras_detalle (iden_pers, num_decre, fech_decre,estado)  VALUES(?, ?, ?,?)";
	private static final String SQL_INSERT_PERSONA_CAMARA = "INSERT INTO cesl_persocamaras (tipo_docu, nume_docu, nomb_perso, iden_pers, fecha_acto, cargo, docu_nombra, nume_acto, rol,fecha_retiro)\n"
			+ " VALUES(?,?, ?,  ?, ?, ?, ?, ?, ?,?)";

	private static final String SQL_SELECT_CAMARADETALLE = "SELECT iden_pers, num_decre, fech_decre,estado FROM cesl_camaras_detalle where  iden_pers=?";
	private static final String SQL_UPDATE_CAMARADETALLE = "UPDATE cesl_camaras_detalle SET num_decre=?, fech_decre=?,estado=? WHERE iden_pers=?";
	private static final String SQL_GET_PERSONASCAMARAS = "SELECT idsecrecamaras, tipo_docu, nume_docu, nomb_perso, iden_pers, fecha_acto, fecha_retiro, cargo, docu_nombra, nume_acto, rol FROM cesl_persocamaras where iden_pers=?";
	private static final String SQL_PERSONACAMARA_EXISTS = "SELECT 1 FROM cesl_persocamaras where iden_pers=? AND nume_docu=? AND tipo_docu=?";

	private static final String SQL_PERSONACAMARA_DELETE = "DELETE FROM  cesl_persocamaras WHERE idsecrecamaras=?";

	private void setConnection() throws Exception {
		if (_myConn == null) {
			InitialContext ic = new InitialContext();
			try {
				DataSource ds = (DataSource) ic.lookup(Constantes.CONNECTION_STRING_JNDI);
				_myConn = ds.getConnection();
			} finally {
				ic.close();
			}
		}
	}

	@Override
	public void close() throws Exception {
		if (_myConn != null) {
			_myConn.close();
		}
	}

	public Cesl_config getDayConfigParameters(String llave) {
		Cesl_config cfg = new Cesl_config();

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_PARAMETERS_BY_KEY);
			stmt.setString(1, llave);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Cesl_config conf = new Cesl_config();
					conf.setLlave(rs.getString("llave").trim());
					if (rs.getString("Valor").contains("_H") || rs.getString("Valor").contains("_C")) {
						conf.setValor(
								rs.getString("Valor").trim().substring(0, rs.getString("Valor").trim().indexOf("_")));
						String businessDays = rs.getString("Valor").trim().substring(
								rs.getString("Valor").trim().indexOf("_"), rs.getString("Valor").trim().length());
						if (businessDays.equals("_H")) {
							conf.setBusinessDays(true);
						} else {
							conf.setBusinessDays(false);
						}
					} else {
						conf.setBusinessDays(false);
						conf.setValor(rs.getString("Valor").trim());

					}

					cfg = conf;
				}
			}
		} catch (SQLException ex) {
			logger.error(ex.toString());
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return cfg;
	}

	public int getValorConcepto(String frntstco, Short fcncpto) {
		int valor = 0;
		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_FRNTSTCO_VALOR);
			stmt.setString(1, frntstco);
			stmt.setShort(2, fcncpto);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				valor = rs.getInt("fvlor");
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return valor;
	}

	public List<Frntstco> getCodigosRentisticos(String frntstco) {
		List<Frntstco> listaFrntstco = new ArrayList<>();

		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_FRNTSTCO);
			stmt.setString(1, frntstco);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Frntstco f = new Frntstco(rs.getShort("fcncpto"), rs.getString("nomb_conc"));
				listaFrntstco.add(f);
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
		}

		return listaFrntstco;
	}

	public List<Region> getDepartamentos() {
		List<Region> departamento = new ArrayList<>();
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_DEPARTAMENTOS);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				// codi_regi, codi_pais, nemo_regi, nomb_regi
				Region d = new Region(rs.getInt("codi_regi"), rs.getString("codi_pais"), rs.getString("nemo_regi"),
						rs.getString("nomb_regi"));
				departamento.add(d);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());

		}
		return departamento;
	}

	public List<Ciudad> getCiudades(int codigoDepartamento) {

		List<Ciudad> ciudades = new ArrayList<>();
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_CIUDADES);
			stmt.setInt(1, codigoDepartamento);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Ciudad c = new Ciudad(rs.getString("codi_pais"), rs.getInt("codi_ciud"), rs.getInt("codi_regi"),
						rs.getString("nomb_ciud"), rs.getString("codi_dane"), rs.getString("esta_ciud"));
				ciudades.add(c);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());

		}
		return ciudades;
	}

	public List<Tasa> getTasas() {
		List<Tasa> listTasa = new ArrayList<>();

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_TASAS);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Tasa t = new Tasa(rs.getInt("id"), rs.getString("nomb_tasa"), rs.getString("tasa_desc"),
						rs.getDouble("tasa"));
				listTasa.add(t);
			}

			return listTasa;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return listTasa;
		}

	}

	public List<Obse_Radi> getObservacionesPorRadicado(int ano_radi, int nume_radi) {
		List<Obse_Radi> comentarios = new ArrayList<>();
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_OBSER_RADICADO);
			stmt.setInt(1, ano_radi);
			stmt.setInt(2, nume_radi);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Obse_Radi obs = new Obse_Radi();
				obs.setAno_radi(rs.getInt("ano_radi"));
				obs.setNume_radi(rs.getInt("nume_radi"));
				obs.setCons_radi(rs.getInt("cons_radi"));
				obs.setCont_radi(rs.getString("cont_radi"));
				obs.setCons_obse(rs.getInt("cons_obse"));
				obs.setText_obse(rs.getString("text_obse"));

				comentarios.add(obs);

			}
			return comentarios;

		} catch (Exception ex) {
			logger.error(ex.toString());
			return comentarios;
		}

	}

	public CamarasDetalle getCamaraDetalles(Long iden_pers) {
		CamarasDetalle cd = new CamarasDetalle();

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_CAMARADETALLE);
			stmt.setLong(1, iden_pers);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				cd.setIden_pers(iden_pers);
				cd.setNum_decre(rs.getInt("num_decre"));
				cd.setFech_decre(new SimpleDateFormat("yyyy-MM-dd").parse(rs.getString("fech_decre")));
				cd.setEstado(rs.getString("estado").equals("AC"));
				break;
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return cd;
	}

	public boolean deletePersonaCamara(Cesl_PersonaCamara personaCamara) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_PERSONACAMARA_DELETE);
			stmt.setLong(1, personaCamara.getIdsecrecamaras());

			int r = stmt.executeUpdate();
			return r > 0;

		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public boolean insertDetalleCamara(CamarasDetalle cd) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_CAMARADETALLE);
			stmt.setLong(1, cd.getIden_pers());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				stmt = _myConn.prepareStatement(SQL_UPDATE_CAMARADETALLE);
				stmt.setInt(1, cd.getNum_decre());
				stmt.setDate(2, new java.sql.Date(cd.getFech_decre().getTime()));
				stmt.setString(3, cd.getEstado() ? "AC" : "IN");
				stmt.setLong(4, cd.getIden_pers());
				return stmt.executeUpdate() > 0;

			} else {
				stmt = _myConn.prepareStatement(SQL_INSERT_DETALLE_CAMARA);
				stmt.setLong(1, cd.getIden_pers());
				stmt.setDate(3, new java.sql.Date(cd.getFech_decre().getTime()));
				stmt.setInt(2, cd.getNum_decre());
				stmt.setString(4, cd.getEstado() ? "AC" : "IN");
				int rowCount = stmt.executeUpdate();
				return rowCount > 0;
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public boolean insertPersonaCamara(Cesl_PersonaCamara personaCamara) {
		// SQL_INSERT_PERSONA_CAMARA SQL_PERSONACAMARA_EXISTS
		// SQL_UPDATE__PERSONAS_CAMARAS

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_PERSONACAMARA_EXISTS);
			stmt.setLong(1, personaCamara.getIden_pers());
			stmt.setInt(2, personaCamara.getNume_docu());
			stmt.setString(3, personaCamara.getTipo_docu());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				// Update
				stmt = _myConn.prepareStatement(SQL_UPDATE__PERSONAS_CAMARAS);
				stmt.setString(1, personaCamara.getTipo_docu());
				stmt.setLong(2, personaCamara.getNume_docu());
				stmt.setString(3, personaCamara.getNomb_perso());

				stmt.setDate(4, Date.valueOf(personaCamara.getFecha_acto()));
				if (!personaCamara.isIsEnabled()) {
					stmt.setDate(5, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				} else {
					Date date = null;
					stmt.setDate(5, date);
				}
				stmt.setString(6, personaCamara.getCargo());
				stmt.setString(7, personaCamara.getDocu_nombra());
				stmt.setString(8, personaCamara.getNume_acto());
				stmt.setString(9, personaCamara.getRol());
				stmt.setLong(10, personaCamara.getIden_pers());
				stmt.setInt(11, personaCamara.getIdsecrecamaras());

				return stmt.executeUpdate() > 0;

			} else {
				stmt = _myConn.prepareStatement(SQL_INSERT_PERSONA_CAMARA);
				stmt.setString(1, personaCamara.getTipo_docu());
				stmt.setLong(2, personaCamara.getNume_docu());
				stmt.setString(3, personaCamara.getNomb_perso());
				stmt.setLong(4, personaCamara.getIden_pers());
				stmt.setDate(5, Date.valueOf(personaCamara.getFecha_acto()));
				stmt.setString(6, personaCamara.getCargo());
				stmt.setString(7, personaCamara.getDocu_nombra());
				stmt.setString(8, personaCamara.getNume_acto());
				stmt.setString(9, personaCamara.getRol());
				if (!personaCamara.isIsEnabled()) {
					stmt.setDate(10, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
				} else {
					Date date = null;
					stmt.setDate(10, date);
				}

				return stmt.executeUpdate() > 0;
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}

	}

	public List<Cesl_PersonaCamara> getAllPersonascamara(Long iden_pers) {
		List<Cesl_PersonaCamara> funcionarioscamara = new ArrayList<>();
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_PERSONASCAMARAS);
			stmt.setLong(1, iden_pers);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_PersonaCamara pc = new Cesl_PersonaCamara();
				pc.setIdsecrecamaras(rs.getInt("idsecrecamaras"));
				pc.setTipo_docu(rs.getString("tipo_docu"));
				pc.setNume_docu(rs.getInt("nume_docu"));
				pc.setNomb_perso(rs.getString("nomb_perso"));
				pc.setIden_pers(rs.getLong("iden_pers"));
				pc.setFecha_acto(rs.getDate("fecha_acto").toLocalDate());

				if (rs.getDate("fecha_retiro") != null) {
					pc.setFecha_retiro(rs.getDate("fecha_retiro").toLocalDate());
					pc.setIsEnabled(false);
					pc.setEstado("Inactivo");
				} else {
					pc.setIsEnabled(true);
					pc.setEstado("Activo");
				}

				pc.setCargo(rs.getString("cargo"));
				pc.setDocu_nombra(rs.getString("docu_nombra"));
				pc.setNume_acto(rs.getString("nume_acto"));
				pc.setRol(rs.getString("rol"));
				funcionarioscamara.add(pc);

			}
		} catch (Exception ex) {
			logger.error(ex.toString());

		}
		return funcionarioscamara;
	}

	public Cesl_tramite insertarTramite(EstadoTramite estado, Double valor_total, TipoTramite tipo, long iden_pers)
			throws Exception {
		setConnection();
		Cesl_tramite tramite = null;
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_INSERT_TRAMITE)) {
			stmt.setString(1, String.valueOf(estado.getValue()));
			stmt.setDouble(2, valor_total == null ? 0 : valor_total);
			stmt.setInt(3, tipo.getValue());
			stmt.setLong(4, iden_pers);
			int rowCount = stmt.executeUpdate();
			if (rowCount > 0) {
				try (PreparedStatement stmt2 = _myConn.prepareStatement(SQL_SELECT_MAX_ID_TRAMITE)) {
					stmt2.setLong(1, iden_pers);
					try (ResultSet rs = stmt2.executeQuery()) {
						if (rs.next()) {
							tramite = new Cesl_tramite();
							tramite.setFecha_creacion(LocalDateTime.now());
							tramite.setIdtramite(rs.getInt("idtramite"));
							tramite.setEstado(estado);
							tramite.setValor_total(valor_total);
							tramite.setIdtiposolicitud(tipo);
							tramite.setIden_pers(iden_pers);
						}
					}
				}
			}
		}
		return tramite;
	}

	public void insertarDetalleTramite(Cesl_detalleSolicitud detalle) throws Exception {
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_INSERT_DETALLE_TRAMITE)) {
			stmt.setString(1, detalle.getTipo_certifica());
			stmt.setString(2, detalle.getTipo_docu());
			if (detalle.getNume_docu() == null) {
				stmt.setNull(3, java.sql.Types.BIGINT);
			} else {
				stmt.setLong(3, detalle.getNume_docu());
			}
			stmt.setInt(4, detalle.getCantidad() == null ? 1 : detalle.getCantidad());
			if (detalle.getAnos() == null) {
				stmt.setNull(5, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(5, detalle.getAnos());
			}
			stmt.setDouble(6, detalle.getValor() == null ? 0 : detalle.getValor());
			if (detalle.getIdcamaracomercio() == null) {
				stmt.setNull(7, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(7, detalle.getIdcamaracomercio());
			}
			stmt.setString(8, detalle.getObservaciones());
			stmt.setInt(9, detalle.getIdtramite());
			stmt.setString(10, detalle.getVariableAdicional1());
			stmt.setString(11, detalle.getVariableAdicional2());
			if (detalle.getFechaAdicional1() == null) {
				stmt.setNull(12, java.sql.Types.DATE);
			} else {
				stmt.setDate(12, Date.valueOf(detalle.getFechaAdicional1()));
			}
			stmt.setInt(13, detalle.getOpcionesEntrega() == null ? 3 : detalle.getOpcionesEntrega());
			stmt.setLong(14, detalle.getConsDire());
			stmt.setLong(15, detalle.getConsEmail());
			stmt.executeUpdate();
		}
	}

	public List<Cesl_config> getDayConfigParameters() throws SQLException, Exception {
		List<Cesl_config> cfg = new ArrayList<>();
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_ALL_PARAMETERS)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Cesl_config conf = new Cesl_config();
					conf.setLlave(rs.getString("llave").trim());
					if (rs.getString("Valor").contains("_H") || rs.getString("Valor").contains("_C")) {
						conf.setValor(
								rs.getString("Valor").trim().substring(0, rs.getString("Valor").trim().indexOf("_")));
						String businessDays = rs.getString("Valor").trim().substring(
								rs.getString("Valor").trim().indexOf("_"), rs.getString("Valor").trim().length());
						if (businessDays.equals("_H")) {
							conf.setBusinessDays(true);
						} else {
							conf.setBusinessDays(false);
						}
					} else {
						conf.setBusinessDays(false);
						conf.setValor(rs.getString("Valor").trim());

					}

					cfg.add(conf);
				}
			}
		}
		return cfg;
	}

	public List<Cesl_tramite> getMyAssignedRequest(Long iden_pers) {
		List<Cesl_tramite> response = new ArrayList<>();

		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_REQUEST_BY_PERSON);
			stmt.setLong(1, iden_pers);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				tramite.setCont_radi(rs.getString("cont_radi").trim());
				tramite.setCons_radi(rs.getInt("cons_radi"));
				tramite.setEstado(rs.getInt("estado"));
				tramite.setValor_total(rs.getDouble("valor_total"));
				tramite.setFecha_creacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				if (rs.getString("medio_respuesta") != null) {
					tramite.setMedio_respuesta(rs.getString("medio_respuesta").trim());
				}
				// +",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu "
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				tramite.setNume_recibo(rs.getInt("num_recibo"));
				tramite.setAno_recibo(rs.getShort("ano_recibo"));
				tramite.setDetalles(getDetallesTramite(rs.getInt("idtramite")));
				tramite.setTipoDcoumento(rs.getString("tipo_docu").trim());
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setFunc_asignado(rs.getLong("func_asignado"));
				tramite.setNotificacion(rs.getString("notificacion") == null ? false
						: rs.getString("notificacion").equalsIgnoreCase("S"));
				response.add(tramite);
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return response;

	}

	public boolean updateNotificacionByTramite(Integer llave, String valor) {
		boolean status = false;
		try {
			setConnection();
			PreparedStatement pr = _myConn.prepareStatement(SQL_UPDATE_NOTIFICACION_BY_TRAMITE);
			pr.setString(1, valor);
			pr.setInt(2, llave);
			if (pr.executeUpdate() == 1) {
				status = true;
			} else {
				status = false;
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return status;

	}

	public List<CamaraComercio> getAllCamaras() {
		List<CamaraComercio> listaCamaras = new ArrayList<>();
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_GET_ALL_CAMARAS);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				CamaraComercio camara = getCamaraComercio(rs.getInt("iden_pers"));
				listaCamaras.add(camara);
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return listaCamaras;
	}

	public List<Cesl_tramite> getPendingRequest(int status) {
		List<Cesl_tramite> response = new ArrayList<>();

		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_PENDING_REQUEST);
			stmt.setInt(1, status);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				if (tramite.getNume_radi() <= 9999) {
					tramite.setStringNume_radi("00" + String.valueOf(tramite.getNume_radi()));
				} else if (tramite.getNume_radi() > 9999 && tramite.getNume_radi() <= 99999) {
					tramite.setStringNume_radi("0" + String.valueOf(tramite.getNume_radi()));
				} else {
					tramite.setStringNume_radi(String.valueOf(tramite.getNume_radi()));
				}

				tramite.setCont_radi(rs.getString("cont_radi").trim());
				tramite.setCons_radi(rs.getInt("cons_radi"));
				tramite.setEstado(rs.getInt("estado"));
				tramite.setValor_total(rs.getDouble("valor_total"));
				tramite.setFecha_creacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				if (rs.getString("medio_respuesta") != null) {
					tramite.setMedio_respuesta(rs.getString("medio_respuesta").trim());
				}
				// +",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu "
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				tramite.setNume_recibo(rs.getInt("num_recibo"));
				tramite.setAno_recibo(rs.getShort("ano_recibo"));
				tramite.setDetalles(getDetallesTramite(rs.getInt("idtramite")));
				tramite.setTipoDcoumento(rs.getString("tipo_docu").trim());
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setFunc_asignado(0L);
				response.add(tramite);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return response;
	}

	public List<Cesl_tramite> getPendingRequestCoordinador() {
		List<Cesl_tramite> response = new ArrayList<>();

		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_PENDING_REQUEST_COORDINADOR);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				if (tramite.getNume_radi() <= 9999) {
					tramite.setStringNume_radi("00" + String.valueOf(tramite.getNume_radi()));
				} else if (tramite.getNume_radi() > 9999 && tramite.getNume_radi() <= 99999) {
					tramite.setStringNume_radi("0" + String.valueOf(tramite.getNume_radi()));
				} else {
					tramite.setStringNume_radi(String.valueOf(tramite.getNume_radi()));
				}

				tramite.setCont_radi(rs.getString("cont_radi").trim());
				tramite.setCons_radi(rs.getInt("cons_radi"));
				tramite.setEstado(rs.getInt("estado"));
				tramite.setValor_total(rs.getDouble("valor_total"));
				tramite.setFecha_creacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				if (rs.getString("medio_respuesta") != null) {
					tramite.setMedio_respuesta(rs.getString("medio_respuesta").trim());
				}
				// +",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu "
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				tramite.setNume_recibo(rs.getInt("num_recibo"));
				tramite.setAno_recibo(rs.getShort("ano_recibo"));
				tramite.setDetalles(getDetallesTramite(rs.getInt("idtramite")));
				tramite.setTipoDcoumento(rs.getString("tipo_docu").trim());
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setFunc_asignado(rs.getLong("func_asignado"));
				tramite.setNombreFuncionario("Prueba");
				response.add(tramite);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return response;
	}

	public List<Cesl_tramite> getActiveCoordinador() {
		List<Cesl_tramite> response = new ArrayList<>();

		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_ACTIVE_REQUEST_COORDINADOR);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				if (tramite.getNume_radi() <= 9999) {
					tramite.setStringNume_radi("00" + String.valueOf(tramite.getNume_radi()));
				} else if (tramite.getNume_radi() > 9999 && tramite.getNume_radi() <= 99999) {
					tramite.setStringNume_radi("0" + String.valueOf(tramite.getNume_radi()));
				} else {
					tramite.setStringNume_radi(String.valueOf(tramite.getNume_radi()));
				}

				tramite.setCont_radi(rs.getString("cont_radi").trim());
				tramite.setCons_radi(rs.getInt("cons_radi"));
				tramite.setEstado(rs.getInt("estado"));
				tramite.setValor_total(rs.getDouble("valor_total"));
				tramite.setFecha_creacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				if (rs.getString("medio_respuesta") != null) {
					tramite.setMedio_respuesta(rs.getString("medio_respuesta").trim());
				}
				// +",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu "
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				tramite.setNume_recibo(rs.getInt("num_recibo"));
				tramite.setAno_recibo(rs.getShort("ano_recibo"));
				tramite.setDetalles(getDetallesTramite(rs.getInt("idtramite")));
				tramite.setTipoDcoumento(rs.getString("tipo_docu").trim());
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setFunc_asignado(rs.getLong("func_asignado"));
				tramite.setNombreFuncionario(rs.getString("nom_func_asignado"));
				response.add(tramite);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return response;
	}

	public List<Cesl_tramite> getMemo() {
		List<Cesl_tramite> response = new ArrayList<>();
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_PENDING_REQUEST_MEMO);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Cesl_tramite tramite = new Cesl_tramite();
				tramite.setIdtramite(rs.getInt("idtramite"));
				tramite.setAno_radi(rs.getShort("ano_radi"));
				tramite.setNume_radi(rs.getInt("nume_radi"));
				if (tramite.getNume_radi() <= 9999) {
					tramite.setStringNume_radi("00" + String.valueOf(tramite.getNume_radi()));
				} else if (tramite.getNume_radi() > 9999 && tramite.getNume_radi() <= 99999) {
					tramite.setStringNume_radi("0" + String.valueOf(tramite.getNume_radi()));
				} else {
					tramite.setStringNume_radi(String.valueOf(tramite.getNume_radi()));
				}

				tramite.setCont_radi(rs.getString("cont_radi").trim());
				tramite.setCons_radi(rs.getInt("cons_radi"));
				tramite.setEstado(rs.getInt("estado"));
				tramite.setValor_total(rs.getDouble("valor_total"));
				tramite.setFecha_creacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
				Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
				if (fechaModi != null) {
					tramite.setFecha_modificacion(fechaModi.toLocalDateTime());
				}
				if (rs.getString("medio_respuesta") != null) {
					tramite.setMedio_respuesta(rs.getString("medio_respuesta").trim());
				}
				// +",busc_pers.nomb_pers,persona.tipo_docu,persona.nume_docu "
				tramite.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
				tramite.setIden_pers(rs.getLong("iden_pers"));
				tramite.setNume_recibo(rs.getInt("num_recibo"));
				tramite.setAno_recibo(rs.getShort("ano_recibo"));
				// tramite.setDetalles(getDetallesTramite(rs.getInt("idtramite")));
				tramite.setTipoDcoumento(rs.getString("tipo_docu").trim());
				tramite.setNumeroIdentificacion(rs.getString("nume_docu"));
				tramite.setNombreSolicitante(rs.getString("nomb_pers").trim());
				tramite.setFunc_asignado(rs.getLong("func_asignado"));
				response.add(tramite);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return response;
	}

	public boolean updatePersonasCamara(PersonaCamara personaCamara) {

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE__PERSONAS_CAMARAS);
			stmt.setString(1, personaCamara.getTipoDocumento());
			stmt.setInt(2, personaCamara.getNumeroDocumento());
			stmt.setString(3, personaCamara.getNombre());
			stmt.setDate(4, Date.valueOf(personaCamara.getFechaActaDelegacion()));
			stmt.setString(5, personaCamara.getCargo());
			stmt.setString(6, personaCamara.getNombreActaDelegacion());
			stmt.setString(6, personaCamara.getNombreActaDelegacion());
			stmt.setString(6, personaCamara.getRol());
			stmt.executeUpdate();
			return true;
		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}
	}

	public boolean asignarRol(Persona persona, int rol) {
		try {

			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_USER_ROL);
			stmt.setInt(1, rol);
			stmt.setLong(2, persona.getId());
			stmt.executeUpdate();
			return true;

		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}
	}

	public boolean guardarTasa(String nomb_tasa, double tasa) {
		//

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TASA);
			stmt.setDouble(1, tasa);
			stmt.setString(2, nomb_tasa);
			if (stmt.executeUpdate() == 0) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException ex) {
			logger.error(ex.toString());

		} catch (Exception ex) {
			logger.error(ex.toString());

		}
		return false;
	}

	public boolean guardarCotizacion(Cesl_tramite tramite, List<Cesl_cotizacion> listaCotizacion) {
		boolean transaction_ok = true;
		try {
			setConnection();
			// TODO: Agregar logica para que guarde la lista de conceptos y el detalle del
			// pago. para el recibo de caja
			PreparedStatement stmt = _myConn.prepareStatement(SQL_INSERT_COTIZACION);

			for (Cesl_cotizacion cotizacion : listaCotizacion) {
				stmt.setInt(1, cotizacion.getIdTramite());
				stmt.setInt(2, cotizacion.getFrntstco().getFcncpto());
				stmt.setString(3, cotizacion.getFrntstco().getNomb_conc());
				stmt.setDouble(4, cotizacion.getFvalor());
				stmt.setInt(5, cotizacion.getCantidad());
				stmt.setDouble(6, cotizacion.getTotalConcepto());
				if (stmt.executeUpdate() == 0) {
					logger.error("No fue posible actualizar la cotizacion para el tramite : " + tramite.getIdtramite());
					transaction_ok = false;
					return transaction_ok;
				}

			}

			stmt = _myConn.prepareStatement(SQL_UPDATE_CESL_TRAMITE);
			stmt.setDouble(1, tramite.getValor_total());
			stmt.setInt(2, tramite.getIdtramite());
			if (stmt.executeUpdate() == 0) {
				logger.error("No fue posible actualizar la cotizacion para el tramite : " + tramite.getIdtramite());
				transaction_ok = false;
			}
			return transaction_ok;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public boolean setFuncionarioAsignado(Long idFuncionarioAsignado, int idtramite, int estado)
			throws SQLException, Exception {
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_CESL_TRAMITE_FUNCIONARIO_ASIGNADO)) {
			stmt.setLong(1, idFuncionarioAsignado);
			stmt.setInt(2, estado);
			stmt.setInt(3, idtramite);
			if (stmt.executeUpdate() == 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean setCeslTramiteEstado(int idtramite, int estado) {

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE_ESTADO);
			stmt.setInt(1, estado);
			stmt.setInt(2, idtramite);
			int rta = stmt.executeUpdate();
			return rta > 0;

		} catch (SQLException ex) {
			logger.error(ex.toString());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}

	}

	public boolean setCeslTramiteEstadoFirmaElectronica(int idtramite, String observaciones) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE_ESTADO_FIRMA_ELECTRONICA);
			stmt.setString(1, observaciones);
			stmt.setInt(2, idtramite);
			int rta = stmt.executeUpdate();
			return rta > 0;
		} catch (SQLException ex) {
			logger.error(ex.toString());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public boolean setCeslTramiteEstadoReasignar(int idtramite, String observaciones) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE_ESTADO_REASIGNAR);
			stmt.setString(1, observaciones);
			stmt.setInt(2, idtramite);
			int rta = stmt.executeUpdate();
			return rta > 0;
		} catch (SQLException ex) {
			logger.error(ex.toString());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public boolean obtenerHash(int idSolicitud, String hash) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_TRAMITE_HASH);
			stmt.setInt(1, idSolicitud);
			stmt.setString(2, hash);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}

	}

	public boolean setCeslTramiteHashPDF(int idtramite, String hashPDF) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE_HASH);
			stmt.setString(1, hashPDF);
			stmt.setInt(2, idtramite);
			int rta = stmt.executeUpdate();
			return rta > 0;
		} catch (SQLException ex) {
			logger.error(ex.toString());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public boolean setCeslTramiteRutaMemo(int idtramite, String rutaMemo) {
		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE_RUTAMEMO);
			stmt.setString(1, rutaMemo);
			stmt.setInt(2, idtramite);
			int rta = stmt.executeUpdate();
			return rta > 0;
		} catch (SQLException ex) {
			logger.error(ex.toString());
			return false;
		} catch (Exception ex) {
			logger.error(ex.toString());
			return false;
		}
	}

	public List<Referencia> GetCamaras() throws Exception {
		List<Referencia> response = new ArrayList<>();
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_CAMARAS)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Referencia r = new Referencia();
					r.setCodigo(String.valueOf(rs.getInt("iden_pers")));
					r.setValor(rs.getString("nomb_empr").trim());
					response.add(r);
				}
			}
		}
		return response;
	}

	public List<Cesl_cotizacion> getCotizacionByIdTramite(int idTramite) {
		List<Cesl_cotizacion> listaCotizacion = new ArrayList<>();

		try {
			setConnection();
			PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_COTIZACION_BY_IDTRAMITE);
			stmt.setInt(1, idTramite);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Frntstco frntstco = new Frntstco(rs.getShort("fcncpto"), rs.getString("nomb_conc"));
				Cesl_cotizacion cotizacion = new Cesl_cotizacion(idTramite, frntstco, rs.getInt("fvalor"),
						rs.getInt("cantidad"), rs.getInt("totalconcepto"));
				listaCotizacion.add(cotizacion);
			}

		} catch (Exception ex) {
			logger.error(ex.toString());
		}

		return listaCotizacion;
	}

	public List<Referencia> GetNumerosRadicacion(int idCamaraComercio, Long idenPers) throws Exception {
		List<Referencia> response = new ArrayList<>();
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_NUMEROS_RADICACION)) {
			stmt.setInt(1, TipoTramite.CERTIFICADO_REPRESENTACION_CAMARAS.getValue());
			stmt.setInt(2, EstadoTramite.FINALIZADO.getValue());
			stmt.setLong(3, idenPers);
			stmt.setInt(4, idCamaraComercio);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Referencia r = new Referencia();
					r.setCodigo(String.format("%s - %s", rs.getInt("ano_radi"), rs.getInt("nume_radi")));
					r.setValor(r.getCodigo());
					response.add(r);
				}
			}
		}
		return response;
	}

	public Double getValorTramite(short codTram, short codEven, short codActua, short codDepe) throws Exception {
		Double valor = null;
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_RENTISTICO_CONCEPTO_TRAMITE)) {
			stmt.setShort(1, codTram);
			stmt.setShort(2, codEven);
			stmt.setShort(3, codActua);
			stmt.setShort(4, codDepe);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					valor = rs.getDouble("fvlor");
				}
			}
		}
		return valor;
	}

	public String[] getRentisticoyConcepto(Perfil perfilTramite) throws Exception {
		return getRentisticoyConcepto(perfilTramite.getTramite(), perfilTramite.getEvento(),
				perfilTramite.getActuacion(), perfilTramite.getDependencia());
	}

	public String[] getRentisticoyConcepto(short codTram, short codEven, short codActua, short codDepe)
			throws Exception {
		String[] codigos = null;
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_RENTISTICO_CONCEPTO_TRAMITE)) {
			stmt.setShort(1, codTram);
			stmt.setShort(2, codEven);
			stmt.setShort(3, codActua);
			stmt.setShort(4, codDepe);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					codigos = new String[] { rs.getString("cod_rentistico"), rs.getString("codigo_concepto") };
				}
			}
		}
		return codigos;
	}

	public Integer getInvoiceId(Integer idTramite, Long idenPers) throws Exception {
		Integer invoiceId = null;
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_INVOICE)) {
			stmt.setInt(1, Constantes.ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE);
			stmt.setInt(2, idTramite);
			stmt.setInt(3, Constantes.CODIGO_PSE);
			stmt.setLong(4, idenPers);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					invoiceId = rs.getInt("invoice");
				}
			}
		}
		return invoiceId;
	}

	public Double getValorTramite(Perfil perfilTramite) throws Exception {
		return getValorTramite(perfilTramite.getTramite(), perfilTramite.getEvento(), perfilTramite.getActuacion(),
				perfilTramite.getDependencia());
	}

	public void actualizarTramite(Cesl_tramite tram) throws Exception {
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE)) {
			stmt.setInt(1, tram.getAno_radi());
			stmt.setInt(2, tram.getNume_radi());
			stmt.setInt(3, tram.getCons_radi());
			stmt.setString(4, tram.getCont_radi());
			stmt.setString(5, String.valueOf(tram.getEstado().getValue()));
			if (tram.getAno_recibo() == null) {
				stmt.setNull(6, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(6, tram.getAno_recibo());
			}
			if (tram.getNume_recibo() == null) {
				stmt.setNull(7, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(7, tram.getNume_recibo());
			}
			stmt.setInt(8, tram.getIdtramite());
			stmt.executeUpdate();
		}
	}

	public Cesl_tramite consultarTramite(Integer idtramite) throws Exception {
		String finalQuery = String.format("%s %s", SQL_SELECT_TRAMITE, "idtramite = ?");
		List<Cesl_tramite> result = consultarTramites(finalQuery, Long.valueOf(idtramite), null, null);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public Cesl_tramite consultarTramite(int idtramite, int tipoTram, int cons_radi) throws Exception {
		String finalQuery = String.format("%s %s", SQL_SELECT_TRAMITE,
				"idtramite = ? AND cons_radi = ? AND idtiposolicitud = ?");
		List<Cesl_tramite> result = consultarTramites(finalQuery, Long.valueOf(idtramite), Long.valueOf(cons_radi),
				Long.valueOf(tipoTram));
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public List<Cesl_tramite> consultarTramites(Long iden_pers, TipoTramite tipoTramite) throws Exception {
		String finalQuery = String.format("%s %s", SQL_SELECT_TRAMITE,
				"iden_pers = ? AND idtiposolicitud = ? ORDER BY idtramite DESC");
		List<Cesl_tramite> result = consultarTramites(finalQuery, iden_pers, Long.valueOf(tipoTramite.getValue()),
				null);
		return result;
	}

	public List<Cesl_tramite> consultarTramites(Long iden_pers) throws Exception {
		String finalQuery = String.format("%s %s", SQL_SELECT_TRAMITE, "iden_pers = ? ORDER BY idtramite DESC");
		List<Cesl_tramite> result = consultarTramites(finalQuery, iden_pers, null, null);
		return result;
	}

	public List<Cesl_tramite> consultarTramitesPendientes() throws Exception {
		String finalQuery = String.format("%s %s", SQL_SELECT_TRAMITE,
				"idtiposolicitud < ? AND (estado = 6 or (estado between 9 AND 12)) ORDER BY fecha_creacion");
		List<Cesl_tramite> result = consultarTramites(finalQuery, Long.valueOf(TipoTramite.COPIAS_SIMPLES.getValue()),
				null, null);
		for (Cesl_tramite tram : result) {
			try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_BUSC_PERS)) {
				stmt.setLong(1, tram.getIden_pers());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						tram.setNombreSolicitante(rs.getString("nomb_pers"));
						tram.setTipoDcoumento(rs.getString("tipo_docu"));
						tram.setNumeroIdentificacion(rs.getString("nume_docu"));
					}
				}
			}
		}
		return result;
	}

	private List<Cesl_tramite> consultarTramites(String sql, Long param1, Long param2, Long param3) throws Exception {
		setConnection();
		List<Cesl_tramite> result = new ArrayList<>();
		try (PreparedStatement stmt = _myConn.prepareStatement(sql)) {
			stmt.setLong(1, param1);
			if (param2 != null) {
				stmt.setLong(2, param2);
			}
			if (param3 != null) {
				stmt.setLong(3, param3);
			}
			try (ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {
					Cesl_tramite tram = parseTramite(rs);
					result.add(tram);
				}
			}
		}
		return result;
	}

	private Cesl_tramite parseTramite(ResultSet rs) throws Exception {
		Cesl_tramite tram = new Cesl_tramite();
		tram.setIdtramite(rs.getInt("idtramite"));
		tram.setIdtiposolicitud(rs.getInt("idtiposolicitud"));
		tram.setAno_radi(rs.getShort("ano_radi"));
		tram.setNume_radi(rs.getInt("nume_radi"));

		if (tram.getNume_radi() <= 9999) {
			tram.setStringNume_radi("00" + String.valueOf(tram.getNume_radi()));
		} else if (tram.getNume_radi() > 9999 && tram.getNume_radi() <= 99999) {
			tram.setStringNume_radi("0" + String.valueOf(tram.getNume_radi()));
		} else {
			tram.setStringNume_radi(String.valueOf(tram.getNume_radi()));
		}

		tram.setCont_radi(rs.getString("cont_radi"));
		tram.setCons_radi(rs.getInt("cons_radi"));
		tram.setEstado(rs.getInt("estado"));
		if (tram.getEstado().equals(EstadoTramite.COMPLEMENTAR)) {
			System.out.println(tram.getIdtramite());
			tram.setDetalles(getDetallesTramite(rs.getInt("idtramite")));
		}
		tram.setFunc_asignado(rs.getLong("func_asignado"));
		tram.setValor_total(rs.getDouble("valor_total"));

		tram.setFecha_creacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
		Timestamp fechaModi = rs.getTimestamp("fecha_modificacion");
		if (fechaModi != null) {
			tram.setFecha_modificacion(fechaModi.toLocalDateTime());
		}
		tram.setMedio_respuesta(rs.getString("medio_respuesta"));
		tram.setIden_pers(rs.getLong("iden_pers"));
		tram.setNume_recibo(rs.getInt("num_recibo"));
		tram.setAno_recibo(rs.getShort("ano_recibo"));
		asegurarEstado(tram);
		return tram;
	}

	private void asegurarEstado(Cesl_tramite tram) throws Exception {
		EstadoTramite estado = tram.getEstado();
		if (estado == EstadoTramite.PRESENTADO) {
			if (existsPendingTransaction(tram.getIdtramite())) {
				tram.setEstado(EstadoTramite.PENDIENTE_CONFIRMACION_PAGO);
			}
		}
	}

	public Integer nextIdInvoice(Cesl_tramite tramite) throws Exception {
		setConnection();
		_myConn.setAutoCommit(false);
		Integer id = null;
		try {
			int rowCount = 0;
			try (PreparedStatement stmt = _myConn.prepareStatement(SQL_INSERT_PAGO)) {
				stmt.setInt(1, Constantes.CODIGO_PSE);
				stmt.setDouble(2, tramite.getValor_total());
				stmt.setInt(3, 0);
				stmt.setDouble(4, tramite.getValor_total());
				stmt.setLong(5, tramite.getIden_pers());
				stmt.setLong(6, EstadoTramite.CREATED.getValue());
				stmt.setInt(7, tramite.getIdtramite());
				stmt.setInt(8, Constantes.ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE);
				rowCount = stmt.executeUpdate();
			}
			if (rowCount > 0) {
				id = getInvoiceId(tramite.getIdtramite(), tramite.getIden_pers());
				try (PreparedStatement stmt = _myConn.prepareStatement(SQL_UPDATE_TRAMITE_ESTADO)) {
					stmt.setString(1, String.valueOf(EstadoTramite.PENDIENTE_CONFIRMACION_PAGO.getValue()));
					stmt.setInt(2, tramite.getIdtramite());
					stmt.executeUpdate();
				}
			}
			_myConn.commit();
		} catch (Exception e) {
			_myConn.rollback();
			throw e;
		} finally {
			_myConn.setAutoCommit(true);
		}
		return id;
	}

	public boolean existsPendingTransaction(int idTramite) throws Exception {
		boolean result = false;
		setConnection();
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_FIRST_PAGO)) {
			stmt.setInt(1, idTramite);
			stmt.setInt(2, Constantes.ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE);
			try (ResultSet rs = stmt.executeQuery()) {
				result = rs.next();
			}
		}
		return result;
	}

	public Cesl_tramite getTramiteFromInvoice(int invoiceId) throws Exception {
		setConnection();
		Cesl_tramite tramite = null;
		Integer idTramite = null;
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_SELECT_PAGO)) {
			pr.setInt(1, Constantes.ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE);
			pr.setString(2, String.valueOf(invoiceId));
			try (ResultSet rs = pr.executeQuery()) {
				if (rs.next()) {
					idTramite = rs.getInt("id_element");
				}
			}
		}
		if (idTramite != null && idTramite > 0) {
			tramite = consultarTramite(idTramite);
		}
		return tramite;
	}

	public Cesl_tramite getTramiteFromNumRadicacion(Integer anoRadi, Integer numeRadi) throws Exception {
		setConnection();
		Cesl_tramite tramite = null;
		Integer idTramite = null;
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_SELECT_TRAMITE_FROM_NUM_RADICACION)) {
			pr.setInt(1, anoRadi);
			pr.setInt(2, numeRadi);
			try (ResultSet rs = pr.executeQuery()) {
				if (rs.next()) {
					idTramite = rs.getInt("idtramite");
				}
			}
		}
		if (idTramite != null && idTramite > 0) {
			tramite = consultarTramite(idTramite);
		}
		return tramite;
	}

	public void updateInvoice(int invoiceId, EstadoTramite transactionState, int idTramite, Integer trazabilityCode,
			Short anoTranRecibo, Integer numeTranRecibo) throws Exception {
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_UPDATE_PAGO)) {
			pr.setInt(1, transactionState.getValue());
			if (trazabilityCode == null) {
				pr.setNull(2, java.sql.Types.INTEGER);
			} else {
				pr.setInt(2, trazabilityCode);
			}
			if (anoTranRecibo == null) {
				pr.setNull(3, java.sql.Types.INTEGER);
			} else {
				pr.setShort(3, anoTranRecibo);
			}
			if (numeTranRecibo == null) {
				pr.setNull(4, java.sql.Types.INTEGER);
			} else {
				pr.setInt(4, numeTranRecibo);
			}
			pr.setInt(5, Constantes.ID_SISTEMA_SEDE_ELECTRONICA_PAGO_PSE);
			pr.setString(6, String.valueOf(invoiceId));
			pr.setInt(7, idTramite);
			pr.executeUpdate();
		}
	}

	public List<Cesl_detalleSolicitud> getDetallesTramite(int idTramite) throws Exception {
		List<Cesl_detalleSolicitud> detalles = new ArrayList<>();
		setConnection();
		List<Referencia> tiposCertificadoSanciones = Utility.GetReferenciaWS("TIPOCERT_SEDELECTRO");
		List<Referencia> tiposSolicitudesCopias = Utility.GetReferenciaWS("TIPOSOL_SEDELECTRO");
		List<Referencia> tiposListadoInfo = Utility.GetReferenciaWS("TIPOSTEM_SEDELECTRO");
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_SELECT_DETALLE_TRAMITE)) {
			pr.setInt(1, idTramite);
			try (ResultSet rs = pr.executeQuery()) {
				while (rs.next()) {
					Cesl_detalleSolicitud detalle = new Cesl_detalleSolicitud();
					detalle.setIdtramite(idTramite);
					detalle.setIddetallesolicitud(rs.getInt("iddetallesolicitud"));
					detalle.setTipo_certifica(rs.getString("tipo_certifica"));
					detalle.setTipo_docu(rs.getString("tipo_docu"));
					detalle.setNume_docu(rs.getLong("nume_docu"));
					detalle.setCantidad(rs.getInt("cantidad"));
					detalle.setAnos(rs.getInt("anos"));
					detalle.setValor(rs.getDouble("valor"));
					detalle.setIdcamaracomercio(rs.getInt("idcamaracomercio"));
					detalle.setObservaciones(rs.getString("observaciones"));
					detalle.setVariableAdicional1(rs.getString("variable_adicional_1"));
					detalle.setVariableAdicional2(rs.getString("variable_adicional_2"));
					detalle.setFechaAdicional1(rs.getDate("fecha_adicional_1"));
					detalle.setOpcionesEntrega(rs.getInt("opciones_entrega"));
					detalle.setCopiaAutenticada(rs.getString("copia_autenticada"));
					detalle.setObservacionesRadicado(rs.getString("observaciones_radicado"));
					detalle.setHashPdf(rs.getString("hash_pdf"));
					detalle.setConsDire(rs.getLong("cons_dire"));
					detalle.setConsEmail(rs.getLong("cons_email"));
					detalle.setRutaMemo(rs.getString("ruta_memo"));

					Integer idtiposolicitud = rs.getInt("idtiposolicitud");

					if (detalle.getIdcamaracomercio() != null) {
						CamaraComercio camara = getCamaraComercio(detalle.getIdcamaracomercio());
						detalle.setIdcamaracomercioData(camara);
					}
					if (detalle.getAnos() != null) {
						detalle.setAnos_descripcion(detalle.getAnos() == 1 ? "Último año"
								: String.format("Últimos %s años", detalle.getAnos()));
					}
					if (detalle.getNume_docu() != null) {
						detalle.setNume_docu_descripcion(
								Utility.tryFormatCurrencyNumber(detalle.getNume_docu(), false));
					}
					List<Referencia> target = null;
					if (idtiposolicitud == TipoTramite.CERTIFICADO_SANCIONES.getValue()) {
						target = tiposCertificadoSanciones;
					} else if (idtiposolicitud == TipoTramite.COPIAS_SIMPLES.getValue()) {
						target = tiposSolicitudesCopias;
					} else if (idtiposolicitud == TipoTramite.LISTADOS_INFORMACION.getValue()) {
						target = tiposListadoInfo;
					}

					if (target != null && target.size() > 0 && detalle.getTipo_certifica() != null) {
						for (Referencia referencia : target) {
							if (referencia.getCodigo().equals(detalle.getTipo_certifica())) {
								detalle.setTipo_certifica_descripcion(referencia.getValor());
								break;
							}
						}
					}
					detalles.add(detalle);
				}
			}
		}
		return detalles;
	}

	public Direccion getDireccion(Long idenPers, Long consDire) throws Exception {
		Direccion result = null;
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_SELECT_DIRECCION)) {
			pr.setLong(1, idenPers);
			pr.setLong(2, consDire);
			try (ResultSet rs = pr.executeQuery()) {
				while (rs.next()) {
					result = new Direccion();
					result.setId(rs.getLong("cons_dire"));
					result.setDescripcion(rs.getString("domi_dire"));
					result.setTipo(rs.getString("tipo_dire"));
					result.setCodigoRegionDesc(rs.getString("nomb_regi"));
					result.setCodigoCiudadDesc(rs.getString("nomb_ciud"));
				}
			}
		}
		return result;
	}

	public Perfil getPerfilCertificado(TipoTramite tipoTramite) throws Exception {
		Perfil result = null;
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_PERFIL_TRAMITE)) {
			pr.setInt(1, tipoTramite.getValue());
			try (ResultSet rs = pr.executeQuery()) {
				if (rs.next()) {
					result = new Perfil();
					result.setDependencia(rs.getShort("codi_depe"));
					result.setTramite(rs.getShort("codi_tram"));
					result.setEvento(rs.getShort("codi_even"));
					result.setActuacion(rs.getShort("codi_actu"));
				}
			}
		}
		return result;
	}

	public CamaraComercio getCamaraComercioByNIT(int nitCamaraComercio) throws Exception {
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_GET_ID_CAMARA_BY_NIT)) {
			pr.setInt(1, nitCamaraComercio);
			try (ResultSet rs = pr.executeQuery()) {
				if (rs.next()) {
					int idenPers = rs.getInt("iden_pers");
					return getCamaraComercio(idenPers);
				}
			}
		}
		return null;
	}

	public CamaraComercio getCamaraComercio(int idCamaraComercio) throws Exception {
		CamaraComercio camara = null;
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_CAMARA)) {
			pr.setInt(1, idCamaraComercio);
			try (ResultSet rs = pr.executeQuery()) {
				if (rs.next()) {
					camara = new CamaraComercio();
					camara.setIdenPers(idCamaraComercio);
					camara.setNombre(rs.getString("nomb_empr"));
					camara.setDigitoVerificacion(rs.getString("digi_veri"));
					camara.setNumeroDecreto(rs.getInt("num_decre"));
					camara.setFechaDecreto(rs.getDate("fech_decre").toLocalDate());
					camara.setNumeroDocumento(rs.getLong("nume_docu"));
					camara.setNumeroDocumentoDesc(Utility.tryFormatCurrencyNumber(camara.getNumeroDocumento(), false));
					camara.setTipoDocumento(rs.getString("tipo_docu"));
				}
			}
		}
		if (camara != null) {
			try (PreparedStatement pr = _myConn.prepareStatement(SQL_SELECT_PERSONAS_CAMARAS)) {
				pr.setInt(1, idCamaraComercio);
				try (ResultSet rs = pr.executeQuery()) {
					while (rs.next()) {
						PersonaCamara persona = new PersonaCamara();
						persona.setCargo(rs.getString("cargo"));
						persona.setFechaActaDelegacion(rs.getDate("fecha_acto").toLocalDate());
						persona.setNombre(rs.getString("nomb_perso"));
						persona.setNumeroActaDelegacion(rs.getString("nume_acto"));
						persona.setNombreActaDelegacion(rs.getString("docu_nombra"));
						persona.setNumeroDocumento(rs.getInt("nume_docu"));
						persona.setNumeroDocumentoDesc(
								Utility.tryFormatCurrencyNumber(persona.getNumeroDocumento(), false));
						persona.setTipoDocumento(rs.getString("tipo_docu"));
						String rol = rs.getString("rol");
						persona.setRol(rol);
						persona.setObservaciones(rs.getString("observaciones"));
						persona.setShowObservaciones(rs.getString("show_obs"));

						if (Constantes.ROL_REPRESENTANTE_LEGAL.equals(rol) && camara.getRepresentante() == null) {

							camara.setRepresentante(persona);
						} else if (Constantes.ROL_SUPLENTE.equals(rol)) {

							camara.getSuplentes().add(persona);
						} else if (Constantes.ROL_SECRETARIO.equals(rol)) {

							camara.getSecretarios().add(persona);
						} else if (Constantes.ROL_REPRESENTANTE_JUDICIAL.equals(rol)) {

							camara.setRepresentanteJudicial(persona);
						}
					}
				}
			}
		}
		return camara;
	}

	public List<Sancion> consultarSanciones(String tipoDocumento, long numeroDocumento, TipoSancion tipoSancion,
			LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws Exception {
		List<Sancion> response = new ArrayList<>();
		setConnection();
		List<String> documentos = new ArrayList<>();
		documentos.add(String.valueOf(numeroDocumento));
		// Si la busqueda es un NIT, se hace la busqueda del numero de documento con la
		// combinacion de todos los posibles digitos de verificacion
		// ya que existen la posibilidad de que el nit se haya registrado con el digito
		// de verificacion pegado y ademas que este no sea el correcto.
		// El numero de documento para un NIT siempre tiene 9 disgitos + el digito de
		// verificacion
		if ("NI".equals(tipoDocumento)) {
			for (int digitoVerificacion = 0; digitoVerificacion <= 9; digitoVerificacion++) {
				documentos.add(String.format("%s%s", numeroDocumento, digitoVerificacion));
			}
		}
		Date fechaInicialSql = Date.valueOf(fechaInicial.toLocalDate());
		Date fechaFinalSql = Date.valueOf(fechaFinal.toLocalDate());
		for (String numDoc : documentos) {
			if (tipoSancion == null || tipoSancion == TipoSancion.Multa) {
				try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_SANCIONES_1)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);

					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Multa);
							s.setFechaActo(rs.getDate("fech_acto"));
							s.setNumeroActo(rs.getInt("nume_acto"));
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							s.setTipoActo(rs.getString("tipo_acto"));
							s.setFirmeza(rs.getBoolean("firmeza"));
							if (!response.contains(s)) {
								response.add(s);
							}
						}
					}
				}
			}
			if (tipoSancion == null || tipoSancion == TipoSancion.Demanda) {
				try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_SANCIONES_2)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Demanda);
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							if (!response.contains(s)) {
								// logger.info(String.format("Q2 %s - %s", s.getAnoRadicacion(),
								// s.getNumeroRadicacion()));
								response.add(s);
							}
						}

					}
				}
				try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_SANCIONES_4)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Demanda);
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							if (!response.contains(s)) {
								// logger.info(String.format("Q4 %s - %s", s.getAnoRadicacion(),
								// s.getNumeroRadicacion()));
								response.add(s);
							}
						}
					}
				}
				try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_SANCIONES_5)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Demanda);
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							if (!response.contains(s)) {
								// logger.info(String.format("Q5 %s - %s", s.getAnoRadicacion(),
								// s.getNumeroRadicacion()));
								response.add(s);
							}
						}
					}
				}
			}
			if (tipoSancion == TipoSancion.ProteccionCompetencia) {
				try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_SANCIONES_3)) {
					stmt.setString(1, tipoDocumento);
					stmt.setString(2, numDoc);
					stmt.setDate(3, fechaInicialSql);
					stmt.setDate(4, fechaFinalSql);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							Sancion s = new Sancion();
							s.setTipo(TipoSancion.Multa);
							s.setFechaActo(rs.getDate("fech_acto"));
							s.setNumeroActo(rs.getInt("nume_acto"));
							s.setAnoRadicacion(rs.getShort("ano_radi"));
							s.setNumeroRadicacion(rs.getInt("nume_radi"));
							s.setTipoActo(rs.getString("tipo_acto"));
							s.setFirmeza(rs.getBoolean("firmeza"));
							if (!response.contains(s)) {
								response.add(s);
							}
						}
					}
				}
			}
		}
		Collections.sort(response);
		return response;
	}

	public Cesl_tramite getDetalleApostilla(int idTramite, int idDetalle, LocalDate fechaTramite) throws Exception {
		setConnection();
		Cesl_tramite tramite = null;
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_SELECT_DETALLE_CERTIFICADO_APOSTILLA)) {
			pr.setInt(1, idDetalle);
			pr.setDate(2, Date.valueOf(fechaTramite));
			pr.setInt(3, EstadoTramite.FINALIZADO.getValue());
			pr.setInt(4, idTramite);
			pr.setInt(5, TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS.getValue());
			try (ResultSet rs = pr.executeQuery()) {
				if (rs.next()) {
					tramite = new Cesl_tramite();
					tramite.setAno_radi(rs.getShort("ano_radi"));
					tramite.setNume_radi(rs.getInt("nume_radi"));
					Cesl_detalleSolicitud detalle = new Cesl_detalleSolicitud();
					detalle.setObservaciones(rs.getString("observaciones"));
					tramite.getDetalles().add(detalle);
				}
			}
		}
		return tramite;
	}

	public List<Cesl_tramite> getTramitesActivos() throws Exception {
		setConnection();
		List<Cesl_tramite> lista = new ArrayList<>();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_TRAMITE_DESCRIPCION)) {
			try (ResultSet rs = pr.executeQuery()) {
				while (rs.next()) {
					Cesl_tramite tramite = new Cesl_tramite();
					tramite.setIdtiposolicitud(TipoTramite.fromValue(rs.getInt("idtiposolicitud")));
					lista.add(tramite);
				}
			}
		}
		return lista;
	}

	public List<PersonaActo> getPartesActo(String tipoActo, int numeActo, Date fechaActo) throws Exception {
		setConnection();
		List<PersonaActo> lista = new ArrayList<>();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_PARTES_ACTO)) {
			pr.setString(1, tipoActo);
			pr.setLong(2, numeActo);
			pr.setDate(3, fechaActo);
			try (ResultSet rs = pr.executeQuery()) {
				while (rs.next()) {
					PersonaActo p = new PersonaActo();
					p.setIdenPers(rs.getLong("iden_pers"));
					p.setNombrePersona(rs.getString("nomb_pers"));
					p.setConstanciaEjecutoria(rs.getString("cons_ejec"));
					p.setFechaConstancia(rs.getDate("fech_cons"));
					p.setFechaEjecutoria(rs.getDate("ejec_indi"));
					p.setFechaNotificacion(rs.getDate("noti_indi"));
					p.setTipoDocumento(rs.getString("tipo_docu"));
					p.setNumeroDocumento(rs.getLong("nume_docu"));
					p.setAnioRadicado(rs.getInt("ano_radi"));
					p.setNumeroRadicado(rs.getLong("nume_radi"));
					lista.add(p);
				}
			}
		}
		return lista;
	}

	public List<Dependencia> getAllDependencies() throws Exception {
		List<Dependencia> listaDependencia = new ArrayList<>();
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_GET_DEPENDENCIES)) {
			try (ResultSet rsDependencies = pr.executeQuery()) {
				while (rsDependencies.next()) {
					Dependencia dependencie = new Dependencia();

					dependencie.setCodi_depe(rsDependencies.getInt("codi_depe"));
					dependencie.setNomb_depe(rsDependencies.getString("nomb_depe"));
					dependencie.setNoab_depe(rsDependencies.getString("noab_depe"));
					dependencie.setEsta_depe(rsDependencies.getString("esta_depe"));
					dependencie.setTipo_acto(rsDependencies.getString("tipo_acto"));
					dependencie.setNume_acto(rsDependencies.getInt("nume_acto"));
					dependencie.setTipo_acti(rsDependencies.getString("tipo_acti"));
					dependencie.setNume_acti(rsDependencies.getInt("nume_acti"));
					dependencie.setIden_resp(rsDependencies.getInt("iden_resp"));
					dependencie.setIden_firm(rsDependencies.getInt("iden_firm"));
					dependencie.setCarg_firm(rsDependencies.getString("carg_firm"));
					dependencie.setDepe_nomi(rsDependencies.getString("depe_nomi"));
					dependencie.setFech_vige(rsDependencies.getTimestamp("fech_vige") != null
							? rsDependencies.getTimestamp("fech_vige").toLocalDateTime()
							: null);
					dependencie.setFech_crea(rsDependencies.getTimestamp("fech_crea") != null
							? rsDependencies.getTimestamp("fech_crea").toLocalDateTime()
							: null);
					dependencie.setFech_acto(rsDependencies.getTimestamp("fech_acto") != null
							? rsDependencies.getTimestamp("fech_acto").toLocalDateTime()
							: null);
					dependencie.setFech_acti(rsDependencies.getTimestamp("fech_acti") != null
							? rsDependencies.getTimestamp("fech_acti").toLocalDateTime()
							: null);
					listaDependencia.add(dependencie);

				}
			}
		}
		return listaDependencia;
	}

	public boolean saveMicelaneos(String llave, String valor) {
		boolean status = false;
		try {
			setConnection();
			PreparedStatement pr = _myConn.prepareStatement(SQL_UPDATE_DAY_PARAMETERS);
			pr.setString(1, valor);
			pr.setString(2, llave);
			if (pr.executeUpdate() == 1) {
				status = true;
			} else {
				status = false;
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
		}
		return status;

	}

	public boolean saveDayConfiguration(List<Cesl_config> localCfg) throws Exception {
		setConnection();
		boolean status = false;
		for (Cesl_config cfg : localCfg) {
			try (PreparedStatement pr = _myConn.prepareStatement(SQL_UPDATE_DAY_PARAMETERS)) {
				if (cfg.isBusinessDays()) {

					pr.setString(1, String.valueOf(cfg.getValor()) + "_H");
				} else {

					pr.setString(1, String.valueOf(cfg.getValor()) + "_C");
				}

				pr.setString(2, cfg.getLlave());

				if (pr.executeUpdate() == 1) {
					status = true;
				} else {
					status = false;
					break;
				}
			}
		}
		return status;
	}

	public boolean ExistCheckSum(String checksum) throws Exception {
		boolean exist = false;
		setConnection();
		try (PreparedStatement pr = _myConn.prepareStatement(SQL_VERIFY_CHECKSUM)) {
			pr.setString(1, "%" + String.format("%s%s", Constantes.FILENAME_CHECKSUM_SEPARATOR, checksum));
			pr.setInt(2, EstadoTramite.FAILED.getValue());
			pr.setInt(3, EstadoTramite.EXPIRED.getValue());
			pr.setInt(4, EstadoTramite.NOT_AUTHORIZED.getValue());
			pr.setInt(5, EstadoTramite.DESISTIDA.getValue());
			pr.setInt(6, TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS.getValue());
			try (ResultSet rs = pr.executeQuery()) {
				exist = rs.next();
			}
		}
		return exist;
	}

	public List<PersonaEmail> consultarPersonas(String nombPers) throws Exception {
		List<PersonaEmail> result = new ArrayList<>();
		setConnection();
		int id = 1;
		try (PreparedStatement stmt = _myConn.prepareStatement(SQL_SELECT_BUSC_PERS_NOMB)) {
			stmt.setString(1, "%" + nombPers + "%");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					PersonaEmail persona = new PersonaEmail();
					persona.setId(id);
					persona.setIdenPers(rs.getLong("iden_pers"));
					persona.setNombrePersona(rs.getString("nomb_pers"));
					persona.setTipoDocumento(rs.getString("tipo_docu"));
					persona.setNumeroDocumento(rs.getLong("nume_docu"));
					persona.setDireEmai(rs.getString("dire_emai"));
					result.add(persona);
					id++;
				}
			}
		}
		return result;
	}

}
