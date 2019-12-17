package usu.pajak.converter;

import com.mongodb.BasicDBObject;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ReportAsetUsu {
    public static void main(String[] args) throws IOException {
//        new ReportAsetUsu().readFile("/pdf/temp");
        new ReportAsetUsu().rekapFile("/pdf/hasil_All.xlsx");
    }

    public void rekapFile(String fileName) throws IOException {
        FileInputStream inputStream = new FileInputStream(new File(fileName));
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        AtomicInteger countRow = new AtomicInteger();
        List<BasicDBObject> listObject = new ArrayList<>();
        AtomicReference<Double> price = new AtomicReference<>(0.0);
        sheet.forEach(row -> {
            if(row.getCell(0)==null) {
                if (row.getCell(2).getCellType().compareTo(CellType.STRING) == 0) {
                    Row rowName = sheet.getRow(countRow.get() - 1);
                    BasicDBObject obj = new BasicDBObject();
                    obj.put("name", rowName.getCell(1).getStringCellValue());
                    obj.put("month", rowName.getCell(2).getNumericCellValue());
                    obj.put("year", rowName.getCell(3).getNumericCellValue());
                    obj.put("price", price.get());
                    price.set(0.0);
                    listObject.add(obj);
                }
            }else{
                price.set(price.get() + row.getCell(4).getNumericCellValue());
            }
            countRow.getAndIncrement();
        });

        Sheet sheetRekap = workbook.createSheet("Rekap2");
        countRow.set(1);
        listObject.forEach(r -> {
            Row row = sheetRekap.createRow(countRow.get());
            row.createCell(0).setCellValue(countRow.get()+".");
            row.createCell(1).setCellValue(r.getString("name"));
            row.createCell(2).setCellValue(r.getInt("month"));
            row.createCell(3).setCellValue(r.getInt("year"));
            row.createCell(4).setCellValue(r.getDouble("price"));
            countRow.getAndIncrement();
        });

        try (OutputStream fileOut = new FileOutputStream("/pdf/hasil_All.xlsx")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public void readFile(String fileDir) throws IOException {
        File directory = new File(fileDir);
        if(directory.isDirectory()) {
            File[] fileExcels = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".xlsx");
                }
            });
            AtomicReference<String> namaItem= new AtomicReference<>("");
            AtomicReference<String> id= new AtomicReference<>("");
            List<BasicDBObject> listObject = new ArrayList<>();
            for (File file : fileExcels) {
                System.out.println(file.getName());
                FileInputStream inputStream = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                AtomicInteger counts = new AtomicInteger(0);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                sheet.forEach(row ->{
                    if(counts.get()<999999999) {
                        System.out.println("Here");
                        if (row.getCell(0).getCellType().compareTo(CellType.NUMERIC) == 0) {
                            Date date = null;
                        if(row.getCell(4).getDateCellValue()!=null) {
                            date = row.getCell(4).getDateCellValue();
                        }else{
                            date = row.getCell(8).getDateCellValue();
                        }
                            String[] stringDate = sdf.format(date).split("-");
                            Integer month = Integer.parseInt(stringDate[1]);
                            Integer year = Integer.parseInt(stringDate[0]);
                            if (year >= 2015) {
                                Double harga = null;
                                if(!(row.getCell(6).getCellType().compareTo(CellType.BLANK)==0))
                                    if(row.getCell(6).getCellType().compareTo(CellType.STRING)==0) {
                                        harga = Double.parseDouble(row.getCell(6).getStringCellValue().substring(1).trim().replace(",", ""));
                                    } else
                                        harga = row.getCell(6).getNumericCellValue();
                                else {
                                    if (row.getCell(11).getCellType().compareTo(CellType.STRING) == 0) {
                                        harga = Double.parseDouble(row.getCell(11).getStringCellValue().substring(1).trim().replace(",", ""));
                                    } else
                                        harga = row.getCell(11).getNumericCellValue();
                                }

                                BasicDBObject obj = new BasicDBObject();
                                obj.put("id", id.get());
                                obj.put("name", namaItem.get());
                                obj.put("month", month);
                                obj.put("year", year);
                                obj.put("harga", harga);
                                listObject.add(obj);
                            }
                        } else if (row.getCell(1).getCellType().compareTo(CellType.STRING) == 0) {
                            if (row.getCell(1).getStringCellValue().contains("(")) {
                                String[] split = row.getCell(1).getStringCellValue().split("\\(");
                                namaItem.set(split[1].replace(")", ""));
                                id.set(split[0].trim());
                            }
                        }

                        System.out.println(counts.get());
                        counts.getAndIncrement();
                    }
                });

//                inputStream = new FileInputStream("/pdf/Hasil.xlsx");

            }
            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("Hasil");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("No.");
            row.createCell(1).setCellValue("Nama Harta");
            row.createCell(2).setCellValue("Bulan Perolehan");
            row.createCell(3).setCellValue("Tahun Perolehan");
            row.createCell(4).setCellValue("Harga Perolehan");

            AtomicInteger count = new AtomicInteger(1);
            Sheet finalSheet = sheet;
            listObject.forEach(r -> {
                Row ro = finalSheet.createRow(count.get());
                ro.createCell(0).setCellValue(count+".");
                ro.createCell(1).setCellValue(r.getString("name"));
                ro.createCell(2).setCellValue(r.getInt("month"));
                ro.createCell(3).setCellValue(r.getInt("year"));
                ro.createCell(4).setCellValue(r.getDouble("harga"));
                count.getAndIncrement();
            });

            try (OutputStream fileOut = new FileOutputStream("/pdf/hasil_Perpustakaan")) {
                workbook.write(fileOut);
            }
            workbook.close();
        }
    }
}
