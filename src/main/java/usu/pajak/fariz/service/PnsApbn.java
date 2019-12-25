package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mongodb.BasicDBObject;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.apache.poi.ss.usermodel.*;
import usu.pajak.fariz.model.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PnsApbn {
    private Datastore datastore;
    private static final String[] listFiles = new String[]{
            "D:/GajiAPBN/kumpulan_pns_januari.xls",
            "D:/GajiAPBN/fwdgaji_februari_2019_usu/kumpulan_gaji_pns_feb.xls",
            "D:/GajiAPBN/gaji_bulan_maret_2019_pns_usu/kumpulan_gaji_maret_pns.xls",
            "D:/GajiAPBN/gaji_bulan_april_2019_pns_usu/kumpulan_gaji_bulan_april.xls",
            "D:/GajiAPBN/gaji_bulan_mei_2019_pns_usu/kumpulan_gaji_mei.xls",
            "D:/GajiAPBN/gaji_juni_2019_pns_usu/kumpulan_gaji_juni.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_juli_2019/kumpulan_gaji_juli.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_agustus_2019/kumpulan_gaji_agustus.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_september2019/kumpulan_gaji_september_2019.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_oktober2019/kumpulan_gaji_oktober_2019.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_nopember_2019/kumpulan_gaji_nopember_2019.xls",
            "D:/GajiAPBN/gaji_pns_usu_bulan_desember_2019/kumpulan_gaji_desember_2019.xls"
    };
    private static final String dataPtkp = "D:/Daftar PTKP-nama-unit-rev1.xls";
    private DecimalFormat dec = new DecimalFormat("#.000");
    private JsonArray jArray;

    private LinkedHashMap<String,Integer> listDataKeluarga = new LinkedHashMap<>();
    private Query<UserPajak> query;

    public static void main(String[] args) throws IOException {
        new PnsApbn();
    }

    public PnsApbn() throws IOException {
        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"revisi_pajak");
        jArray = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/1.0/users/","GET"), JsonArray.class);
        getDataKeluarga();
        for(int i =1; i<=12; i++)
            calculateTax(i);
    }

    private void calculateTax(Integer bulan) throws IOException{
        Workbook workbook = WorkbookFactory.create(new File(listFiles[bulan-1]));
        workbook.forEach(sheet -> {
            sheet.forEach(row -> {
                row.forEach(cell -> {
                    if(cell.getColumnIndex() == 8 && cell.getRow().getRowNum() > 0) {
                        String nip = cell.getStringCellValue();
                        query = datastore.createQuery(UserPajak.class)
                                .disableValidation();
                        query.or(query.criteria("nip_gpp").contains(nip),query.criteria("nip_simsdm").contains(nip));
                        UserPajak userPajak = query.first();
                        if(userPajak!=null){
                            String[] fullName = getFullName(userPajak.getId_user());

                            PendapatanTetaps pendapatanTetaps = getPendapatanTetaps(userPajak,row,fullName);

                            BasicDBObject apbnPayment = getApbnPayment(sheet,row, pendapatanTetaps);
                            pendapatanTetaps.setApbn_payment(apbnPayment);

                            updateTotalPendapatanSetahun(userPajak,pendapatanTetaps);

                            Query<PendapatanTetaps> qPt = datastore.createQuery(PendapatanTetaps.class).disableValidation()
                                    .filter("id_user", userPajak.getId_user());
                            List<PendapatanTetaps> pendapatanTetapsList = qPt.find().toList();
                            PendapatanTetaps pBefore = pendapatanTetapsList.get(pendapatanTetapsList.size()-1);

                            BigDecimal nettoPendapatan;

                            /*if(pendapatanTetaps.getPajak().getNetto_pendapatan().compareTo(pBefore.getPajak().getNetto_pendapatan())>0){ // jika pendapatan menaik
                                nettoPendapatan = pendapatanTetaps.getPajak().getNetto_pendapatan().subtract(pBefore.getPajak().getNetto_pendapatan());
                                BigDecimal pkp = nettoPendapatan.multiply(BigDecimal.valueOf(13-bulan));
                                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().add(pkp));
                                if(userPajak.getTotal_pendapatan().getTotal_pkp().compareTo(BigDecimal.ZERO)>0){
                                    userPajak.getTotal_pendapatan().setTotal_pkp(userPajak.getTotal_pendapatan().getTotal_pkp().add(nettoPendapatan));
                                }else{
                                    if(userPajak.getTotal_pendapatan().getSisa_ptkp().compareTo(pkp)>=0){ // jika masih ada sisa ptkp
                                        userPajak.getTotal_pendapatan().setSisa_ptkp(userPajak.getTotal_pendapatan().getSisa_ptkp().subtract(pkp));
                                        pkp = BigDecimal.ZERO;
                                    }else{ // jika habis
                                        pkp = pkp.subtract(userPajak.getTotal_pendapatan().getSisa_ptkp());
                                        userPajak.getTotal_pendapatan().setSisa_ptkp(BigDecimal.ZERO);
                                        userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
                                    }
                                }

                                calculatePajak(userPajak,pendapatanTetaps,pkp,userPajak.getSetting_pajak().getReminder(),userPajak.getSetting_pajak().getIndex());

                            }else if(pendapatanTetaps.getPajak().getNetto_pendapatan().compareTo(pBefore.getPajak().getNetto_pendapatan())==0){ //pendapatan sama
                                pendapatanTetaps.getPajak().setPph21(pBefore.getPajak().getPph21());
                                pendapatanTetaps.getPajak().set_recordCalTax(pBefore.getPajak().get_recordCalTax());
                            }else{// jika pendapatan kurang dari sebelumnya

                            }*/
                            nettoPendapatan = pendapatanTetaps.getPajak().getNetto_pendapatan();
                            BigDecimal pkp;
                            userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().add(nettoPendapatan));
                            if(userPajak.getTotal_pendapatan().getTotal_pkp().compareTo(BigDecimal.ZERO)>0){
                                userPajak.getTotal_pendapatan().setTotal_pkp(userPajak.getTotal_pendapatan().getTotal_pkp().add(nettoPendapatan));
                                pkp = nettoPendapatan;
                            }else{
                                if(userPajak.getTotal_pendapatan().getSisa_ptkp().compareTo(nettoPendapatan)>=0){ // jika masih ada sisa ptkp
                                    userPajak.getTotal_pendapatan().setSisa_ptkp(userPajak.getTotal_pendapatan().getSisa_ptkp().subtract(nettoPendapatan));
                                    pkp = BigDecimal.ZERO;
                                }else{ // jika habis
                                    pkp = nettoPendapatan.subtract(userPajak.getTotal_pendapatan().getSisa_ptkp());
                                    userPajak.getTotal_pendapatan().setSisa_ptkp(BigDecimal.ZERO);
                                    userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
                                }
                            }

                            calculatePajak(userPajak,pendapatanTetaps,pkp,userPajak.getSetting_pajak().getReminder(),userPajak.getSetting_pajak().getIndex());

                            if(userPajak.getPph21().getPns() != null)
                                userPajak.getPph21().setPns(userPajak.getPph21().getPns().add(BigDecimal.valueOf(row.getCell(34).getNumericCellValue())));
                            else
                                userPajak.getPph21().setPns(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()));

                            datastore.save(userPajak);
                            datastore.save(pendapatanTetaps);
                        }else{ // jika belum ada user nya masuk ke sini
                            createNewUserPajak(sheet,row,cell,bulan);
                        }
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

    private PendapatanTetaps getPendapatanTetaps(UserPajak userPajak, Row row, String[] fullName){
        PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
        Integer bulan = new Integer(row.getCell(4).getStringCellValue());
        Integer tahun = Integer.parseInt(row.getCell(5).getStringCellValue());
        pendapatanTetaps.setId_user(userPajak.getId_user());
        pendapatanTetaps.setSalary_id(999999999);
        Unit unit = new Unit();
        unit.setId(Integer.parseInt(fullName[3]));
        unit.setName(fullName[4]);
        pendapatanTetaps.setUnit(unit);
        pendapatanTetaps.setMonth(bulan);
        pendapatanTetaps.setYear(tahun);

        return pendapatanTetaps;
    }

    private BasicDBObject getApbnPayment(Sheet sheet, Row row, PendapatanTetaps pendapatanTetaps){
        BasicDBObject apbnPayment = new BasicDBObject();
        BigDecimal brutoPendapatan=new BigDecimal("0.00");
        for(int i=22;i<34;i++){
            BigDecimal pendapatanSementara = new BigDecimal(row.getCell(i).getNumericCellValue());
            apbnPayment.put(sheet.getRow(0).getCell(i).getStringCellValue(),pendapatanSementara);
            brutoPendapatan= brutoPendapatan.add(pendapatanSementara);
        }

        apbnPayment.put(sheet.getRow(0).getCell(34).getStringCellValue(),
                new BigDecimal(row.getCell(34).getNumericCellValue()));// PPH 21 yang digenerate oleh sistem Penggajian Pemerintah

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
        apbnPayment.put("pot_pensiun",potonganPensiun);
        pajak.setJaminan_pensiun_ht(potonganPensiun);

        totalPotongan = biayaJabatan.add(potonganPensiun);

        BigDecimal nettoPendapatan = brutoPendapatan.subtract(totalPotongan);
        pajak.setNetto_pendapatan(nettoPendapatan);
        pendapatanTetaps.setPajak(pajak);

        return apbnPayment;
    }

    private void updateTotalPendapatanSetahun(UserPajak userPajak, PendapatanTetaps pendapatanTetaps){
        if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun() == null){
            userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(pendapatanTetaps.getPajak().getBiaya_jabatan());
        }else{
            userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(
                    userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().add(pendapatanTetaps.getPajak().getBiaya_jabatan())
            );
        }

        if(userPajak.getTotal_pendapatan().getJaminan_pensiun_ht_setahun() == null){
            userPajak.getTotal_pendapatan().setJaminan_pensiun_ht_setahun(pendapatanTetaps.getPajak().getJaminan_pensiun_ht());
        }else{
            userPajak.getTotal_pendapatan().setJaminan_pensiun_ht_setahun(
                    userPajak.getTotal_pendapatan().getJaminan_pensiun_ht_setahun().add(pendapatanTetaps.getPajak().getJaminan_pensiun_ht())
            );
        }
    }

    private void calculatePajak(UserPajak userPajak, PendapatanTetaps pendapatanTetaps, BigDecimal pkp, BigDecimal reminder, Integer index){
        TarifPajak t = new TarifPajak();
        t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP,true);
        pendapatanTetaps.getPajak().setPph21(t.getListPph21());
        userPajak.getSetting_pajak().setReminder(t.getReminderPajak());
        userPajak.getSetting_pajak().setIndex(t.getIndex());
        UserPajakTax upt = new UserPajakTax();
        upt.setIndex(t.getIndex());
        upt.setReminder(t.getReminderPajak());
        pendapatanTetaps.getPajak().set_recordCalTax(upt);
        pendapatanTetaps.setStatus(true);
    }

    private void createNewUserPajak(Sheet sheet, Row row, Cell cell, Integer bulan){
        String nip = cell.getStringCellValue();
        Integer idUser = isNip(nip);
        if(idUser == 0){
            System.out.println(row.getCell(8).getStringCellValue());
            System.out.println(row.getCell(9).getStringCellValue());
            idUser = isNip(row.getCell(52).getStringCellValue());
        }else System.out.println(nip);
        if(idUser != 0){
            String[] fullName = getFullName(idUser.toString());
            UserPajak userPajak = getUserPajak(idUser.toString(),nip,row,fullName);

            PendapatanTetaps pendapatanTetaps = getPendapatanTetaps(userPajak,row,fullName);

            BasicDBObject apbnPayment = getApbnPayment(sheet,row, pendapatanTetaps);
            pendapatanTetaps.setApbn_payment(apbnPayment);
            Integer ptkp =54000000;
            if(listDataKeluarga.get(nip)==null) {
                System.out.println("Tidak ada pada kumpulan data nip:"+nip);
                ptkp = Ptkp.getInstance.getPtkp(idUser.toString());
                System.out.println(ptkp);
            }else
                ptkp = listDataKeluarga.get(nip);

            BigDecimal ptkpSetahun = new BigDecimal(ptkp.toString());
//            BigDecimal nettoPendapatanSetahun = pendapatanTetaps.getPajak().getNetto_pendapatan().multiply(BigDecimal.valueOf(13-bulan));
            BigDecimal nettoPendapatanSetahun = pendapatanTetaps.getPajak().getNetto_pendapatan();
            BigDecimal pkpSetahun,sisaPtkpSetahun;

            userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

            if(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun() == null){
                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(nettoPendapatanSetahun);
            }else{
                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(
                        userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().add(nettoPendapatanSetahun)
                );
            }

            updateTotalPendapatanSetahun(userPajak,pendapatanTetaps);

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

            calculatePajak(userPajak,pendapatanTetaps,pkpSetahun,BigDecimal.valueOf(50000000),0);

            userPajak.getPph21().setPns(BigDecimal.valueOf(row.getCell(34).getNumericCellValue()));

            datastore.save(userPajak);
            datastore.save(pendapatanTetaps);
        }else{
            System.out.println("idUser tak ada di simsdm nip:"+nip);
        }
    }
}
