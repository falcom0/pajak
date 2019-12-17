package usu.utbk.print;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.text.DocumentException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PrintCard {
    public static void main(String[] args) throws DocumentException, SAXException, ParserConfigurationException {
        try {
            PrintCard pc = new PrintCard();
            pc.generatePDFFromHTML("target/input/test.html");
//            pc.mergePdf();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergePdf() throws IOException {
        PDDocument document = new PDDocument();
        String[] pdfFiles = new String[96];
        String idTemplate = "target/output/ITS";
        for(int i=1; i<=96;i++) {
            String id = idTemplate + String.format("%03d", i);
            pdfFiles[i-1] = id+".pdf";
        }
        PDFMergerUtility ut = new PDFMergerUtility();

        for (String pdfFile: pdfFiles) {
            ut.addSource(new File(pdfFile));
        }
        ut.setDestinationStream(new FileOutputStream("/testMerge.pdf"));
        ut.mergeDocuments();
    }

    private void generatePDFFromHTML(String filename) throws IOException, DocumentException, ParserConfigurationException, SAXException {
//        cardPengawas();
        cardITSupport();
//        updateNpwpGolonganPengawas();
    }

    private void updateNpwpGolonganPengawas() throws IOException {
        File newFile = new File("/daftar_pengawas_utbk_2019.xls");
        FileInputStream inputStream = new FileInputStream(newFile);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(1);
        sheet.forEach(row -> {

            String nip = row.getCell(3).getStringCellValue();
            JsonElement je = null;
            try {
                je = new Gson().fromJson(callApi("https://api.usu.ac.id/0.1/users/search?query="+nip,"GET"), JsonElement.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

                 if(je != null) {
                JsonArray jsonArray = je.getAsJsonObject().get("response").getAsJsonObject().get("data").getAsJsonArray();
                if(jsonArray.size()>0) {
                    JsonObject jo = jsonArray.get(jsonArray.size() - 1).getAsJsonObject();
                    Cell cell = row.createCell(7);
                    cell.setCellValue(jo.get("npwp").getAsString());

                    cell = row.createCell(8);
                    if (jo.has("rank")) {
                        JsonObject jsonCode = jo.get("rank").getAsJsonObject();
                        if (jsonCode != null) {
                            System.out.println(nip);
                            JsonElement golonganElement = jsonCode.get("code");
                            if (!golonganElement.isJsonNull()) {
                                cell.setCellValue(golonganElement.getAsString());

                            } else
                                cell.setCellValue("");
                        } else
                            cell.setCellValue("");
                    } else {
                        cell.setCellValue("");
                    }
                }else{

                }
            }
        });

        try (OutputStream fileOut = new FileOutputStream(newFile)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

   private void cardPengawas() throws IOException {
       AtomicReference<String> nama = new AtomicReference<>("Hendi");
       AtomicReference<String> id = new AtomicReference<>("ITS007");
       AtomicReference<String> unit = new AtomicReference<>("-");
       AtomicReference<String> lokasi1 = new AtomicReference<>("-");
       AtomicReference<String> jabatan = new AtomicReference<>("IT-SUPPORT");
       AtomicReference<String> ruang = new AtomicReference<>("-");
       AtomicReference<String> foto = new AtomicReference<>("-");
       AtomicReference<String> tanggal = new AtomicReference<>("-");
       AtomicReference<String> alamat = new AtomicReference<>("-");

       ConverterProperties properties = new ConverterProperties();
       properties.setBaseUri("target/input/");
//       PdfWriter writer = new PdfWriter("target/output/kartuAdminTeknis.pdf");
       PdfWriter writer = new PdfWriter("target/output/kartuPengawas27-28.pdf");
       PdfDocument pdf = new PdfDocument(writer);
       Document document = new Document(pdf);

       String idTemplate = "ASTR-";
       final int[] count = {1};
       Workbook workbook = WorkbookFactory.create(new File("/daftar_pengawas_utbk_2019.xls"));
       Sheet sheet = workbook.getSheetAt(0);
//       Row row = sheet.getRow(0);
       sheet.forEach(row -> {
           nama.set(row.getCell(5).getStringCellValue());
//           id.set(idTemplate + String.format("%03d", count[0]));
           id.set(Double.toString(row.getCell(3).getNumericCellValue()));
           unit.set(row.getCell(6).getStringCellValue());
           lokasi1.set(row.getCell(0).getStringCellValue());
           jabatan.set(row.getCell(7).getStringCellValue());
           ruang.set(row.getCell(1).getStringCellValue());
           if(row.getCell(4).getCellType().compareTo(CellType.NUMERIC) == 0){
               foto.set(Double.toString(row.getCell(4).getNumericCellValue()));
           }else{
               foto.set(row.getCell(4).getStringCellValue());
           }
           tanggal.set(row.getCell(2).getStringCellValue());
           alamat.set(row.getCell(8).getStringCellValue());

           File input = new File("target/input/index.html");
           org.jsoup.nodes.Document doc = null;
           try {
               doc = Jsoup.parse(input, "UTF-8");
           } catch (IOException e) {
               e.printStackTrace();
           }

           Elements nameEl = doc.select("td.name"); // a with href
           nameEl.append(nama.get());

           nameEl = doc.select("img.pasfoto");
           try {
               if(callApiUsu("https://simsdm.usu.ac.id/photos/"+foto+".jpg","GET")) {
                   nameEl.attr("src", "https://simsdm.usu.ac.id/photos/" + foto + ".jpg");
               }else{
                   nameEl.attr("src", "images/foto.png");
               }
           } catch (IOException e) {
               e.printStackTrace();
           }

           Elements idEl = doc.select("td.idenditas"); // a with href
           idEl.append(id.get());

           nameEl = doc.select("p.name"); // a with href
           nameEl.append(nama.get());

           nameEl = doc.select("h1.judul"); // a with href
           nameEl.append(jabatan.get());

           nameEl = doc.select("p.unit"); // a with href
           nameEl.append(unit.get());

           idEl = doc.select("p.idenditas"); // a with href
           idEl.append(id.get());

           nameEl = doc.select("p.lokasi1"); // a with href
           nameEl.append(lokasi1.get());

           idEl = doc.select("p.jabatan"); // a with href
           idEl.append(jabatan.get());

           idEl = doc.select("p.ruang"); // a with href
           idEl.append(ruang.get());

           idEl = doc.select("p.tanggal"); // a with href
           idEl.append(tanggal.get());

           idEl = doc.select("p.alamat"); // a with href
           idEl.append(alamat.get());

           List<IElement> elements =
                   null;
           try {
               elements = HtmlConverter.convertToElements(doc.html(), properties);
           } catch (IOException e) {
               e.printStackTrace();
           }
           pdf.setTagged();

           document.setMargins(5, 5, 50, 5);
           for (IElement element : elements) {
               document.add((IBlockElement) element);
           }
           document.add(new AreaBreak());

           count[0]++;
       });

       document.close();
   }

   private void cardITSupport() throws IOException {
       String idTemplate = "ITS";
       final int[] count = {25};
       Workbook workbook = WorkbookFactory.create(new File("/list Teknis Support.xls"));
       Sheet sheet = workbook.getSheetAt(1);
       sheet.forEach(row -> {
           String nama = row.getCell(0).getStringCellValue();
           String id = idTemplate+String.format("%03d" , count[0]);

           try {
               ConverterProperties properties = new ConverterProperties();
               properties.setBaseUri("");

               PdfWriter writer = new PdfWriter("target/output/"+id+".pdf");
               PdfDocument pdf = new PdfDocument(writer);

               File input = new File("target/input/index.html");

               org.jsoup.nodes.Document doc = Jsoup.parse(input, "UTF-8");

               Elements nameEl = doc.select("td.name"); // a with href
               nameEl.append(nama);

               Elements idEl = doc.select("td.idenditas"); // a with href
               idEl.prepend(id);

               List<IElement> elements =
                       HtmlConverter.convertToElements(doc.html(), properties);
               pdf.setTagged();
               Document document = new Document(pdf);
               document.setMargins(5, 5, 50, 5);
               for (IElement element : elements) {
                   document.add((IBlockElement)element);
               }
               document.close();

           } catch (IOException e) {
               e.printStackTrace();
           }

           count[0]++;
       });
   }


    private static boolean callApiUsu(String ep, String method) throws IOException {
//        String endpoint = "https://api.usu.ac.id/0.2/salary_receipts";
        URL obj = new URL(ep);
        HttpsURLConnection conn= (HttpsURLConnection) obj.openConnection();

        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( false );
        conn.setDoOutput( true );
        conn.setDoInput(true);

//        DataOutputStream wr;
//        wr = new DataOutputStream(conn.getOutputStream());
//        wr.writeBytes(postData);
//        wr.flush();
//        wr.close();
        StringBuffer response = new StringBuffer();
        if(conn.getResponseCode() == 200) {
            return true;
        }else{
            return false;
        }
    }

    private static String callApi(String ep, String method) throws IOException {
//        String endpoint = "https://api.usu.ac.id/0.2/salary_receipts";
        URL obj = new URL(ep);
        HttpsURLConnection conn= (HttpsURLConnection) obj.openConnection();

        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( false );
        conn.setDoOutput( true );
        conn.setDoInput(true);

//        DataOutputStream wr;
//        wr = new DataOutputStream(conn.getOutputStream());
//        wr.writeBytes(postData);
//        wr.flush();
//        wr.close();
        StringBuffer response = new StringBuffer();
        if(conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
//            System.out.println(inputLine);
            }
            in.close();
            return response.toString();
        }else{
            return null;
        }
    }
}
