package usu.pajak.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import usu.pajak.model.PendapatanTetaps;
import usu.pajak.model.TarifPajak;
import usu.pajak.model.UserPajak;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PnsApbn {
//    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://fariz:Laru36Dema@clusterasetmongo-shard-00-00-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-01-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-02-t3kc1.mongodb.net:27017/test?ssl=true&replicaSet=ClusterAsetMongo-shard-0&authSource=admin&retryWrites=true")); //connect to mongodb
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/new_pajak_2019")); //connect to mongodb
    private static Datastore datastore = new Morphia().createDatastore(client, "new_pajak_2019");

    private static final String[] SAMPLE_XLSX_FILE_PATH2 = new String[]{"D:\\GajiAPBN\\fwdgaji_februari_2019_usu\\kumpulan_gaji_pns_feb.xls",
            "D:/GajiAPBN/gaji_bulan_maret_2019_pns_usu/kumpulan_gaji_maret_pns.xls",
            "D:/GajiAPBN/gaji_bulan_april_2019_pns_usu/kumpulan_gaji_bulan_april.xls",
            "D:/GajiAPBN/gaji_bulan_mei_2019_pns_usu/kumpulan_gaji_mei.xls",
            "D:/GajiAPBN/gaji_juni_2019_pns_usu/kumpulan_gaji_juni.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_juli_2019/kumpulan_gaji_juli.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_agustus_2019/kumpulan_gaji_agustus.xls"
    };
    private static final String SAMPLE_XLSX_FILE_PATH = "D:\\GajiAPBN\\kumpulan_pns_januari.xls";
    private static final String DataPTKP_XLSX_FILE_PATH = "D:\\Daftar PTKP-nama-unit-rev1.xls";

    private final Double persenPotJabatan = Double.valueOf("0.05");
    private final Double persenPotPensiun = Double.valueOf("0.0475");

    private DecimalFormat dec = new DecimalFormat("#.000");
//    MathContext mc = new MathContext(2, RoundingMode.HALF_UP);


    private JsonArray jArray;

    private LinkedHashMap<String,Integer> listDataKeluarga = new LinkedHashMap<>();
    private Query<UserPajak> query;

    public PnsApbn(int bulan){
        try {
            if(bulan == 1)
                januari2019();
            else{
                februari2019(bulan-2);
            }

//            else if(bulan == 3)
//                februari2019(1);
//            else if(bulan)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    private void februari2019(int ind) throws IOException{
        String response = callApi("https://api.usu.ac.id/1.0/users/","GET");
        jArray = new Gson().fromJson(response, JsonArray.class);
        getDataKeluarga();

        Workbook workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH2[ind]));
        System.out.println(SAMPLE_XLSX_FILE_PATH2[ind]);
        workbook.forEach(sheet -> {
            sheet.forEach(row -> {
                row.forEach(cell -> {
                    if(cell.getColumnIndex() == 8 && cell.getRow().getRowNum() > 0) {
                        String nip = cell.getStringCellValue();
                        query = datastore.createQuery(UserPajak.class).filter("nip_gpp", nip);
                        UserPajak userPajak = query.get();
                        if(userPajak == null){
                            query = datastore.createQuery(UserPajak.class).filter("nip_simsdm",nip);
                            userPajak = query.get();
                        }
                        UpdateOperations<UserPajak> ops = datastore.createUpdateOperations(UserPajak.class);
                        if(nip.equalsIgnoreCase("130900663000000000"))
                            System.out.println("");
                        if(userPajak != null){
//                            BasicDBList arrayListPendapatan = new BasicDBList();
                            BasicDBList arrayListPendapatan = userPajak.getPendapatan_tetap();
//                            BasicDBList arrayListPPh21 = new BasicDBList();

                            Integer bulan = Integer.parseInt(row.getCell(4).getStringCellValue());
                            Integer tahun = Integer.parseInt(row.getCell(5).getStringCellValue());

                            String[] fullName = getFullName(userPajak.getId_user());

                            BasicDBObject pendapatan = new BasicDBObject();
                            pendapatan.put("activity_id","apbn");
                            pendapatan.put("activity_title", "APBN");
                            pendapatan.put("request_id","apbn");
                            pendapatan.put("salary_id","apbn");
                            pendapatan.put("type_id","apbn");
                            pendapatan.put("type_title","Gaji APBN");
                            pendapatan.put("unit_id",fullName[3]);
                            pendapatan.put("unit_name",fullName[4]);
                            pendapatan.put("bulan",Integer.toString(bulan));
                            pendapatan.put("tahun",Integer.toString(tahun));

                            BigDecimal brutoPendapatan=new BigDecimal("0.00");
                            BigDecimal nettoTakeHomePay=new BigDecimal("0.00");
                            BigDecimal nettoPendapatan=new BigDecimal("0.00");
                            for(int i=22;i<34;i++){
                                BigDecimal pendapatanSementara = new BigDecimal(row.getCell(i).getNumericCellValue());
                                pendapatan.put(sheet.getRow(0).getCell(i).getStringCellValue(),pendapatanSementara.toString());
                                brutoPendapatan= brutoPendapatan.add(pendapatanSementara);
                            }

                            pendapatan.put(sheet.getRow(0).getCell(34).getStringCellValue(),
                                    new BigDecimal(row.getCell(34).getNumericCellValue()).toString());

                            for(int i=36;i<43;i++){
                                BigDecimal potongan = new BigDecimal(row.getCell(i).getNumericCellValue());
                                pendapatan.put("bkn-"+sheet.getRow(0).getCell(i).getStringCellValue(),potongan.toString());
                            }

                            pendapatan.put("bruto_pendapatan",brutoPendapatan.toString());

                            nettoTakeHomePay = new BigDecimal(row.getCell(43).getNumericCellValue());

                            BigDecimal totalPotongan;

                            BigDecimal potonganJabatan = brutoPendapatan.multiply(BigDecimal.valueOf(persenPotJabatan));
                            if(potonganJabatan.compareTo(BigDecimal.valueOf(500000.00)) <= -1) {
                                pendapatan.put("pot_jabatan", potonganJabatan.toString());
                            }else{
                                potonganJabatan = new BigDecimal("500000.00");
                                pendapatan.put("pot_jabatan", potonganJabatan.toString());
                            }

                            BigDecimal tunjangan = new BigDecimal("0.00");
                            tunjangan = tunjangan.add(new BigDecimal(pendapatan.get("gjpokok").toString())
                                    .add(new BigDecimal(pendapatan.get("tjistri").toString())
                                            .add(new BigDecimal(pendapatan.get("tjanak").toString()))));

                            BigDecimal potonganPensiun = tunjangan.multiply(BigDecimal.valueOf(persenPotPensiun));
                            pendapatan.put("pot_pensiun",potonganPensiun.toString());

                            totalPotongan = potonganJabatan.add(potonganPensiun);

                            nettoPendapatan = brutoPendapatan.subtract(totalPotongan);

                            pendapatan.put("netto_TakeHomePay",nettoTakeHomePay.toString());
                            pendapatan.put("netto_pendapatan",nettoPendapatan.toString());

                            Integer ptkp =54000000;
                            if(listDataKeluarga.get(nip)==null) {
                                System.out.println("Tidak ada pada kumpulan data nip:"+nip);
                                Integer sPtkp = new ApiRka().hitungPtkp(userPajak.getId_user());
                                if(sPtkp > 0){
                                    ptkp = sPtkp;
                                }
                            }else
                                ptkp = listDataKeluarga.get(nip);

                            BigDecimal ptkpSetahun = new BigDecimal(ptkp.toString());
                            BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(12));
                            BigDecimal pkpSetahun,sisaPtkpSetahun;

                            userPajak.setPotongan_jabatan_A2_setahun(new BigDecimal(userPajak.getPotongan_jabatan_A2_setahun()).add(potonganJabatan).toString());
                            ops.set("potongan_jabatan_A2_setahun",new BigDecimal(userPajak.getPotongan_jabatan_A2_setahun()).add(potonganJabatan).toString());

                            if(nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0){
                                pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                                sisaPtkpSetahun = new BigDecimal("0.00");
//                                pendapatan.put("pkp_setahun",pkpSetahun.toString());
//                                pendapatan.put("sisa_ptkp_setahun",sisaPtkpSetahun.toString());
                            }else{
                                pkpSetahun = new BigDecimal("0.00");
                                sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
//                                pendapatan.put("pkp_setahun",pkpSetahun.toString());
//                                pendapatan.put("sisa_ptkp_setahun",sisaPtkpSetahun.toString());
                            }

                            TarifPajak t = new TarifPajak();
                            t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP,true);
                            pendapatan.put("pph21",t.getListPph21());
//                            pendapatan.put("pph21_layer",t.getIndex().toString());
//                            pendapatan.put("pph21_reminder",t.getReminderPajak().toString());
//                            pendapatan.put("update_time",new Timestamp(new Date().getTime()).toString());

//                            BasicDBObject pendapatanSebelumnya = (BasicDBObject) userPajak.getPendapatan_tdk_tetap().get(userPajak.getPendapatan_tdk_tetap().size()-1);
//                            BigDecimal sisa_ptkp_sebulan_sebelumnya = new BigDecimal(pendapatanSebelumnya.get("sisa_ptkp_sebulan").toString());
//                            BigDecimal ptkpSebulan = sisa_ptkp_sebulan_sebelumnya.add(
//                                    ptkpSetahun.divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP));
//
//                            BigDecimal pkp_sebulan=new BigDecimal("0.00"),sisa_ptkp_sebulan=new BigDecimal("0.00");
//                            if(nettoPendapatan.compareTo(ptkpSebulan)>=0){
//                                pkp_sebulan = nettoPendapatan.subtract(ptkpSebulan);
//                                pendapatan.put("pkp_sebulan",pkp_sebulan.toString());
//                                pendapatan.put("sisa_ptkp_sebulan","0.00");
//                            }else{
//                                sisa_ptkp_sebulan = ptkpSebulan.subtract(nettoPendapatan);
//                                pendapatan.put("pkp_sebulan","0.00");
//                                pendapatan.put("sisa_ptkp_sebulan",sisa_ptkp_sebulan.toString());
//                            }

                            arrayListPendapatan.add(pendapatan);
                            userPajak.setPendapatan_tetap(arrayListPendapatan);
                            ops.push("pendapatan_tetap",arrayListPendapatan);

                            userPajak.setTotal_pph21_usu(new BigDecimal(userPajak.getTotal_pph21_usu()).add(BigDecimal.valueOf(row.getCell(34).getNumericCellValue())).toString());
                            ops.set("total_pph21_usu",new BigDecimal(userPajak.getTotal_pph21_usu()).add(BigDecimal.valueOf(row.getCell(34).getNumericCellValue())).toString());
                            userPajak.setTotal_pph21_usu_dibayar(new BigDecimal(userPajak.getTotal_pph21_usu_dibayar()).add(BigDecimal.valueOf(row.getCell(34).getNumericCellValue())).toString());
                            ops.set("total_pph21_usu_dibayar",new BigDecimal(userPajak.getTotal_pph21_usu_dibayar()).add(BigDecimal.valueOf(row.getCell(34).getNumericCellValue())).toString());
//                            total_pph21_usu
//                            :
//                            "258741.6000"
//                            total_pph21_usu_dibayar
//                            :
//                            "28112.0"
//                            BigDecimal netto_pendapatan_setahun = new BigDecimal(userPajak.getNetto_pendapatan_setahun()).add(nettoPendapatan);
//                            ops.set("netto_pendapatan_setahun",netto_pendapatan_setahun.toString());
//                            BigDecimal sisa_ptkp = new BigDecimal(userPajak.getSisa_ptkp()).subtract(nettoPendapatan);
//                            ops.set("sisa_ptkp",sisa_ptkp.toString());
//                            BigDecimal total_pkp = new BigDecimal(userPajak.getTotal_pkp()).add(pkp_sebulan);

//                            datastore.update(query,ops);
                            datastore.save(userPajak);
                        }
                        else{
                            System.out.println("NIP_GPP yg tidak ada di mongo:"+nip);
                            Integer idUser = isNip(nip);
                            if(idUser==0) {
                                System.out.println(row.getCell(52).getStringCellValue());
                                idUser = isNip(row.getCell(52).getStringCellValue());
                            }else{
                                System.out.println(nip);
                            }
                            if(idUser != 0){
                                userPajak = new UserPajak();
                                userPajak.setId_user(idUser.toString());
                                userPajak.setNip_gpp(nip);
                                if(row.getCell(52) != null)
                                    userPajak.setNip_simsdm(row.getCell(52).getStringCellValue());
                                else
                                    userPajak.setNip_simsdm("");

                                String[] fullName = getFullName(idUser.toString());
                                userPajak.setFront_degree(fullName[0]);
                                userPajak.setFull_name(fullName[1]);
                                userPajak.setBehind_degree(fullName[2]);
                                BasicDBList arrayListPendapatan = new BasicDBList();

                                String bulan = new Integer(row.getCell(4).getStringCellValue()).toString();
                                String tahun = row.getCell(5).getStringCellValue();

                                BasicDBObject pendapatanTetaps = new BasicDBObject();
//                            PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
//                            pendapatanTetaps.set_idUser(idUser.toString());
//                            pendapatanTetaps.put("_idUser",idUser.toString());
                                pendapatanTetaps.put("activity_id","apbn");
                                pendapatanTetaps.put("activity_title", "APBN");
                                pendapatanTetaps.put("request_id","apbn");
                                pendapatanTetaps.put("salary_id","apbn");
                                pendapatanTetaps.put("type_id","apbn");
                                pendapatanTetaps.put("type_title","Gaji APBN");
                                pendapatanTetaps.put("unit_id",fullName[3]);
                                pendapatanTetaps.put("unit_name",fullName[4]);
                                pendapatanTetaps.put("bulan",bulan);
                                pendapatanTetaps.put("tahun",tahun);
                                String npwp = row.getCell(12).getStringCellValue();
                                userPajak.setNpwp(npwp);

                                BigDecimal brutoPendapatan=new BigDecimal("0.00");
                                BigDecimal nettoTakeHomePay=new BigDecimal("0.00");
                                BigDecimal nettoPendapatan=new BigDecimal("0.00");
                                for(int i=22;i<34;i++){
                                    BigDecimal pendapatanSementara = new BigDecimal(row.getCell(i).getNumericCellValue());
                                    pendapatanTetaps.put(sheet.getRow(0).getCell(i).getStringCellValue(),pendapatanSementara.toString());
                                    brutoPendapatan= brutoPendapatan.add(pendapatanSementara);
                                }

                                pendapatanTetaps.put(sheet.getRow(0).getCell(34).getStringCellValue(),
                                        new BigDecimal(row.getCell(34).getNumericCellValue()).toString());

                                for(int i=36;i<43;i++){
                                    BigDecimal potongan = new BigDecimal(row.getCell(i).getNumericCellValue());
                                    pendapatanTetaps.put("bkn-"+sheet.getRow(0).getCell(i).getStringCellValue(),potongan.toString());
                                }

                                pendapatanTetaps.put("bruto_pendapatan",brutoPendapatan.toString());

                                nettoTakeHomePay = new BigDecimal(row.getCell(43).getNumericCellValue());

                                BigDecimal totalPotongan;

                                BigDecimal potonganJabatan = brutoPendapatan.multiply(BigDecimal.valueOf(persenPotJabatan));
                                if(potonganJabatan.compareTo(BigDecimal.valueOf(500000.00)) <= -1) {
                                    pendapatanTetaps.put("pot_jabatan", potonganJabatan.toString());
                                }else{
                                    potonganJabatan = new BigDecimal("500000.00");
                                    pendapatanTetaps.put("pot_jabatan", potonganJabatan.toString());
                                }

                                BigDecimal tunjangan = new BigDecimal("0.00");
                                tunjangan = tunjangan.add(new BigDecimal(pendapatanTetaps.get("gjpokok").toString())
                                        .add(new BigDecimal(pendapatanTetaps.get("tjistri").toString())
                                                .add(new BigDecimal(pendapatanTetaps.get("tjanak").toString()))));

                                BigDecimal potonganPensiun = tunjangan.multiply(BigDecimal.valueOf(persenPotPensiun));
                                pendapatanTetaps.put("pot_pensiun",potonganPensiun.toString());

                                totalPotongan = potonganJabatan.add(potonganPensiun);

                                nettoPendapatan = brutoPendapatan.subtract(totalPotongan);

                                pendapatanTetaps.put("netto_TakeHomePay",nettoTakeHomePay.toString());
                                pendapatanTetaps.put("netto_pendapatan",nettoPendapatan.toString());

                                Integer ptkp =54000000;
                                if(listDataKeluarga.get(nip)==null) {
                                    System.out.println("Tidak ada pada kumpulan data nip:"+nip);
                                }else
                                    ptkp = listDataKeluarga.get(nip);

                                //Hitungan Setahun
                                BigDecimal ptkpSetahun = new BigDecimal(ptkp.toString());
                                BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(13-Integer.parseInt(bulan)));
                                BigDecimal pkpSetahun,sisaPtkpSetahun;

                                userPajak.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());
                                userPajak.setPotongan_jabatan_A2_setahun(potonganJabatan.toString());
                                userPajak.setPotongan_jabatan_A1_setahun("0.00");
                                userPajak.setPtkp_setahun(ptkpSetahun.toString());

//                            pendapatan.put("ptkp_setahun",ptkpSetahun.toString());
                                if(nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0){
                                    pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                                    sisaPtkpSetahun = new BigDecimal("0.00");
                                    userPajak.setSisa_ptkp(sisaPtkpSetahun.toString());
                                    userPajak.setTotal_pkp(pkpSetahun.toString());
                                }else{
                                    pkpSetahun = new BigDecimal("0.00");
                                    sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                                    userPajak.setSisa_ptkp(sisaPtkpSetahun.toString());
                                    userPajak.setTotal_pkp(pkpSetahun.toString());
                                }

                                TarifPajak t = new TarifPajak();
                                t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP,true);
                                pendapatanTetaps.put("pph21",t.getListPph21());
//                            datastore.save(pendapatanTetaps);
                                arrayListPendapatan.add(pendapatanTetaps);
                                userPajak.setPendapatan_tetap(arrayListPendapatan);
                                userPajak.setPendapatan_tdk_tetap(new BasicDBList());
                                userPajak.setReminder_pajak(t.getReminderPajak().toString());
                                userPajak.setIndex_layer_pajak(t.getIndex().toString());
//                            pendapatan.put("pph21_layer",t.getIndex().toString());
//                            pendapatan.put("pph21_reminder",t.getReminderPajak().toString());
//                            pendapatan.put("update_time",new Timestamp(new Date().getTime()).toString());
//
//                            BigDecimal ptkpSebulan = ptkpSetahun.divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP);
//                            pendapatan.put("ptkp_sebulan",ptkpSebulan.toString());
//
//                            BigDecimal pkp_sebulan=new BigDecimal(0.00),sisa_ptkp_sebulan=new BigDecimal(0.00);
//                            if(nettoPendapatan.compareTo(ptkpSebulan)>=0){
//                                pkp_sebulan = nettoPendapatan.subtract(ptkpSebulan);
//                                pendapatan.put("pkp_sebulan",pkp_sebulan.toString());
//                                pendapatan.put("sisa_ptkp_sebulan","0.00");
//                            }else{
//                                sisa_ptkp_sebulan = ptkpSebulan.subtract(nettoPendapatan);
//                                pendapatan.put("pkp_sebulan","0.00");
//                                pendapatan.put("sisa_ptkp_sebulan",sisa_ptkp_sebulan.toString());
//                            }
//
//                            arrayListPendapatan.add(pendapatan);

//                            userPajak.setPendapatan_tdk_tetap(arrayListPendapatan);
//                            userPajak.setNetto_pendapatan_setahun(nettoPendapatan.multiply(new BigDecimal("12.00")).toString());


                                userPajak.setTotal_pph21_usu_dibayar(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()).toString());
                                userPajak.setTotal_pph21_usu(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()).toString());
                                userPajak.setTotal_pph21_pribadi("0.00");
                                userPajak.setTimestamp(new Timestamp(new Date().getTime()).toString());

                                datastore.save(userPajak);
                                return;
                            }else
                                System.out.println("idUser tak ada di simsdm nip:"+nip);
                        }
                    }
                });
            });
        });
    }

    private void januari2019() throws IOException {
        String response = callApi("https://api.usu.ac.id/1.0/users/","GET");
        jArray = new Gson().fromJson(response, JsonArray.class);
        getDataKeluarga();

        Workbook workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));
        workbook.forEach(sheet -> {
            sheet.forEach(row -> {
                row.forEach(cell -> {
                    if(cell.getColumnIndex() == 8 && cell.getRow().getRowNum() > 0) {
                        String nip = cell.getStringCellValue();
                        Integer idUser = isNip(nip);
                        if(idUser==0) {
                            System.out.println(row.getCell(52).getStringCellValue());
                            idUser = isNip(row.getCell(52).getStringCellValue());
                        }else{
                            System.out.println(nip);
                        }
                        if(idUser != 0){
                            UserPajak userPajak = new UserPajak();
                            userPajak.setId_user(idUser.toString());
                            userPajak.setNip_gpp(nip);
                            if(row.getCell(52) != null)
                                userPajak.setNip_simsdm(row.getCell(52).getStringCellValue());
                            else
                                userPajak.setNip_simsdm("");

                            String[] fullName = getFullName(idUser.toString());
                            userPajak.setFront_degree(fullName[0]);
                            userPajak.setFull_name(fullName[1]);
                            userPajak.setBehind_degree(fullName[2]);
                            BasicDBList arrayListPendapatan = new BasicDBList();

                            String bulan = new Integer(row.getCell(4).getStringCellValue()).toString();
                            String tahun = row.getCell(5).getStringCellValue();

                            BasicDBObject pendapatanTetaps = new BasicDBObject();
//                            PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
//                            pendapatanTetaps.set_idUser(idUser.toString());
//                            pendapatanTetaps.put("_idUser",idUser.toString());
                            pendapatanTetaps.put("activity_id","apbn");
                            pendapatanTetaps.put("activity_title", "APBN");
                            pendapatanTetaps.put("request_id","apbn");
                            pendapatanTetaps.put("salary_id","apbn");
                            pendapatanTetaps.put("type_id","apbn");
                            pendapatanTetaps.put("type_title","Gaji APBN");
                            pendapatanTetaps.put("unit_id",fullName[3]);
                            pendapatanTetaps.put("unit_name",fullName[4]);
                            pendapatanTetaps.put("bulan",bulan);
                            pendapatanTetaps.put("tahun",tahun);
                            String npwp = row.getCell(12).getStringCellValue();
                            userPajak.setNpwp(npwp);

                            BigDecimal brutoPendapatan=new BigDecimal("0.00");
                            BigDecimal nettoTakeHomePay=new BigDecimal("0.00");
                            BigDecimal nettoPendapatan=new BigDecimal("0.00");
                            for(int i=22;i<34;i++){
                                BigDecimal pendapatanSementara = new BigDecimal(row.getCell(i).getNumericCellValue());
                                pendapatanTetaps.put(sheet.getRow(0).getCell(i).getStringCellValue(),pendapatanSementara.toString());
                                brutoPendapatan= brutoPendapatan.add(pendapatanSementara);
                            }

                            pendapatanTetaps.put(sheet.getRow(0).getCell(34).getStringCellValue(),
                                    new BigDecimal(row.getCell(34).getNumericCellValue()).toString());

                            for(int i=36;i<43;i++){
                                BigDecimal potongan = new BigDecimal(row.getCell(i).getNumericCellValue());
                                pendapatanTetaps.put("bkn-"+sheet.getRow(0).getCell(i).getStringCellValue(),potongan.toString());
                            }

                            pendapatanTetaps.put("bruto_pendapatan",brutoPendapatan.toString());

                            nettoTakeHomePay = new BigDecimal(row.getCell(43).getNumericCellValue());

                            BigDecimal totalPotongan;

                            BigDecimal potonganJabatan = brutoPendapatan.multiply(BigDecimal.valueOf(persenPotJabatan));
                            if(potonganJabatan.compareTo(BigDecimal.valueOf(500000.00)) <= -1) {
                                pendapatanTetaps.put("pot_jabatan", potonganJabatan.toString());
                            }else{
                                potonganJabatan = new BigDecimal("500000.00");
                                pendapatanTetaps.put("pot_jabatan", potonganJabatan.toString());
                            }

                            BigDecimal tunjangan = new BigDecimal("0.00");
                            tunjangan = tunjangan.add(new BigDecimal(pendapatanTetaps.get("gjpokok").toString())
                                    .add(new BigDecimal(pendapatanTetaps.get("tjistri").toString())
                                            .add(new BigDecimal(pendapatanTetaps.get("tjanak").toString()))));

                            BigDecimal potonganPensiun = tunjangan.multiply(BigDecimal.valueOf(persenPotPensiun));
                            pendapatanTetaps.put("pot_pensiun",potonganPensiun.toString());

                            totalPotongan = potonganJabatan.add(potonganPensiun);

                            nettoPendapatan = brutoPendapatan.subtract(totalPotongan);

                            pendapatanTetaps.put("netto_TakeHomePay",nettoTakeHomePay.toString());
                            pendapatanTetaps.put("netto_pendapatan",nettoPendapatan.toString());

                            Integer ptkp =54000000;
                            if(listDataKeluarga.get(nip)==null) {
                                System.out.println("Tidak ada pada kumpulan data nip:"+nip);
                            }else
                                ptkp = listDataKeluarga.get(nip);

                            //Hitungan Setahun
                            BigDecimal ptkpSetahun = new BigDecimal(ptkp.toString());
                            BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(12));
                            BigDecimal pkpSetahun,sisaPtkpSetahun;

                            userPajak.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());

                            userPajak.setPotongan_jabatan_A2_setahun(potonganJabatan.toString());
                            userPajak.setPotongan_jabatan_A1_setahun("0.00");
                            userPajak.setPtkp_setahun(ptkpSetahun.toString());

//                            pendapatan.put("ptkp_setahun",ptkpSetahun.toString());
                            if(nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0){
                                pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                                sisaPtkpSetahun = new BigDecimal("0.00");
                                userPajak.setSisa_ptkp(sisaPtkpSetahun.toString());
                                userPajak.setTotal_pkp(pkpSetahun.toString());
                            }else{
                                pkpSetahun = new BigDecimal("0.00");
                                sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                                userPajak.setSisa_ptkp(sisaPtkpSetahun.toString());
                                userPajak.setTotal_pkp(pkpSetahun.toString());
                            }

                            TarifPajak t = new TarifPajak();
                            t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP,true);
                            pendapatanTetaps.put("pph21",t.getListPph21());
//                            datastore.save(pendapatanTetaps);
                            arrayListPendapatan.add(pendapatanTetaps);
                            userPajak.setPendapatan_tetap(arrayListPendapatan);
                            userPajak.setPendapatan_tdk_tetap(new BasicDBList());
                            userPajak.setReminder_pajak(t.getReminderPajak().toString());
                            userPajak.setIndex_layer_pajak(t.getIndex().toString());
//                            pendapatan.put("pph21_layer",t.getIndex().toString());
//                            pendapatan.put("pph21_reminder",t.getReminderPajak().toString());
//                            pendapatan.put("update_time",new Timestamp(new Date().getTime()).toString());
//
//                            BigDecimal ptkpSebulan = ptkpSetahun.divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP);
//                            pendapatan.put("ptkp_sebulan",ptkpSebulan.toString());
//
//                            BigDecimal pkp_sebulan=new BigDecimal(0.00),sisa_ptkp_sebulan=new BigDecimal(0.00);
//                            if(nettoPendapatan.compareTo(ptkpSebulan)>=0){
//                                pkp_sebulan = nettoPendapatan.subtract(ptkpSebulan);
//                                pendapatan.put("pkp_sebulan",pkp_sebulan.toString());
//                                pendapatan.put("sisa_ptkp_sebulan","0.00");
//                            }else{
//                                sisa_ptkp_sebulan = ptkpSebulan.subtract(nettoPendapatan);
//                                pendapatan.put("pkp_sebulan","0.00");
//                                pendapatan.put("sisa_ptkp_sebulan",sisa_ptkp_sebulan.toString());
//                            }
//
//                            arrayListPendapatan.add(pendapatan);

//                            userPajak.setPendapatan_tdk_tetap(arrayListPendapatan);
//                            userPajak.setNetto_pendapatan_setahun(nettoPendapatan.multiply(new BigDecimal("12.00")).toString());


                            userPajak.setTotal_pph21_usu_dibayar(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()).toString());
                            userPajak.setTotal_pph21_usu(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()).toString());
                            userPajak.setTotal_pph21_pribadi("0.00");
                            userPajak.setTimestamp(new Timestamp(new Date().getTime()).toString());

                            datastore.save(userPajak);
                            return;
                        }else
                            System.out.println("idUser tak ada di simsdm nip:"+nip);
                    }
                });
            });
        });
    }

    private int isNip(String nip){
        AtomicReference<Integer> result = new AtomicReference<>(0);
        jArray.forEach(jObj -> {
            if(jObj.getAsJsonObject().get("nip").getAsString().equalsIgnoreCase(nip)){
                result.set(Integer.parseInt(jObj.getAsJsonObject().get("id").getAsString()));
                return;
            }
        });
        return result.get();
    }

    private String[] getFullName(String idUser){
        AtomicReference<String[]> result = new AtomicReference<>();
        jArray.forEach(jObj -> {
            if(jObj.getAsJsonObject().get("id").getAsString().equalsIgnoreCase(idUser)){
                result.set(new String[]{jObj.getAsJsonObject().get("front_degree").getAsString(),jObj.getAsJsonObject().get("full_name").getAsString(),
                        jObj.getAsJsonObject().get("behind_degree").getAsString(),
                        jObj.getAsJsonObject().get("work_unit_id").getAsString(),
                        jObj.getAsJsonObject().get("work_unit").getAsString(),
                        jObj.getAsJsonObject().get("npwp").getAsString().replaceAll("\\D","")});
                return;
            }
        });
        return result.get();
    }

    private void getDataKeluarga()throws IOException{
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

    private String callApi(String ep, String method) throws IOException {
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
