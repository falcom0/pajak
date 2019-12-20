package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import usu.pajak.fariz.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Tax {
    private Datastore datastore;

    public static void main(String[] args) throws IOException {
        Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&request_id=13763","GET"),Salary.class); //13763
        new Tax(salary);
    }

    public Tax(Salary salary){
        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"revisi_pajak");
        filterTax(salary);

    }

    public void filterTax(Salary salary){
        if(salary != null){
            if(salary.getResponse().getSalary_receivers().stream().anyMatch(Objects::nonNull)){
                List<SalaryDetail> allData = salary.getResponse().getSalary_receivers();

                //Filter berdasarkan Luar / Dalam
                //1. Luar : Pisahkan : a. MWA b. Tenaga Ahli c. Dosen Luar Negri
                List<SalaryDetail> listLuar = allData.stream().filter( c ->
                    c.getUser().getId() == null
                ).collect(Collectors.toList());
                List<SalaryDetail> listMwa = listLuar.stream().filter( r -> r.getUnit().getId() == 1 || r.getUnit().getId() == 171).collect(Collectors.toList());
                List<SalaryDetail> listTA = listLuar.stream().filter(r-> r.getUnit().getId() != 1 || r.getUnit().getId() != 171).collect(Collectors.toList());

                //2. Dalam : Pisahkan : a. Gaji Tetap b. Honor
                List<SalaryDetail> listDalam = allData.stream().filter(c -> c.getUser().getId() != null).collect(Collectors.toList());
                List<SalaryDetail> gaji = listDalam.stream()
                        .filter(c ->
                                c.getPayment().getAsJsonObject().has("basic_salary") ||
                                c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
                        .collect(Collectors.toList());
//                List<SalaryDetail> honorGaji = allData.stream()
//                        .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
//                        .filter(c -> (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
//                                && c.getPayment().getAsJsonObject().has("p1"))
//                        .collect(Collectors.toList());
                List<SalaryDetail> honor = listDalam.stream()
                        .filter(c -> !(c.getPayment().getAsJsonObject().has("basic_salary")))
                        .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
                                && c.getPayment().getAsJsonObject().has("p1")))
                        .collect(Collectors.toList());

                List<SalaryDetail> honorBknJasmed = honor.stream()
                        .filter(c -> !((c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 49) ||
                                (c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 50))
                        )
                        .collect(Collectors.toList());

                List<SalaryDetail> honorJasmed = honor.stream()
                        .filter(c -> (c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 49) ||
                                (c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 50)
                        )
                        .collect(Collectors.toList());


                if(listMwa.size() > 0){

                }

                if(listTA.size() > 0){

                }

                if(gaji.size() > 0){
                    calculateTaxSalary(gaji);
                }

                if(honorBknJasmed.size() > 0){
                    calculateTaxHonor(honorBknJasmed);
                }

                if(honorJasmed.size() > 0){
                    calculateTaxJasmed(honorJasmed);
                }
            }else{ }
        }else{ }
    }

    public void pnsSalary(){

    }

    private void calculateTaxSalary(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTetaps> qPt = datastore.createQuery(PendapatanTetaps.class)
                    .disableValidation()
                    .filter("id_user", sd.getUser().getId().toString());
            if(qPt.count()==0){ //not exist (insert new data)
                PendapatanTetaps pendapatanTetaps = initializePendapatanTetap(sd, userPajak);

                BigDecimal totalPendapatanSementara = getPendapatanSementara(sd);

                Pajak pajak = new Pajak();
                pajak.setTotal_pendapatan_rka(totalPendapatanSementara);
                pajak.setJkk(StaticValue.jkk);
                pajak.setJkm(StaticValue.jkm);
                pajak.setBpjs_kesehatan(StaticValue.bpjs_kesehatan);

                totalPendapatanSementara = totalPendapatanSementara.add(StaticValue.jkk).add(StaticValue.jkm).add(StaticValue.bpjs_kesehatan);
                pajak.setBruto_pendapatan(totalPendapatanSementara);

                BigDecimal biayaJabatan = getBiayaJabatan(totalPendapatanSementara,userPajak,pajak);

                BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(biayaJabatan);
                pajak.setNetto_pendapatan(nettoPendapatan);

                BigDecimal pkpSetahun = new BigDecimal(0.00), pkp = new BigDecimal(0.00);
                BigDecimal ptkpSetahun = new BigDecimal(Ptkp.getInstance.getPtkp(sd.getUser().getId().toString()));

                if(userPajak.getTotal_pendapatan().getPtkp_setahun() == null)
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

                Integer month = Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]);
                pendapatanTetaps.setMonth(month);
                pendapatanTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));
                BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(13-month));

                pkpSetahun = calculateSisaPtkp(userPajak,nettoPendapatanSetahun,pkpSetahun,ptkpSetahun);

                TarifPajak t = resultTax(userPajak,pkpSetahun,true);

                BasicDBList listPph21 = t.getListPph21();
                BigDecimal total_pph21_sementara = sumPPH21(listPph21);

                pajak.setNetto_take_homepay(totalPendapatanSementara.subtract(total_pph21_sementara));
                pajak.setPph21(listPph21);
                userPajak.getSetting_pajak().setReminder(t.getReminderPajak());
                userPajak.getSetting_pajak().setIndex(t.getIndex());
                pendapatanTetaps.setPajak(pajak);

                if(userPajak.getPph21().getUsu()==null) {
                    userPajak.getPph21().setUsu(total_pph21_sementara);
                }else {
                    userPajak.getPph21().setUsu(userPajak.getPph21().getUsu().add(total_pph21_sementara));
                }

                datastore.save(userPajak);
                datastore.save(pendapatanTetaps);

            }else{ //already has data pendapatan tetap
                Query<PendapatanTetaps> qPt2 = datastore.createQuery(PendapatanTetaps.class)
                        .disableValidation()
                       . filter("id_user", sd.getUser().getId())
                        .filter("salary_id", sd.getId());
                if(qPt2.count()==0){ //salary not duplicate
                    List<PendapatanTetaps> listPendapatanTetap = qPt.find().toList();
                    PendapatanTetaps pBefore = listPendapatanTetap.get(listPendapatanTetap.size()-1);
                    BigDecimal tBefore = pBefore.getPajak().getTotal_pendapatan_rka();
                    BigDecimal tNow = getPendapatanSementara(sd);
                    if(tBefore.compareTo(tNow)==0){ // just input not updating netto setahun
                        PendapatanTetaps pendapatanTetaps = initializePendapatanTetap(sd, userPajak);

                        Pajak pajak = pBefore.getPajak();
                        tNow = tNow.add(StaticValue.jkk).add(StaticValue.jkm).add(StaticValue.bpjs_kesehatan);
                        BigDecimal biayaJabatan = getBiayaJabatan(tNow,userPajak,pajak);
                        BigDecimal nettoPendapatan = tNow.subtract(biayaJabatan);
                        pajak.setNetto_pendapatan(nettoPendapatan);

                        pendapatanTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
                        pendapatanTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));

                        pendapatanTetaps.setPajak(pajak);
                        pendapatanTetaps.setStatus(true);
                        userPajak.getPph21().setUsu(userPajak.getPph21().getUsu().add(sumPPH21(pajak.getPph21())));

                        datastore.save(userPajak);
                        datastore.save(pendapatanTetaps);
                    }else{ // there is some difference
                        //jika lebih dari sebelumya hanya hitung yg berlebihnya saja
                        //jika kurang dari sebelumnya ga tau mau diapain
                    }
                }else{ //salary sudah ada (salary duplicate)
                    // kirim hasil pph 21 ke RKA
                }
            }
        });
    }

    private void calculateTaxHonor(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation()
                    .filter("id_user", sd.getUser().getId().toString());
//            if(qPt==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);
                BigDecimal totalPendapatanSementara = getPendapatanSementara(sd);
                Pajak pajak = new Pajak();
                pajak.setTotal_pendapatan_rka(totalPendapatanSementara);
                pajak.setBruto_pendapatan(totalPendapatanSementara);

                BigDecimal biayaJabatan = getBiayaJabatan(totalPendapatanSementara,userPajak,pajak);

                BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(biayaJabatan);
                pajak.setNetto_pendapatan(nettoPendapatan);

                BigDecimal ptkpSetahun = new BigDecimal(Ptkp.getInstance.getPtkp(sd.getUser().getId().toString()));

                BigDecimal pkp = new BigDecimal(0.00);

                if(userPajak.getTotal_pendapatan().getPtkp_setahun() == null)
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

                pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
                pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));

                pkp = calculateSisaPtkp(userPajak,nettoPendapatan,pkp,ptkpSetahun);

                TarifPajak t = resultTax(userPajak, pkp, false);

//                BasicDBList listPph21 = t.getListPph21();
                BigDecimal total_pph21_sementara = sumPPH21(t.getListPph21());

                pajak.setNetto_take_homepay(totalPendapatanSementara.subtract(total_pph21_sementara));
                pajak.setPph21(t.getListPph21());
                UserPajakTax upt = new UserPajakTax();
                upt.setIndex(t.getIndex());
                upt.setReminder(t.getReminderPajak());
                pajak.set_recordCalTax(upt);
                userPajak.getSetting_pajak().setReminder(t.getReminderPajak());
                userPajak.getSetting_pajak().setIndex(t.getIndex());
                pendapatanTdkTetaps.setPajak(pajak);
                pendapatanTdkTetaps.setStatus(true);

                if(userPajak.getPph21().getUsu()==null) {
                    userPajak.getPph21().setUsu(total_pph21_sementara);
                }else {
                    userPajak.getPph21().setUsu(userPajak.getPph21().getUsu().add(total_pph21_sementara));
                }

                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
//            }else{
//
//            }
        });
    }

    private void calculateTaxJasmed(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            UserPajak userPajak = getUserPajak(sd);

            PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);

            BigDecimal brutoPendapatan = getPendapatanSementara(sd);

            Pajak pajak = new Pajak();
            pajak.setTotal_pendapatan_rka(brutoPendapatan);
            pajak.setBruto_pendapatan(brutoPendapatan);
//            pajak.setNetto_pendapatan(brutoPendapatan);

            BigDecimal ptkpSetahun = new BigDecimal(Ptkp.getInstance.getPtkp(sd.getUser().getId().toString()));
            if(userPajak.getTotal_pendapatan().getPtkp_setahun() == null)
                userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

            pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
            pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));

            if(userPajak.getTotal_pendapatan().getBruto_jasa_setahun() == null)
                userPajak.getTotal_pendapatan().setBruto_jasa_setahun(brutoPendapatan);
            else
                userPajak.getTotal_pendapatan().setBruto_jasa_setahun(
                        userPajak.getTotal_pendapatan().getBruto_jasa_setahun().add(brutoPendapatan)
                );

            BigDecimal pkp = brutoPendapatan.multiply(new BigDecimal(0.5));
            if(userPajak.getTotal_pendapatan().getTotal_pkp_jasa() == null)
                userPajak.getTotal_pendapatan().setTotal_pkp_jasa(pkp);
            else
                userPajak.getTotal_pendapatan().setTotal_pkp_jasa(
                        userPajak.getTotal_pendapatan().getTotal_pkp_jasa().add(pkp)
                );

            TarifPajak t = resultTaxJasa(userPajak, pkp);
            BigDecimal total_pph21_sementara = sumPPH21(t.getListPph21());

            pajak.setNetto_take_homepay(brutoPendapatan.subtract(total_pph21_sementara));
            pajak.setPph21(t.getListPph21());
            UserPajakTax upt = new UserPajakTax();
            upt.setIndex_jasmed(t.getIndex());
            upt.setReminder_jasmed(t.getReminderPajak());
            pajak.set_recordCalTax(upt);
            userPajak.getSetting_pajak().setReminder_jasmed(t.getReminderPajak());
            userPajak.getSetting_pajak().setIndex_jasmed(t.getIndex());
            pendapatanTdkTetaps.setPajak(pajak);
            pendapatanTdkTetaps.setStatus(true);

            if(userPajak.getPph21().getJasa()==null) {
                userPajak.getPph21().setJasa(total_pph21_sementara);
            }else {
                userPajak.getPph21().setJasa(userPajak.getPph21().getUsu().add(total_pph21_sementara));
            }

            datastore.save(userPajak);
            datastore.save(pendapatanTdkTetaps);
        });
    }

    private TarifPajak resultTax(UserPajak userPajak,BigDecimal pkp, boolean rutin){
        TarifPajak t = new TarifPajak();
        BigDecimal reminder;
        Integer index;
        if(userPajak.getPph21().getUsu()==null){
            reminder = new BigDecimal("50000000.00");
            index = 0;
        }else{
            reminder = userPajak.getSetting_pajak().getReminder();
            index = userPajak.getSetting_pajak().getIndex();
        }

        if(userPajak.getNpwp_simsdm()==null||userPajak.getNpwp_simsdm().equalsIgnoreCase("")){
            if(userPajak.getNpwp()==null || userPajak.getNpwp().equalsIgnoreCase(""))
                t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, rutin);
            else
                t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, rutin);
        }else{
            t.hitungPajak(reminder,pkp,index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, rutin);
        }

        return t;
    }

    private TarifPajak resultTaxJasa(UserPajak userPajak, BigDecimal pkp){
        TarifPajak t = new TarifPajak();
        BigDecimal reminderJasmed;
        Integer indexJasmed;
        if(userPajak.getPph21().getUsu()==null){
            reminderJasmed = new BigDecimal("50000000.00");
            indexJasmed = 0;
        }else{
            reminderJasmed = userPajak.getSetting_pajak().getReminder_jasmed();
            indexJasmed = userPajak.getSetting_pajak().getIndex_jasmed();
        }

        if(userPajak.getNpwp_simsdm()==null||userPajak.getNpwp_simsdm().equalsIgnoreCase("")){
            if(userPajak.getNpwp()==null || userPajak.getNpwp().equalsIgnoreCase(""))
                t.hitungPajak(reminderJasmed,pkp,indexJasmed, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
            else
                t.hitungPajak(reminderJasmed,pkp,indexJasmed, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
        }else{
            t.hitungPajak(reminderJasmed,pkp,indexJasmed, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
        }

        return t;
    }

    private UserPajak getUserPajak(SalaryDetail sd){
        Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user", sd.getUser().getId().toString());
        UserPajak userPajak = query.first();
        if(userPajak!=null) { // update data
            if(userPajak.getNpwp_simsdm()==null) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
            else if (userPajak.getNpwp_simsdm().isEmpty()) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
        }else{//insert data
            userPajak = new UserPajak();
            userPajak.setId_user(sd.getUser().getId().toString());
            userPajak.setNpwp("");
            userPajak.setNpwp_simsdm(sd.getUser().getNpwp());
            userPajak.setFront_degree(sd.getUser().getFront_degree());
            userPajak.setFull_name(sd.getUser().getFull_name());
            userPajak.setBehind_degree(sd.getUser().getBehind_degree());
            userPajak.setNip_simsdm(sd.getUser().getNip_nik().toString());
            userPajak.setGroup(sd.getUser().getGroup());
            userPajak.setTotal_pendapatan(new UserPajakPendapatan());
            userPajak.setSetting_pajak(new UserPajakTax());
            userPajak.setPph21(new UserPajakPPH());
        }
        return userPajak;
    }

    private BigDecimal sumPPH21(BasicDBList listPph21){
        BigDecimal total_pph21_sementara = BigDecimal.ZERO;
        for(int i=0;i<listPph21.size();i++) {
            BasicDBObject obj = (BasicDBObject) listPph21.get(i);
            total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
        }
        return total_pph21_sementara;
    }

    private PendapatanTetaps initializePendapatanTetap(SalaryDetail sd, UserPajak userPajak) {
        PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
        pendapatanTetaps.setId_user(userPajak.getId_user());
        pendapatanTetaps.setSalary_id(sd.getId().intValue());
        pendapatanTetaps.setUnit(sd.getUnit());
        BasicDBObject rkaPayment = BasicDBObject.parse(sd.getPayment().getAsJsonObject().toString());
        pendapatanTetaps.setRka_payment(rkaPayment);
        return  pendapatanTetaps;
    }

    private PendapatanTdkTetaps initializePendapatanTdkTetap(SalaryDetail sd, UserPajak userPajak){
        PendapatanTdkTetaps pendapatanTdkTetaps = new PendapatanTdkTetaps();
        pendapatanTdkTetaps.setId_user(userPajak.getId_user());
        pendapatanTdkTetaps.setSalary_id(sd.getId().intValue());
        pendapatanTdkTetaps.setUnit(sd.getUnit());
        BasicDBObject rkaPayment = BasicDBObject.parse(sd.getPayment().getAsJsonObject().toString());
        pendapatanTdkTetaps.setRka_payment(rkaPayment);
        return  pendapatanTdkTetaps;
    }

    private BigDecimal getPendapatanSementara(SalaryDetail sd){
        return new BigDecimal(sd.getPayment().getAsJsonObject().entrySet().stream()
                .filter(row -> {
                    if(row.getValue().isJsonObject()) return false;
                    else
                        if(row.getValue().isJsonPrimitive())
                            if(row.getValue().getAsJsonPrimitive().isNumber() && !(row.getKey().contains("pph21"))) return true;
                            else return false;
                        else return  false;
                })
                .mapToInt(row -> {
                    if(!row.getKey().equalsIgnoreCase("returned"))
                        return row.getValue().getAsInt();
                    else
                        return 0;
                }).sum() - sd.getPayment().getAsJsonObject().get("returned").getAsInt());
    }

    private BigDecimal getBiayaJabatan(BigDecimal totalPendapatanSementara, UserPajak userPajak, Pajak pajak){
        BigDecimal biayaJabatan = totalPendapatanSementara.multiply(StaticValue.persenBiayaJabatan);
        if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun() == null){
            if (biayaJabatan.compareTo(StaticValue.limitBiayaJabatan) <= 0) {
                pajak.setBiaya_jabatan(biayaJabatan);
                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(biayaJabatan);
            }else {
                biayaJabatan = BigDecimal.ZERO;
                pajak.setBiaya_jabatan(biayaJabatan);
                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(StaticValue.limitBiayaJabatan);
            }
        }else{
            if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().compareTo(StaticValue.limitBiayaJabatan)<0){
                if(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().add(biayaJabatan).compareTo(StaticValue.limitBiayaJabatan)<0){
                    userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(
                            userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun().add(biayaJabatan));
                    pajak.setBiaya_jabatan(biayaJabatan);
                }else{
                    biayaJabatan = StaticValue.limitBiayaJabatan.subtract(userPajak.getTotal_pendapatan().getBiaya_jabatan_setahun());
                    userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(StaticValue.limitBiayaJabatan);
                }
            }else{
                biayaJabatan = BigDecimal.ZERO;
                pajak.setBiaya_jabatan(biayaJabatan);
                userPajak.getTotal_pendapatan().setBiaya_jabatan_setahun(StaticValue.limitBiayaJabatan);
            }
        }
        return biayaJabatan;
    }

    private BigDecimal calculateSisaPtkp(UserPajak userPajak, BigDecimal nettoPendapatan, BigDecimal pkp, BigDecimal ptkp){
        boolean isNettoPendapatanSetahun = false;
        if(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun() == null){
            userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(nettoPendapatan);
        }else{
            if(userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().compareTo(BigDecimal.ZERO)>0) {
                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(
                        userPajak.getTotal_pendapatan().getNetto_pendapatan_setahun().add(nettoPendapatan)
                );
                isNettoPendapatanSetahun = true;
            }else{
                userPajak.getTotal_pendapatan().setNetto_pendapatan_setahun(nettoPendapatan);
            }
        }

        BigDecimal sisaPtkpSetahun;
        if(isNettoPendapatanSetahun){
            if(userPajak.getTotal_pendapatan().getSisa_ptkp().compareTo(BigDecimal.ZERO)>0){
                sisaPtkpSetahun = userPajak.getTotal_pendapatan().getSisa_ptkp();
                if(sisaPtkpSetahun.compareTo(nettoPendapatan) > 0){
                    sisaPtkpSetahun = sisaPtkpSetahun.subtract(nettoPendapatan);
                    pkp = BigDecimal.ZERO;
                }else{
                    pkp = nettoPendapatan.subtract(sisaPtkpSetahun);
                    sisaPtkpSetahun = BigDecimal.ZERO;
                }
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
                userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
            }else{
                pkp = nettoPendapatan;
                userPajak.getTotal_pendapatan().setTotal_pkp(userPajak.getTotal_pendapatan().getTotal_pkp().add(nettoPendapatan));
                sisaPtkpSetahun = BigDecimal.ZERO;
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
            }
        }else {
            if (nettoPendapatan.compareTo(ptkp) >= 0) {
                pkp = nettoPendapatan.subtract(ptkp);
                sisaPtkpSetahun = new BigDecimal(0.00);
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
                userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
            } else {
                userPajak.getTotal_pendapatan().setTotal_pkp(pkp);
                sisaPtkpSetahun = ptkp.subtract(nettoPendapatan);
                userPajak.getTotal_pendapatan().setSisa_ptkp(sisaPtkpSetahun);
            }
        }
        return pkp;
    }
}
