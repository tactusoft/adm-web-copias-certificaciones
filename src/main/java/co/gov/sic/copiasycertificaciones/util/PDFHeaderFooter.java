package co.gov.sic.copiasycertificaciones.util;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;

public class PDFHeaderFooter extends PdfPageEventHelper {

    protected String barcode;
    protected PdfPTable tableHeader;
    protected float tableHeightHeader;
    protected PdfPTable tableFooter;
    protected float tableHeightFooter;
    protected static final float spacingAfterHeader = 15f;
    protected static final float spacingBeforeFooter = 10f;
    public static final int MARGIN_LEFT = 52;
    public static final int MARGIN_RIGHT = 52;
    public static final int MARGIN_BOTTOM = 25;
    public static final int MARGIN_TOP = 25;
    public static final int QR_CODE_SIZE = 180;
    public static final float IMAGE_300_DPI = 72f / 300f;
    private static final BaseColor colorBlue = new BaseColor(60, 98, 171);
    private static final BaseColor colorBlack = new BaseColor(0, 0, 0);
    private static final BaseColor colorGray = new BaseColor(231, 232, 231);
    private static final Font footerFontBlack = new Font(Font.FontFamily.HELVETICA, Constantes.PDF_FOOTER_FONT_SIZE, Font.NORMAL, colorBlack);
    private static final Font footerFontBlackStrong = new Font(Font.FontFamily.HELVETICA, Constantes.PDF_FOOTER_FONT_SIZE, Font.BOLD, colorBlack);
    private static final Font linkFontBlue = new Font(Font.FontFamily.HELVETICA, Constantes.PDF_FOOTER_FONT_SIZE, Font.NORMAL, colorBlue);
    private static final Font linkFont = new Font(Font.FontFamily.HELVETICA, Constantes.PDF_FOOTER_FONT_SIZE, Font.NORMAL, colorBlack);
    protected final Logger logger = LoggerFactory.getLogger(PDFHeaderFooter.class);

    public PdfPCell getResourceImageAsPDFCell(String imageName) throws Exception {
        Image img = getResourceImage(imageName);
        PdfPCell cell = new PdfPCell(img, false);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(0);
        return cell;
    }

    public Image getResourceImage(String imageName) throws Exception {
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String path = servletContext.getRealPath(String.format("WEB-INF/templates/img/%s", imageName));
        Image img = Image.getInstance(path);
        img.scaleAbsolute(IMAGE_300_DPI * img.getWidth(), IMAGE_300_DPI * img.getHeight());
        return img;
    }
    
    private PdfPCell getImageCodeQRAsPDFCell() throws Exception {
        byte[] imageInByte = Utility.getImageCodeQRAsBytes(Constantes.URL_ENCUESTA);
        Image img = Image.getInstance(imageInByte);
        img.setBorder(0);
        img.setUrl(new URL(Constantes.URL_ENCUESTA));
        img.scaleAbsolute(IMAGE_300_DPI * img.getWidth(), IMAGE_300_DPI * img.getHeight());
        
        PdfPCell cell = new PdfPCell(img, false);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(0);
        cell.setPadding(0);
        return cell;
    }


    private PdfPCell getEmptyCell() {
        PdfPCell emptyCell = new PdfPCell();
        emptyCell.setBorder(0);
        return emptyCell;
    }

    public PDFHeaderFooter(boolean addQRCode, boolean addHeader, boolean addFooter, String barcode) throws Exception {
        this.barcode = barcode;

        float totalTableWidth = PageSize.LETTER.getWidth() - MARGIN_LEFT
                - MARGIN_RIGHT;

        if (addHeader) {
            tableHeader = new PdfPTable(3);
            tableHeader.setTotalWidth(totalTableWidth);
            tableHeader.setLockedWidth(true);
            tableHeader.addCell(getEmptyCell());
            PdfPCell cellLogoSIC = getResourceImageAsPDFCell("sic.png");
            tableHeader.addCell(cellLogoSIC);
            //Es necesario agregar una celda vacia para obtener la altura total de la tabla para el margen superior
            tableHeader.addCell(getEmptyCell());
            tableHeightHeader = tableHeader.getTotalHeight();
            //Si se va a gregar un codigo de barras, se debe borrar las celdas agreegadas, volver a agregarlas y dejar la ultima celda para el codigo de barras mas adelante en el evento cirre de la pagina
            if (!Utility.isNullOrEmptyTrim(barcode)) {
                tableHeader.deleteBodyRows();
                cellLogoSIC.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                tableHeader.addCell(cellLogoSIC);
                tableHeader.addCell(getEmptyCell());
            }
        }
        tableFooter = new PdfPTable(new float[]{5f, 56f, 31f, 8f});
        tableFooter.setTotalWidth(PageSize.LETTER.getWidth() - 60);
        tableFooter.setLockedWidth(true);
        if (addQRCode) {
            PdfPCell cell1 = new PdfPCell();
            Paragraph p1 = new Paragraph(
                    "La información recolectada en este documento aplica y respeta todos los principios y disposiciones contemplados en la Ley 1581 de 2012 para la Protección de Datos Personales, sus decretos reglamentarios y demás normatividad vigente que la complemente, modifique o derogue. Sus datos serán usados únicamente en ejercicio de las funciones propias de la Superintendencia de Industria y Comercio. Puede consultar nuestra política de protección de datos personales en el enlace ", footerFontBlack);
            Anchor anchor1 = new Anchor(Constantes.URL_DATOS_PERSONALES.replace(Constantes.URL_PROTOCOL_HTTPS, Constantes.STR_EMPTY), linkFontBlue);
            anchor1.setReference(Constantes.URL_DATOS_PERSONALES);
            p1.add(anchor1);
            p1.add(". ");
            Chunk chunk = new Chunk("Lo invitamos a evaluar su experiencia en la generación de este certificado a través del siguiente código QR:", footerFontBlackStrong);
            p1.add(chunk);
            p1.setAlignment(Element.ALIGN_LEFT);
            cell1.addElement(p1);
            cell1.setColspan(3);
            cell1.setBorder(0);
            cell1.setPaddingBottom(6);
            cell1.setPaddingLeft(5);
            tableFooter.addCell(cell1);

            PdfPCell cellQR = getImageCodeQRAsPDFCell();
            tableFooter.addCell(cellQR);
        }
        if (addFooter) {
            PdfPCell cell = new PdfPCell();
            Paragraph p1 = new Paragraph(
                    "Señor ciudadano, para hacer segumiento a su solicitud, la entidad le ofrece los siguientes canales:",
                    footerFontBlackStrong);
            p1.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p1);

            Paragraph p2 = new Paragraph("", footerFontBlack);
            Anchor anchor = new Anchor(Constantes.URL_SIC.replace(Constantes.URL_PROTOCOL_HTTPS, Constantes.STR_EMPTY), linkFont);
            anchor.setReference(Constantes.URL_SIC);
            p2.add(anchor);
            p2.add(" - Teléfono en Bogotá: 5920400 - Línea gratuita a nivel nacional: 018000910165");
            p1.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p2);

            Paragraph p3 = new Paragraph("Dirección: Cra. 13 # 27-00 pisos 1,3,4,5,6,7 y 10 - Bogotá D.C. - Colombia",
            		footerFontBlack);
            p3.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p3);

            Paragraph p4 = new Paragraph("Teléfono: (571) 5870000 - e-mail: ", footerFontBlack);
            Anchor anchor2 = new Anchor("contactenos@sic.gov.co", linkFont);
            anchor2.setReference("mailto:contactenos@sic.gov.co");
            p4.add(anchor2);
            p4.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p4);

            cell.setPaddingBottom(5);
            cell.setPaddingTop(0);
            cell.setPaddingLeft(5);
            cell.setPaddingRight(5);
            cell.setBorderWidthRight(2);
            cell.setBorderWidthLeft(2);
            cell.setBorderWidthTop(0);
            cell.setBorderWidthBottom(0);
            cell.setBorderColor(colorGray);
            cell.setColspan(2);
            tableFooter.addCell(cell);

            PdfPCell cellLogoVerde = getResourceImageAsPDFCell("verde.png");
            cellLogoVerde.setRowspan(2);
            cellLogoVerde.setColspan(2);
            cellLogoVerde.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            tableFooter.addCell(cellLogoVerde);

            PdfPCell cellBandera = getResourceImageAsPDFCell("bandera.jpg");
            cellBandera.setPaddingTop(10);
            cellBandera.setColspan(4);
            cellBandera.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            //tableFooter.addCell(cellBandera);
        }
        tableHeightFooter = tableFooter.getTotalHeight();

    }

    public float getTableHeaderHeight() {
        return tableHeightHeader + spacingAfterHeader;
    }

    public float getTableFooterHeight() {
        return tableHeightFooter + spacingBeforeFooter;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {

    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        addHeader(writer, document);
        addFooter(writer, document);
    }

    private void addBarcode(PdfWriter writer) {
        Barcode128 code128 = new Barcode128();
        code128.setFont(null);
        code128.setCode(this.barcode);
        code128.setCodeType(Barcode128.CODE128);
        Image img = code128.createImageWithBarcode(writer.getDirectContent(), null, null);
        img.setAlignment(Image.ALIGN_RIGHT);
        PdfPCell cellBarCode = new PdfPCell();
        cellBarCode.addElement(img);
        Paragraph phraseCod = new Paragraph(barcode, footerFontBlack);
        phraseCod.setAlignment(Paragraph.ALIGN_RIGHT);
        cellBarCode.addElement(phraseCod);
        cellBarCode.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cellBarCode.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cellBarCode.setBorder(0);
        cellBarCode.setPadding(0);
        cellBarCode.setPaddingLeft(10);
        tableHeader.addCell(cellBarCode);
        tableHeightHeader = tableHeader.getTotalHeight();
    }

    private void addHeader(PdfWriter writer, Document document) {
        if (tableHeader != null) {
            if (!Utility.isNullOrEmptyTrim(this.barcode)) {
                addBarcode(writer);
            }
            tableHeader.writeSelectedRows(0, -1, document.left(),
                    document.top() + tableHeightHeader + spacingAfterHeader, writer.getDirectContent());
        }
    }

    private void addFooter(PdfWriter writer, Document document) {
        if (tableFooter != null) {
            tableFooter.writeSelectedRows(0, -1, MARGIN_BOTTOM + 5,
                    document.bottom() - spacingBeforeFooter, writer.getDirectContent());
        }
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {

    }

}
