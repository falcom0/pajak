package usu.pajak.converter;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFMarkedContentExtractor;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConvertPdf2Excel {

    public static void main(String args[]) throws IOException {
        new ConvertPdf2Excel().convert();
    }

    private void convert() throws IOException {
        /*PdfReader reader = new PdfReader(new FileInputStream("/pdf/000 USU.pdf"));
        PdfDocument pdf = new PdfDocument(reader);
        Document document = new Document(pdf);
        document.getPdfDocument().getFirstPage();
        document.close();
        pdf.close();
        reader.close();*/
//        new PDFMarkedContentExtractor().getCurrentPage().
        PDDocument document = PDDocument.load(new File("/pdf/000 USU.pdf"));
        document.getClass();

        if (!document.isEncrypted()) {

            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);

            PDFTextStripper tStripper = new PDFTextStripper();

            String pdfFileInText = tStripper.getText(document);
            //System.out.println("Text:" + st);

            // split by whitespace
            String lines[] = pdfFileInText.split("\\r?\\n");
            for (String line : lines) {
                System.out.println(line);
            }

        }
    }
}
