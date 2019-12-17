package usu.utbk.verification;

import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VerificationPeserta {

    public static void main(String[] args) throws IOException {
        List<JsonObject> listHtml = new VerificationPeserta().mergeAllHTML(args[0]);
        List<JsonObject> listExcel = new VerificationPeserta().mergeAllExcel(args[0]);
        listExcel.forEach(row -> {
            String id = row.get("id").getAsString();
            String name = row.get("name").getAsString();
            Predicate<JsonObject> p1 = s -> s.get("id").getAsString().equalsIgnoreCase(id);
            Predicate<JsonObject> p2 = s -> s.get("name").getAsString().equalsIgnoreCase(name);
            if(listHtml.stream().anyMatch(p1))
                row.addProperty("result",listHtml.stream().anyMatch(p2));
            else
                row.addProperty("result",listHtml.stream().anyMatch(p1));
        });

        List<JsonObject> listResult = listExcel.stream().filter(row -> {
            if (!row.get("result").getAsBoolean())
                return true;
            else
                return false;
        }).collect(Collectors.toList());
        if(listResult.size()>0){
            Iterator<JsonObject> iterator = listResult.iterator();
            while(iterator.hasNext()){
                JsonObject obj = iterator.next();
                System.out.println(obj.toString());
            }
        }else
            System.out.println("All Data are VERIFIED. Total Data : "+listExcel.size());
    }

    private void test() throws IOException {
        int sesi = 2;
        int idHtml;
        int idXls;

        for(int y = 0; y < 3; y++) {
            idHtml = 1;
//            idXls = 58;

//                File newFile = new File("/UTBK/excel/sesi"+sesi+"/peserta_12102" + String.format("%02d", idXls) + "_sesi_"+String.format("%02d", sesi)+".xlsx");
            File newFile = new File("/UTBK/excel/peserta_1210101_sesi_"+String.format("%02d", sesi)+".xlsx");
            FileInputStream inputStream = new FileInputStream(newFile);
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 1;
            Row header;
            Cell cell;

            for (int x = 0; x < 17; x++) {
                File input = new File("/UTBK/html/BAPU_121"+String.format("%02d", sesi)+"01" + String.format("%02d", idHtml) + ".html");
                org.jsoup.nodes.Document doc = null;
                try {
                    doc = Jsoup.parse(input, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Element table : doc.select("table.tabelpesertaborder2")) {

                    // loop through all tr of table
                    for (Element row : table.select("tr")) {
                        // create row for each            tag
                        if(sheet.getRow(rowCount)!=null)
                            header = sheet.getRow(rowCount);
                        else
                            header = sheet.createRow(rowCount);
                        // loop through all tag of    tag
//                Elements ths = row.select("th");
                        int count = 0;
                /*for (Element element : ths) {
                    // set header style
                    cell = header.createCell(count);
                    cell.setCellValue(element.text());
//                    cell.setCellStyle(headerStyle);
                    count++;
                }*/
                        // now loop through all td tag
                        if (row.select("th").isEmpty()) {
                            Elements tds = row.select("td");
                            count = 7;
                            for (int i = 0; i < tds.size(); i++) {
                                // create cell for each tag
                                if (i == 2) {
                                    Element element = tds.get(i);
                                    if (!element.text().isEmpty()) {
                                        System.out.println("BAPU_121"+String.format("%02d", sesi)+"01" + String.format("%02d", idHtml) + ".html");
                                        System.out.println(element.text());
                                        cell = header.createCell(count);
                                        cell.setCellValue(element.text());
                                        cell = header.createCell(count + 1);
                                        cell.setCellFormula("IF(G" + (rowCount + 1) + "=H" + (rowCount + 1) + ",\"OK\",\"TIDAK\")");
                                        cell.setCellType(CellType.FORMULA);
                                    }
                                }
                            }
                            rowCount++;
                        }
                        // set auto size column for excel sheet
                        sheet = workbook.getSheetAt(0);
                        for (int j = 0; j < row.select("th").size(); j++) {
                            sheet.autoSizeColumn(j);
                        }
                    }
//            rowCount++;
                }

                idHtml++;
//                idXls++;
            }

            try (OutputStream fileOut = new FileOutputStream(newFile)) {
                workbook.write(fileOut);
            }
            workbook.close();
            sesi++;
        }
    }

    private List<JsonObject> mergeAllHTML(String fileDir)throws IOException{
        File directory = new File(fileDir);
        List<JsonObject> listPesertaHtml = new ArrayList<>();
        if(directory.isDirectory()){
            File[] fileHtmls = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.getName().endsWith(".htm"))
                        return true;
                    else
                        return pathname.getName().endsWith(".html");
                }
            });
            for(File file : fileHtmls){
                org.jsoup.nodes.Document doc = null;
                doc = Jsoup.parse(file, "UTF-8");
                for (Element table : doc.select("table.tabelpesertaborder2")) {
                    for (Element row : table.select("tr")) {
                        if (row.select("th").isEmpty()) {
                            Elements tds = row.select("td");
                            for (int i = 0; i < tds.size(); i++) {
                                if (i == 2) {
                                    Element element = tds.get(i);
                                    if (!element.text().isEmpty()) {
                                        JsonObject obj = new JsonObject();
                                        Element idElement = tds.get(1);
                                        obj.addProperty("id",idElement.text());
                                        obj.addProperty("name",element.text().replace(" ",""));
                                        obj.addProperty("file",file.getName());
                                        listPesertaHtml.add(obj);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }else{
            System.out.println("Directory not found");
            System.exit(-1);
        }
        return listPesertaHtml;
    }

    private List<JsonObject> mergeAllExcel(String fileDir) throws IOException {
        File directory = new File(fileDir);
        List<JsonObject> listPesertaExcel = new ArrayList<>();
        if(directory.isDirectory()) {
            File[] fileExcels = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".xlsx");
                }
            });
            for (File file : fileExcels) {
                FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                AtomicInteger count = new AtomicInteger();
                sheet.forEach(row -> {
                    if(count.get() >= 1) {
                        String id = row.getCell(5).getStringCellValue();
                        String name = row.getCell(6).getStringCellValue().replace(" ","");
                        String lokasi = row.getCell(2).getStringCellValue();
                        String ruang = row.getCell(4).getStringCellValue();
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", id);
                        obj.addProperty("name", name);
                        obj.addProperty("lokasi",lokasi);
                        obj.addProperty("ruang",ruang);
                        obj.addProperty("file",file.getName());
                        listPesertaExcel.add(obj);
                    }
                    count.getAndIncrement();
                });

                try (OutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                workbook.close();
            }
        }else{
            System.out.println("Directory not found");
            System.exit(-1);
        }

        return listPesertaExcel;
    }
}
