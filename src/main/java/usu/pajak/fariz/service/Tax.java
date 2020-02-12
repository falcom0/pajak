package usu.pajak.fariz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dev.morphia.Datastore;
import dev.morphia.query.CriteriaContainer;
import dev.morphia.query.Query;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.bson.types.Decimal128;
import usu.pajak.fariz.model.*;
import usu.pajak.services.ApiRka;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Tax {
    private static CellStyle currency;
    private Datastore datastore;
    private JsonArray jsonArray;
    public static final int REQUEST  = 0;
    public static final int SALARY = 1;

    public static void main(String[] args) throws IOException {
        Tax tax = new Tax();
        tax.getListTax("1","9",false, "NON-PNBP");

        //request_id=13763
//        System.out.println(Ptkp.getInstance.getPtkp("3654"));

//        Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&user_id=3654","GET"),Salary.class);
//        new Tax(salary);

//        new PnsApbn();
//        for (int i=1;i<=12;i++) {
//            long start = System.currentTimeMillis();
//            Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month="+i, "GET"), Salary.class); //13763
//            long end = System.currentTimeMillis();
//            System.out.println("Api bulan "+i+" waktu :"+(end-start));
//            start = System.currentTimeMillis();
//            new Tax(salary);
//            end = System.currentTimeMillis();
//            System.out.println("Perhitungan bulan "+i+" waktu :"+(end-start));
//        }
//        new Tax().printToExcel();
//        long start = System.currentTimeMillis();
//        Salary salary = new Gson().fromJson(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month="+12, "GET"), Salary.class); //13763
//        long end = System.currentTimeMillis();
//        System.out.println("Api bulan "+12+" waktu :"+(end-start));
//        start = System.currentTimeMillis();
//        new Tax(salary);
//        end = System.currentTimeMillis();
//        System.out.println("Perhitungan bulan "+12+" waktu :"+(end-start));
    }

    public JsonArray getJsonArray() {
        return jsonArray;
    }

    public void printToExcel() throws IOException {
        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoDatabase mongoDatabase = mongoClient.getDatabase("revisi_pajak");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("TotalPph21");
        FindIterable<Document> findIterable = mongoCollection.find();
        Workbook workbook = WorkbookFactory.create(new File("D:/PAJAK_2019.xls"));
        currency = workbook.createCellStyle();
        currency.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        Sheet sheet = workbook.createSheet("PPH21");
        Row row = sheet.createRow(0);
        final Cell[] cell = {row.createCell(0)};
        cell[0].setCellValue("No.");
        cell[0] = row.createCell(1);
        cell[0].setCellValue("id_user");
        cell[0] = row.createCell(2);
        cell[0].setCellValue("Nama");
        cell[0] = row.createCell(3);
        cell[0].setCellValue("Unit");
        cell[0] = row.createCell(4);
        cell[0].setCellValue("Total PKP");
        cell[0] = row.createCell(5);
        cell[0].setCellValue("Total PKP Jasa Medis");
        cell[0] = row.createCell(6);
        cell[0].setCellValue("PPH 21 yang telah dibayar USU");
        cell[0] = row.createCell(7);
        cell[0].setCellValue("PPH 21 yang telah dibayar melalui APBN");
        cell[0] = row.createCell(8);
        cell[0].setCellValue("Total yang telah dibayar");
        cell[0] = row.createCell(9);
        cell[0].setCellValue("PPH 21 yang seharusnya (Progresif)");
        cell[0] = row.createCell(10);
        cell[0].setCellValue("PPH 21 yang seharusnya (Jasa Medis)");
        cell[0] = row.createCell(11);
        cell[0].setCellValue("Total PPH 21 dari sistem");
        cell[0] = row.createCell(12);
        cell[0].setCellValue("Lebih / Kurang Bayar");
        final int[] i = {1};
        findIterable.forEach((Consumer<? super Document>) document -> {
            if(!document.getString("_id").contains("-")){
                Row r = sheet.createRow(i[0]);
                Cell c = r.createCell(0);
                c.setCellValue(i[0] +".");
                c = r.createCell(1);
                c.setCellValue(document.getString("_id"));
                c = r.createCell(2);
                Document up = (Document)document.get("user");
                c.setCellValue(up.getString("full_name"));
                c = r.createCell(3);
                try {
                    BasicDBObject bdo = BasicDBObject.parse(ReceiveRka.getInstance.callApiUsu("https://api.usu.ac.id/0.1/users/"+document.getString("_id"),"GET"));
                    BasicDBObject data = (BasicDBObject) bdo.get("data");
                    c.setCellValue(data.getString("work_unit"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                c = r.createCell(4);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                Document totPen = (Document)up.get("total_pendapatan");
                if(totPen.get("total_pkp") != null) {
                    Decimal128 total_pkp = (Decimal128) totPen.get("total_pkp");
                    c.setCellValue(total_pkp.doubleValue());
                }else{
                    c.setCellValue(0.0);
                }
                c = r.createCell(5);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(totPen.get("total_pkp_jasa") != null) {
                    Decimal128 total_pkp = (Decimal128)totPen.get("total_pkp_jasa");
                    c.setCellValue(total_pkp.doubleValue());
                }else
                    c.setCellValue(0.0);
                c = r.createCell(6);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                c.setCellValue(document.getInteger("total_pph21_rka"));
                c = r.createCell(7);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("pns") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("pns");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("pns") instanceof Integer){
                    Integer pkp = (Integer) document.get("pns");
                    c.setCellValue(pkp);
                }
                c = r.createCell(8);
                c.setCellType(CellType.NUMERIC);
                c.setCellStyle(currency);
                c.setCellFormula("SUM(G"+(i[0]+1)+":H"+(i[0]+1)+")");
                c = r.createCell(9);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("usu") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("usu");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("usu") instanceof Integer){
                    Integer pkp = (Integer) document.get("usu");
                    c.setCellValue(pkp);
                }
                c = r.createCell(10);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                if(document.get("jasa") instanceof Decimal128) {
                    Decimal128 total_pkp = (Decimal128) document.get("jasa");
                    c.setCellValue(total_pkp.doubleValue());
                }else if(document.get("jasa") instanceof Integer){
                    Integer pkp = (Integer) document.get("jasa");
                    c.setCellValue(pkp);
                }
                c = r.createCell(11);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                c.setCellFormula("SUM(J"+(i[0]+1)+":K"+(i[0]+1)+")");
                c = r.createCell(12);
                c.setCellStyle(currency);
                c.setCellType(CellType.NUMERIC);
                c.setCellFormula("I"+(i[0]+1)+"-L"+(i[0]+1));
                i[0]++;
            }
        });
        try (OutputStream fileOut = new FileOutputStream("D:/HASIL_PPH21.xls")) {
            workbook.write(fileOut);
        }
        workbook.close();
        System.out.println(mongoCollection.countDocuments());
    }

    public Tax(){
        datastore = MongoDb.getInstance.getDatastore(MongoDb.SERVER,"revisi_pajak");
    }

    public Tax(Salary salary){
        datastore = MongoDb.getInstance.getDatastore(MongoDb.LOCAL,"revisi_pajak");
        jsonArray  = new JsonArray();
        filterTax(salary);
    }

    public void filterTax(Salary salary){
        if(salary != null){
            if(salary.getResponse().getSalary_receivers().stream().anyMatch(Objects::nonNull)){
                List<SalaryDetail> allData = salary.getResponse().getSalary_receivers();

                //Filter berdasarkan Luar / Dalam
                //1. Luar : Pisahkan : a. MWA b. Tenaga Ahli c. Dosen Luar Negri
                List<SalaryDetail> listLuar = allData.stream()
                        .filter( c -> c.getUser().getId() == null)
                        .collect(Collectors.toList());
                List<SalaryDetail> listMwa = listLuar.stream()
                        .filter( r -> r.getUnit().getId() == 1)
                        .collect(Collectors.toList());
                List<SalaryDetail> listTA = listLuar.stream()
                        .filter( r -> r.getUnit().getId() != 1)
                        .collect(Collectors.toList());

                //2. Dalam : Pisahkan : a. Gaji Tetap b. Honor
                List<SalaryDetail> listDalam = allData.stream()
                        .filter(c -> c.getUser().getId() != null)
                        .collect(Collectors.toList());
//                List<SalaryDetail> gaji = listDalam.stream()
//                        .filter(c ->
//                                (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId() != 0) &&
//                                        (c.getPayment().getAsJsonObject().has("basic_salary") ||
//                                c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23))
//                        .collect(Collectors.toList());

//                listDalam.stream().filter(c ->
//                        (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId()!=0) &&
//                                (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt() == 23
//                                        || c.getPayment().getAsJsonObject().has("basic_salary"))).collect(Collectors.toList())

//                List<SalaryDetail> honorPNS = listDalam.stream().filter(c -> c.getUser().getGroup().getId()==1 || c.getUser().getGroup().getId()==0).collect(Collectors.toList());
//
//                List<SalaryDetail> honorNonPNS = listDalam.stream()
//                        .filter(c -> (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId()!=0) && !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt() == 23 || c.getPayment().getAsJsonObject().has("basic_salary")))
//                        .collect(Collectors.toList());
//
//                List<SalaryDetail> honor = new ArrayList<>(honorPNS);
//                honor.addAll(honorNonPNS);

                //                        .filter(c -> !(c.getPayment().getAsJsonObject().has("basic_salary")))
//                        .filter(c -> (c.getUser().getGroup().getId()==1 || c.getUser().getGroup().getId() == 0) &&
//                                c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
//                        .filter(c -> (c.getUser().getGroup().getId()!=1 && c.getUser().getGroup().getId() != 0) &&
//                                !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
//                                && c.getPayment().getAsJsonObject().has("p1")))

                List<SalaryDetail> honorBknJasmed = listDalam.stream()
                        .filter(c -> !((c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 49))
                        )
                        .collect(Collectors.toList());

                List<SalaryDetail> honorJasmed = listDalam.stream()
                        .filter(c -> (c.getPayment().getAsJsonObject().getAsJsonObject("type").get("id").getAsInt() == 49)
                        )
                        .collect(Collectors.toList());


                if(listMwa.size() > 0){
                    calculateTaxMwa(listMwa);
                }

                if(listTA.size() > 0){
                    calculateTaxTa(listTA);
                }

//                if(gaji.size() > 0){
//                    calculateTaxSalary(gaji);
//                }

                if(honorBknJasmed.size() > 0){
                    calculateTaxHonor(honorBknJasmed);
                }

                if(honorJasmed.size() > 0){
                    calculateTaxJasmed(honorJasmed);
                }
            }else{ }
        }else{ }
    }

    private void calculateTaxMwa(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajakLuar(sd,"MWA");

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
            qPt.and(qPt.criteria("id_user").contains(userPajak.getId_user()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = calculateModelJasaTax(sd,userPajak,BigDecimal.valueOf(1));
                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
            }else{//duplicate request
                PendapatanTdkTetaps ptt = qPt.first();
                BigDecimal total_pph21_sementara = sumPPH21(ptt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private void calculateTaxTa(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajakLuar(sd,"TA");

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
            qPt.and(qPt.criteria("id_user").contains(userPajak.getId_user()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = calculateModelJasaTax(sd,userPajak,BigDecimal.valueOf(0.5));
                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
            }else{//duplicate request
                PendapatanTdkTetaps ptt = qPt.first();
                BigDecimal total_pph21_sementara = sumPPH21(ptt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
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
                BigDecimal ptkpSetahun = new BigDecimal(new Ptkp().getPtkp(sd.getUser().getId().toString()));

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
                Query<PendapatanTetaps> x = datastore.createQuery(PendapatanTetaps.class)
                        .disableValidation();
//                        .filter("id_user", sd.getUser().getId().toString())
//                        .filter("salary_id", sd.getId().intValue());
                x.and(x.criteria("id_user").equal(sd.getUser().getId().toString()),x.criteria("salary_id").equal(sd.getId().intValue()));
//                qPt.filter("salary_id",sd.getId().intValue());
                if(x.count()==0){ //salary not duplicate
                    List<PendapatanTetaps> listPendapatanTetap = qPt.find().toList();
                    PendapatanTetaps pBefore = listPendapatanTetap.get(listPendapatanTetap.size()-1);
                    BigDecimal tBefore = pBefore.getPajak().getTotal_pendapatan_rka();
                    BigDecimal tNow = getPendapatanSementara(sd);
                    if(tBefore == null){ // kemungkinan pendapatan apbn bercampur dgn gaji
                        tBefore = pBefore.getPajak().getBruto_pendapatan();
                        System.out.println(pBefore.getSalary_id()+" Salary_id");
                        System.out.println(userPajak.getId_user()+" UserPajak");
                        System.out.println(listPendapatanTetap.size()+" Pendapatan Tetap Size");
                    }
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
                        /**
                         * TODO
                         *
                         * jika lebih dari sebelumya hanya hitung yg berlebihnya saja
                         * jika kurang dari sebelumnya ga tau mau diapain
                         */
                        System.out.println("Here Gaji Tidak Sama dgn bulan sebelumnya");
                    }
                }else{ //salary sudah ada (salary duplicate)
                    /**
                     * TODO
                     *
                     * kirim hasil pph 21 ke RKA
                     */
                }
            }
        });
    }

    private void calculateTaxHonor(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
//                    .filter("id_user", sd.getUser().getId().toString())
//                    .filter("salary_id", sd.getId().toString());
            qPt.and(qPt.criteria("id_user").contains(sd.getUser().getId().toString()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null){
                PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);
                BigDecimal totalPendapatanSementara = getPendapatanSementara(sd);
                Pajak pajak = new Pajak();
                pajak.setTotal_pendapatan_rka(totalPendapatanSementara);
                pajak.setBruto_pendapatan(totalPendapatanSementara);

                BigDecimal biayaJabatan = getBiayaJabatan(totalPendapatanSementara,userPajak,pajak);

                BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(biayaJabatan);
                pajak.setNetto_pendapatan(nettoPendapatan);

                BigDecimal pkp = new BigDecimal(0.00);

                if(userPajak.getTotal_pendapatan().getPtkp_setahun() == null) {
                    BigDecimal ptkpSetahun = new BigDecimal(new Ptkp().getPtkp(sd.getUser().getId().toString()));
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);
                }

                pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
                pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));

                pkp = calculateSisaPtkp(userPajak,nettoPendapatan,pkp,userPajak.getTotal_pendapatan().getPtkp_setahun());

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

                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());

                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
                jsonArray.add(jsonObject);
            }else{ //duplicate request
                PendapatanTdkTetaps ptt = qPt.first();
                BigDecimal total_pph21_sementara = sumPPH21(ptt.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private void calculateTaxJasmed(List<SalaryDetail> salaryDetailList){
        salaryDetailList.forEach(sd -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id",sd.getId());
            UserPajak userPajak = getUserPajak(sd);

            Query<PendapatanTdkTetaps> qPt = datastore.createQuery(PendapatanTdkTetaps.class)
                    .disableValidation();
//                    .filter("id_user", sd.getUser().getId().toString())
//                    .filter("salary_id", sd.getId().toString());
            qPt.and(qPt.criteria("id_user").contains(sd.getUser().getId().toString()),qPt.criteria("salary_id").equal(sd.getId().intValue()));
            if(qPt.first()==null) {
                PendapatanTdkTetaps pendapatanTdkTetaps = calculateModelJasaTax(sd,userPajak,BigDecimal.valueOf(0.5));

                BigDecimal ptkpSetahun = new BigDecimal(new Ptkp().getPtkp(sd.getUser().getId().toString()));
                if (userPajak.getTotal_pendapatan().getPtkp_setahun() == null)
                    userPajak.getTotal_pendapatan().setPtkp_setahun(ptkpSetahun);

                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
                datastore.save(userPajak);
                datastore.save(pendapatanTdkTetaps);
            }else{//duplicate request
                PendapatanTdkTetaps pendapatanTdkTetaps = qPt.first();
                BigDecimal total_pph21_sementara = sumPPH21(pendapatanTdkTetaps.getPajak().getPph21());
                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                jsonArray.add(jsonObject);
            }
        });
    }

    private TarifPajak resultTax(UserPajak userPajak,BigDecimal pkp, boolean rutin){
        TarifPajak t = new TarifPajak();
        BigDecimal reminder;
        Integer index;
        if(userPajak.getPph21().getUsu()==null){
            if(userPajak.getPph21().getPns()==null) {
                reminder = new BigDecimal("50000000.00");
                index = 0;
            }else{
                reminder = userPajak.getSetting_pajak().getReminder();
                index = userPajak.getSetting_pajak().getIndex();
            }
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
        if(userPajak.getPph21().getJasa()==null){
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

    private UserPajak getUserPajakLuar(SalaryDetail sd, String type){
        UserPajak userPajak;
        Query<UserPajak> qUP = datastore.createQuery(UserPajak.class).disableValidation();
        qUP.and(qUP.criteria("id_user").contains(type),qUP.criteria("full_name").containsIgnoreCase(sd.getUser().getFull_name().trim()));
        if(qUP.first()==null){//input user baru
            userPajak = new UserPajak();
            userPajak.setId_user(type+"-"+System.currentTimeMillis());
            userPajak.setNpwp_simsdm("0");
            userPajak.setFull_name(sd.getUser().getFull_name());
            userPajak.setTotal_pendapatan(new UserPajakPendapatan());
            userPajak.setSetting_pajak(new UserPajakTax());
            userPajak.setPph21(new UserPajakPPH());
        }else{// user udah ada
            userPajak = qUP.first();
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

    private PendapatanTdkTetaps calculateModelJasaTax(SalaryDetail sd, UserPajak userPajak, BigDecimal persenKenaPajak){
        PendapatanTdkTetaps pendapatanTdkTetaps = initializePendapatanTdkTetap(sd, userPajak);
        BigDecimal brutoPendapatan = getPendapatanSementara(sd);
        Pajak pajak = new Pajak();
        pajak.setTotal_pendapatan_rka(brutoPendapatan);
        pajak.setBruto_pendapatan(brutoPendapatan);
        pendapatanTdkTetaps.setMonth(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[1]));
        pendapatanTdkTetaps.setYear(Integer.parseInt(sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString().split(" ")[0].split("-")[0]));
        if (userPajak.getTotal_pendapatan().getBruto_jasa_setahun() == null)
            userPajak.getTotal_pendapatan().setBruto_jasa_setahun(brutoPendapatan);
        else
            userPajak.getTotal_pendapatan().setBruto_jasa_setahun(
                    userPajak.getTotal_pendapatan().getBruto_jasa_setahun().add(brutoPendapatan)
            );
        BigDecimal pkp = brutoPendapatan.multiply(persenKenaPajak);
        if (userPajak.getTotal_pendapatan().getTotal_pkp_jasa() == null)
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

        if (userPajak.getPph21().getJasa() == null) {
            userPajak.getPph21().setJasa(total_pph21_sementara);
        } else {
            userPajak.getPph21().setJasa(userPajak.getPph21().getJasa().add(total_pph21_sementara));
        }

        return pendapatanTdkTetaps;
    }

    public Pajak getDetailTax(Integer salaryId){
        Query<PendapatanTetaps> pendapatanTetapsQuery = datastore.createQuery(PendapatanTetaps.class).disableValidation();
        pendapatanTetapsQuery.criteria("salary_id").equal(salaryId);
        if(pendapatanTetapsQuery.first()!=null){
            PendapatanTetaps pendapatanTetaps = pendapatanTetapsQuery.first();
            return pendapatanTetaps.getPajak();
        }else{
            Query<PendapatanTdkTetaps> pendapatanTdkTetapsQuery = datastore.createQuery(PendapatanTdkTetaps.class).disableValidation();
            pendapatanTdkTetapsQuery.criteria("salary_id").equal(salaryId);
            if(pendapatanTdkTetapsQuery.first()!=null){
                PendapatanTdkTetaps pendapatanTdkTetaps = pendapatanTdkTetapsQuery.first();
                return pendapatanTdkTetaps.getPajak();
            }else{
                return null;
            }
        }
    }

    public UserPajakPendapatan getProfileTax(String userId){
        Query<UserPajak> userPajakQuery = datastore.createQuery(UserPajak.class).disableValidation();
        userPajakQuery.criteria("id_user").equalIgnoreCase(userId);
        if(userPajakQuery.first() != null){
            UserPajak userPajak = userPajakQuery.first();
            return userPajak.getTotal_pendapatan();
        }else{
            return null;
        }
    }

    public List<UserPajak> getListTax(String month, String unitId, boolean pegawaiLuar, String sumberDana){
        Query<PendapatanTdkTetaps> pendapatanTdkTetapsQuery = datastore.find(PendapatanTdkTetaps.class).disableValidation();
        Query<UserPajak> userPajakQuery = datastore.find(UserPajak.class).disableValidation();
        if(unitId != null){ // specific unit
            pendapatanTdkTetapsQuery.and(
                    pendapatanTdkTetapsQuery.criteria("month").equal(Integer.parseInt(month)),
                    pendapatanTdkTetapsQuery.criteria("unit.id").equal(Integer.parseInt(unitId)),
                    pendapatanTdkTetapsQuery.criteria("rka_payment.activity.source_of_fund").equalIgnoreCase(sumberDana)
            );
            if(!pegawaiLuar){
                pendapatanTdkTetapsQuery.criteria("id_user").not().containsIgnoreCase("-");
            }else{
                pendapatanTdkTetapsQuery.criteria("id_user").containsIgnoreCase("-");
            }

            List<PendapatanTdkTetaps> pendapatanTdkTetapsList = pendapatanTdkTetapsQuery.find().toList();
            Map<String, BigDecimal> pendapatan = pendapatanTdkTetapsList.stream()
                .collect(
                    Collectors.groupingBy(
                        PendapatanTdkTetaps::getId_user,
                        Collectors.mapping(
                            PendapatanTdkTetaps::getNettoPendapatan,
                            Collectors.reducing(
                                BigDecimal.ZERO,
                                BigDecimal::add
                            )
                        )
                    )
                );

            Map<String, BigDecimal> pajak = pendapatanTdkTetapsList.stream()
                    .collect(
                            Collectors.groupingBy(
                                    PendapatanTdkTetaps::getId_user,
                                    Collectors.mapping(
                                            PendapatanTdkTetaps::getPph21,
                                            Collectors.reducing(
                                                    BigDecimal.ZERO,
                                                    BigDecimal::add
                                            )
                                    )
                            )
                    );

            userPajakQuery.field("id_user").in(pendapatan.keySet());
            List<UserPajak> userPajakList = userPajakQuery.find().toList();
            userPajakList.forEach(e -> {
                e.setTotalNettoPendapatan(pendapatan.get(e.getId_user()));
                e.setTotalPph21(pajak.get(e.getId_user()));
            });
//            userPajakList.stream().forEach(System.out::println);
            return userPajakList;
        }else{ // general or whole USU
            return null;
        }

    }

    public BuktiPotong getBuktiPotong(String userId){
        try {
            ApiRka.callApiUsu("https://api.usu.ac.id/","GET");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteTax(Integer type, Integer value){
        switch (type) {
            case REQUEST:
                Query<PendapatanTdkTetaps> pendapatanTdkTetapsQuery = datastore.createQuery(PendapatanTdkTetaps.class).disableValidation();
                pendapatanTdkTetapsQuery.criteria("request_id").equal(value);
                if(pendapatanTdkTetapsQuery.first()!=null){
                    List<PendapatanTdkTetaps> pendapatanTdkTetapsList = pendapatanTdkTetapsQuery.asList();
                    for (PendapatanTdkTetaps pendapatanTdkTetaps: pendapatanTdkTetapsList) {
                        Query<UserPajak> query = datastore.createQuery(UserPajak.class).disableValidation();
                        query.criteria("id_user").equalIgnoreCase(pendapatanTdkTetaps.getId_user());
                        UserPajak userPajak = query.first();

                    }
//                    return pendapatanTdkTetaps.getPajak();
                }else{
//                    return null;
                }
                break;
            case SALARY:
                pendapatanTdkTetapsQuery = datastore.createQuery(PendapatanTdkTetaps.class).disableValidation();
                pendapatanTdkTetapsQuery.criteria("salary_id").equal(value);
                if(pendapatanTdkTetapsQuery.first()!=null){
                    PendapatanTdkTetaps pendapatanTdkTetaps = pendapatanTdkTetapsQuery.first();
                    Query<UserPajak> query = datastore.createQuery(UserPajak.class).disableValidation();
                    query.criteria("id_user").equalIgnoreCase(pendapatanTdkTetaps.getId_user());
                    UserPajak userPajak = query.first();
//                    return pendapatanTdkTetaps.getPajak();
                }else{
//                    return null;
                }
                break;
            default:
                break;
        }
    }
}
