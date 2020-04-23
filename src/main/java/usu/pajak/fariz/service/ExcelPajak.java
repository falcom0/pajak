package usu.pajak.fariz.service;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.*;
import usu.pajak.model.Salary;
import usu.pajak.model.SalaryDetail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.util.List;

public class ExcelPajak {
    private String[] headerPajak = new String[]{"Masa Pajak","Tahun Pajak","Pembetulan","NPWP","Nama","Kode Pajak","Jumlah Bruto","Jumlah PPh"};
    private String[] headerPajakLuar = new String[]{"Masa Pajak","Tahun Pajak","Pembetulan","Nomor Bukti Potong","NPWP","NIK","Nama","Alamat","WP Luar Negeri","Kode Negara",
            "Kode Pajak","Jumlah Bruto","Jumlah DPP","Tanpa NPWP","Tarif","Jumlah PPh","NPWP Pemotong","Nama Pemotong","Tanggal Bukti Potong"};
    private static CellStyle currency;

    public static void main(String[] args) {
        try {
            new ExcelPajak().export(true, "BOPTN-BH","LUAR-BPPTN",2019);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void export(boolean pegawaiLuar, String sumberDana, String filename, int year) throws IOException {
        String response = ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.1/units","GET");
        DataUnit dataUnit = new Gson().fromJson(response, DataUnit.class);
        String[] months = new DateFormatSymbols().getMonths();
        for(int i=0;i<12;i++){
            Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
            currency = workbook.createCellStyle();
            currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            for(int j=1;j<dataUnit.getData().size();j++){
                Parent par = dataUnit.getData().get(j);
                Children children = new Children();
                children.setId(par.getId());
                children.setName(par.getName());

                setToExcel(i,children,sumberDana,pegawaiLuar,year,workbook);
            }

            for(int j=0;j<dataUnit.getData().get(0).getChildren().size();j++) {
                Children children = dataUnit.getData().get(0).getChildren().get(j);
                setToExcel(i,children,sumberDana,pegawaiLuar,year,workbook);
            }

            try (OutputStream fileOut = new FileOutputStream("D:/PJK-usu/["+filename+"]PAJAK_"+year+"_NON_FINAL_BULAN_"+(i+1)+".xls")) {
                workbook.write(fileOut);
            }
            workbook.close();
            System.out.println(i+1);
        }
    }

    private void setToExcel(int i, Children children, String sumberDana, boolean pegawaiLuar, int year, Workbook workbook) throws IOException {
        Salary salary = new Gson().fromJson(
                ReceiveRka.getInstance.callApiUsu(
                        "https://api.usu.ac.id/0.2/salary_receipts?status=1&year="+year+"&source_of_fund="+sumberDana+
                                "&month="+(i+1)+"&unit_id="+children.getId()+"&mode=summary2", "GET")
                , Salary.class);
        if(salary.getResponse() != null) {
            List<SalaryDetail> listSalaryDetail = salary.getResponse().getSalary_receivers();
            if (listSalaryDetail.size() > 0) {
                Sheet sheet = workbook.createSheet(children.getName().replaceAll("/", ""));
                Row row = sheet.createRow(0);
                if (!pegawaiLuar) {
                    for (int k = 0; k < headerPajak.length; k++) {
                        row.createCell(k).setCellValue(headerPajak[k]);
                    }
                } else {
                    for (int k = 0; k < headerPajakLuar.length; k++) {
                        row.createCell(k).setCellValue(headerPajakLuar[k]);
                    }
                }

                int count = 1;
                for (SalaryDetail up : listSalaryDetail) {
                    Integer totalBruto = up.getSummary().get("total").getAsInt();
                    Integer totalPph21 = up.getSummary().get("pph21").getAsInt();
                    BigInteger totalPKP = BigInteger.valueOf(0);
                    BigInteger tarif = BigInteger.valueOf(0);

                    if (totalBruto > 0) {
                        Row rowPajak = sheet.createRow(count);
                        if (!pegawaiLuar) {
                            Cell cell = rowPajak.createCell(0);
                            cell.setCellValue(i + 1);
                            cell = rowPajak.createCell(1);
                            cell.setCellValue(year);
                            cell = rowPajak.createCell(2);
                            cell.setCellValue(0);
                            cell = rowPajak.createCell(3);
                            cell.setCellValue((up.getUser().getNpwp()));
                            cell = rowPajak.createCell(4);
                            cell.setCellValue(up.getUser().getFull_name());
                            cell = rowPajak.createCell(5);
                            cell.setCellValue("21-100-01");
                            cell = rowPajak.createCell(6);
                            cell.setCellValue(totalBruto);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellStyle(currency);

                            cell = rowPajak.createCell(7);
                            cell.setCellValue(totalPph21);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellStyle(currency);
                        } else {
                            Cell cell = rowPajak.createCell(0);
                            cell.setCellValue(i + 1);
                            cell = rowPajak.createCell(1);
                            cell.setCellValue(year);
                            cell = rowPajak.createCell(2);
                            cell.setCellValue(0);
                            cell = rowPajak.createCell(4);
                            cell.setCellValue((up.getUser().getNpwp()));
                            cell = rowPajak.createCell(6);
                            cell.setCellValue(up.getUser().getFull_name());
                            cell = rowPajak.createCell(8);
                            cell.setCellValue("N");
                            cell = rowPajak.createCell(10);
                            cell.setCellValue("21-100-03");
                            cell = rowPajak.createCell(11);
                            cell.setCellValue(totalBruto);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellStyle(currency);

                            cell = rowPajak.createCell(12);
                            cell.setCellValue(totalPKP.intValue()); //DPP
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellStyle(currency);

                            cell = rowPajak.createCell(13);
                            cell.setCellValue("Y");

                            cell = rowPajak.createCell(14);
                            cell.setCellValue(tarif.intValue()); //Tarif
                            cell.setCellType(CellType.NUMERIC);

                            cell = rowPajak.createCell(15);
                            cell.setCellValue(totalPph21);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellStyle(currency);
                        }
                        count++;
                    }
                }

                if (count > 1) {
                    if (!pegawaiLuar) {
                        Row rowTotal = sheet.createRow(count);
                        Cell cellTotal = rowTotal.createCell(0);
                        cellTotal.setCellValue("TOTAL");
                        cellTotal = rowTotal.createCell(6);
                        cellTotal.setCellFormula("SUM(G2:G" + (count) + ")");
                        cellTotal.setCellType(CellType.FORMULA);
                        cellTotal.setCellStyle(currency);
                        cellTotal = rowTotal.createCell(7);
                        cellTotal.setCellFormula("SUM(H2:H" + (count) + ")");
                        cellTotal.setCellType(CellType.FORMULA);
                        cellTotal.setCellStyle(currency);
                    } else {
                        Row rowTotal = sheet.createRow(count);
                        Cell cellTotal = rowTotal.createCell(0);
                        cellTotal.setCellValue("TOTAL");
                        cellTotal = rowTotal.createCell(11);
                        cellTotal.setCellFormula("SUM(L2:L" + (count) + ")");
                        cellTotal.setCellType(CellType.FORMULA);
                        cellTotal.setCellStyle(currency);
                        cellTotal = rowTotal.createCell(12);
                        cellTotal.setCellFormula("SUM(M2:M" + (count) + ")");
                        cellTotal.setCellType(CellType.FORMULA);
                        cellTotal.setCellStyle(currency);
                        cellTotal = rowTotal.createCell(15);
                        cellTotal.setCellFormula("SUM(P2:P" + (count) + ")");
                        cellTotal.setCellType(CellType.FORMULA);
                        cellTotal.setCellStyle(currency);
                    }
                    count++;
                }
            }
        }
    }
}

class DataUnit{
    private List<Parent> data;

    public List<Parent> getData() {
        return data;
    }
}

class Parent{
    private String id;
    private String name;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    private List<Children> children;

    public List<Children> getChildren() {
        return children;
    }
}

class Children{
    private String id;
    private String name;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}