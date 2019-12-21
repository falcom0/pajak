package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import usu.pajak.fariz.model.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PnsApbn {
    private Datastore datastore;
    private static final String[] listFiles = new String[]{
            "D:\\GajiAPBN\\kumpulan_pns_januari.xls",
            "D:\\GajiAPBN\\fwdgaji_februari_2019_usu\\kumpulan_gaji_pns_feb.xls",
            "D:/GajiAPBN/gaji_bulan_maret_2019_pns_usu/kumpulan_gaji_maret_pns.xls",
            "D:/GajiAPBN/gaji_bulan_april_2019_pns_usu/kumpulan_gaji_bulan_april.xls",
            "D:/GajiAPBN/gaji_bulan_mei_2019_pns_usu/kumpulan_gaji_mei.xls",
            "D:/GajiAPBN/gaji_juni_2019_pns_usu/kumpulan_gaji_juni.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_juli_2019/kumpulan_gaji_juli.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_agustus_2019/kumpulan_gaji_agustus.xls"
    };
    private static final String dataPtkp = "D:\\Daftar PTKP-nama-unit-rev1.xls";
    private DecimalFormat dec = new DecimalFormat("#.000");
    private JsonArray jArray;

    private LinkedHashMap<String,Integer> listDataKeluarga = new LinkedHashMap<>();
    private Query<UserPajak> query;

    public PnsApbn() throws IOException {
        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"revisi_pajak");
        jArray = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/1.0/users/","GET"), JsonArray.class);
        getDataKeluarga();
    }

    private void jan() throws IOException{
        Workbook workbook = WorkbookFactory.create(new File(listFiles[0]));
        workbook.forEach(sheet -> {
            sheet.forEach(row -> {
                row.forEach(cell -> {
                    if(cell.getColumnIndex() == 8 && cell.getRow().getRowNum() > 0){
                        String nip = cell.getStringCellValue();
                        Integer idUser = isNip(nip);
                        if(idUser == 0){
                            System.out.println(row.getCell(52).getStringCellValue());
                            idUser = isNip(row.getCell(52).getStringCellValue());
                        }else System.out.println(nip);
                        if(idUser != 0){
                            /**
                             * TODO
                             * Get UserPajak from database if null
                             * else create
                             */
                            String[] fullName = getFullName(idUser.toString());
                            UserPajak userPajak = getUserPajak(idUser.toString(),nip,row,fullName);

                            PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
                            Integer bulan = new Integer(row.getCell(4).getStringCellValue());
                            Integer tahun = Integer.parseInt(row.getCell(5).getStringCellValue());
                            pendapatanTetaps.setId_user(idUser.toString());
                            pendapatanTetaps.setSalary_id(999999999);
                            Unit unit = new Unit();
                            unit.setId(Integer.parseInt(fullName[3]));
                            unit.setName(fullName[4]);
                            pendapatanTetaps.setUnit(unit);
                            pendapatanTetaps.setMonth(bulan);
                            pendapatanTetaps.setYear(tahun);

                            BigDecimal brutoPendapatan=new BigDecimal("0.00");
                            BasicDBObject apbnPayment = new BasicDBObject();
                            for(int i=22;i<34;i++){
                                BigDecimal pendapatanSementara = new BigDecimal(row.getCell(i).getNumericCellValue());
                                apbnPayment.put(sheet.getRow(0).getCell(i).getStringCellValue(),pendapatanSementara);
                                brutoPendapatan= brutoPendapatan.add(pendapatanSementara);
                            }

                            apbnPayment.put(sheet.getRow(0).getCell(34).getStringCellValue(),
                                    new BigDecimal(row.getCell(34).getNumericCellValue()));

                            for(int i=36;i<43;i++){
                                BigDecimal potongan = new BigDecimal(row.getCell(i).getNumericCellValue());
                                apbnPayment.put("bkn-"+sheet.getRow(0).getCell(i).getStringCellValue(),potongan);
                            }

                            Pajak pajak = new Pajak();
                            pajak.setBruto_pendapatan(brutoPendapatan);

                            BigDecimal nettoTakeHomePay = new BigDecimal(row.getCell(43).getNumericCellValue());
                            pajak.setNetto_take_homepay(nettoTakeHomePay);
                            BigDecimal totalPotongan;

                            BigDecimal biayaJabatan = brutoPendapatan.multiply(StaticValue.persenBiayaJabatan);
                            if(biayaJabatan.compareTo(BigDecimal.valueOf(500000.00)) <= -1) {
                                apbnPayment.put("pot_jabatan", biayaJabatan);
                            }else{
                                biayaJabatan = new BigDecimal("500000.00");
                                apbnPayment.put("pot_jabatan", biayaJabatan);
                            }
                            pajak.setBiaya_jabatan(biayaJabatan);

                            BigDecimal tunjangan = new BigDecimal("0.00");
                            tunjangan = tunjangan.add(new BigDecimal(apbnPayment.get("gjpokok").toString())
                                    .add(new BigDecimal(apbnPayment.get("tjistri").toString())
                                            .add(new BigDecimal(apbnPayment.get("tjanak").toString()))));

                            BigDecimal potonganPensiun = tunjangan.multiply(StaticValue.persenPotPensiun);
                            apbnPayment.put("pot_pensiun",potonganPensiun.toString());
                            pajak.setJaminan_pensiun_ht(potonganPensiun);

                            totalPotongan = biayaJabatan.add(potonganPensiun);

                            BigDecimal nettoPendapatan = brutoPendapatan.subtract(totalPotongan);
                            pajak.setNetto_pendapatan(nettoPendapatan);

                            pendapatanTetaps.setApbn_payment(apbnPayment);

                            Integer ptkp =54000000;
                            if(listDataKeluarga.get(nip)==null) {
                                System.out.println("Tidak ada pada kumpulan data nip:"+nip);
                            }else
                                ptkp = listDataKeluarga.get(nip);

                            BigDecimal ptkpSetahun = new BigDecimal(ptkp.toString());
                            BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(12));
                            BigDecimal pkpSetahun,sisaPtkpSetahun;

                            userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

                            if(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun() == null){
                                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(nettoPendapatanSetahun);
                            }else{
                                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(
                                        userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().add(nettoPendapatanSetahun)
                                );
                            }

                            if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun() == null){
                                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(biayaJabatan);
                            }else{
                                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(
                                        userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().add(biayaJabatan)
                                );
                            }

                            if(userPajak.getTotal_pendapatan().getJaminan_pensiun_ht_setahun() == null){
                                userPajak.getTotal_pendapatan().setJaminan_pensiun_ht_setahun(potonganPensiun);
                            }else{
                                userPajak.getTotal_pendapatan().setJaminan_pensiun_ht_setahun(
                                        userPajak.getTotal_pendapatan().getJaminan_pensiun_ht_setahun().add(potonganPensiun)
                                );
                            }

                            if(nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0){
                                pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                                sisaPtkpSetahun = new BigDecimal("0.00");
                                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
                                userPajak.getTotal_pendapatan().setTotal_pkp(pkpSetahun);
                            }else{
                                pkpSetahun = new BigDecimal("0.00");
                                sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
                                userPajak.getTotal_pendapatan().setTotal_pkp(pkpSetahun);
                            }

                            TarifPajak t = new TarifPajak();
                            t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0, usu.pajak.model.TarifPajak.LAYER_SETAHUN, usu.pajak.model.TarifPajak.TARIF_NPWP,true);
                            pajak.setPph21(t.getListPph21());
                            userPajak.getSetting_pajak().setReminder(t.getReminderPajak());
                            userPajak.getSetting_pajak().setIndex(t.getIndex());
                            UserPajakTax upt = new UserPajakTax();
                            upt.setIndex(t.getIndex());
                            upt.setReminder(t.getReminderPajak());
                            pajak.set_recordCalTax(upt);
                            pendapatanTetaps.setPajak(pajak);
                            pendapatanTetaps.setStatus(true);

                            userPajak.getPph21().setPns(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()));

                            datastore.save(userPajak);
                            datastore.save(pendapatanTetaps);
                        }else System.out.println("idUser tak ada di simsdm nip:"+nip);
                    }
                });
            });
        });
    }

    private UserPajak getUserPajak(String idUser, String nip, Row row, String[] fullName){
        Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user", idUser);
        UserPajak userPajak = query.first();
        if(userPajak!=null) { // update data
//            if(userPajak.getNpwp_simsdm()==null) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
//            else if (userPajak.getNpwp_simsdm().isEmpty()) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
        }else{//insert data
            userPajak = new UserPajak();
            userPajak.setId_user(idUser);
            userPajak.setNip_gpp(nip);
            String npwp = row.getCell(12).getStringCellValue();
            userPajak.setNpwp(npwp);
            userPajak.setNpwp_simsdm(fullName[5]);
            if(row.getCell(52) != null)
                userPajak.setNip_simsdm(row.getCell(52).getStringCellValue());
            else
                userPajak.setNip_simsdm("");
            userPajak.setFront_degree(fullName[0]);
            userPajak.setFull_name(fullName[1]);
            userPajak.setBehind_degree(fullName[2]);

            Group group = new Group();
            group.setId(Integer.parseInt(fullName[6]));
            group.setTitle(fullName[7]);
            userPajak.setGroup(group);
            userPajak.setTotal_pendapatan(new UserPajakPendapatan());
            userPajak.setSetting_pajak(new UserPajakTax());
            userPajak.setPph21(new UserPajakPPH());
        }
        return userPajak;
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
                result.set(new String[]{
                        jObj.getAsJsonObject().get("front_degree").getAsString(),
                        jObj.getAsJsonObject().get("full_name").getAsString(),
                        jObj.getAsJsonObject().get("behind_degree").getAsString(),
                        jObj.getAsJsonObject().get("work_unit_id").getAsString(),
                        jObj.getAsJsonObject().get("work_unit").getAsString(),
                        jObj.getAsJsonObject().get("npwp").getAsString().replaceAll("\\D",""),
                        jObj.getAsJsonObject().get("type").getAsString(),
                        jObj.getAsJsonObject().get("type_str").getAsString(),
                });
                return;
            }
        });
        return result.get();
    }

    private void getDataKeluarga()throws IOException{
        Workbook workbook = WorkbookFactory.create(new File(dataPtkp));
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
}
