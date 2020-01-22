package usu.pajak.util;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import usu.pajak.model.Pegawai;
import usu.pajak.model.UserPajak;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ExcelReader {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://fariz:Laru36Dema@clusterasetmongo-shard-00-00-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-01-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-02-t3kc1.mongodb.net:27017/test?ssl=true&replicaSet=ClusterAsetMongo-shard-0&authSource=admin&retryWrites=true")); //connect to mongodb
    private static Datastore datastore = new Morphia().createDatastore(client, "pajak");

    public static final String SAMPLE_XLSX_FILE_PATH = "D:\\GajiAPBN\\fwdgaji_februari_2019_usu\\kumpulan_gaji_pns_feb.xls";
    public static final String DataPTKP_XLSX_FILE_PATH = "D:\\Daftar PTKP-nama-unit-rev1.xls";
    private static CellStyle style,styleWarning;
    private static JsonArray jArray;
    private static final Integer TK0 = 1000;
    private static final Integer TK1 = 1001;
    private static final Integer TK2 = 1002;
    private static final Integer TK3 = 1003;
    private static final Integer K0 = 1100;
    private static final Integer K1 = 1101;
    private static final Integer K2 = 1102;
    private static final Integer K3 = 1103;

    // Not sure??
    private static final Integer KI0 = 1110;
    private static final Integer KI1 = 1111;
    private static final Integer KI2 = 1112;
    private static final Integer KI3 = 1113;

    private static final Double persenPotJabatan = Double.valueOf("0.05");
    private static final Double persenPotPensiun = Double.valueOf("0.0475");

    private static DecimalFormat dec = new DecimalFormat("#.00");

    private static Pegawai pegawai;

    private static LinkedHashMap<String,Integer> listDataKeluarga = new LinkedHashMap<>();
    private static Query<UserPajak> query;

    private static void getDataKeluarga()throws IOException{
        Workbook workbook = WorkbookFactory.create(new File(DataPTKP_XLSX_FILE_PATH));
        Sheet sheet = workbook.getSheetAt(0);
        sheet.forEach(row -> {
            AtomicReference<String> key = new AtomicReference<>("");
            AtomicInteger value = new AtomicInteger(0);
            row.forEach(cell -> {
                if(cell.getColumnIndex()==2 && cell.getRow().getRowNum() > 0){
                    key.set(cell.getStringCellValue());
                    value.set(Double.valueOf(row.getCell(5).getNumericCellValue()).intValue());
                }
            });
                if(listDataKeluarga.get(key.toString())== null){
                    listDataKeluarga.put(key.toString(), value.intValue());
                }
        });
        workbook.close();
    }

    private static void cocokanSimSdm()throws IOException{
        Workbook workbook = WorkbookFactory.create(new File("D:/Daftar PTKP-nama-unit-rev.xls"));
        Sheet sheet = workbook.getSheetAt(0);
        style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sheet.forEach(row -> {
//            AtomicReference<String> key = new AtomicReference<>("");
//            AtomicInteger value = new AtomicInteger(0);
            row.forEach(cell -> {
                if(cell.getColumnIndex()==2 && cell.getRow().getRowNum() > 0){
//                    AtomicReference<HashMap<String,String>> result = new AtomicReference<HashMap<String, String>>();
                    AtomicReference<Boolean> result = new AtomicReference<Boolean>(false);
                    jArray.forEach(jObj -> {
                        if(jObj.getAsJsonObject().get("nip").getAsString().equalsIgnoreCase(cell.getStringCellValue())){
                            row.createCell(9).setCellValue(jObj.getAsJsonObject().get("work_unit_id").getAsString());
                            row.createCell(10).setCellValue(jObj.getAsJsonObject().get("work_unit").getAsString());
                            row.createCell(11).setCellValue(jObj.getAsJsonObject().get("npwp").getAsString().replaceAll("\\D",""));
                            result.set(true);
//                            HashMap<String,String> hashMap = new HashMap<>();
//                            hashMap.put("nip",jObj.getAsJsonObject().get("nip").getAsString());
//                            hashMap.put("npwp",jObj.getAsJsonObject().get("npwp").getAsString());
//                            hashMap.put("work_unit_id",jObj.getAsJsonObject().get("work_unit_id").getAsString());
//                            hashMap.put("work_unit",jObj.getAsJsonObject().get("work_unit").getAsString());
//                            result.set(hashMap);
                            return;
                        }
                    });
                    if(!result.get()) {
                        row.setRowStyle(style);
                        cell.setCellStyle(style);
                    }
                }
            });
        });
        try (OutputStream fileOut = new FileOutputStream("D:/Daftar PTKP-nama-unit-rev1.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public static void main(String[] args) throws IOException {
        String response = callApi("https://api.usu.ac.id/1.0/users/","GET");
        jArray = new Gson().fromJson(response, JsonArray.class);
        getDataKeluarga();
        System.out.println(SAMPLE_XLSX_FILE_PATH);
//        cocokanSimSdm();
        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));

        // Retrieving the number of sheets in the Workbook
        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

        /*
           =============================================================
           Iterating over all the sheets in the workbook (Multiple ways)
           =============================================================
        */

        // 1. You can obtain a sheetIterator and iterate over it
//        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
//        System.out.println("Retrieving Sheets using Iterator");
//        while (sheetIterator.hasNext()) {
//            Sheet sheet = sheetIterator.next();
//            System.out.println("=> " + sheet.getSheetName());
//        }

        // 2. Or you can use a for-each loop
//        System.out.println("Retrieving Sheets using for-each loop");
//        for(Sheet sheet: workbook) {
//            System.out.println("=> " + sheet.getSheetName());
//        }

        // 3. Or you can use a Java 8 forEach with lambda
//        System.out.println("Retrieving Sheets using Java 8 forEach with lambda");
//        workbook.forEach(sheet -> {
//            System.out.println("=> " + sheet.getSheetName());
//        });

        /*
           ==================================================================
           Iterating over all the rows and columns in a Sheet (Multiple ways)
           ==================================================================
        */

        // Getting the Sheet at index zero
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        // 1. You can obtain a rowIterator and columnIterator and iterate over them
//        System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
//        Iterator<Row> rowIterator = sheet.rowIterator();
//        while (rowIterator.hasNext()) {
//            Row row = rowIterator.next();
//
//            // Now let's iterate over the columns of the current row
//            Iterator<Cell> cellIterator = row.cellIterator();
//
//            while (cellIterator.hasNext()) {
//                Cell cell = cellIterator.next();
//                String cellValue = dataFormatter.formatCellValue(cell);
//                System.out.print(cellValue + "\t");
//            }
//            System.out.println();
//        }

        // 2. Or you can use a for-each loop to iterate over the rows and columns
//        System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
//        for (Row row: sheet) {
//            for(Cell cell: row) {
//                String cellValue = dataFormatter.formatCellValue(cell);
//                System.out.print(cellValue + "\t");
//            }
//            System.out.println();
//        }

        // 3. Or you can use Java 8 forEach loop with lambda
        System.out.println("\n\nIterating over Rows and Columns using Java 8 forEach with lambda\n");
        style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        styleWarning = workbook.createCellStyle();
        styleWarning.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        styleWarning.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        styleWarning.setFillPattern(FillPatternType.SOLID_FOREGROUND);

//        sheet.shiftColumns(13,sheet.getLastRowNum()+1,1);
//        sheet.getRow(0).createCell(51).setCellValue("NPWP SIMSDM");
//        sheet.getRow(0).createCell(48).setCellValue("PTKP dari File ini");
//        sheet.getRow(0).createCell(49).setCellValue("PTKP dari SIMSDM");
//        sheet.getRow(0).createCell(50).setCellValue("Selisih PTKP");
//        sheet.getRow(0).createCell(52).setCellValue("PPH21");

        workbook.forEach(shet -> {
            shet.forEach(row -> {
                AtomicBoolean rowCheck = new AtomicBoolean(false);
                row.forEach(cell -> {
                    if(cell.getColumnIndex() == 8 && cell.getRow().getRowNum() > 0) {
                        // Check through api simsdm if there is same nip.
                        String nip = cell.getStringCellValue();
//                    Integer idUser = isNip(nip);
//                    if(idUser==0)
//                        idUser = isNip(row.getCell(52).getStringCellValue());
                        query = datastore.createQuery(UserPajak.class).filter("nip_gpp",nip);
                        UserPajak userPajak = query.get();
                        UpdateOperations<UserPajak> ops = datastore.createUpdateOperations(UserPajak.class);
                        if(userPajak != null){

                            rowCheck.set(false);

//                        UserPajak userPajak = new UserPajak();
//                        userPajak.setId_user(idUser.toString());
//                        userPajak.setNip_gpp(nip);
//                        if(row.getCell(52) != null)
//                            userPajak.setNip_simsdm(row.getCell(52).getStringCellValue());
//                        else
//                            userPajak.setNip_simsdm("");

//                        String[] fullName = getFullName(idUser.toString());
//                        userPajak.setFront_degree(fullName[0]);
//                        userPajak.setFull_name(fullName[1]);
//                        userPajak.setBehind_degree(fullName[2]);

                            BasicDBList arrayListPendapatan = new BasicDBList();
                            BasicDBList arrayListPPh21 = new BasicDBList();

                            Integer bulan = Integer.parseInt(row.getCell(4).getStringCellValue());
                            Integer tahun = Integer.parseInt(row.getCell(5).getStringCellValue());

                            //gaji & tunjangan
                            Integer brutoPendapatan = 0,nettoTakeHomePay=0;
                            BasicDBObject listPendapatan = new BasicDBObject();
                            listPendapatan.put("activity_id","apbn");
                            listPendapatan.put("request_id","apbn");
                            listPendapatan.put("salary_id","apbn");
                            listPendapatan.put("bulan",Integer.toString(bulan));
                            listPendapatan.put("tahun",Integer.toString(tahun));

                            for(int i=22;i<34;i++){
                                Integer pendapatan = Double.valueOf(row.getCell(i).getNumericCellValue()).intValue();
                                listPendapatan.put(sheet.getRow(0).getCell(i).getStringCellValue(),Integer.toString(pendapatan));
                                brutoPendapatan += pendapatan;
                                nettoTakeHomePay += pendapatan;
                            }

                            //bukan potongan pajak
                            for(int i=36;i<43;i++){
                                Integer potongan = Double.valueOf(row.getCell(i).getNumericCellValue()).intValue();
                                listPendapatan.put("bkn-"+sheet.getRow(0).getCell(i).getStringCellValue(),Integer.toString(potongan));
                                nettoTakeHomePay -= potongan;
                            }

                            //potongan jabatan
                            Integer totalPotongan = 0;
                            Double potonganJabatan = persenPotJabatan * brutoPendapatan;
                            potonganJabatan = pembulatan100(potonganJabatan);
                            listPendapatan.put("pot_jabatan",dec.format(potonganJabatan));

                            //potongan pensiun
                            Integer tunjangan = Integer.parseInt((String) listPendapatan.get("gjpokok"))+
                                    Integer.parseInt((String) listPendapatan.get("tjistri"))+Integer.parseInt((String) listPendapatan.get("tjanak"));
                            Double potonganPensiun = persenPotPensiun * tunjangan;
                            potonganPensiun = pembulatan100(potonganPensiun);
                            listPendapatan.put("pot_pensiun",dec.format(potonganPensiun));

                            totalPotongan = potonganJabatan.intValue()+(potonganPensiun.intValue());

                            //netto
                            Integer nettoPendapatan = brutoPendapatan - totalPotongan;

                            listPendapatan.put("netto_TakeHomePay",Integer.toString(nettoTakeHomePay));
                            listPendapatan.put("netto_pendapatan",Integer.toString(nettoPendapatan));

                            //ptkp
                            Integer ptkp =0;
                            if(listDataKeluarga.get(nip)==null) {
                                System.out.println("Tidak ada pada kumpulan data nip:"+nip);
                            }else
                                ptkp = listDataKeluarga.get(nip);

//                        listPendapatan.put("ptkp_setahun",Integer.toString(ptkp));

                            Integer ptkpSebulan = ptkp/12;
                            listPendapatan.put("ptkp_sebulan",Integer.toString(ptkpSebulan));

                            Integer pkp_sebulan=0,sisa_ptkp_sebulan=0;
                            if(nettoPendapatan>=ptkpSebulan){
                                pkp_sebulan = nettoPendapatan-ptkpSebulan;
                                listPendapatan.put("pkp_sebulan",Integer.toString(pkp_sebulan));
                                listPendapatan.put("sisa_ptkp_sebulan","0");
                            }else{
                                sisa_ptkp_sebulan = ptkpSebulan-nettoPendapatan;
                                listPendapatan.put("pkp_sebulan","0");
                                listPendapatan.put("sisa_ptkp_sebulan",Integer.toString(sisa_ptkp_sebulan));
                            }

                            BasicDBObject listPPh21 = new BasicDBObject();
                            Double tarifPph21 = 0.05;
                            listPPh21.put("tarif","0.05");
                            listPPh21.put("pkp",Integer.toString(pkp_sebulan));
                            listPPh21.put("hasil",Integer.toString(Double.valueOf(row.getCell(34).getNumericCellValue()).intValue()));
                            arrayListPPh21.add(listPPh21);
                            listPendapatan.put("pph21",arrayListPPh21);
                            listPendapatan.put("update_time",new Timestamp(new Date().getTime()).toString());
                            arrayListPendapatan.add(listPendapatan);

                            ops.push("pendapatan",arrayListPendapatan);
//                        userPajak.setPendapatan_tdk_tetap(arrayListPendapatan);
//                        userPajak.setNetto_pendapatan_setahun(Integer.toString(nettoPendapatan)); // setelah di januari di tambahkan ambil dari database
                            Integer netto_pendapatan_setahun = Integer.parseInt(userPajak.getNetto_pendapatan_setahun())+nettoPendapatan;
                            ops.set("netto_pendapatan_setahun",netto_pendapatan_setahun.toString());
//                        userPajak.setPtkp_setahun(Integer.toString(ptkp));
//                        userPajak.setSisa_ptkp(Integer.toString(ptkp-nettoPendapatan)); // diubah menjadi sisa ptkp yg bulan sebelumnya dikurangi netto pendapatan
                            Integer sisa_ptkp = Integer.parseInt(userPajak.getSisa_ptkp()) - nettoPendapatan;
                            ops.set("sisa_ptkp",sisa_ptkp.toString());

//                        userPajak.setTotal_pkp(Integer.toString(pkp_sebulan)); // di akumulasi ke data sebelumnya
                            Integer total_pkp = Integer.parseInt(userPajak.getTotal_pkp()) + pkp_sebulan;
                            ops.set("total_pkp", total_pkp.toString());

                            AtomicReference<Double> total_pph21_usu = new AtomicReference<>(Double.valueOf(userPajak.getTotal_pph21_usu()));
                            arrayListPPh21.forEach(wer -> {
                                total_pph21_usu.updateAndGet(v -> v + Double.valueOf(((BasicDBObject) wer).get("hasil").toString()));
                            });
                            ops.set("total_pph21_usu",dec.format(total_pph21_usu.get()));
//                        userPajak.setTotal_pph21_usu(Integer.toString(Double.valueOf(row.getCell(34).getNumericCellValue()).intValue())); //diubah menjadi increment berdasarkan hasil perhitungan pajak

//                        userPajak.setTotal_pph21_pribadi("0"); // utk di sistem pajak usu

//                        Timestamp
                            userPajak.setTimestamp(new Timestamp(new Date().getTime()).toString());
                            ops.set("timestamp",new Timestamp(new Date().getTime()).toString());

                            datastore.update(query,ops);
//                        Double resPph21 = tarifPph21*pkp_sebulan*12;
//                        Double pem100 = pembulatan100(resPph21);
//                        resPph21+=pem100;
//                        Integer penambah = ceilCustom(dec.format(resPph21/12));
//                        Integer pph21 =penambah;
//                        listPendapatan.put("pph21",pph21.intValue());
//                        row.createCell(52).setCellValue(tarifPph21*pkp_setahun/12);
//                        row.createCell(53).setCellValue(pph21);
//
//                        row.createCell(48).setCellValue(ptkp);
//
//                        row.createCell(49).setCellValue(ptkpSimSdm);
//
//                        row.createCell(50).setCellValue(selisih);
//                        if(selisih != 0) {
//                            cell.setCellStyle(styleWarning);
////                            cell.getRow().setRowStyle(styleWarning);
//                        }

                            //check npwp
//                        String npwp = row.getCell(12).getStringCellValue();
//                        userPajak.setNpwp(npwp);
//
//                        datastore.save(userPajak);
                            return;
                        }else{
//                        cell.setCellStyle(style);
//                        cell.getRow().setRowStyle(style);
//                        rowCheck.set(true);
                        }
                    }else{
//                    if(rowCheck.get()) cell.setCellStyle(style);
                    }
                });

            });
        });


//        try (OutputStream fileOut = new FileOutputStream("D:\\GajiAPBN\\1_Gaji_Januari_2019_BPA-rev2.xls")) {
//            workbook.write(fileOut);
//        }

        // Closing the workbook
        workbook.close();
    }

    private static Integer parseKdKawinToPtkp(Integer kdKawin){
        Integer result = kdKawin.intValue()==TK0.intValue() ? 54000000 :
                        (kdKawin.intValue()==TK1.intValue() || kdKawin.intValue()==K0.intValue()) ? 58500000 :
                        (kdKawin.intValue()==TK2.intValue() || kdKawin.intValue()==K1.intValue()) ? 63000000 :
                        (kdKawin.intValue()==TK3.intValue() || kdKawin==K2.intValue()) ? 67500000 :
                         kdKawin.intValue()==K3.intValue() ? 72000000 :
                         kdKawin.intValue()==KI0.intValue() ? 112500000 :
                         kdKawin.intValue()==KI1.intValue() ? 117000000 :
                         kdKawin.intValue()==KI2.intValue() ? 121500000 :
                         kdKawin.intValue()==KI3.intValue() ? 126000000 : 0;

        return result;
    }

    private static int isNip(String nip){
        AtomicReference<Integer> result = new AtomicReference<>(0);
        jArray.forEach(jObj -> {
            if(jObj.getAsJsonObject().get("nip").getAsString().equalsIgnoreCase(nip)){
                result.set(Integer.parseInt(jObj.getAsJsonObject().get("id").getAsString()));
                return;
            }
        });
        return result.get();
    }

    private static String[] getFullName(String idUser){
        AtomicReference<String[]> result = new AtomicReference<>();
        jArray.forEach(jObj -> {
            if(jObj.getAsJsonObject().get("id").getAsString().equalsIgnoreCase(idUser)){
                result.set(new String[]{jObj.getAsJsonObject().get("front_degree").getAsString(),jObj.getAsJsonObject().get("full_name").getAsString(),
                        jObj.getAsJsonObject().get("behind_degree").getAsString()});
                return;
            }
        });
        return result.get();
    }

    public static Double pembulatan100(Double nilai){
        String credits = dec.format(nilai);
        Double pembulatan = 0.0;
        Double nil = Double.valueOf(credits);
        if(credits.length()>3)
            pembulatan = 100 - Double.valueOf(credits.substring(credits.length()-5,credits.length()));

        return Double.valueOf(dec.format(pembulatan+nil));
    }

//    private static Double pembulatanKoma(Double nilai){
//        String credits = dec.format(nilai);
//        Double pembulatan = 1 - Double.valueOf("0"+credits.substring(credits.length()-3,credits.length()));
//
//        return Double.valueOf(dec.format(nilai+pembulatan));
//    }
public static int ceilCustom(String number)
{
    int decimalNum = 0;
    if(number.length()>3) {
        String[] numberInfo = number.split("\\.");
        int boundary = 5;
        decimalNum = Integer.parseInt(numberInfo[0]);

        if (numberInfo.length > 1) {
            char[] commaNum = numberInfo[1].toCharArray();

            for (int i = commaNum.length - 1; i > 0; i--) {
                if (commaNum[i] >= boundary) {
                    commaNum[i - 1] = (char) ((commaNum[i] - '0') + 1);
                }
            }

            if (commaNum[0] >= boundary) {
                decimalNum += 1;
            }
        }
    }

    return decimalNum;
}

    private static Double hitungPPH21(){
        return 0.0;
    }

    private static String callApi(String ep, String method) throws IOException {
        URL obj = new URL(ep);
        HttpsURLConnection conn= (HttpsURLConnection) obj.openConnection();

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




