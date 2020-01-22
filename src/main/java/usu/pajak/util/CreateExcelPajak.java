package usu.pajak.util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import usu.pajak.model.Salary;
import usu.pajak.model.UserPajak;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class CreateExcelPajak {
    private String[] header = new String[]{"No.","NIP","Nama","NPWP","Kegiatan","Jenis","Keterangan","Nilai"};
    private String[] headerPajak = new String[]{"Masa Pajak","Tahun Pajak","Pembetulan","NPWP","Nama","Kode Pajak","Jumlah Bruto","Jumlah PPh"};
    private String[] headerPajakLuar = new String[]{"Masa Pajak","Tahun Pajak","Pembetulan","Nomor Bukti Potong","NPWP","NIK","Nama","Alamat","WP Luar Negeri","Kode Negara",
            "Kode Pajak","Jumlah Bruto","Jumlah DPP","Tanpa NPWP","Tarif","Jumlah PPh","NPWP Pemotong","Nama Pemotong","Tanggal Bukti Potong"};
//    private MongoClient client = new MongoClient(new MongoClientURI("mongodb://fariz:Laru36Dema@clusterasetmongo-shard-00-00-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-01-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-02-t3kc1.mongodb.net:27017/test?ssl=true&replicaSet=ClusterAsetMongo-shard-0&authSource=admin&retryWrites=true")); //connect to mongodb
private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_rev")); //connect to mongodb
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_rev");
    private static CellStyle style,currency,styleWarning;
    private String[] headerMonth = new String[]{"No.","NIP","Nama","NPWP","Kegiatan","Jenis","Bruto Pendapatan","Pengurang Jabatan","Pengurang Pensiun",
            "Netto Pendapatan","Netto Setahun","PTKP Sebulan","PTKP Setahun","PKP","PKP Setahun","Total PPH21"};
    private String[] headerLaporanPajak = new String[] {"No.","Nama Pegawai","NPWP","Jumlah Penghasilan Netto PNS (APBN)","Jumlah Penghasilan Bruto Non PNBP","Biaya Jabatan","PTKP","Penghasilan Neto","Penghasilan Kena Pajak","PPH 21 Terutang","Tanggal Bayar","Lebih Bayar Pajak"};

//    private String[] headerPerson = new String[]{"No.","NIP","Nama","NPWP","Keterangan"};

    public CreateExcelPajak() throws IOException{
//        createExcelBasedOnUnit();
        createExcelBasedOnMonth("false","NON-PNBP","TETAP");
//        createExcelBasedOnMonth("true","NON-PNBP","LUAR");
//        createExcelBasedOnPerson();
//        createExcelBasedOnDJP();
    }

    private void createExcelBasedOnDJP() throws IOException{
        String[] months = new DateFormatSymbols().getMonths();
        for(int i=0;i<6;i++) {
            Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
            currency = workbook.createCellStyle();
            currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
            Sheet sheet = workbook.createSheet(months[i]);
            Row rowHeader = sheet.createRow(0);
            for(int j=0;j<headerLaporanPajak.length;j++){
                rowHeader.createCell(j).setCellValue(headerLaporanPajak[j]);
            }
            Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
            query.and(query.or(query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(Integer.toString(i+1)),
                    query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(Integer.toString(i+1))),
                    query.criteria("id_user").not().containsIgnoreCase("-"));
            List<UserPajak> listResult = query.asList();
            int z =1;
            final int b = i;
            for(int k=0;k<listResult.size();k++){
                UserPajak up = listResult.get(k);
                Row rows = sheet.createRow(z);
                rows.createCell(0).setCellValue(z);
                rows.createCell(1).setCellValue(up.getFront_degree()+" "+up.getFull_name()+" "+up.getBehind_degree());
                rows.createCell(2).setCellValue((up.getNpwp_simsdm()==null||up.getNpwp_simsdm().isEmpty())? up.getNpwp() : up.getNpwp_simsdm());

                // Netto APBN
                BigDecimal brutoGajiNonPnbp = new BigDecimal("0.0");
                BigDecimal nettoGajiNonPnbp = new BigDecimal("0.0");
                BigDecimal potonganJabatan = new BigDecimal("0.0");
                BigDecimal pajakNonPnbp = new BigDecimal("0.0");
                BigDecimal pkpNonPnbp = new BigDecimal("0.0");
                boolean statusNipGpp = false;
                if(up.getPendapatan_tetap()!=null) {
                    if(up.getNip_gpp()!=null)
                        if(!up.getNip_gpp().isEmpty())
                            statusNipGpp = true;
                    if (statusNipGpp) {
                        BasicDBList listTetap = up.getPendapatan_tetap().stream().filter(e -> {
                            BasicDBObject obj = (BasicDBObject) e;
                            Integer bln = Integer.parseInt(Integer.toString(b + 1));
                            Integer bulan = Integer.parseInt(obj.getString("bulan"));
                            if (obj.getString("activity_id").equalsIgnoreCase("apbn") && bulan == bln)
                                return true;
                            else {
                                return false;
                        /*Integer bulan = Integer.parseInt(obj.getString("bulan"));
                        if(bulan != Integer.parseInt(Integer.toString(i+1))){
                            return false;
                        }else{
                            if(!obj.getString("unit_id").equalsIgnoreCase(unitId)){
                                return false;
                            }else{
                                if(!obj.getString("source_of_fund").equalsIgnoreCase(sumberDana)){
                                    return false;
                                }else{
                                    return true;
                                }
                            }
                        }*/
                            }
                        }).collect(Collectors.toCollection(BasicDBList::new));
                        if(listTetap.size()>0) {
                            BasicDBObject o = (BasicDBObject) listTetap.get(0);
                            Cell cell = rows.createCell(3);
                            cell.setCellType(CellType.NUMERIC);
                            cell.setCellStyle(currency);
                            cell.setCellValue(new BigDecimal(o.getString("netto_pendapatan")).doubleValue());
                        }
                    } else { // pendapatan tetap masuk ke bruto pendapatan non pnbp
                        BasicDBList listTetap = up.getPendapatan_tetap().stream().filter(e -> {
                            BasicDBObject obj = (BasicDBObject) e;
                            Integer bln = Integer.parseInt(Integer.toString(b + 1));
                            Integer bulan = Integer.parseInt(obj.getString("bulan"));
                            if ((!obj.getString("activity_id").equalsIgnoreCase("apbn")) && bulan == bln)
                                return true;
                            else {
                                return false;
                            }
                        }).collect(Collectors.toCollection(BasicDBList::new));
                        if(listTetap.size()>0) {
                            BasicDBObject o = (BasicDBObject) listTetap.get(0);
                            brutoGajiNonPnbp = brutoGajiNonPnbp.add(new BigDecimal(o.getString("bruto_pendapatan")));
                            nettoGajiNonPnbp = nettoGajiNonPnbp.add(new BigDecimal(o.getString("netto_pendapatan")));
                            potonganJabatan = potonganJabatan.add(new BigDecimal(o.getString("pot_jabatan")));
                            BasicDBList listPajak = (BasicDBList) o.get("pph21");
                            for (Object e : listPajak) {
                                BasicDBObject od = (BasicDBObject) e;
                                pajakNonPnbp = pajakNonPnbp.add(new BigDecimal(od.getString("_hasil")));
                                pkpNonPnbp = pkpNonPnbp.add(new BigDecimal(od.getString("_pkp")));
                            }
                        }

                        Cell cell = rows.createCell(3);
                        cell.setCellType(CellType.NUMERIC);
                        cell.setCellStyle(currency);
                        cell.setCellValue(new BigDecimal("0.0").doubleValue());
                    }
                }

                if(up.getPendapatan_tdk_tetap()!=null) {
                    BasicDBList listTdkTetap = up.getPendapatan_tdk_tetap().stream().filter(e -> {
                        BasicDBObject obj = (BasicDBObject) e;
                        Integer bln = Integer.parseInt(Integer.toString(b + 1));
                        Integer bulan = Integer.parseInt(obj.getString("bulan"));
                        if (bulan == bln)
                            return true;
                        else {
                            return false;
                        }
                    }).collect(Collectors.toCollection(BasicDBList::new));

                    //Bruto NON PNBP
                    BigDecimal brutoNonApbn = listTdkTetap.stream().map(x -> {
                        BasicDBObject o = (BasicDBObject) x;
                        return new BigDecimal(o.getString("bruto_pendapatan"));
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal returnNonApbn = listTdkTetap.stream().map(x -> {
                        BasicDBObject o = (BasicDBObject) x;
                        if(o.containsField("returned"))
                            return new BigDecimal(o.getString("returned"));
                        else
                            return  new BigDecimal("0.0");
                    }).reduce(BigDecimal.ZERO, BigDecimal::subtract);
                    brutoNonApbn = brutoNonApbn.subtract(returnNonApbn);
                    brutoNonApbn = brutoNonApbn.add(brutoGajiNonPnbp);
                    Cell cell = rows.createCell(4);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellStyle(currency);
                    cell.setCellValue(brutoNonApbn.doubleValue());

                    //biaya jabatan
                    BigDecimal biayaJabatan = listTdkTetap.stream().map(x -> {
                        BasicDBObject o = (BasicDBObject) x;
                        if(o.containsField("pot_jabatan"))
                            return new BigDecimal(o.getString("pot_jabatan"));
                        else
                            return new BigDecimal("0.0");
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    biayaJabatan = biayaJabatan.add(potonganJabatan);
                    cell = rows.createCell(5);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellStyle(currency);
                    cell.setCellValue(biayaJabatan.doubleValue());

                    //PTKP
                    cell = rows.createCell(6);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellStyle(currency);
                    cell.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());

                    //Penghasilan Neto
                    BigDecimal nettoNonApbn = listTdkTetap.stream().map(x -> {
                        BasicDBObject o = (BasicDBObject) x;
                        if(o.containsField("netto_pendapatan"))
                            return new BigDecimal(o.getString("netto_pendapatan"));
                        else
                            return  new BigDecimal("0.0");
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    nettoNonApbn = nettoNonApbn.add(nettoGajiNonPnbp);
                    cell = rows.createCell(7);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellStyle(currency);
                    cell.setCellValue(nettoNonApbn.doubleValue());

                    //PKP
                    pkpNonPnbp = pkpNonPnbp.add(listTdkTetap.stream().map(x -> {
                        BasicDBObject o = (BasicDBObject) x;
                        BasicDBList l = (BasicDBList) o.get("pph21");
                        return l.stream().map(t -> {
                            BasicDBObject s = (BasicDBObject) t;
                            return new BigDecimal(s.getString("_pkp"));
                        }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    }).reduce(BigDecimal.ZERO, BigDecimal::add));
                    cell = rows.createCell(8);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellStyle(currency);
                    cell.setCellValue(pkpNonPnbp.doubleValue());

                    //pph21
                    pajakNonPnbp = pajakNonPnbp.add(listTdkTetap.stream().map(x -> {
                        BasicDBObject o = (BasicDBObject) x;
                        BasicDBList l = (BasicDBList) o.get("pph21");
                        return l.stream().map(t -> {
                            BasicDBObject s = (BasicDBObject) t;
                            return new BigDecimal(s.getString("_hasil"));
                        }).reduce(BigDecimal.ZERO, BigDecimal::add);
                    }).reduce(BigDecimal.ZERO, BigDecimal::add));
                    cell = rows.createCell(9);
                    cell.setCellType(CellType.NUMERIC);
                    cell.setCellStyle(currency);
                    cell.setCellValue(pajakNonPnbp.doubleValue());
                }
                z++;
            }
            try (OutputStream fileOut = new FileOutputStream("D:\\[DJP]Laporan Pajak 2019 Bulan "+months[i]+".xls")) {
                workbook.write(fileOut);
            }
            workbook.close();
        }
    }

    private void createExcelBasedOnPerson() throws IOException{
        Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
        currency = workbook.createCellStyle();
        currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        List<UserPajak> listResult = datastore.createQuery(UserPajak.class).asList();
        System.out.println("Size : "+listResult.size());
        Integer xTimes = listResult.size()/800;
        Integer reminder = listResult.size()%800;
        for(int a=0;a<xTimes+1;a++) {
            Sheet sheet = workbook.createSheet("Pajak Pegawai "+(a+1));
            Row rowHeader = sheet.createRow(0);
            for (int j = 0; j < header.length; j++) {
                rowHeader.createCell(j).setCellValue(header[j]);
            }

            int z = 1;
            int batasAwal = a * 800;
            int batasAkhir = (a+1)*800;
            if(a==4)
                batasAkhir = batasAwal+reminder;
            for (int i = batasAwal; i < batasAkhir; i++) {
                UserPajak up = listResult.get(i);
                Row rows = sheet.createRow(z);
                Cell cell = rows.createCell(0);
                cell.setCellValue(i + 1);
                if (up.getNip_gpp() == null || up.getNip_gpp().equalsIgnoreCase(""))
                    rows.createCell(1).setCellValue(up.getNip_simsdm());
                else
                    rows.createCell(1).setCellValue(up.getNip_gpp());

                rows.createCell(2).setCellValue(up.getFront_degree() + " " + up.getFull_name() + " " + up.getBehind_degree());
                rows.createCell(3).setCellValue(up.getNpwp());

                BasicDBList pendapatan_tetap = new BasicDBList();
                BasicDBList pendapatan_tdk_tetap = new BasicDBList();
                if(up.getPendapatan_tetap() != null)
                    pendapatan_tetap = up.getPendapatan_tetap();
                if(up.getPendapatan_tdk_tetap() != null)
                    pendapatan_tdk_tetap = up.getPendapatan_tdk_tetap();
                int count = 0;
                for (int j = 0; j < pendapatan_tetap.size(); j++) {
                    BasicDBObject obj = (BasicDBObject) pendapatan_tetap.get(j);
                    if (count == 0) {
                        rows.createCell(4).setCellValue(obj.getString("activity_title"));
                        rows.createCell(5).setCellValue(obj.getString("type_title"));
                        rows.createCell(6).setCellValue("bulan");
                        rows.createCell(7).setCellValue(obj.getString("bulan"));
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pendapatan bruto");
                        Cell cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pengurang biaya jabatan");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pengurang iuran pensiun");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pendapatn netto");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("ptkp setahun");
                        cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        z++;
                        /*rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pkp sebulan");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("sisa ptkp sebulan");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("sisa_ptkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);*/
                        BasicDBList listPph = (BasicDBList) obj.get("pph21");
                        for (Object p : listPph) {
                            BasicDBObject t = (BasicDBObject) p;
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("tarif");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_tarif")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pkp");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_pkp")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pph21");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_hasil")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pendapatan setelah dipotong pajak");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_TakeHomePay")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                    } else {
                        Row add = sheet.createRow(z);
                        add.createCell(4).setCellValue(obj.getString("activity_title"));
                        add.createCell(5).setCellValue(obj.getString("type_title"));
                        add.createCell(6).setCellValue("bulan");
                        add.createCell(7).setCellValue(obj.getString("bulan"));
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pendapatan bruto");
                        Cell cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pengurang biaya jabatan");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pengurang iuran pensiun");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pendapatn netto");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("ptkp setahun");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        /*add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pkp sebulan");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("sisa ptkp sebulan");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("sisa_ptkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);*/
                        BasicDBList listPph = (BasicDBList) obj.get("pph21");
                        for (Object p : listPph) {
                            BasicDBObject t = (BasicDBObject) p;
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("tarif");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_tarif")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pkp");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_pkp")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pph21");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_hasil")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pendapatan setelah dipotong pajak");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_TakeHomePay")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                    }
                    count++;
                    z++;
                }
                z++;
                for (int j = 0; j < pendapatan_tdk_tetap.size(); j++) {
                    BasicDBObject obj = (BasicDBObject) pendapatan_tdk_tetap.get(j);
                    if (count == 0) {
                        rows.createCell(4).setCellValue(obj.getString("activity_title"));
                        rows.createCell(5).setCellValue(obj.getString("type_title"));
                        rows.createCell(6).setCellValue("bulan");
                        rows.createCell(7).setCellValue(obj.getString("bulan"));
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pendapatan bruto");
                        Cell cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pengurang biaya jabatan");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pengurang iuran pensiun");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pendapatn netto");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        /*rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("ptkp sebulan");
                        cellB = rows.createCell(7);
                        if (obj.getString("ptkp_sebulan") == null)
                            cellB.setCellValue("");
                        else {
                            cellB.setCellValue(new BigDecimal(obj.getString("ptkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pkp sebulan");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("sisa ptkp sebulan");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("sisa_ptkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);*/
                        BasicDBList listPph = (BasicDBList) obj.get("pph21");
                        for (Object p : listPph) {
                            BasicDBObject t = (BasicDBObject) p;
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("tarif");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_tarif")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pkp");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_pkp")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pph21");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_hasil")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        z++;
                        rows = sheet.createRow(z);
                        rows.createCell(6).setCellValue("pendapatan setelah dipotong pajak");
                        cellB = rows.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_TakeHomePay")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                    } else {
                        Row add = sheet.createRow(z);
                        add.createCell(4).setCellValue(obj.getString("activity_title"));
                        add.createCell(5).setCellValue(obj.getString("type_title"));
                        add.createCell(6).setCellValue("bulan");
                        add.createCell(7).setCellValue(obj.getString("bulan"));
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pendapatan bruto");
                        Cell cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pengurang biaya jabatan");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pengurang iuran pensiun");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pendapatn netto");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        /*add = sheet.createRow(z);
                        add.createCell(6).setCellValue("ptkp sebulan");
                        cellB = add.createCell(7);
                        if (obj.getString("ptkp_sebulan") == null)
                            cellB.setCellValue("");
                        else {
                            cellB.setCellValue(new BigDecimal(obj.getString("ptkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pkp sebulan");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("sisa ptkp sebulan");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("sisa_ptkp_sebulan")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);*/
                        BasicDBList listPph = (BasicDBList) obj.get("pph21");
                        for (Object p : listPph) {
                            BasicDBObject t = (BasicDBObject) p;
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("tarif");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_tarif")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pkp");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_pkp")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pph21");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(t.getString("_hasil")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        z++;
                        add = sheet.createRow(z);
                        add.createCell(6).setCellValue("pendapatan setelah dipotong pajak");
                        cellB = add.createCell(7);
                        cellB.setCellValue(new BigDecimal(obj.getString("netto_TakeHomePay")).doubleValue());
                        cellB.setCellType(CellType.NUMERIC);
                        cellB.setCellStyle(currency);
                    }
                    count++;
                    z++;
                }
                z++;
            }
        }
        try (OutputStream fileOut = new FileOutputStream("D:\\PAJAK_2019_BASEDON_PERSON-new2.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private void createExcelBasedOnMonth() throws  IOException{
        Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
        currency = workbook.createCellStyle();
        currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        String[] months = new DateFormatSymbols().getMonths();
        for(int i=0;i<3;i++){
            Sheet sheet = workbook.createSheet(months[i]);
            Row rowHeader = sheet.createRow(0);
            for(int j=0;j<headerMonth.length;j++){
                rowHeader.createCell(j).setCellValue(headerMonth[j]);
            }

//            List<UserPajak> listResult = datastore.createQuery(UserPajak.class).disableValidation()
//                    .filter("pendapatan_tetap.bulan", Integer.toString(i+1))
//                    .filter("pendapatan_tdk_tetap.bulan", Integer.toString(i+1)).asList();
            Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
            query.or(query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(Integer.toString(i+1)),
                    query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(Integer.toString(i+1)));
            List<UserPajak> listResult = query.asList();
            int z =1;
            for(int k=0;k<listResult.size();k++){
                UserPajak up = listResult.get(k);
                Row rows = sheet.createRow(z);
                rows.createCell(0).setCellValue(i+1);
                if(up.getNip_gpp() == null || up.getNip_gpp().equalsIgnoreCase(""))
                    rows.createCell(1).setCellValue(up.getNip_simsdm());
                else
                    rows.createCell(1).setCellValue(up.getNip_gpp());
                rows.createCell(2).setCellValue(up.getFront_degree()+" "+up.getFull_name()+" "+up.getBehind_degree());
                rows.createCell(3).setCellValue(up.getNpwp());

                BasicDBList pendapatan_tetap = up.getPendapatan_tetap();
                BasicDBList pendapatan_tdk_tetap = up.getPendapatan_tdk_tetap();

                int count=0;
                if(pendapatan_tetap != null)
                for(int l=0;l<pendapatan_tetap.size();l++){
                    BasicDBObject obj = (BasicDBObject) pendapatan_tetap.get(l);
                    if(obj.getString("bulan").replaceFirst("^0+(?!$)", "").equalsIgnoreCase(Integer.toString(i+1)) && !obj.getString("salary_id").equalsIgnoreCase("apbn")){
                        if(count==0){
                            rows.createCell(4).setCellValue(obj.getString("activity_title"));
                            rows.createCell(5).setCellValue(obj.getString("type_title"));
                            Cell cellB = rows.createCell(6);
                            cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(8);
//                            if(obj.getString("pot_pensiun"))
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(9);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            /*cellB = rows.createCell(10);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(11);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            cellB = rows.createCell(12);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            /*cellB = rows.createCell(13);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(14);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            BasicDBList listPph = (BasicDBList) obj.get("pph21");
                            BigDecimal totalPph21 = BigDecimal.valueOf(0.00);
                            for (Object p : listPph) {
                                BasicDBObject t = (BasicDBObject) p;
                                totalPph21 = totalPph21.add(new BigDecimal(t.getString("_hasil")));
                            }
                            cellB = rows.createCell(15);
                            cellB.setCellValue(totalPph21.doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }else{
                            Row add = sheet.createRow(z);
                            add.createCell(4).setCellValue(obj.getString("activity_title"));
                            add.createCell(5).setCellValue(obj.getString("type_title"));
                            Cell cellB = add.createCell(6);
                            cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(8);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(9);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                           /* cellB = add.createCell(10);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(11);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            cellB = add.createCell(12);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            /*cellB = add.createCell(13);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(14);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            BasicDBList listPph = (BasicDBList) obj.get("pph21");
                            BigDecimal totalPph21 = BigDecimal.valueOf(0.00);
                            for (Object p : listPph) {
                                BasicDBObject t = (BasicDBObject) p;
                                totalPph21 = totalPph21.add(new BigDecimal(t.getString("_hasil")));
                            }
                            cellB = add.createCell(15);
                            cellB.setCellValue(totalPph21.doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        count++;
                        z++;
                    }
                }
                z++;

//                count=0;
                if(pendapatan_tdk_tetap != null)
                for(int l=0;l<pendapatan_tdk_tetap.size();l++){
                    BasicDBObject obj = (BasicDBObject) pendapatan_tdk_tetap.get(l);
                    if(obj.getString("bulan").replaceFirst("^0+(?!$)", "").equalsIgnoreCase(Integer.toString(i+1)) && !obj.getString("salary_id").equalsIgnoreCase("apbn")){
                        if(count==0){
                            rows.createCell(4).setCellValue(obj.getString("activity_title"));
                            rows.createCell(5).setCellValue(obj.getString("type_title"));
                            Cell cellB = rows.createCell(6);
                            cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(8);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(9);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            /*cellB = rows.createCell(10);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(11);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            cellB = rows.createCell(12);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            /*cellB = rows.createCell(13);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = rows.createCell(14);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            BasicDBList listPph = (BasicDBList) obj.get("pph21");
                            BigDecimal totalPph21 = BigDecimal.valueOf(0.00);
                            for (Object p : listPph) {
                                BasicDBObject t = (BasicDBObject) p;
                                totalPph21 = totalPph21.add(new BigDecimal(t.getString("_hasil")));
                            }
                            cellB = rows.createCell(15);
                            cellB.setCellValue(totalPph21.doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }else{
                            Row add = sheet.createRow(z);
                            add.createCell(4).setCellValue(obj.getString("activity_title"));
                            add.createCell(5).setCellValue(obj.getString("type_title"));
                            Cell cellB = add.createCell(6);
                            cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(8);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(9);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                           /* cellB = add.createCell(10);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(11);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            cellB = add.createCell(12);
                            cellB.setCellValue(new BigDecimal(up.getPtkp_setahun()).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            /*cellB = add.createCell(13);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            cellB = add.createCell(14);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).multiply(BigDecimal.valueOf(12.00)).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            BasicDBList listPph = (BasicDBList) obj.get("pph21");
                            BigDecimal totalPph21 = BigDecimal.valueOf(0.00);
                            for (Object p : listPph) {
                                BasicDBObject t = (BasicDBObject) p;
                                totalPph21 = totalPph21.add(new BigDecimal(t.getString("_hasil")));
                            }
                            cellB = add.createCell(15);
                            cellB.setCellValue(totalPph21.doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        count++;
                        z++;
                    }
                }
                z++;
            }
        }
        try (OutputStream fileOut = new FileOutputStream("D:\\PAJAK_2019_JAN_new.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    private void createExcelBasedOnMonth(String pegawaiLuar, String sumberDana, String filename) throws IOException{
        String response = callApi("https://api.usu.ac.id/0.1/units","GET", true);
        DataUnit du = new Gson().fromJson(response, DataUnit.class);
        ApiRka apiRka = new ApiRka();
        String[] months = new DateFormatSymbols().getMonths();
        for(int i=10;i<12;i++){
            Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
            currency = workbook.createCellStyle();
            currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            for(int z=1;z<du.getData().size();z++){
                Parent par = du.getData().get(z);
                Children children = new Children();
                children.setId(par.getId());
                children.setName(par.getName());

                String responsePajak = callApi("http://localhost:8253/list-tax?month="+Integer.toString(i+1)+"&unit_id="+
                        children.getId()+"&apbn=true&pegawai_luar="+pegawaiLuar+"&sumber_dana="+sumberDana,"GET",false);
                UserPajak[] listResult = new Gson().fromJson(responsePajak, UserPajak[].class);
                Salary salary = new Gson().fromJson(
                        apiRka.callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipts?status=1&year=2019&source_of_fund="+sumberDana+
                                        "&month="+Integer.toString(i+1)+"&unit_id="+children.getId(), "GET")
                        , Salary.class);

                if(listResult.length>0) {
                    Sheet sheet = workbook.createSheet(children.getName().replaceAll("/", ""));
                    Row row = sheet.createRow(0);
                    if(pegawaiLuar.equalsIgnoreCase("false")) {
                        for (int k = 0; k < headerPajak.length; k++) {
                            row.createCell(k).setCellValue(headerPajak[k]);
                        }
                    }else{
                        for (int k = 0; k < headerPajakLuar.length; k++) {
                            row.createCell(k).setCellValue(headerPajakLuar[k]);
                        }
                    }

                    int count = 1;
                    for (UserPajak up : listResult) {
                        Integer totalBruto = 0;
                        Integer totalPph21 = 0;
                        BigInteger totalPKP = BigInteger.valueOf(0);
                        BigInteger tarif = BigInteger.valueOf(0);
                        BasicDBList listTetap = up.getPendapatan_tetap();
                        if (listTetap != null) {
                            Iterator iterator = listTetap.iterator();
                            while (iterator.hasNext()) {
                                LinkedTreeMap obj = (LinkedTreeMap) iterator.next();

                                BigDecimal total_pph21_sementara = new BigDecimal("0.00");
                                ArrayList listPph = (ArrayList) obj.get("pph21");
                                for (Object p : listPph) {
                                    LinkedTreeMap t = (LinkedTreeMap) p;
                                    total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(t.get("_hasil").toString()));
                                    totalPKP = totalPKP.add(new BigDecimal(t.get("_pkp").toString()).toBigInteger());
                                    if(tarif.compareTo(new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger())<0){
                                        tarif = new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger();
                                    }
//                                    tarif = tarif.add(new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger());
                                }

                                BigDecimal insurance = new BigDecimal("0.00");
                                if (obj.containsKey("jkk")) {
                                    insurance = insurance.add(new BigDecimal(obj.get("jkk").toString()))
                                            .add(new BigDecimal(obj.get("jkm").toString())).add(new BigDecimal(obj.get("bpjs_kesehatan").toString()));
                                }
                                BigDecimal returned = new BigDecimal("0.00");
                                if (obj.containsKey("returned")) {
                                    returned = new BigDecimal(obj.get("returned").toString());
                                }
                                totalBruto = totalBruto + new BigDecimal(obj.get("bruto_pendapatan").toString()).subtract(insurance).subtract(returned).toBigInteger().intValue();
//                                totalPph21 = totalPph21 + total_pph21_sementara.toBigInteger().intValue();

                                AtomicReference<Integer> api_pph21 = new AtomicReference<>(0);
                                salary.getResponse().getSalary_receivers().stream().anyMatch(e -> {
                                    if (obj.get("salary_id").toString().equalsIgnoreCase(e.getId().toString())) {
                                        api_pph21.set(e.getPayment().getAsJsonObject().get("pph21").getAsInt());
                                        return true;
                                    } else
                                        return false;
                                });
                                totalPph21 = totalPph21 + api_pph21.get();
                            }

                        }

                        BasicDBList listTdkTetap = up.getPendapatan_tdk_tetap();
                        if (listTdkTetap != null) {
                            Iterator iterator = listTdkTetap.iterator();
                            while (iterator.hasNext()) {
                                LinkedTreeMap obj = (LinkedTreeMap) iterator.next();

                                BigDecimal total_pph21_sementara = new BigDecimal("0.00");
                                ArrayList listPph = (ArrayList) obj.get("pph21");
                                for (Object p : listPph) {
                                    LinkedTreeMap t = (LinkedTreeMap) p;
                                    total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(t.get("_hasil").toString()));
                                    totalPKP = totalPKP.add(new BigDecimal(t.get("_pkp").toString()).toBigInteger());
                                    if(tarif.compareTo(new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger())<0){
                                        tarif = new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger();
                                    }
                                }
                                BigDecimal insurance = new BigDecimal("0.00");
                                if (obj.containsKey("jkk")) {
                                    insurance = insurance.add(new BigDecimal(obj.get("jkk").toString()))
                                            .add(new BigDecimal(obj.get("jkm").toString())).add(new BigDecimal(obj.get("bpjs_kesehatan").toString()));
                                }
                                BigDecimal returned = new BigDecimal("0.00");
                                if (obj.containsKey("returned")) {
                                    returned = new BigDecimal(obj.get("returned").toString());
                                }
                                totalBruto = totalBruto + new BigDecimal(obj.get("bruto_pendapatan").toString()).subtract(insurance).subtract(returned).toBigInteger().intValue();
//                                totalPph21 = totalPph21 + total_pph21_sementara.toBigInteger().intValue();
                                AtomicReference<Integer> api_pph21 = new AtomicReference<>(0);
                                salary.getResponse().getSalary_receivers().stream().anyMatch(e -> {
                                    if (obj.get("salary_id").toString().equalsIgnoreCase(e.getId().toString())) {
                                        api_pph21.set(e.getPayment().getAsJsonObject().get("pph21").getAsInt());
                                        return true;
                                    } else
                                        return false;
                                });
                                totalPph21 = totalPph21 + api_pph21.get();
                            }
                        }
                        if(totalBruto > 0){
                            if(pegawaiLuar.equalsIgnoreCase("false")) {
                                Row rowPajak = sheet.createRow(count);
                                Cell cell = rowPajak.createCell(0);
                                cell.setCellValue(i + 1);
                                cell = rowPajak.createCell(1);
                                cell.setCellValue(2019);
                                cell = rowPajak.createCell(2);
                                cell.setCellValue(0);
                                cell = rowPajak.createCell(3);
                                cell.setCellValue((up.getNpwp_simsdm() == null || up.getNpwp_simsdm().isEmpty()) ? up.getNpwp() : up.getNpwp_simsdm());
                           /* if (up.getNpwp() != null || !up.getNpwp().isEmpty()) {
                                cell.setCellValue(up.getNpwp());
                            } else if (up.getNpwp_simsdm() != null || !up.getNpwp_simsdm().isEmpty()) {
                                cell.setCellValue(up.getNpwp_simsdm());
                            } else {
                                cell.setCellValue("0");
                            }*/
                                cell = rowPajak.createCell(4);
                                cell.setCellValue(up.getFull_name());
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
                            }else{
                                Row rowPajak = sheet.createRow(count);
                                Cell cell = rowPajak.createCell(0);
                                cell.setCellValue(i + 1);
                                cell = rowPajak.createCell(1);
                                cell.setCellValue(2019);
                                cell = rowPajak.createCell(2);
                                cell.setCellValue(0);
                                cell = rowPajak.createCell(4);
                                cell.setCellValue((up.getNpwp_simsdm() == null || up.getNpwp_simsdm().isEmpty()) ? up.getNpwp() : up.getNpwp_simsdm());
                           /* if (up.getNpwp() != null || !up.getNpwp().isEmpty()) {
                                cell.setCellValue(up.getNpwp());
                            } else if (up.getNpwp_simsdm() != null || !up.getNpwp_simsdm().isEmpty()) {
                                cell.setCellValue(up.getNpwp_simsdm());
                            } else {
                                cell.setCellValue("0");
                            }*/
                                cell = rowPajak.createCell(6);
                                cell.setCellValue(up.getFull_name());
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
                        if(pegawaiLuar.equalsIgnoreCase("false")) {
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
                        }else{
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

            for(int j=0;j<du.getData().get(0).getChildren().size();j++) {
                Children children = du.getData().get(0).getChildren().get(j);

                String responsePajak = callApi("http://localhost:8253/list-tax?month="+Integer.toString(i+1)+"&unit_id="+
                        children.getId()+"&apbn=true&pegawai_luar="+pegawaiLuar+"&sumber_dana="+sumberDana,"GET",false);
                UserPajak[] listResult = new Gson().fromJson(responsePajak, UserPajak[].class);

                Salary salary = new Gson().fromJson(
                        apiRka.callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipts?status=1&year=2019&source_of_fund="+sumberDana+
                                        "&month="+Integer.toString(i+1)+"&unit_id="+children.getId(), "GET")
                        , Salary.class);

                if(listResult.length>0) {
                    Sheet sheet = workbook.createSheet(children.getName().replaceAll("/", ""));
                    Row row = sheet.createRow(0);
                    if(pegawaiLuar.equalsIgnoreCase("false")) {
                        for (int k = 0; k < headerPajak.length; k++) {
                            row.createCell(k).setCellValue(headerPajak[k]);
                        }
                    }else{
                        for (int k = 0; k < headerPajakLuar.length; k++) {
                            row.createCell(k).setCellValue(headerPajakLuar[k]);
                        }
                    }


                    int count = 1;
                    for (UserPajak up : listResult) {
                        Integer totalBruto = 0;
                        Integer totalPph21 = 0;
                        BigInteger totalPKP = BigInteger.ZERO;
                        BigInteger tarif = BigInteger.ZERO;
                        BasicDBList listTetap = up.getPendapatan_tetap();
                        if (listTetap != null) {
                            Iterator iterator = listTetap.iterator();
                            while (iterator.hasNext()) {
                                LinkedTreeMap obj = (LinkedTreeMap) iterator.next();

                                BigDecimal total_pph21_sementara = new BigDecimal("0.00");
                                ArrayList listPph = (ArrayList) obj.get("pph21");
                                for (Object p : listPph) {
                                    LinkedTreeMap t = (LinkedTreeMap) p;
                                    total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(t.get("_hasil").toString()));
                                    totalPKP = totalPKP.add(new BigDecimal(t.get("_pkp").toString()).toBigInteger());
                                    if(tarif.compareTo(new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger())<0){
                                        tarif = new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger();
                                    }
                                }

                                BigDecimal insurance = new BigDecimal("0.00");
                                if (obj.containsKey("jkk")) {
                                    insurance = insurance.add(new BigDecimal(obj.get("jkk").toString()))
                                            .add(new BigDecimal(obj.get("jkm").toString())).add(new BigDecimal(obj.get("bpjs_kesehatan").toString()));
                                }

                                BigDecimal returned = new BigDecimal("0.00");
                                if (obj.containsKey("returned")) {
                                    returned = new BigDecimal(obj.get("returned").toString());
                                }
                                totalBruto = totalBruto + new BigDecimal(obj.get("bruto_pendapatan").toString()).subtract(insurance).subtract(returned).toBigInteger().intValue();
//                                totalPph21 = totalPph21 + total_pph21_sementara.toBigInteger().intValue();
                                AtomicReference<Integer> api_pph21 = new AtomicReference<>(0);
                                salary.getResponse().getSalary_receivers().stream().anyMatch(e -> {
                                    if(obj.get("salary_id").toString().equalsIgnoreCase(e.getId().toString())){
                                        api_pph21.set(e.getPayment().getAsJsonObject().get("pph21").getAsInt());
                                        return true;
                                    }else
                                        return false;
                                });
                                totalPph21 = totalPph21 + api_pph21.get();
                            }
                        }

                        BasicDBList listTdkTetap = up.getPendapatan_tdk_tetap();
                        if (listTdkTetap != null) {
                            Iterator iterator = listTdkTetap.iterator();
                            while (iterator.hasNext()) {
                                LinkedTreeMap obj = (LinkedTreeMap) iterator.next();

                                BigDecimal total_pph21_sementara = new BigDecimal("0.00");
                                ArrayList listPph = (ArrayList) obj.get("pph21");
                                for (Object p : listPph) {
                                    LinkedTreeMap t = (LinkedTreeMap) p;
                                    total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(t.get("_hasil").toString()));
                                    totalPKP = totalPKP.add(new BigDecimal(t.get("_pkp").toString()).toBigInteger());
                                    if(tarif.compareTo(new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger())<0){
                                        tarif = new BigDecimal(t.get("_tarif").toString()).multiply(new BigDecimal(100)).toBigInteger();
                                    }
                                }

                                BigDecimal insurance = new BigDecimal("0.00");
                                if (obj.containsKey("jkk")) {
                                    insurance = insurance.add(new BigDecimal(obj.get("jkk").toString()))
                                            .add(new BigDecimal(obj.get("jkm").toString())).add(new BigDecimal(obj.get("bpjs_kesehatan").toString()));
                                }

                                BigDecimal returned = new BigDecimal("0.00");
                                if (obj.containsKey("returned")) {
                                    returned = new BigDecimal(obj.get("returned").toString());
                                }
                                totalBruto = totalBruto + new BigDecimal(obj.get("bruto_pendapatan").toString()).subtract(insurance).subtract(returned).toBigInteger().intValue();
//                                totalPph21 = totalPph21 + total_pph21_sementara.toBigInteger().intValue();
                                AtomicReference<Integer> api_pph21 = new AtomicReference<>(0);
                                salary.getResponse().getSalary_receivers().stream().anyMatch(e -> {
                                    if(obj.get("salary_id").toString().equalsIgnoreCase(e.getId().toString())){
                                        api_pph21.set(e.getPayment().getAsJsonObject().get("pph21").getAsInt());
                                        return true;
                                    }else
                                        return false;
                                });
                                totalPph21 = totalPph21 + api_pph21.get();
                            }
                        }

                        if(totalBruto > 0) {
                            if(pegawaiLuar.equalsIgnoreCase("false")) {
                                Row rowPajak = sheet.createRow(count);
                                Cell cell = rowPajak.createCell(0);
                                cell.setCellValue(i + 1);
                                cell = rowPajak.createCell(1);
                                cell.setCellValue(2019);
                                cell = rowPajak.createCell(2);
                                cell.setCellValue(0);
                                cell = rowPajak.createCell(3);
                                cell.setCellValue((up.getNpwp_simsdm() == null || up.getNpwp_simsdm().isEmpty()) ? up.getNpwp() : up.getNpwp_simsdm());
                        /*if (up.getNpwp() != null || !up.getNpwp().isEmpty()) {
                            cell.setCellValue(up.getNpwp());
                        } else if (up.getNpwp_simsdm() != null || !up.getNpwp_simsdm().isEmpty()) {
                            cell.setCellValue(up.getNpwp_simsdm());
                        } else {
                            cell.setCellValue("");
                        }*/
                                cell = rowPajak.createCell(4);
                                cell.setCellValue(up.getFull_name());
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
                            }else{
                                Row rowPajak = sheet.createRow(count);
                                Cell cell = rowPajak.createCell(0);
                                cell.setCellValue(i + 1);
                                cell = rowPajak.createCell(1);
                                cell.setCellValue(2019);
                                cell = rowPajak.createCell(2);
                                cell.setCellValue(0);
                                cell = rowPajak.createCell(4);
                                cell.setCellValue((up.getNpwp_simsdm() == null || up.getNpwp_simsdm().isEmpty()) ? up.getNpwp() : up.getNpwp_simsdm());
                               /* if (up.getNpwp() != null || !up.getNpwp().isEmpty()) {
                                    cell.setCellValue(up.getNpwp());
                                } else if (up.getNpwp_simsdm() != null || !up.getNpwp_simsdm().isEmpty()) {
                                    cell.setCellValue(up.getNpwp_simsdm());
                                } else {
                                    cell.setCellValue("0");
                                }*/
                                cell = rowPajak.createCell(6);
                                cell.setCellValue(up.getFull_name());
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
                        if(pegawaiLuar.equalsIgnoreCase("false")) {
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
                        }else{
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

            try (OutputStream fileOut = new FileOutputStream("D:\\PJK-usu\\["+filename+"]PAJAK_2019_NON_FINAL_REV_BULAN_"+(i+1)+".xls")) {
                workbook.write(fileOut);
            }
            workbook.close();
            System.out.println(i+1);
        }
    }

    private void createExcelBasedOnUnit() throws IOException{
        String response = callApi("https://api.usu.ac.id/0.1/units","GET", true);
        DataUnit du = new Gson().fromJson(response, DataUnit.class);
        Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
        style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        currency = workbook.createCellStyle();
        currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

//        for(int x=0;x<4;x++){
//            Parent par = du.getData().get(x);
//            Children children = new Children();
//            children.setId(par.getId());
//            children.setName(par.getName());
        for(int x=0;x<du.getData().get(0).getChildren().size();x++){
            Children children = du.getData().get(0).getChildren().get(x);
            Sheet sheet = workbook.createSheet(children.getName().replaceAll("/",""));
            Row row = sheet.createRow(0);
            for(int i=0;i<header.length;i++){
                row.createCell(i).setCellValue(header[i]);
            }

            List<UserPajak> listResult = datastore.createQuery(UserPajak.class).disableValidation()
                    .filter("pendapatan.unit_id", children.getId()).asList();
//            int count
            int z =1;
            for(int i=0;i<listResult.size();i++){
                UserPajak up = listResult.get(i);
                Row rows = sheet.createRow(z);
                Cell cell = rows.createCell(0);
                cell.setCellValue(i+1);
                if(up.getNip_gpp() == null || up.getNip_gpp().equalsIgnoreCase(""))
                    rows.createCell(1).setCellValue(up.getNip_simsdm());
                else
                    rows.createCell(1).setCellValue(up.getNip_gpp());

                rows.createCell(2).setCellValue(up.getFront_degree()+" "+up.getFull_name()+" "+up.getBehind_degree());
                rows.createCell(3).setCellValue(up.getNpwp());
//                rows.setRowStyle(style);
                BasicDBList pendapatan = up.getPendapatan_tdk_tetap();
                BasicDBList refinePendapatan = pendapatan;

                int count=0;
                for (int j = 0; j < pendapatan.size(); j++) {
                    BasicDBObject obj = (BasicDBObject) pendapatan.get(j);
                    if (!(obj.getString("unit_id").equalsIgnoreCase(children.getId()) /*|| obj.getString("type_id").equalsIgnoreCase("apbn")*/)) {
//                        refinePendapatan.remove(j);
                    } else {

                        if (count == 0) {
                            rows.createCell(4).setCellValue(obj.getString("activity_title"));
                            rows.createCell(5).setCellValue(obj.getString("type_title"));
                            rows.createCell(6).setCellValue("bulan");
                            rows.createCell(7).setCellValue(obj.getString("bulan"));
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pendapatan bruto");
                            Cell cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pengurang biaya jabatan");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pengurang iuran pensiun");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pendapatn netto");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            /*rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("ptkp sebulan");
                            cellB = rows.createCell(7);
                            if(obj.getString("ptkp_sebulan") == null)
                                cellB.setCellValue("");
                            else {
                                cellB.setCellValue(new BigDecimal(obj.getString("ptkp_sebulan")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                            }
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pkp sebulan");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("sisa ptkp sebulan");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("sisa_ptkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);*/
                            BasicDBList listPph = (BasicDBList) obj.get("pph21");
                            for (Object p : listPph) {
                                BasicDBObject t = (BasicDBObject) p;
                                z++;
                                rows = sheet.createRow(z);
                                rows.createCell(6).setCellValue("tarif");
                                cellB = rows.createCell(7);
                                cellB.setCellValue(new BigDecimal(t.getString("_tarif")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                                z++;
                                rows = sheet.createRow(z);
                                rows.createCell(6).setCellValue("pkp");
                                cellB = rows.createCell(7);
                                cellB.setCellValue(new BigDecimal(t.getString("_pkp")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                                z++;
                                rows = sheet.createRow(z);
                                rows.createCell(6).setCellValue("pph21");
                                cellB = rows.createCell(7);
                                cellB.setCellValue(new BigDecimal(t.getString("_hasil")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                            }
                            z++;
                            rows = sheet.createRow(z);
                            rows.createCell(6).setCellValue("pendapatan setelah dipotong pajak");
                            cellB = rows.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_TakeHomePay")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        } else {
//                            System.out.println("Here "+j);
                            Row add = sheet.createRow(z);
                            add.createCell(4).setCellValue(obj.getString("activity_title"));
                            add.createCell(5).setCellValue(obj.getString("type_title"));
                            add.createCell(6).setCellValue("bulan");
                            add.createCell(7).setCellValue(obj.getString("bulan"));
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pendapatan bruto");
                            Cell cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("bruto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pengurang biaya jabatan");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_jabatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pengurang iuran pensiun");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pot_pensiun")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pendapatn netto");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_pendapatan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("ptkp sebulan");
                            cellB = add.createCell(7);
                            if(obj.getString("ptkp_sebulan") == null)
                                cellB.setCellValue("");
                            else {
                                cellB.setCellValue(new BigDecimal(obj.getString("ptkp_sebulan")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                            }
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pkp sebulan");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("pkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("sisa ptkp sebulan");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("sisa_ptkp_sebulan")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                            BasicDBList listPph = (BasicDBList) obj.get("pph21");
                            for (Object p : listPph) {
                                BasicDBObject t = (BasicDBObject) p;
                                z++;
                                add = sheet.createRow(z);
                                add.createCell(6).setCellValue("tarif");
                                cellB = add.createCell(7);
                                cellB.setCellValue(new BigDecimal(t.getString("_tarif")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                                z++;
                                add = sheet.createRow(z);
                                add.createCell(6).setCellValue("pkp");
                                cellB = add.createCell(7);
                                cellB.setCellValue(new BigDecimal(t.getString("_pkp")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                                z++;
                                add = sheet.createRow(z);
                                add.createCell(6).setCellValue("pph21");
                                cellB = add.createCell(7);
                                cellB.setCellValue(new BigDecimal(t.getString("_hasil")).doubleValue());
                                cellB.setCellType(CellType.NUMERIC);
                                cellB.setCellStyle(currency);
                            }
                            z++;
                            add = sheet.createRow(z);
                            add.createCell(6).setCellValue("pendapatan setelah dipotong pajak");
                            cellB = add.createCell(7);
                            cellB.setCellValue(new BigDecimal(obj.getString("netto_TakeHomePay")).doubleValue());
                            cellB.setCellType(CellType.NUMERIC);
                            cellB.setCellStyle(currency);
                        }
                        count++;
                        z++;
                    }
                }
                z++;
            }
        }
        try (OutputStream fileOut = new FileOutputStream("D:\\PAJAK_2019_0-REV-AGAIN3.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static void main(String[] args) throws IOException {
        new CreateExcelPajak();
    }

    private String callApi(String ep, String method, boolean ssl) throws IOException {
        URL obj = new URL(ep);

        if(ssl) {
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setConnectTimeout(300);
            conn.setRequestMethod( method );
            conn.setUseCaches( true );
            conn.setDoOutput( true );
            conn.setDoInput(true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }else {
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setConnectTimeout(300);
            conn.setRequestMethod( method );
            conn.setUseCaches( true );
            conn.setDoOutput( true );
            conn.setDoInput(true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
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
