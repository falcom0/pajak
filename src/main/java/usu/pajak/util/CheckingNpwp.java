package usu.pajak.util;

import org.apache.poi.ss.usermodel.*;
import usu.fp.report.TimeRangeService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class CheckingNpwp {
    private static String npwp_tdk_valid = "C:\\Users\\PSI-DEV-7\\Downloads\\sortiran pajak bulan 8.xlsx";

    public static void main(String[] args) throws IOException, SQLException {
//        String s = "99555922122000";
//        System.out.println(Pattern.compile(".*"+s+".*").matcher("099555922122000").find());
        TimeRangeService t = new TimeRangeService();
        t.connect();
        Connection con = t.getConnectionPeople();
        Statement statement = con.createStatement();
        String query = "select tax_id from user_biography";
        ResultSet rs = statement.executeQuery(query);
        List<String> listNpwp = new ArrayList<>();
        while(rs.next()){
            if(!rs.getString("tax_id").trim().isEmpty()) {
                String npwp = rs.getString("tax_id").replaceAll("\\D","");
                listNpwp.add(npwp);
            }
        }
        Workbook workbook = WorkbookFactory.create(new File(npwp_tdk_valid));
        Sheet sheet = workbook.getSheetAt(0);
        Sheet hasilSheet = workbook.createSheet("HASIL");
        sheet.forEach(row -> {
            if(row.getRowNum()>0) {
                Row hasilRow = hasilSheet.createRow(row.getRowNum());
                row.forEach(cell -> {
                    if(cell.getColumnIndex()==0) {
                        if(!cell.getStringCellValue().isEmpty()) {
                            Cell targetCell = hasilRow.createCell(0);
                            AtomicReference<String> newNpwp = new AtomicReference<>("");
                            listNpwp.forEach(npwp -> {
                                if (Pattern.compile(".*" + cell.getStringCellValue() + ".*").matcher(npwp).find()) {
                                    newNpwp.set(npwp);
                                }
                            });
                            targetCell.setCellValue(newNpwp.get());
                        }
                    }
                });
            }
        });

        try (OutputStream fileOut = new FileOutputStream("C:\\Users\\PSI-DEV-7\\Downloads\\REV-sortiran pajak bulan 8.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }
}
