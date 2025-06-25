package co.gov.sic.copiasycertificaciones.util;

import static co.gov.sic.copiasycertificaciones.util.PDFHeaderFooter.QR_CODE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import sic.ws.interop.api.InteropWSClient;
import sic.ws.interop.entities.Referencia;
import sic.ws.interop.entities.enums.TipoReferenciaEnum;
import sic.ws.interop.entities.radicacion.Radicacion;
import sic.ws.interop.entities.response.ResponseReferencia;

@Named("utility")
@SessionScoped
public class Utility implements Serializable {

	private static final long serialVersionUID = 4220775032698261759L;

	public final static DateTimeFormatter DD_MM_YYYY = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_DD_MM_YYYY, Constantes.LOCALE_ES_CO);
	public final static SimpleDateFormat DD_MM_YYYY_HH_MM_AA = new SimpleDateFormat(
			Constantes.DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter DD_MM_YYYY_HH_MM_AA_LD = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_DD_MM_YYYY_AM_PM, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter YYYY_MM_DD_LD = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_YYYY_MM_DD, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern(Constantes.DATE_TIME_FORMAT_YYYYMMDD,
			Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter DD_MMM_YYYY = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_DD_MMM_YYYY, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter D_MMM_YYYY = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_D_MMM_YYYY, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter DD_MMMM_YYYY = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_DD_MMMM_YYYY, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter D_MMMM_YYYY = DateTimeFormatter
			.ofPattern(Constantes.DATE_TIME_FORMAT_D_MMMM_YYYY, Constantes.LOCALE_ES_CO);
	public final static DateTimeFormatter MMMMM = DateTimeFormatter.ofPattern(Constantes.DATE_TIME_FORMAT_MMMM,
			Constantes.LOCALE_ES_CO);
	public final static NumberFormat MONEY_FORMAT = NumberFormat.getCurrencyInstance(Constantes.LOCALE_ES_CO);
	public final static NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance(Constantes.LOCALE_ES_CO);
	protected final static Logger logger = LoggerFactory.getLogger(Utility.class);

	public Utility() {
	}

	public static String ConverToXML(Object obj, Class<? extends Object> sourceClass)
			throws JAXBException, IOException {
		String result = null;
		StringWriter sw = null;
		try {
			JAXBContext context = JAXBContext.newInstance(sourceClass);
			Marshaller jaxbMarshaller = context.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			sw = new StringWriter();
			jaxbMarshaller.marshal(obj, sw);
			result = sw.toString();
		} finally {
			if (sw != null) {
				sw.close();
			}
		}
		return result;
	}

	public static String ConverToXML(Object obj) throws JAXBException, IOException {
		return ConverToXML(obj, obj.getClass());
	}

	public static Object ConverFromXML(String xml, Class<?> targetClass) throws JAXBException {
		StringReader sr = null;
		Object respuesta = null;
		try {
			JAXBContext context = JAXBContext.newInstance(targetClass);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			// Desactivar acceso a entidades externas
			unmarshaller.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			unmarshaller.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			sr = new StringReader(xml);
			respuesta = unmarshaller.unmarshal(sr);
		} finally {
			if (sr != null) {
				sr.close();
			}
		}
		return respuesta;

	}

	public static java.sql.Date convertUtilToSql(java.util.Date uDate) {
		java.sql.Date sDate = new java.sql.Date(uDate.getTime());
		return sDate;
	}

	private static final Pattern DIACRITICS_AND_FRIENDS = Pattern
			.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

	public static String stripDiacritics(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
		return str;
	}

	/**
	 * Convierte a letras un numero de la forma $123,456.32
	 *
	 * @param number Numero en representacion texto
	 * @throws NumberFormatException Si valor del numero no es valido (fuera de
	 *                               rango o )
	 * @return Numero en letras
	 */
	public static String convertNumberToLetter(String number) throws NumberFormatException {
		return convertNumberToLetter(Double.parseDouble(number));
	}

	/**
	 * Convierte un numero en representacion numerica a uno en representacion de
	 * texto. El numero es valido si esta entre 0 y 999'999.999
	 *
	 * @param doubleNumber Numero a convertir
	 * @return Numero convertido a texto
	 * @throws NumberFormatException Si el numero esta fuera del rango
	 */
	public static String convertNumberToLetter(double doubleNumber) throws NumberFormatException {

		StringBuilder converted = new StringBuilder();

		String patternThreeDecimalPoints = "#.###";

		DecimalFormat format = new DecimalFormat(patternThreeDecimalPoints);
		format.setRoundingMode(RoundingMode.DOWN);

		// formateamos el numero, para ajustarlo a el formato de tres puntos
		// decimales
		String formatedDouble = format.format(doubleNumber);
		doubleNumber = Double.parseDouble(formatedDouble);

		// Validamos que sea un numero legal
		if (doubleNumber > 999999999) {
			throw new NumberFormatException("El numero es mayor de 999'999.999, " + "no es posible convertirlo");
		}

		if (doubleNumber < 0) {
			throw new NumberFormatException("El numero debe ser positivo");
		}

		String splitNumber[] = String.valueOf(doubleNumber).replace('.', '#').split("#");

		// Descompone el trio de millones
		int millon = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0], 8))
				+ String.valueOf(getDigitAt(splitNumber[0], 7)) + String.valueOf(getDigitAt(splitNumber[0], 6)));
		if (millon == 1) {
			converted.append("Un millon ");
		} else if (millon > 1) {
			converted.append(convertNumber(String.valueOf(millon))).append("Millones ");
		}

		// Descompone el trio de miles
		int miles = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0], 5))
				+ String.valueOf(getDigitAt(splitNumber[0], 4)) + String.valueOf(getDigitAt(splitNumber[0], 3)));
		if (millon >= 1) {
			if (miles == 1) {
				converted.append(convertNumber(String.valueOf(miles))).append("mil ");
			} else if (miles > 1) {
				converted.append(convertNumber(String.valueOf(miles))).append("mil ");
			}
		} else {
			if (miles == 1) {
				converted.append("Un mil ");
			}

			if (miles > 1) {
				converted.append(convertNumber(String.valueOf(miles))).append("mil ");
			}
		}

		// Descompone el ultimo trio de unidades
		int cientos = Integer.parseInt(String.valueOf(getDigitAt(splitNumber[0], 2))
				+ String.valueOf(getDigitAt(splitNumber[0], 1)) + String.valueOf(getDigitAt(splitNumber[0], 0)));
		if (miles >= 1 || millon >= 1) {
			if (cientos >= 1) {
				converted.append(convertNumber(String.valueOf(cientos)));
			}
		} else {
			if (cientos == 1) {
				converted.append("Un ");
			}
			if (cientos > 1) {
				converted.append(convertNumber(String.valueOf(cientos)));
			}
		}

		if (millon + miles + cientos == 0) {
			converted.append("Cero ");
		}

		// Descompone los centavos
		String valor = splitNumber[1];
		/*
		 * if (valor.length() == 1) {
		 * converted.append(splitNumber[1]).append("0").append("/100 "); } else {
		 * converted.append(splitNumber[1]).append("/100 "); }
		 */
		/* converted.append("U.S. DOLARES**"); */
		return converted.toString().toLowerCase();
	}

	/**
	 * Convierte los trios de numeros que componen las unidades, las decenas y las
	 * centenas del numero.
	 *
	 * @param number Numero a convetir en digitos
	 * @return Numero convertido en letras
	 */
	private static String convertNumber(String number) {

		if (number.length() > 3) {
			throw new NumberFormatException("La longitud maxima debe ser 3 digitos");
		}

		// Caso especial con el 100
		if (number.equals("100")) {
			return "Cien ";
		}

		StringBuilder output = new StringBuilder();
		if (getDigitAt(number, 2) != 0) {
			output.append(Constantes.CENTENAS[getDigitAt(number, 2) - 1]);
		}

		int k = Integer.parseInt(String.valueOf(getDigitAt(number, 1)) + String.valueOf(getDigitAt(number, 0)));

		if (k <= 20) {
			output.append(Constantes.UNIDADES[k]);
		} else if (k > 30 && getDigitAt(number, 0) != 0) {
			output.append(Constantes.DECENAS[getDigitAt(number, 1) - 2]).append("Y ")
					.append(Constantes.UNIDADES[getDigitAt(number, 0)]);
		} else {
			output.append(Constantes.DECENAS[getDigitAt(number, 1) - 2])
					.append(Constantes.UNIDADES[getDigitAt(number, 0)].replace("Seis", "Séis"));
		}

		return output.toString();
	}

	/**
	 * Retorna el digito numerico en la posicion indicada de derecha a izquierda
	 *
	 * @param origin   Cadena en la cual se busca el digito
	 * @param position Posicion de derecha a izquierda a retornar
	 * @return Digito ubicado en la posicion indicada
	 */
	private static int getDigitAt(String origin, int position) {
		if (origin.length() > position && position >= 0) {
			return origin.charAt(origin.length() - position - 1) - 48;
		}
		return 0;
	}

	public static String tryFormatVisualDate(Date date) {
		try {
			if (date == null) {
				return "";
			}
			String fecha = DD_MM_YYYY_HH_MM_AA.format(date);
			return fecha;
		} catch (Exception e) {
			return date.toString();
		}
	}

	public static String tryFormatVisualDate(LocalDateTime date) {
		try {
			if (date == null) {
				return "";
			}
			String fecha = DD_MM_YYYY_HH_MM_AA_LD.format(date);
			return fecha;
		} catch (Exception e) {
			return date.toString();
		}
	}

	public static String tryFormatCurrencyNumber(Object number, boolean withSign) {
		return tryFormatCurrencyNumber(number, withSign, false);
	}

	public static String tryFormatCurrencyNumber(Object number, boolean withSign, boolean withDecimal) {
		if (number != null) {
			String result = withSign || withDecimal ? MONEY_FORMAT.format(number) : INTEGER_FORMAT.format(number);
			// logger.info(String.format("%s = %s", number, result));
			return withSign ? result : result.replace("$", Constantes.STR_EMPTY);
		} else {
			return Constantes.STR_EMPTY;
		}
	}

	public static boolean isNullOrEmptyTrim(String value) {
		return value == null || Constantes.STR_EMPTY.equals(value.trim());
	}

	public static String customTrim(String value) {
		return value != null ? value.trim() : value;
	}

	public static LocalDateTime getStartDateFromTodayAnYears(int anos, LocalDateTime fechaRadicacion) {
		LocalDateTime returnValue = fechaRadicacion;
		if (anos != 0) {
			returnValue = fechaRadicacion.minusYears(anos);
		}
		return returnValue;
	}

	public static String getURLConsultaRadicacionWeb(Radicacion radi) {
		return String.format(Constantes.URL_WEB_CONSULTA_TRAMITE, radi.getAnio(), radi.getNumero(),
				radi.getConsecutivo());
	}

	public static ByteArrayOutputStream zipDirectory(String directoryPath) throws Exception {
		File fileToZip = new File(directoryPath);
		try (ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
				zipFile(fileToZip, fileToZip.getName(), zipOut);
			}
			return fos;
		}
	}

	public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
		}
	}

	public static BufferedImage getImageCodeQR(String codeContent) throws WriterException {
		Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
		hintMap.put(EncodeHintType.CHARACTER_SET, Constantes.UTF_8);
		hintMap.put(EncodeHintType.MARGIN, 0);
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix byteMatrix = qrCodeWriter.encode(codeContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE,
				hintMap);
		int CrunchifyWidth = byteMatrix.getWidth();
		BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth, BufferedImage.TYPE_INT_RGB);
		image.createGraphics();

		Graphics2D graphics = (Graphics2D) image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
		graphics.setColor(Color.BLACK);

		for (int i = 0; i < CrunchifyWidth; i++) {
			for (int j = 0; j < CrunchifyWidth; j++) {
				if (byteMatrix.get(i, j)) {
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}

		return image;
	}

	public static List<X509Certificate> getPDFInfoCertificate(String path) throws Exception {
		try (FileInputStream stream = new FileInputStream(path)) {
			return getPDFInfoCertificate(stream);
		}
	}

	public static List<X509Certificate> getPDFInfoCertificate(InputStream fileStream) throws Exception {
		List<X509Certificate> result = new ArrayList<>();
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		PdfReader pdfReader = new PdfReader(fileStream);
		AcroFields fields = pdfReader.getAcroFields();
		ArrayList<String> names = fields.getSignatureNames();
		for (String name : names) {
			PdfPKCS7 pkcs7 = fields.verifySignature(name);
			// Solo se agregan firmas que sean validas
			if (pkcs7.verify()) {
				result.add(pkcs7.getSigningCertificate());
			}
		}
		pdfReader.close();
		return result;
	}

	public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
		if (dateToConvert != null) {
			return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} else {
			return null;
		}
	}

	public static void copyFile(String target, InputStream source) throws Exception {
		Path targetLocation = Paths.get(target);
		if (!Files.exists(targetLocation.getParent())) {
			Files.createDirectories(targetLocation);
		}
		Files.copy(source, targetLocation, StandardCopyOption.REPLACE_EXISTING);
	}

	public static String encodeBase64(byte[] data) {
		return new String(Base64.getEncoder().encode(data));
	}

	public static String encodeBase64(String filePath) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(filePath));
		return encodeBase64(data);
	}

	public static String decodeBase64(String val) {
		Base64.Decoder dec = Base64.getDecoder();
		return new String(dec.decode(val));
	}

	public static String encryptNumber(int source) {
		return Integer.toHexString(source);
	}

	public static int decryptBarCode(String source, char encodedPadChar) {
		return Integer.parseInt(source.replace(String.valueOf(encodedPadChar), ""), 16);
	}

	public static String pad(int length, String val, char paddigChar, boolean padLeft) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(paddigChar);
		}
		return padLeft ? (sb.substring(val.length()) + val) : (val + sb.substring(val.length()));
	}

	public static String getBarCode(int idTramite, int consRadi, TipoTramite tipoTramite) {
		return String.format("CB-%s-%s-%s", pad(5, encryptNumber(idTramite), Constantes.ENCODE_PAD_CHAR1, true),
				pad(2, String.valueOf(tipoTramite.getValue()), Constantes.ENCODE_PAD_CHAR2, false),
				pad(5, encryptNumber(consRadi), Constantes.ENCODE_PAD_CHAR3, true)).toUpperCase();
	}

	public String replaceNewLines(String source) {
		if (source != null) {
			return source.replace("\n", "<br/>");
		} else {
			return null;
		}
	}

	public static InteropWSClient GetWSClient() {
		return new InteropWSClient(Constantes.WS_INTEROP_USER, Constantes.WS_INTEROP_PASS, Constantes.URL_WS_INTEROP,
				true);
	}

	public static List<Referencia> GetReferenciaWS(String nombreReferencia) throws IOException {
		InteropWSClient wsInteropClient = GetWSClient();
		ResponseReferencia response = wsInteropClient.utilReferencia(nombreReferencia);
		if (response.getCodigo() == 0 && response.getResultado().size() > 0) {
			return response.getResultado().get(0).getValores();
		}
		return null;
	}

	public static List<Referencia> GetReferenciaWS(TipoReferenciaEnum nombreReferencia) throws IOException {
		InteropWSClient wsInteropClient = GetWSClient();
		ResponseReferencia response = wsInteropClient.utilReferencia(nombreReferencia);
		if (response.getCodigo() == 0 && response.getResultado().size() > 0) {
			return response.getResultado().get(0).getValores();
		}
		return null;
	}

	public static String removeBreakLinesTabsAndExtraSpaces(String target) {
		// Se remueven los saltos de linea, tabs y espacios en blanco de mas
		if (target != null) {
			return target.replaceAll("\\s+", " ").trim();
		} else {
			return null;
		}
	}
	
	public static byte[] getImageCodeQRAsBytes(String url) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        }
    }
}
