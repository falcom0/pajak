package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mongodb.BasicDBObject;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import usu.pajak.fariz.model.Salary;
import usu.pajak.fariz.model.SalaryDetail;
import usu.pajak.fariz.model.PendapatanTetaps;
import usu.pajak.fariz.model.UserPajak;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Tax {
    private Datastore datastore;

    public static void main(String[] args) throws IOException {
        Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?request_id=2011","GET"),Salary.class);
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
                        .filter(c ->
                                !(c.getPayment().getAsJsonObject().has("basic_salary")) ||
                                        !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23))
//                        .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
//                                && c.getPayment().getAsJsonObject().has("p1")))
                        .collect(Collectors.toList());

                if(listMwa.size() > 0){

                }

                if(listTA.size() > 0){

                }

                if(gaji.size() > 0){
                    calculateTaxSalary(gaji);
                }

                if(honor.size() > 0){
                    calculateTaxSalary(honor);
                }
            }else{ }
        }else{ }
    }

    private void calculateTaxSalary(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user", sd.getUser().getId().toString());
            UserPajak userPajak = query.get();
            if(userPajak!=null){ // update data
                if(userPajak.getNpwp_simsdm()==null) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}
                else if (userPajak.getNpwp_simsdm().isEmpty()) {userPajak.setNpwp_simsdm(sd.getUser().getNpwp());}

                Query<PendapatanTetaps> qPt = datastore.createQuery(PendapatanTetaps.class)
                        .disableValidation()
                        .filter("id_user", userPajak.getId_user())
                        .filter("salary_id", sd.getId());
                if(qPt.first()==null){ //not exist (insert new data)
                    PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
                    pendapatanTetaps.set_idUser(userPajak.getId_user());
                    pendapatanTetaps.setSalary_id(sd.getId().intValue());
                    BasicDBObject details = BasicDBObject.parse(sd.getPayment().getAsJsonObject().toString());
//                    details.addProperty("pph21",hasilPph21);
                    details.put("status", true);
//                    details.addProperty("created_at", createdAt);
//                    details.addProperty("updated_at",updatedAt);
                    pendapatanTetaps.setDetails(details);
                    datastore.save(pendapatanTetaps);
                }else{ //already exist (if you want to update ? is here.)

                }
            }else{ // insert data
                userPajak = new UserPajak();
                userPajak.setId_user(sd.getUser().getId().toString());
                userPajak.setNpwp("");
                userPajak.setNpwp_simsdm(sd.getUser().getNpwp());
                userPajak.setFront_degree(sd.getUser().getFront_degree());
                userPajak.setFull_name(sd.getUser().getFull_name());
                userPajak.setBehind_degree(sd.getUser().getBehind_degree());
                userPajak.setNip_simsdm(sd.getUser().getNip_nik().toString());
                userPajak.setGroup(sd.getUser().getGroup());

                datastore.save(userPajak);

                PendapatanTetaps pendapatanTetaps = new PendapatanTetaps();
                pendapatanTetaps.set_idUser(userPajak.getId_user());
                pendapatanTetaps.setSalary_id(sd.getId().intValue());
                pendapatanTetaps.setUnit(sd.getUnit());
                BasicDBObject details = BasicDBObject.parse(sd.getPayment().getAsJsonObject().toString());
                details.putIfAbsent("jkk", StaticValue.jkk);
                details.putIfAbsent("jkm", StaticValue.jkm);
                details.putIfAbsent("bpjs_kesehatan", StaticValue.bpjs_kesehatan);
                BigDecimal totalPendapatanSementara = getPendapatanSementara(sd);
                totalPendapatanSementara = totalPendapatanSementara.add(StaticValue.jkk).add(StaticValue.jkm).add(StaticValue.bpjs_kesehatan);

                BigDecimal biayaJabatan = totalPendapatanSementara.multiply(StaticValue.persenBiayaJabatan);
                if (biayaJabatan.compareTo(StaticValue.limitBiayaJabatan) <= 0) { details.putIfAbsent("biaya_jabatan", biayaJabatan);}
                else {details.putIfAbsent("biaya_jabatan", "6000000.00");}


//                if(userPajak.getPotongan_jabatan_A1_setahun() == null)userPajak.setPotongan_jabatan_A1_setahun(new BigDecimal(0.00));
//                if(userPajak.getNetto_pendapatan_setahun() == null)userPajak.setNetto_pendapatan_setahun(new BigDecimal(0.00));
//                if(userPajak.getSisa_ptkp() == null)

//                Iterator<Map.Entry<String, JsonElement>> iterator = sd.getPayment().getAsJsonObject().entrySet().iterator();
//                BigDecimal totalPendapatanSementara = new BigDecimal(0.00);
//                while(iterator.hasNext()){
//                    Map.Entry<String, JsonElement> map = iterator.next();
//                    try{
//                        Integer value = map.getValue().getAsInt();
//                    }catch (Exception e){
//                        continue;
//                    }
//                }
//                    details.addProperty("pph21",hasilPph21);
                details.put("status", true);
//                    details.addProperty("created_at", createdAt);
//                    details.addProperty("updated_at",updatedAt);
                pendapatanTetaps.setDetails(details);
                datastore.save(pendapatanTetaps);
            }
        });
    }

    private void calculateTaxHonor(){

    }

    private BigDecimal getPendapatanSementara(SalaryDetail sd){
        return new BigDecimal(sd.getPayment().getAsJsonObject().entrySet().stream()
                .filter(row -> row.getValue().equals(Integer.class))
                .mapToInt(row -> {
                    if(!row.getKey().equalsIgnoreCase("return"))
                        return row.getValue().getAsInt();
                    else
                        return 0;
                }).sum() - sd.getPayment().getAsJsonObject().get("return").getAsInt());
    }
}
