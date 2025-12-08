package co.gov.sic.copiasycertificaciones.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import co.gov.sic.copiasycertificaciones.entities.Cesl_detalleSolicitud;
import co.gov.sic.copiasycertificaciones.enums.TipoTramite;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import sic.ws.interop.entities.Persona;

public class PDFGeneratorService {

	protected final static Logger logger = LoggerFactory.getLogger(PDFGeneratorService.class);
	protected final static Font fontBlack = new Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL);
	protected final static Font fontBlackSmall = new Font(Font.FontFamily.HELVETICA, Constantes.PDF_FOOTER_FONT_SIZE,
			Font.NORMAL);
	protected final static Font fontBlackBold = new Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD);
	protected final static Font fontBlackBoldSmall = new Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD);

	public PDFGeneratorService() {

	}

	public static ByteArrayOutputStream createPdf(String content, String title, String subject, String keywords,
			boolean isCertificacion, String barcode) throws Exception {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			PDFHeaderFooter event = new PDFHeaderFooter(isCertificacion, true, true, barcode);
			Document document = new Document(PageSize.LETTER, PDFHeaderFooter.MARGIN_LEFT, PDFHeaderFooter.MARGIN_RIGHT,
					PDFHeaderFooter.MARGIN_TOP + event.getTableHeaderHeight(),
					PDFHeaderFooter.MARGIN_BOTTOM + event.getTableFooterHeight());
			PdfWriter writer = PdfWriter.getInstance(document, bos);
			writer.setFullCompression();
			writer.setPageEvent(event);
			document.open();
			try (ByteArrayInputStream input = new ByteArrayInputStream(content.getBytes(Constantes.UTF_8))) {
				XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
				ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
						.getContext();
				String pathFonts = servletContext.getRealPath("webfonts/");
				String pathResources = servletContext.getRealPath("WEB-INF/templates/");
				worker.parseXHtml(writer, document, input, null, Charset.forName(Constantes.UTF_8),
						new XMLWorkerFontProvider(pathFonts), pathResources);
			}
			document.addAuthor(Constantes.AUTHOR);
			document.addCreationDate();
			document.addProducer();
			document.addSubject(subject);
			document.addCreator(Constantes.AUTHOR);
			document.addTitle(title);
			document.addKeywords(keywords);
			document.close();
			return bos;
		}
	}

	public static void AddSignPage(String source, String target, Cesl_detalleSolicitud detalle, Persona radicador)
			throws Exception {
		Calendar fecha = Calendar.getInstance();
		int ano = fecha.get(Calendar.YEAR);
		int mes = fecha.get(Calendar.MONTH);
		int dia = fecha.get(Calendar.DAY_OF_MONTH);
		String MES[] = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
				"Octubre", "Noviembre", "Diciembre" };
		String descMes = MES[mes];
		byte[] fileBytes = null;
		List<X509Certificate> certificatesInfo = Utility.getPDFInfoCertificate(source);
		String nombreSecretarioCamra = CertificateInfo.getSubjectFields(certificatesInfo.get(0)).getField("CN");

		PdfReader reader = new PdfReader(source);

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			PDFHeaderFooter event = new PDFHeaderFooter(true, false, false, null);
			Document document = new Document(reader.getPageSize(1), PDFHeaderFooter.MARGIN_LEFT,
					PDFHeaderFooter.MARGIN_RIGHT, PDFHeaderFooter.MARGIN_TOP + event.getTableHeaderHeight(),
					PDFHeaderFooter.MARGIN_BOTTOM + event.getTableFooterHeight());
			PdfWriter writer = PdfWriter.getInstance(document, bos);
			writer.setFullCompression();
			writer.setPageEvent(event);
			document.open();

			PdfPTable mainTable = new PdfPTable(new float[] { 22f, 56f, 22f });
			mainTable.setWidthPercentage(100);
			mainTable.setTotalWidth(
					PageSize.LETTER.getWidth() - PDFHeaderFooter.MARGIN_LEFT - PDFHeaderFooter.MARGIN_RIGHT);
			mainTable.setLockedWidth(true);

			Paragraph p1 = new Paragraph(String.format(
					"La Secretaría General AD HOC de la Superintendencia de Industria y Comercio certifica que la firma estampada en este documento corresponde a %s, y se encuentra registrada en ésta Superintendencia.\n\nSe expide en Bogotá a los %s día(s) del mes de %s de %s.",
					nombreSecretarioCamra, dia, descMes, ano), fontBlack);
			p1.setAlignment(Element.ALIGN_JUSTIFIED);

			PdfPCell cellP1 = new PdfPCell();
			cellP1.addElement(p1);
			cellP1.setBorder(0);
			cellP1.setPaddingBottom(50);
			cellP1.setColspan(3);
			mainTable.addCell(cellP1);

			PdfPCell cellGrafo = event.getResourceImageAsPDFCell(String.format("FIRMA_SECRETARIO_AD_HOC_%s.PNG",
					Constantes.WS_CANCILLERIA_ID_AUTORIDAD_SECRETARIO_AD_HOC));
			cellGrafo.setColspan(3);
			mainTable.addCell(cellGrafo);

			Paragraph p3 = new Paragraph(String.format("%s\n", Constantes.WS_SIGN_NOMBRE_SECRETARIO_AD_HOC),
					fontBlackBold);
			p3.setAlignment(Element.ALIGN_CENTER);
			p3.add(new Chunk(String.format("%s\n", Constantes.WS_SIGN_CARGO_SECRETARIO_AD_HOC, fontBlackBoldSmall)));

			PdfPCell cellP3 = new PdfPCell();
			cellP3.addElement(p3);
			cellP3.setBorder(0);
			cellP3.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			cellP3.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			cellP3.setPaddingTop(10);
			cellP3.setColspan(3);
			mainTable.addCell(cellP3);

			Paragraph p4 = new Paragraph(Constantes.TEXTO_LEGALIZACION_FIRMA_SECRETARIO_AD_HOC, fontBlackSmall);
			p4.setAlignment(Element.ALIGN_CENTER);

			PdfPCell emptyCell1 = new PdfPCell();
			emptyCell1.setBorder(0);
			mainTable.addCell(emptyCell1);

			PdfPCell cellP4 = new PdfPCell();
			cellP4.addElement(p4);
			cellP4.setBorder(0);
			cellP4.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			cellP4.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			cellP4.setPaddingTop(10);
			mainTable.addCell(cellP4);

			PdfPCell emptyCell2 = new PdfPCell();
			emptyCell2.setBorder(0);
			mainTable.addCell(emptyCell2);

			document.add(mainTable);
			document.close();
			fileBytes = bos.toByteArray();
		}

		String fileNameTemp = target.replace(".PDF", "_1.PDF");
		PdfStamper stamper = null;
		FileOutputStream fosTemp = null;

		try {
			fosTemp = new FileOutputStream(fileNameTemp);
			stamper = new PdfStamper(reader, fosTemp);
			stamper.setFormFlattening(true);
		} finally {
			if (stamper != null) {
				try {
					stamper.close();
				} catch (Exception e) {
				}
			}
			if (fosTemp != null) {
				try {
					fosTemp.close();
				} catch (IOException e) {
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
				}
			}
		}

		reader = new PdfReader(fileNameTemp);
		Document document = new Document();

		try (FileOutputStream out = new FileOutputStream(target)) {
			PdfCopy copy = new PdfCopy(document, out);
			copy.setFullCompression();

			document.open();
			int numPages = reader.getNumberOfPages();
			for (int index = 1; index <= numPages; index++) {
				copy.addPage(copy.getImportedPage(reader, index));
			}

			PdfReader reader2 = null;
			try {
				reader2 = new PdfReader(fileBytes);
				copy.addPage(copy.getImportedPage(reader2, 1));
			} finally {
				if (reader2 != null) {
					try {
						reader2.close();
					} catch (Exception e) {
					}
				}
			}

			document.addAuthor(Constantes.AUTHOR);
			document.addCreationDate();
			document.addProducer();
			document.addSubject(TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS.getDescripcion());
			document.addCreator(Constantes.AUTHOR);
			document.addTitle(TipoTramite.CERTIFICADO_FIRMAS_SECRETARIOS_CAMARAS.getDescripcion());
			document.addKeywords(Constantes.KEYWORDS_PDF_FIRMA_SECRETARIO);

			document.close();
			copy.close();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
				}
			}
		}

		Files.delete(Paths.get(fileNameTemp));
	}
}
