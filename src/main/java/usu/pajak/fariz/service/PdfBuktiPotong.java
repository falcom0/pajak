package usu.pajak.fariz.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.ColumnDocumentRenderer;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import static com.itextpdf.html2pdf.html.AttributeConstants.DATA;

public class PdfBuktiPotong {

    public static void main(String[] args) throws IOException {
        new PdfBuktiPotong().createPdf("/test7.pdf");
    }

    private void assignToObject(){

    }

    public void createPdf(String dest) throws IOException {
        //Initialize PDF writer
        PdfWriter writer = new PdfWriter(dest);
        //Initialize PDF document
        PdfDocument pdf = new PdfDocument(writer);
        // Initialize document
        PageSize pageSize = PageSize.A4;
        Document document = new Document(pdf, pageSize);
        document.setMargins(20, 20, 20, 20);
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        /*//Set column parameters
        float offSet = 36;
        float columnWidth = (pageSize.getWidth() - offSet * 2 + 10) / 3;
        float columnHeight = pageSize.getHeight() - offSet * 2;;

        //Define column areas
        Rectangle[] columns = {
                new Rectangle(offSet - 5, offSet, columnWidth, 400),
                new Rectangle(offSet + columnWidth, offSet, columnWidth, 400),
                new Rectangle(offSet + columnWidth * 2 + 5, offSet, columnWidth, 400)};
        document.setRenderer(new ColumnDocumentRenderer(document, columns));
        Paragraph p1 = new Paragraph("KEMENTERIAN KEUANGAN RI DIREKTORAT JENDERAL PAJAK")
                .setFont(font)
                .setFontSize(14);
        Image image = new Image(ImageDataFactory.create("/foto.png")).scaleToFit(100,100);
        document.add(image);
        document.add(p1);
        document.add(new Paragraph("BUKTI PEMOTONGAN PAJAK PENGHASILAN PASAL 21 BAGI PEGAWAI TETAP ATAU PENERIMA PENSIUN ATAU TUNJANGAN HARI TUA/JAMINAN HARI TUA BERKALA"));
*/
        Image image = new Image(ImageDataFactory.create("/foto.png")).scaleToFit(100,100);
        Table table = new Table(new float[]{3, 3, 3,3});
        table.setWidth(UnitValue.createPercentValue(100));
        Cell c1 = new Cell();
        c1.add(image);
        c1.add(new Paragraph("KEMENTERIAN KEUANGAN RI DIREKTORAT JENDERAL PAJAK"));
        table.addCell(c1);
        Cell c2 = new Cell();
        c2.add(new Paragraph("BUKTI PEMOTONGAN PAJAK PENGHASILAN PASAL 21 BAGI PEGAWAI TETAP ATAU PENERIMA PENSIUN ATAU TUNJANGAN HARI TUA/JAMINAN HARI TUA BERKALA"));
        table.addCell(c2);
        Cell c3 = new Cell(2,2);
        c3.add(new Paragraph("NOMOR: 1 . 1 - 12____ . 18____ - ____________"));
        table.addCell(c3);


//        BufferedReader br = new BufferedReader(new FileReader(DATA));
//        String line = br.readLine();

//        process(table, line, bold, true);
//        while ((line = br.readLine()) != null) {
//        process(table, line, font, false);
//        }
//        Table table = new Table(UnitValue.createPercentArray(new float[] { 4, 1, 3}))
//                .useAllAvailableWidth();
//        FileInputStream sr = File.OpenText(DATA);
//        String line = sr.readLine();
//        Process(table, line, bold, true);
//        while ((line = sr.ReadLine()) != null) {
//            Process(table, line, font, false);
//        }
//        sr.Close();
        document.add(table);
        //Close document
        document.close();
    }

    public void process(Table table, String line, PdfFont font, boolean isHeader) {
        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        while (tokenizer.hasMoreTokens()) {
            if (isHeader) {
                table.addHeaderCell(
                        new Cell().add(
                                new Paragraph(tokenizer.nextToken()).setFont(font)));
            } else {
                table.addCell(
                        new Cell().add(
                                new Paragraph(tokenizer.nextToken()).setFont(font)));
            }
        }
    }

//    public void export() throws DocumentException, IOException, URISyntaxException {
//        Document document = new Document();
////        document.
//        PdfWriter.getInstance(document, new FileOutputStream("/iTextHelloWorld.pdf"));
//
//        document.open();
//        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
////        Chunk chunk = new Chunk("Hello World", font);
//        PdfPTable table = new PdfPTable(3);
////        table.
//        addTableHeader(table);
//        addRows(table);
//        addCustomRows(table);
//
//        document.add(table);
//
////        document.add(chunk);
//        document.close();
//    }
//
//    private void addTableHeader(PdfPTable table) {
//        Stream.of("column header 1", "column header 2", "column header 3")
//                .forEach(columnTitle -> {
//                    PdfPCell header = new PdfPCell();
//                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
//                    header.setBorderWidth(2);
//                    header.setPhrase(new Phrase(columnTitle));
//                    table.addCell(header);
//                });
//    }
//
//    private void addRows(PdfPTable table) {
//        table.addCell("row 1, col 1");
//        table.addCell("row 1, col 2");
//        table.addCell("row 1, col 3");
//    }
//
//    private void addCustomRows(PdfPTable table)
//            throws URISyntaxException, BadElementException, IOException {
////        Path path = Paths.get(ClassLoader.getSystemResource("D:/foto.png").toURI());
////        Path path = Paths.get(new URI("/foto.png"));
//        Image img = Image.getInstance(new File("/foto.png").getAbsolutePath());
//        img.scalePercent(10);
//
//        PdfPCell imageCell = new PdfPCell(img);
//        table.addCell(imageCell);
//
//        PdfPCell horizontalAlignCell = new PdfPCell(new Phrase("row 2, col 2"));
//        horizontalAlignCell.setHorizontalAlignment(Element.ALIGN_CENTER);
//        table.addCell(horizontalAlignCell);
//
//        PdfPCell verticalAlignCell = new PdfPCell(new Phrase("row 2, col 3"));
//        verticalAlignCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
//        table.addCell(verticalAlignCell);
//    }
}
