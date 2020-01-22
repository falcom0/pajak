package usu.pajak.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.*;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import usu.pajak.model.*;
import usu.pajak.util.UserSimSdm;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ApiRka {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_server")); //connect to mongodb
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_server");
    private static final Double persenPotJabatan = Double.valueOf("0.05");
    private static final Double persenPotPensiun = Double.valueOf("0.0475");
    private List<UserSimSdm> listUserSimsdm;
    private JsonArray jsonArray = new JsonArray();
    private Logger logger;

    public ApiRka(){

    }

    public ApiRka(Logger logger){
        this.logger = logger;
    }

    public void setJsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public JsonArray getJsonArray() {
        return jsonArray;
    }

    private Integer listPtkpSimsdm(String userId){
            Integer initialPtkp = 54000000;
            Integer ptkp = 0;

            UserSimSdm us = null;
            for(int i=0;i<listUserSimsdm.size();i++){
                us = listUserSimsdm.get(i);
                if(us.getId().equalsIgnoreCase(userId))
                    break;
            }
            int count = 0;
            if(us.getGender().equalsIgnoreCase("Pria") && us.getHas_couple())  count++;
            else if(us.getGender().equalsIgnoreCase("Wanita") && us.getHas_couple()) return initialPtkp;
            if(us.getNum_of_children()>0 && us.getNum_of_children()<=3) count += us.getNum_of_children();
            Integer addPtkp = 4500000;
            addPtkp = count * addPtkp;
            ptkp = initialPtkp+addPtkp;
            return ptkp;
    }

    public static Integer hitungPtkp(String userId){
        Integer initialPtkp = 54000000;
        Integer ptkp = 0;
        try {
//            Thread.sleep(100);
            Response response = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.1/users/" + userId + "/ptkp", "GET"), Response.class);
            UserSimSdm us = response.getResponse().get(0);
            int count = 0;
            if(us.getGender().equalsIgnoreCase("Pria") && us.getHas_couple())  count++;
            else if(us.getGender().equalsIgnoreCase("Wanita") && us.getHas_couple()) return initialPtkp;
            if(us.getNum_of_children()>0 && us.getNum_of_children()<=3) count += us.getNum_of_children();
            Integer addPtkp = 4500000;
            addPtkp = count * addPtkp;
            ptkp = initialPtkp+addPtkp;
            return ptkp;
        }catch (IOException io){
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static Token getSSO(String identity, String password) throws IOException {
        String url = "https://akun.usu.ac.id/auth/login/apps?random_char=TVWBJBSuwyewbwgcuw23657438zs";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "identity="+identity+"&password="+password;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr;
        wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        InputStream response = con.getInputStream();

        Scanner scanner = new Scanner(response);
        String responseBody = scanner.useDelimiter("\\A").next();
        Token token = new Gson().fromJson(responseBody,Token.class);
//        System.out.println(token.getToken());
        return token;
    }

    public boolean serviceCalculateTax(Salary salary){
        if(salary != null && salary.getResponse().getSalary_receivers().size()>0){
            Response resp = null;
            try {
                resp = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.1/users/ptkp","GET"), Response.class);
                listUserSimsdm = resp.getResponse();
            } catch (IOException e) {
                e.printStackTrace();
                logger.info(e.getMessage());
            }


            List<SalaryDetail> totalData = salary.getResponse().getSalary_receivers();
            List<SalaryDetail> gaji = totalData.stream()
                    .filter(c -> c.getPayment().getAsJsonObject().has("basic_salary"))
                    .collect(Collectors.toList());
            List<SalaryDetail> honorGaji = totalData.stream()
                    .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                    .filter(c -> (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
                            && c.getPayment().getAsJsonObject().has("p1"))
                    .collect(Collectors.toList());
            List<SalaryDetail> honor = totalData.stream()
                    .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                    .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
                            && c.getPayment().getAsJsonObject().has("p1")))
                    .collect(Collectors.toList());


            boolean result = false;

            if(gaji.size()>0) {
                logger.info("Jumlah permintaan Gaji Non PNS: "+gaji.size());
                result = hitungPPH21forApi(gaji, true);
            }

            if(honorGaji.size()>0) {
                logger.info("Jumlah permintaan Gaji Honor: "+honorGaji.size());
                result = hitungPPH21forApi(honorGaji, true);
            }

            if(honor.size()>0) {
                logger.info("Jumlah permintaan Adhoc : "+honor.size());
                result = hitungPPH21forApi(honor, false);
            }

            return result;
        }else
            return false;
    }

    public void asalData(String month, String year){
        Response resp = null;
        try {
            resp = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.1/users/ptkp","GET"), Response.class);
            listUserSimsdm = resp.getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Salary salary = null;
        try {
            salary = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?status=1&month="+month, "GET"), Salary.class);
            System.out.println("Bulan :"+month);
            List<SalaryDetail> totalData = salary.getResponse().getSalary_receivers();
            List<SalaryDetail> gaji = totalData.stream()
                    .filter(c -> c.getPayment().getAsJsonObject().has("basic_salary"))
//                    .filter(c -> c.getId().intValue()==93237)//11872  36158 93237
                    .collect(Collectors.toList());
            List<SalaryDetail> honorGaji = totalData.stream()
                    .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                    .filter(c -> (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23)
                    && c.getPayment().getAsJsonObject().has("p1"))
//                    .filter(c -> c.getId().intValue()==37579)
                    //37579
                    .collect(Collectors.toList());
            List<SalaryDetail> honor = totalData.stream()
                    .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                    .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
                            && c.getPayment().getAsJsonObject().has("p1")))
//                    .filter(c -> c.getId().intValue()==33124 || c.getId().intValue()==64771 )
//                    .filter(c -> (c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23))
                    .collect(Collectors.toList());
//            List<SalaryDetail> pns = totalData.stream()
//                    .filter(c -> c.getUser().getId()!=null)
//                    .filter(c -> c.getPayment().getAsJsonObject().has("basic_salary"))
//                    .filter(c ->(c.getUser().getGroup().getId() == 5) || (c.getUser().getGroup().getId() == 6) )
//                    .collect(Collectors.toList());
//            List<SalaryDetail> non_pns = totalData.stream()
//                    .filter(c -> c.getId().intValue()!=11872)
//                    .filter(c -> c.getPayment().getAsJsonObject().has("basic_salary"))
//                    .filter(c -> !((c.getUser().getGroup().getId() == 0) || (c.getUser().getGroup().getId() == 1)))
//                    .collect(Collectors.toList());

            System.out.println("Total Data :"+totalData.size());
            System.out.println("Gaji :"+gaji.size());
            System.out.println("Honor Gaji :"+honorGaji.size());
            System.out.println("Honor :"+honor.size());
//            System.out.println("Total PNS :"+pns.size());
//            System.out.println("Total NON-PNS :"+non_pns.size());

//            gaji.get(0).getUser().setId(new BigInteger("6283"));
//            gaji.get(0).getUser().setFull_name("Vita Cita Emia Tarigan");
//            gaji.get(0).getUser().setNip_nik(new BigInteger("198404182018112001"));
//            gaji.get(0).getUser().setFront_degree("Dr");
//            gaji.get(0).getUser().setBehind_degree("S.H.,L.LM");
//            gaji.get(0).getUser().setNpwp("353365950121000");

//            honorGaji.get(0).getUser().setId(new BigInteger("4081"));
//            honorGaji.get(0).getUser().setFull_name("Asrol Pasaribu");
//            honorGaji.get(0).getUser().setNip_nik(new BigInteger("67050509041001"));
//            honorGaji.get(0).getUser().setFront_degree("");
//            honorGaji.get(0).getUser().setBehind_degree("");
//            honorGaji.get(0).getUser().setNpwp("360885131126000");

//            honor.get(0).getUser().setId(new BigInteger("6386"));
//            honor.get(0).getUser().setFull_name("May Hana Bilqis Rangkuti");
//            honor.get(0).getUser().setNip_nik(new BigInteger("1271215911900001"));
//            honor.get(0).getUser().setFront_degree("");
//            honor.get(0).getUser().setBehind_degree("SE, M.Si.");
//            honor.get(0).getUser().setNpwp("725497309121000");
//
//            honor.get(1).getUser().setId(new BigInteger("6386"));
//            honor.get(1).getUser().setFull_name("May Hana Bilqis Rangkuti");
//            honor.get(1).getUser().setNip_nik(new BigInteger("1271215911900001"));
//            honor.get(1).getUser().setFront_degree("");
//            honor.get(1).getUser().setBehind_degree("SE, M.Si.");
//            honor.get(1).getUser().setNpwp("725497309121000");


            if(gaji.size()>0) {
//                logger.info("Jumlah permintaan Gaji Non PNS: "+gaji.size());
                hitungPPH21forApi(gaji, true);
            }

            if(honorGaji.size()>0) {
//                logger.info("Jumlah permintaan Gaji Honor: "+honorGaji.size());
                hitungPPH21forApi(honorGaji, true);
            }

            if(honor.size()>0) {
//                logger.info("Jumlah permintaan Adhoc : "+honor.size());
                hitungPPH21forApi(honor, false);
            }
//            hitungPPH21forApi(gaji,true);
//            hitungPPH21forApi(honorGaji,true);
//            hitungPPH21forApi(honor,false);

//            hitungPPH21(month,year,gaji,true);
//            hitungPPH21(month,year,honorGaji,true);
//            hitungPPH21(month,year,honor,false);
//            hitungPPH21forApi(totalData,false);
//            hitungPPH21(month,year,totalData);
        } catch (IOException e) {
            e.printStackTrace();
        }
//            String sisaData = readFile("D:\\GajiAPBN\\sisaDataygBelumTerinput.txt", StandardCharsets.UTF_8);
//            Salary salary = new Gson().fromJson(sisaData,Salary.class);
    }

    private boolean hitungPPH21forApi(List<SalaryDetail> listSalaryDetail ,boolean gaji){
        boolean result = false;
        for(SalaryDetail sd : listSalaryDetail){
            if(sd.getUser().getId() == null){
                logger.info("Null id on salary_id : "+sd.getId()+" Full Name : "+sd.getUser().getFull_name());
                boolean mwa;
                if(sd.getUnit().getId()==1 || sd.getUnit().getId()==171){
                    mwa = true;
                }else{
                    mwa = false;
                }
                result = saveDataMWA_TenagaAhli(sd,mwa);
            }else {
                logger.info("Database salary_id : "+sd.getId()+" Id User : "+sd.getUser().getId()+" Full Name : "+sd.getUser().getFull_name());
                result = saveData(sd,gaji);
            }
        }
        return result;
    }

    private boolean saveData(SalaryDetail sd, boolean pendapatanTetap){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",sd.getId());
        Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user", sd.getUser().getId().toString());
        UserPajak p = query.get();
        if(p != null){
            if(p.getPendapatan_tetap() != null) {
                for (Object object:p.getPendapatan_tetap()) {
                    BasicDBObject pendapatan = (BasicDBObject) object;
                    if(pendapatan.getString("salary_id").equalsIgnoreCase(sd.getId().toString())) {
                        logger.info("Salary_id : "+sd.getId().toString()+" telah ada pada database.");
                        return false;
                    }
                }
            }

            if(p.getPendapatan_tdk_tetap() != null){
                for (Object object:p.getPendapatan_tdk_tetap()) {
                    BasicDBObject pendapatan = (BasicDBObject) object;
                    if(pendapatan.getString("salary_id").equalsIgnoreCase(sd.getId().toString())) {
                        logger.info("Salary_id : "+sd.getId().toString()+" telah ada pada database.");
                        return false;
                    }
                }
            }
        }
        BasicDBList listPph21;
        BasicDBList listPendapatan = new BasicDBList();
        BigDecimal basicSalary = setBasicSalary(pendapatanTetap,sd);
        boolean insert;
        if (p == null) {
            p = new UserPajak();
            p.setId_user(sd.getUser().getId().toString());
            p.setNpwp("");
            p.setNpwp_simsdm(sd.getUser().getNpwp());
            p.setFront_degree(sd.getUser().getFront_degree());
            p.setFull_name(sd.getUser().getFull_name());
            p.setBehind_degree(sd.getUser().getBehind_degree());
            p.setNip_simsdm(sd.getUser().getNip_nik().toString());
            insert = true;
        }else {
            if(p.getNpwp_simsdm()==null) {
                p.setNpwp_simsdm(sd.getUser().getNpwp());
            }else if (p.getNpwp_simsdm().isEmpty()) {
                p.setNpwp_simsdm(sd.getUser().getNpwp());
            }
            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                if(p.getPendapatan_tetap() != null)
                    listPendapatan = p.getPendapatan_tetap();
            }else{
                if(p.getPendapatan_tdk_tetap() != null)
                    listPendapatan = p.getPendapatan_tdk_tetap();
            }
            insert = false;
        }

        BasicDBObject pendapatan = new BasicDBObject();
        String date = sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString();
        String split[] = date.split("-");
        String year = split[0];
        String month = split[1];
        Integer idType = setBasicInput(pendapatan, sd, month, year);

        Iterator<Map.Entry<String, JsonElement>> iterator = sd.getPayment().getAsJsonObject().entrySet().iterator();

        BigDecimal totalPendapatanSementara = new BigDecimal(0.00)/*,totalGajiTunjangan = new BigDecimal("0.00")*/;
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> map = iterator.next();
            pendapatan.put(map.getKey(), map.getValue().getAsString());
            if(map.getKey().equalsIgnoreCase("returned")){
                totalPendapatanSementara = totalPendapatanSementara.subtract(map.getValue().getAsBigDecimal());
            }else{
                totalPendapatanSementara = totalPendapatanSementara.add(map.getValue().getAsBigDecimal());
            }
        }

        if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
            BigDecimal jkk = new BigDecimal("6597.78"), jkm = new BigDecimal("8247.22"), bpjs_kesehatan = new BigDecimal("137454.00");
            pendapatan.put("jkk", jkk.toString());
            pendapatan.put("jkm", jkm.toString());
            pendapatan.put("bpjs_kesehatan", bpjs_kesehatan.toString());
            totalPendapatanSementara = totalPendapatanSementara.add(jkk).add(jkm).add(bpjs_kesehatan);
        }

        pendapatan.put("bruto_pendapatan",totalPendapatanSementara.toString());

        BigDecimal totalPotongan = new BigDecimal(0.00);
        BigDecimal potonganJabatan = new BigDecimal(0.00);
        if(!pendapatan.getString("type_id").equalsIgnoreCase("49")) {
            if (insert) {
                potonganJabatan = totalPendapatanSementara.multiply(BigDecimal.valueOf(persenPotJabatan));
                if (potonganJabatan.compareTo(new BigDecimal("6000000.00")) <= 0) {
                    pendapatan.put("pot_jabatan", potonganJabatan.toString());
                } else {
                    pendapatan.put("pot_jabatan", "6000000.00");
                }
            } else {
                if(p.getPotongan_jabatan_A1_setahun() == null){
                    p.setPotongan_jabatan_A1_setahun("0.00");
                }
                if(p.getNetto_pendapatan_setahun() == null){
                    p.setNetto_pendapatan_setahun("0.00");
                }
                if(p.getSisa_ptkp() == null){
                    BigDecimal ptkpSetahun = new BigDecimal(hitungPtkp(sd.getUser().getId().toString()));
                    if(p.getPtkp_setahun() == null)
                        p.setPtkp_setahun(ptkpSetahun.toString());
                    else
                        ptkpSetahun = new BigDecimal(p.getPtkp_setahun());
                    p.setSisa_ptkp(ptkpSetahun.toString());
                    if(p.getTotal_pkp() == null)
                        p.setTotal_pkp("0.00");
                }

                BigDecimal potonganJabatanSetahun = new BigDecimal(p.getPotongan_jabatan_A1_setahun());
                BigDecimal limitPotonganJabatan = new BigDecimal("6000000.00");

                if (potonganJabatanSetahun.compareTo(limitPotonganJabatan) < 0) {
                    potonganJabatan = totalPendapatanSementara.multiply(BigDecimal.valueOf(persenPotJabatan));
                    if (potonganJabatanSetahun.add(potonganJabatan).compareTo(limitPotonganJabatan) <= 0) {
                        potonganJabatanSetahun = potonganJabatanSetahun.add(potonganJabatan);
                        pendapatan.put("pot_jabatan", potonganJabatan.toString());
                        p.setPotongan_jabatan_A1_setahun(potonganJabatanSetahun.toString());
                    } else {
                        potonganJabatan = limitPotonganJabatan.subtract(potonganJabatanSetahun);
                        potonganJabatanSetahun = potonganJabatanSetahun.add(potonganJabatan);
                        pendapatan.put("pot_jabatan", potonganJabatan.toString());
                        p.setPotongan_jabatan_A1_setahun(potonganJabatanSetahun.toString());
                    }
                } else
                    pendapatan.put("pot_jabatan", "0.00");
            }
        }else{
            pendapatan.put("pot_jabatan", "0.00");
            if(p.getPotongan_jabatan_A1_setahun() == null){
                p.setPotongan_jabatan_A1_setahun("0.00");
            }
            if(p.getNetto_pendapatan_setahun() == null){
                p.setNetto_pendapatan_setahun("0.00");
            }
            if(p.getSisa_ptkp() == null){
                BigDecimal ptkpSetahun = new BigDecimal(hitungPtkp(sd.getUser().getId().toString()));
                if(p.getPtkp_setahun() == null)
                    p.setPtkp_setahun(ptkpSetahun.toString());
                else
                    ptkpSetahun = new BigDecimal(p.getPtkp_setahun());
                p.setSisa_ptkp(ptkpSetahun.toString());
                if(p.getTotal_pkp() == null)
                    p.setTotal_pkp("0.00");
            }
        }

        pendapatan.put("pot_pensiun", "0.00");
        totalPotongan = new BigDecimal(pendapatan.get("pot_jabatan").toString())
                .add(new BigDecimal(pendapatan.get("pot_pensiun").toString()));

        BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(totalPotongan);
        BigDecimal pkpSetahun = new BigDecimal("0.00");
        BigDecimal pkp = new BigDecimal("0.00");
        if(!pendapatan.getString("type_id").equalsIgnoreCase("49")) {
            if (insert) {
                BigDecimal ptkpSetahun = new BigDecimal(hitungPtkp(sd.getUser().getId().toString()));
                BigDecimal sisaPtkpSetahun, nettoPendapatanSetahun;
                if (pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO) > 0) {
                    nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(12));
                    p.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());

                    p.setPotongan_jabatan_A2_setahun("0.00");
                    p.setPotongan_jabatan_A1_setahun(potonganJabatan.toString());

                    p.setPtkp_setahun(ptkpSetahun.toString());

                    if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                        pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                        sisaPtkpSetahun = new BigDecimal("0.00");
                        p.setSisa_ptkp(sisaPtkpSetahun.toString());
                        p.setTotal_pkp(pkpSetahun.toString());
                    } else {
                        pkpSetahun = new BigDecimal("0.00");
                        sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                        p.setSisa_ptkp(sisaPtkpSetahun.toString());
                        p.setTotal_pkp(pkpSetahun.toString());
                    }
                } else {
                    nettoPendapatanSetahun = nettoPendapatan; // apakah harusnya di tambahkan dengan nilai sebelumnya

                    p.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());

                    p.setPotongan_jabatan_A2_setahun("0.00");
                    p.setPotongan_jabatan_A1_setahun(potonganJabatan.toString());

                    p.setPtkp_setahun(ptkpSetahun.toString());

                    if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                        pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                        sisaPtkpSetahun = new BigDecimal("0.00");
                        p.setSisa_ptkp(sisaPtkpSetahun.toString());
                        p.setTotal_pkp(pkpSetahun.toString());
                    } else {
                        pkpSetahun = new BigDecimal("0.00");
                        sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                        p.setSisa_ptkp(sisaPtkpSetahun.toString());
                        p.setTotal_pkp(pkpSetahun.toString());
                    }
                }
            } else {
                if (pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO) > 0) {
                    if (listPendapatan.size() == 0) {
                        BigDecimal nettoPendapatanSetahunBefore = new BigDecimal(p.getNetto_pendapatan_setahun());
                        BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(new BigDecimal(12));
                        nettoPendapatanSetahun = nettoPendapatanSetahun.add(nettoPendapatanSetahunBefore);
                        p.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());
                    }
                } else {
                    if(p.getPotongan_jabatan_A1_setahun() == null){
                        p.setPotongan_jabatan_A1_setahun("0.00");
                    }
                    if(p.getNetto_pendapatan_setahun() == null){
                        p.setNetto_pendapatan_setahun("0.00");
                    }
                    if(p.getSisa_ptkp() == null){
                        BigDecimal ptkpSetahun = new BigDecimal(hitungPtkp(sd.getUser().getId().toString()));
                        if(p.getPtkp_setahun() == null)
                            p.setPtkp_setahun(ptkpSetahun.toString());
                        else
                            ptkpSetahun = new BigDecimal(p.getPtkp_setahun());
                        p.setSisa_ptkp(ptkpSetahun.toString());
                        if(p.getTotal_pkp() == null)
                            p.setTotal_pkp("0.00");
                    }

                    BigDecimal nettoPendapatanSetahun = new BigDecimal(p.getNetto_pendapatan_setahun());
                    nettoPendapatanSetahun = nettoPendapatanSetahun.add(nettoPendapatan);
                    p.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());
                }

                if (pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO) > 0) {

                }else {
                    BigDecimal sisa_ptkp = new BigDecimal(p.getSisa_ptkp());
                    BigDecimal pkpBonus = new BigDecimal(p.getTotal_pkp());

                    if (nettoPendapatan.compareTo(sisa_ptkp) >= 0) {
                        pkpBonus = pkpBonus.add(nettoPendapatan.subtract(sisa_ptkp));
                        pkp = nettoPendapatan.subtract(sisa_ptkp);
                        sisa_ptkp = new BigDecimal("0.00");
                        p.setSisa_ptkp(sisa_ptkp.toString());
                        p.setTotal_pkp(pkpBonus.toString());
                    } else {
                        sisa_ptkp = sisa_ptkp.subtract(nettoPendapatan);
                        pkp = new BigDecimal("0.00");
                        p.setSisa_ptkp(sisa_ptkp.toString());
                        p.setTotal_pkp(pkpBonus.toString());
                    }
                }
            }
        }

        if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
            pendapatanTetap = true;
        }else{
            pendapatanTetap = false;
        }

        TarifPajak t = new TarifPajak();

        if(insert){
            if(pendapatan.getString("type_id").equalsIgnoreCase("49")) {
                pkpSetahun = totalPendapatanSementara.multiply(new BigDecimal("0.5"));
            }

            if(p.getNpwp_simsdm()==null||p.getNpwp_simsdm().equalsIgnoreCase("")){
                if(p.getNpwp()==null || p.getNpwp().equalsIgnoreCase(""))
                    t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP, pendapatanTetap);
                else
                    t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }else{
                t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }
        }else{
            BigDecimal reminderPajak = new BigDecimal("0.00");
            Integer indexLayerPajak = 0;

            if(pendapatan.getString("type_id").equalsIgnoreCase("49")){
                pkp = totalPendapatanSementara.multiply(new BigDecimal("0.5"));
                if(p.getReminder_pajak_jasa_medis() != null) {
                    reminderPajak = new BigDecimal(p.getReminder_pajak_jasa_medis());
                    indexLayerPajak = Integer.parseInt(p.getIndex_layer_pajak_jasa_medis());
                }else{
                    reminderPajak = new BigDecimal("50000000");
                    indexLayerPajak = 0;
                }
            }else{
                if(p.getReminder_pajak() == null){
                    reminderPajak = new BigDecimal("50000000.00");
                    indexLayerPajak = 0;
                }else {
                    reminderPajak = new BigDecimal(p.getReminder_pajak());
                    indexLayerPajak = Integer.parseInt(p.getIndex_layer_pajak());
                }
            }

            if(p.getNpwp_simsdm()==null||p.getNpwp_simsdm().equalsIgnoreCase("")){
                if(p.getNpwp()==null || p.getNpwp().equalsIgnoreCase(""))
                    t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP, pendapatanTetap);
                else
                    t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }else{
                t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }
        }

        listPph21 = t.getListPph21();

        BigDecimal total_pph21_sementara = new BigDecimal(0.00);
        for(int i=0;i<listPph21.size();i++) {
            BasicDBObject obj = (BasicDBObject) listPph21.get(i);
            total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
        }

        if(p != null){
            if(p.getTotal_pph21_lebih_bayar() != null){
                BigDecimal pph21LebihBayar = new BigDecimal(p.getTotal_pph21_lebih_bayar());
                BasicDBList listPph21Rev = new BasicDBList();
                BasicDBObject pph21Rev = new BasicDBObject();
                if(pph21LebihBayar.compareTo(BigDecimal.ZERO)>0){
                    pph21Rev.put("_ket","Lebih Bayar");
                    if(total_pph21_sementara.compareTo(pph21LebihBayar) > 0){
                        total_pph21_sementara = total_pph21_sementara.subtract(pph21LebihBayar);
                        pph21LebihBayar = new BigDecimal("0.00");
                    }else{
                        pph21LebihBayar = pph21LebihBayar.subtract(total_pph21_sementara);
                        total_pph21_sementara = new BigDecimal("0.00");
                    }
                    pph21Rev.put("_hasil",total_pph21_sementara.toString());
                    p.setTotal_pph21_lebih_bayar(pph21LebihBayar.toString());
                    listPph21Rev.add(pph21Rev);
                    pendapatan.put("pph21_rev",listPph21Rev);
                }
            }
        }

        jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());

        if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
            BigDecimal jkk = new BigDecimal("6597.78"), jkm = new BigDecimal("8247.22"), bpjs_kesehatan = new BigDecimal("137454.00");
            BigDecimal nettoTakeHomePay = nettoPendapatan.subtract(jkk).subtract(jkm).subtract(bpjs_kesehatan).subtract(total_pph21_sementara);
            pendapatan.put("netto_TakeHomePay", nettoTakeHomePay.toString());
        }else {
            pendapatan.put("netto_TakeHomePay", nettoPendapatan.subtract(total_pph21_sementara).toString());
        }

        pendapatan.put("netto_pendapatan", nettoPendapatan.toString());

        pendapatan.put("pph21",listPph21);

        listPendapatan.add(pendapatan);

        if(pendapatanTetap) {
            p.setPendapatan_tetap(listPendapatan);
        }else{
            p.setPendapatan_tdk_tetap(listPendapatan);
        }

        if(pendapatan.getString("type_id").equalsIgnoreCase("49")){
            p.setReminder_pajak_jasa_medis(t.getReminderPajak().toString());
            p.setIndex_layer_pajak_jasa_medis(t.getIndex().toString());
        }else {
            p.setReminder_pajak(t.getReminderPajak().toString());
            p.setIndex_layer_pajak(t.getIndex().toString());
        }

        if(insert){
            p.setTotal_pph21_usu(total_pph21_sementara.toString());
            p.setTotal_pph21_usu_dibayar("0.00");
            p.setTotal_pph21_pribadi("0.00");
        }else{
            BigDecimal totalPph21Usu = new BigDecimal(p.getTotal_pph21_usu()).add(total_pph21_sementara);
            p.setTotal_pph21_usu(totalPph21Usu.toString());
        }

        p.setTimestamp(new Timestamp(new Date().getTime()).toString());
        datastore.save(p);

        jsonArray.add(jsonObject);
        return true;
    }

    private boolean saveDataMWA_TenagaAhli(SalaryDetail sd, boolean mwa){
        // langsung di kali tarif pasal 17
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id",sd.getId());

//        Query<UserPajak> query = datastore.createQuery(UserPajak.class)
////                .field("users.id").
//                .filter("id_user","/TA-/")
//                .filter("full_name", sd.getUser().getFull_name());
        Query<UserPajak> query = datastore.find(UserPajak.class);
        query.and(query.criteria("id_user").containsIgnoreCase("TA"),query.criteria("full_name").containsIgnoreCase(sd.getUser().getFull_name()));
        UserPajak p = query.get();

        if(p == null && mwa==true) {
            query = datastore.find(UserPajak.class);
            query.and(query.criteria("id_user").containsIgnoreCase("MWA"), query.criteria("full_name").containsIgnoreCase(sd.getUser().getFull_name()));
            p = query.get();
        }

        if(p != null){
            if(p.getPendapatan_tetap() != null) {
                for (Object object:p.getPendapatan_tetap()) {
                    BasicDBObject pendapatan = (BasicDBObject) object;
                    if(pendapatan.getString("salary_id").equalsIgnoreCase(sd.getId().toString())) {
                        logger.info("Salary_id : "+sd.getId().toString()+" telah ada pada database.");
                        return false;
                    }
                }
            }

            if(p.getPendapatan_tdk_tetap() != null){
                for (Object object:p.getPendapatan_tdk_tetap()) {
                    BasicDBObject pendapatan = (BasicDBObject) object;
                    if(pendapatan.getString("salary_id").equalsIgnoreCase(sd.getId().toString())) {
                        logger.info("Salary_id : " + sd.getId().toString() + " telah ada pada database.");
                        return false;
                    }
                }
            }
        }
        BasicDBList listPph21;
        BasicDBList listPendapatan = new BasicDBList();
        boolean insert;
        if (p == null) {
            p = new UserPajak();
            if(mwa)
                p.setId_user("MWA-"+System.currentTimeMillis());
            else
                p.setId_user("TA-"+System.currentTimeMillis());
            if(sd.getUser().getNpwp() == null) {
                p.setNpwp("");
                p.setNpwp_simsdm("");
            }else{
                p.setNpwp(sd.getUser().getNpwp());
                p.setNpwp_simsdm("");
            }
            p.setFront_degree("");
            p.setFull_name(sd.getUser().getFull_name());
            p.setBehind_degree("");
            p.setNip_simsdm("");
            insert = true;
        }else {
            if(p.getNpwp_simsdm()==null) {
                p.setNpwp_simsdm(sd.getUser().getNpwp());
            }else if (p.getNpwp_simsdm().isEmpty()) {
                p.setNpwp_simsdm(sd.getUser().getNpwp());
            }
            insert = false;
            if(p.getPendapatan_tdk_tetap() != null)
                listPendapatan = p.getPendapatan_tdk_tetap();
        }

        BasicDBObject pendapatan = new BasicDBObject();
        String date = sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("updated_time").getAsString();
        String split[] = date.split("-");
        String year = split[0];
        String month = split[1];
        Integer idType = setBasicInput(pendapatan, sd, month, year);

        Iterator<Map.Entry<String, JsonElement>> iterator = sd.getPayment().getAsJsonObject().entrySet().iterator();

        BigDecimal brutoPendapatan = new BigDecimal(0.00)/*,totalGajiTunjangan = new BigDecimal("0.00")*/;
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> map = iterator.next();
            pendapatan.put(map.getKey(), map.getValue().getAsString());
            if(map.getKey().equalsIgnoreCase("returned")){
                brutoPendapatan = brutoPendapatan.subtract(map.getValue().getAsBigDecimal());
            }else{
                brutoPendapatan = brutoPendapatan.add(map.getValue().getAsBigDecimal());
            }
        }

        pendapatan.put("bruto_pendapatan",brutoPendapatan.toString());

        BigDecimal pkp;
        if(mwa)
            pkp = brutoPendapatan;
        else
            pkp = brutoPendapatan.multiply(new BigDecimal("0.5"));


        TarifPajak t = new TarifPajak();
        if(insert) {
            if(p.getNpwp_simsdm()==null||p.getNpwp_simsdm().equalsIgnoreCase("")){
                if(p.getNpwp()==null || p.getNpwp().equalsIgnoreCase(""))
                    t.hitungPajak(new BigDecimal("50000000.00"),pkp,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP, false);
                else
                    t.hitungPajak(new BigDecimal("50000000.00"),pkp,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, false);
            }else{
                t.hitungPajak(new BigDecimal("50000000.00"),pkp,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, false);
            }
        }else {
            BigDecimal reminderPajak = new BigDecimal(p.getReminder_pajak());
            Integer indexLayerPajak = Integer.parseInt(p.getIndex_layer_pajak());
            if(p.getNpwp_simsdm()==null||p.getNpwp_simsdm().equalsIgnoreCase("")){
                if(p.getNpwp()==null || p.getNpwp().equalsIgnoreCase(""))
                    t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP, false);
                else
                    t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, false);
            }else{
                t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, false);
            }
        }

        listPph21 = t.getListPph21();

        BigDecimal total_pph21_sementara = new BigDecimal(0.00);
        for(int i=0;i<listPph21.size();i++) {
            BasicDBObject obj = (BasicDBObject) listPph21.get(i);
            total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
        }

        if(p != null){
            if(p.getTotal_pph21_lebih_bayar() != null){
                BigDecimal pph21LebihBayar = new BigDecimal(p.getTotal_pph21_lebih_bayar());
                BasicDBList listPph21Rev = new BasicDBList();
                BasicDBObject pph21Rev = new BasicDBObject();
                if(pph21LebihBayar.compareTo(BigDecimal.ZERO)>0){
                    pph21Rev.put("_ket","Lebih Bayar");
                    if(total_pph21_sementara.compareTo(pph21LebihBayar) > 0){
                        total_pph21_sementara = total_pph21_sementara.subtract(pph21LebihBayar);
                        pph21LebihBayar = new BigDecimal("0.00");
                    }else{
                        pph21LebihBayar = pph21LebihBayar.subtract(total_pph21_sementara);
                        total_pph21_sementara = pph21LebihBayar.subtract(total_pph21_sementara);
                    }
                    pph21Rev.put("_hasil",total_pph21_sementara.toString());
                    p.setTotal_pph21_lebih_bayar(pph21LebihBayar.toString());
                    listPph21Rev.add(pph21Rev);
                    pendapatan.put("pph21_rev",listPph21Rev);
                }
            }
        }



        jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());

        pendapatan.put("netto_TakeHomePay", brutoPendapatan.subtract(total_pph21_sementara).toString());

        pendapatan.put("pph21",listPph21);

        listPendapatan.add(pendapatan);

        p.setPendapatan_tdk_tetap(listPendapatan);

        p.setReminder_pajak(t.getReminderPajak().toString());
        p.setIndex_layer_pajak(t.getIndex().toString());

        if(insert){
            p.setTotal_pph21_usu(total_pph21_sementara.toString());
            p.setTotal_pph21_usu_dibayar("0.00");
            p.setTotal_pph21_pribadi("0.00");
        }else{
            BigDecimal totalPph21Usu = new BigDecimal(p.getTotal_pph21_usu()).add(total_pph21_sementara);
            p.setTotal_pph21_usu(totalPph21Usu.toString());
        }

        p.setTimestamp(new Timestamp(new Date().getTime()).toString());
        datastore.save(p);
        jsonArray.add(jsonObject);
        return  true;
    }

    private void hitungPPH21(String month,String tahun, List<SalaryDetail> listSalaryDetail ,boolean gaji){
        int countGaji = 1;
        for(SalaryDetail sd : listSalaryDetail){
            Query<UserPajak> query = null;
            UserPajak p = null;
//            System.out.println("salary_id"+sd.getId());
            if(sd.getUser().getId() == null){
                System.out.println("Null id on salary_id:"+sd.getId());
                System.out.println("Full name :"+sd.getUser().getFull_name());
            }else {
                insertUpdateData(sd.getUser().getId().toString(),sd,month,tahun,countGaji,gaji);
                countGaji++;
            }
        }
    }

    private int counts = 1;

    private void insertUpdateData(String id,SalaryDetail sd, String month, String tahun, int countGaji, boolean pendapatanTetap){
        Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user", id);
        UserPajak p = query.get();

//        long start,end;
        BasicDBList listPph21;
        if (p == null) { // insert data hanya utk gaji pokok
            UserPajak user = new UserPajak();
//            start = System.currentTimeMillis();
            user.setId_user(sd.getUser().getId().toString());
            user.setNpwp("");
            user.setNpwp_simsdm(sd.getUser().getNpwp());
            user.setFront_degree(sd.getUser().getFront_degree());
            user.setFull_name(sd.getUser().getFull_name());
            user.setBehind_degree(sd.getUser().getBehind_degree());
            user.setNip_simsdm(sd.getUser().getNip_nik().toString());

            BasicDBList listPendapatan = new BasicDBList();

            BigDecimal basicSalary = setBasicSalary(pendapatanTetap,sd);

            BasicDBObject pendapatan = new BasicDBObject();
            Integer idType = setBasicInput(pendapatan, sd, month, tahun);

            Iterator<Map.Entry<String, JsonElement>> iterator = sd.getPayment().getAsJsonObject().entrySet().iterator();

            BigDecimal totalPendapatanSementara = new BigDecimal("0.00");
            while (iterator.hasNext()) {
                Map.Entry<String, JsonElement> map = iterator.next();
                pendapatan.put(map.getKey(), map.getValue().getAsString());
                totalPendapatanSementara = totalPendapatanSementara.add(map.getValue().getAsBigDecimal());
            }

            // JKT/JKM, JKK, BPJS Kesehatan
            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                BigDecimal jkk = new BigDecimal("6597.78"), jkm = new BigDecimal("8247.22"), bpjs_kesehatan = new BigDecimal("137454.00");
                pendapatan.put("jkk", jkk.toString());
                pendapatan.put("jkm", jkm.toString());
                pendapatan.put("bpjs_kesehatan", bpjs_kesehatan.toString());
                totalPendapatanSementara = totalPendapatanSementara.add(jkk).add(jkm).add(bpjs_kesehatan);
            }

            pendapatan.put("bruto_pendapatan",totalPendapatanSementara.toString());

            BigDecimal totalPotongan = new BigDecimal("0.00");
            BigDecimal potonganJabatan = totalPendapatanSementara.multiply(BigDecimal.valueOf(persenPotJabatan));
            if (potonganJabatan.compareTo(new BigDecimal("6000000.00")) <= 0) {
                pendapatan.put("pot_jabatan", potonganJabatan.toString());
            } else {
                pendapatan.put("pot_jabatan", "6000000.00");
            }

            pendapatan.put("pot_pensiun", "0.00");
            totalPotongan = new BigDecimal(pendapatan.get("pot_jabatan").toString())
                    .add(new BigDecimal(pendapatan.get("pot_pensiun").toString()));

            BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(totalPotongan);

            BigDecimal ptkpSetahun = new BigDecimal(hitungPtkp(sd.getUser().getId().toString()));
            BigDecimal pkpSetahun, sisaPtkpSetahun,nettoPendapatanSetahun;
            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                nettoPendapatanSetahun = nettoPendapatan.multiply(BigDecimal.valueOf(12));
                user.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());

                user.setPotongan_jabatan_A2_setahun("0.00");
                user.setPotongan_jabatan_A1_setahun(potonganJabatan.toString());

                user.setPtkp_setahun(ptkpSetahun.toString());

                if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                    pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                    sisaPtkpSetahun = new BigDecimal("0.00");
                    user.setSisa_ptkp(sisaPtkpSetahun.toString());
                    user.setTotal_pkp(pkpSetahun.toString());
                } else {
                    pkpSetahun = new BigDecimal("0.00");
                    sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                    user.setSisa_ptkp(sisaPtkpSetahun.toString());
                    user.setTotal_pkp(pkpSetahun.toString());
                }
            }else{
                nettoPendapatanSetahun = nettoPendapatan;

                user.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());

                user.setPotongan_jabatan_A2_setahun("0.00");
                user.setPotongan_jabatan_A1_setahun(potonganJabatan.toString());

                user.setPtkp_setahun(ptkpSetahun.toString());

                if (nettoPendapatanSetahun.compareTo(ptkpSetahun) >= 0) {
                    pkpSetahun = nettoPendapatanSetahun.subtract(ptkpSetahun);
                    sisaPtkpSetahun = new BigDecimal("0.00");
                    user.setSisa_ptkp(sisaPtkpSetahun.toString());
                    user.setTotal_pkp(pkpSetahun.toString());
                } else {
                    pkpSetahun = new BigDecimal("0.00");
                    sisaPtkpSetahun = ptkpSetahun.subtract(nettoPendapatanSetahun);
                    user.setSisa_ptkp(sisaPtkpSetahun.toString());
                    user.setTotal_pkp(pkpSetahun.toString());
                }
            }

            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                pendapatanTetap = true;
            }else{
                pendapatanTetap = false;
            }
            TarifPajak t = new TarifPajak();
            if(user.getNpwp_simsdm()==null||user.getNpwp_simsdm().equalsIgnoreCase("")){
                if(user.getNpwp()==null || user.getNpwp().equalsIgnoreCase(""))
                    t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP, pendapatanTetap);
                else
                    t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }else{
                t.hitungPajak(new BigDecimal("50000000.00"),pkpSetahun,0,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }

            listPph21 = t.getListPph21();

            BigDecimal total_pph21_sementara = new BigDecimal(0.00);
            for(int i=0;i<listPph21.size();i++) {
                BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
            }

            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                BigDecimal jkk = new BigDecimal("6597.78"), jkm = new BigDecimal("8247.22"), bpjs_kesehatan = new BigDecimal("137454.00");
                BigDecimal nettoTakeHomePay = nettoPendapatan.subtract(jkk).subtract(jkm).subtract(bpjs_kesehatan).subtract(total_pph21_sementara);
                pendapatan.put("netto_TakeHomePay", nettoTakeHomePay.toString());
            }else
                pendapatan.put("netto_TakeHomePay", nettoPendapatan.subtract(total_pph21_sementara).toString());
            pendapatan.put("netto_pendapatan", nettoPendapatan.toString());

            pendapatan.put("pph21",listPph21);

            listPendapatan.add(pendapatan);

            if(pendapatanTetap) {
                user.setPendapatan_tetap(listPendapatan);
            }else{
                user.setPendapatan_tdk_tetap(listPendapatan);
            }

            user.setReminder_pajak(t.getReminderPajak().toString());
            user.setIndex_layer_pajak(t.getIndex().toString());
//            pendapatan.put("pph21_layer",t.getIndex().toString());
//            pendapatan.put("pph21_reminder",t.getReminderPajak().toString());
//            pendapatan.put("update_time",new Timestamp(new Date().getTime()).toString());


//            BigDecimal ptkp_sebulan = ptkpSetahun.divide(new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP);
//            pendapatan.put("ptkp_sebulan", ptkp_sebulan.toString());
//            BigDecimal pkp_sebulan = new BigDecimal("0.00"), sisa_ptkp_sebulan = new BigDecimal("0.00");
//            if (nettoPendapatan.compareTo(ptkp_sebulan) >= 0) {
//                pkp_sebulan = nettoPendapatan.subtract(ptkp_sebulan);
//                pendapatan.put("pkp_sebulan", pkp_sebulan.toString());
//                pendapatan.put("sisa_ptkp_sebulan", sisa_ptkp_sebulan.toString());
//            } else {
//                sisa_ptkp_sebulan = ptkp_sebulan.subtract(nettoPendapatan);
//                pendapatan.put("pkp_sebulan", pkp_sebulan.toString());
//                pendapatan.put("sisa_ptkp_sebulan", sisa_ptkp_sebulan.toString());
//            }


//            pendapatan.put("update_time", new Timestamp(new Date().getTime()).toString());


//            user.setPendapatan_tdk_tetap(listPendapatan);

            user.setTotal_pph21_usu_dibayar("0.00");
            user.setTotal_pph21_usu(total_pph21_sementara.toString());
            user.setTotal_pph21_pribadi("0.00");
            user.setTimestamp(new Timestamp(new Date().getTime()).toString());
            datastore.save(user);
//            end = System.currentTimeMillis();
//            System.out.println("LOG- Save to db :"+(end-start)/1000);

            System.out.println("Save : "+counts+" user_id:"+user.getId_user()+" salary_id:"+sd.getId());
            counts++;

        } else { //update data pendapatan/gaji/tunjangan dll.
            System.out.println("User Id :"+p.getId_user());
            System.out.println("Salary Id :"+sd.getId());

//            UpdateOperations<UserPajak> ops = datastore.createUpdateOperations(UserPajak.class);

            BasicDBList newArrayListPendapatan = new BasicDBList();

            BigDecimal basicSalary = setBasicSalary(pendapatanTetap,sd);

            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                if(p.getPendapatan_tetap() != null)
                    newArrayListPendapatan = p.getPendapatan_tetap();
            }else{
                if(p.getPendapatan_tdk_tetap() != null)
                    newArrayListPendapatan = p.getPendapatan_tdk_tetap();
            }

//            BasicDBObject pendapatanSebelumnya = (BasicDBObject) arrayListPendapatan.get(arrayListPendapatan.size() - 1);
            BasicDBObject pendapatan = new BasicDBObject();
            Integer idType = setBasicInput(pendapatan, sd, month, tahun);

            Iterator<Map.Entry<String, JsonElement>> iterator = sd.getPayment().getAsJsonObject().entrySet().iterator();

            BigDecimal totalPendapatanSementara = new BigDecimal(0.00)/*,totalGajiTunjangan = new BigDecimal("0.00")*/;
            while (iterator.hasNext()) {
                Map.Entry<String, JsonElement> map = iterator.next();
                pendapatan.put(map.getKey(), map.getValue().getAsString());
                totalPendapatanSementara = totalPendapatanSementara.add(map.getValue().getAsBigDecimal());
            }

//            totalGajiTunjangan = totalPendapatanSementara;
            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                BigDecimal jkk = new BigDecimal("6597.78"), jkm = new BigDecimal("8247.22"), bpjs_kesehatan = new BigDecimal("137454.00");
                pendapatan.put("jkk", jkk.toString());
                pendapatan.put("jkm", jkm.toString());
                pendapatan.put("bpjs_kesehatan", bpjs_kesehatan.toString());
                totalPendapatanSementara = totalPendapatanSementara.add(jkk).add(jkm).add(bpjs_kesehatan);
            }

            pendapatan.put("bruto_pendapatan",totalPendapatanSementara.toString());

            BigDecimal totalPotongan = new BigDecimal(0.00);
            BigDecimal potonganJabatanSetahun = new BigDecimal(p.getPotongan_jabatan_A1_setahun());
            BigDecimal limitPotonganJabatan = new BigDecimal("6000000.00");

            if (potonganJabatanSetahun.compareTo(limitPotonganJabatan) < 0) {
                BigDecimal potonganJabatan = totalPendapatanSementara.multiply(BigDecimal.valueOf(persenPotJabatan));
                if (potonganJabatanSetahun.add(potonganJabatan).compareTo(limitPotonganJabatan) <= 0) {
                    potonganJabatanSetahun = potonganJabatanSetahun.add(potonganJabatan);
                    pendapatan.put("pot_jabatan", potonganJabatan.toString());
//                    ops.set("potongan_jabatan_A1_setahun",potonganJabatanSetahun.toString());
                    p.setPotongan_jabatan_A1_setahun(potonganJabatanSetahun.toString());
                } else {
                    potonganJabatan = limitPotonganJabatan.subtract(potonganJabatanSetahun);
                    potonganJabatanSetahun = potonganJabatanSetahun.add(potonganJabatan);
                    pendapatan.put("pot_jabatan", potonganJabatan.toString());
//                    ops.set("potongan_jabatan_A1_setahun",potonganJabatanSetahun.toString());
                    p.setPotongan_jabatan_A1_setahun(potonganJabatanSetahun.toString());
                }
            } else
                pendapatan.put("pot_jabatan", "0.00");

//            if (sd.getPayment().getAsJsonObject().has("basic_salary")) {
//                BigDecimal potonganPensiun = totalGajiTunjangan.multiply(BigDecimal.valueOf(persenPotPensiun));
//                pendapatan.put("pot_pensiun", potonganPensiun.toString());
//            } else {
//                pendapatan.put("pot_pensiun", "0.00");
//            }
            pendapatan.put("pot_pensiun", "0.00");
            totalPotongan = new BigDecimal(pendapatan.get("pot_jabatan").toString())
                    .add(new BigDecimal(pendapatan.get("pot_pensiun").toString()));

            BigDecimal nettoPendapatan = totalPendapatanSementara.subtract(totalPotongan);

            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0){
                if(newArrayListPendapatan.size()==0) {
                    BigDecimal nettoPendapatanSetahunBefore = new BigDecimal(p.getNetto_pendapatan_setahun());
                    BigDecimal nettoPendapatanSetahun = nettoPendapatan.multiply(new BigDecimal(12));
                    nettoPendapatanSetahun = nettoPendapatanSetahun.add(nettoPendapatanSetahunBefore);
                    p.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());
                }
            }else{
                BigDecimal nettoPendapatanSetahun = new BigDecimal(p.getNetto_pendapatan_setahun());
                nettoPendapatanSetahun = nettoPendapatanSetahun.add(nettoPendapatan);
                p.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());
            }


            BigDecimal sisa_ptkp = new BigDecimal(p.getSisa_ptkp());
            BigDecimal pkpBonus = new BigDecimal(p.getTotal_pkp());
            BigDecimal pkp;
            if(nettoPendapatan.compareTo(sisa_ptkp) >= 0) {
                pkpBonus = pkpBonus.add(nettoPendapatan.subtract(sisa_ptkp));
                pkp = nettoPendapatan.subtract(sisa_ptkp);
                sisa_ptkp = new BigDecimal("0.00");
//                ops.set("sisa_ptkp", sisa_ptkp.toString());
                p.setSisa_ptkp(sisa_ptkp.toString());
//                ops.set("total_pkp", pkpBonus.toString());
                p.setTotal_pkp(pkpBonus.toString());
            }else{
                sisa_ptkp = sisa_ptkp.subtract(nettoPendapatan);
                pkp = new BigDecimal("0.00");
//                ops.set("sisa_ptkp", sisa_ptkp.toString());
                p.setSisa_ptkp(sisa_ptkp.toString());
//                ops.set("total_pkp", pkpBonus.toString());
                p.setTotal_pkp(pkpBonus.toString());
            }

            BigDecimal reminderPajak = new BigDecimal(p.getReminder_pajak());
            Integer indexLayerPajak = Integer.parseInt(p.getIndex_layer_pajak());


            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                pendapatanTetap = true;
            }else{
                pendapatanTetap = false;
            }
            TarifPajak t = new TarifPajak();
            if(p.getNpwp_simsdm()==null||p.getNpwp_simsdm().equalsIgnoreCase("")){
                if(p.getNpwp()==null || p.getNpwp().equalsIgnoreCase(""))
                    t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP, pendapatanTetap);
                else
                    t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }else{
                t.hitungPajak(reminderPajak,pkp,indexLayerPajak,TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP, pendapatanTetap);
            }

            listPph21 = t.getListPph21();

            BigDecimal total_pph21_sementara = new BigDecimal(0.00);
            for(int i=0;i<listPph21.size();i++) {
                BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
            }

            if(pendapatanTetap && basicSalary.compareTo(BigDecimal.ZERO)>0) {
                BigDecimal jkk = new BigDecimal("6597.78"), jkm = new BigDecimal("8247.22"), bpjs_kesehatan = new BigDecimal("137454.00");
                BigDecimal nettoTakeHomePay = nettoPendapatan.subtract(jkk).subtract(jkm).subtract(bpjs_kesehatan).subtract(total_pph21_sementara);
                pendapatan.put("netto_TakeHomePay", nettoTakeHomePay.toString());
            }else
                pendapatan.put("netto_TakeHomePay", nettoPendapatan.subtract(total_pph21_sementara).toString());
            pendapatan.put("netto_pendapatan", nettoPendapatan.toString());

            pendapatan.put("pph21",listPph21);

            newArrayListPendapatan.add(pendapatan);

            if(pendapatanTetap){
//                if (p.getPendapatan_tetap() != null)
//                    ops.push("pendapatan_tetap", newArrayListPendapatan);
//                else
//                    ops.set("pendapatan_tetap",newArrayListPendapatan);
                p.setPendapatan_tetap(newArrayListPendapatan);
            }else {
//                if (p.getPendapatan_tdk_tetap() != null)
//                    ops.push("pendapatan_tdk_tetap", newArrayListPendapatan);
//                else
//                    ops.set("pendapatan_tdk_tetap", newArrayListPendapatan);
                p.setPendapatan_tdk_tetap(newArrayListPendapatan);
            }

//            ops.set("reminder_pajak",t.getReminderPajak().toString());
            p.setReminder_pajak(t.getReminderPajak().toString());
//            ops.set("index_layer_pajak",t.getIndex().toString());
            p.setIndex_layer_pajak(t.getIndex().toString());
            BigDecimal totalPph21Usu = new BigDecimal(p.getTotal_pph21_usu()).add(total_pph21_sementara);
//            ops.set("total_pph21_usu",totalPph21Usu.toString());
            p.setTotal_pph21_usu(totalPph21Usu.toString());

//            pendapatan.put("ptkp_setahun", pendapatanSebelumnya.getString("ptkp_setahun"));
//                        pendapatan.put("ptkp_sebulan", (String) pendapatanSebelumnya.get("ptkp_sebulan"));

//            Integer bulanSebelumnya = Integer.parseInt(pendapatanSebelumnya.get("bulan").toString());
//            Integer bulanSaatIni = Integer.parseInt(month);
//            BigDecimal sisa_ptkp_sebulan_sebelumnya = new BigDecimal("0.00");
//            if(bulanSebelumnya != bulanSaatIni){
//                sisa_ptkp_sebulan_sebelumnya = new BigDecimal(pendapatanSebelumnya.get("sisa_ptkp_sebulan").toString());
//                sisa_ptkp_sebulan_sebelumnya = sisa_ptkp_sebulan_sebelumnya.add(
//                        new BigDecimal(pendapatanSebelumnya.getString("ptkp_setahun")).divide(
//                                new BigDecimal("12.00"),2,BigDecimal.ROUND_HALF_UP));
//            }else if(bulanSebelumnya == bulanSaatIni){
//                sisa_ptkp_sebulan_sebelumnya = new BigDecimal(pendapatanSebelumnya.get("sisa_ptkp_sebulan").toString());
//            }

//            BigDecimal pkp_sebulan = new BigDecimal("0.00"), sisa_ptkp_sebulan = new BigDecimal("0.00");
//            if (sisa_ptkp_sebulan_sebelumnya.compareTo(BigDecimal.ZERO) > 0) {
//                if (sisa_ptkp_sebulan_sebelumnya.compareTo(nettoPendapatan) <= 0) {
//                    // netto pendapatan - sisa ptkp sebulan
//                    pkp_sebulan = nettoPendapatan.subtract(sisa_ptkp_sebulan_sebelumnya);
//                    pendapatan.put("pkp_sebulan", pkp_sebulan.toString());
//                    pendapatan.put("sisa_ptkp_sebulan", "0.00");
//                } else {
//                    // sisa ptkp sebulan - neto pendapatan
//                    sisa_ptkp_sebulan = sisa_ptkp_sebulan_sebelumnya.subtract(nettoPendapatan);
//                    pendapatan.put("pkp_sebulan", "0.00");
//                    pendapatan.put("sisa_ptkp_sebulan", sisa_ptkp_sebulan.toString());
//                }
//            } else {
//                pkp_sebulan = nettoPendapatan;
//                pendapatan.put("pkp_sebulan", nettoPendapatan.toString());
//                pendapatan.put("sisa_ptkp_sebulan", sisa_ptkp_sebulan.toString());
//            }

//            BigDecimal pengurang = new BigDecimal(pendapatanSebelumnya.get("pph21_reminder").toString());
//            Integer index = Integer.parseInt(pendapatanSebelumnya.get("pph21_layer").toString());
//            TarifPajak tp = new TarifPajak();
//            BigDecimal pkpSetahun = pkp_sebulan.multiply(new BigDecimal("12.00"));
//            if(pengurang.compareTo(pkpSetahun) < 0){
//                if(p.getNpwp().equalsIgnoreCase("")||p.getNpwp()==null)
//                    if(p.getNpwp_simsdm().equalsIgnoreCase("")||p.getNpwp_simsdm()==null)
//                        tp.hitungPajak(pengurang, index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP);
//                    else
//                        tp.hitungPajak(pengurang, index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP);
//                else
//                    tp.hitungPajak(pengurang, index, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP);
//
//                BigDecimal tambah = pkpSetahun.subtract(pengurang);
//                index = tp.getIndex();
//                if(index > 2)
//                    index = 2;
//                if(p.getNpwp().equalsIgnoreCase("")||p.getNpwp()==null)
//                    if(p.getNpwp_simsdm().equalsIgnoreCase("")||p.getNpwp_simsdm()==null)
//                        tp.hitungPajak(tambah, index+1, TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP);
//                    else
//                        tp.hitungPajak(tambah, index+1, TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP);
//                else
//                    tp.hitungPajak(tambah, index+1, TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP);
//            }else{
//                if(p.getNpwp().equalsIgnoreCase("")||p.getNpwp()==null)
//                    if(p.getNpwp_simsdm().equalsIgnoreCase("")||p.getNpwp_simsdm()==null)
//                        tp.hitungPajak(pkpSetahun, index, TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NON_NPWP);
//                    else
//                        tp.hitungPajak(pkpSetahun, index, TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP);
//                else
//                    tp.hitungPajak(pkpSetahun, index, TarifPajak.LAYER_SETAHUN,TarifPajak.TARIF_NPWP);
//            }
//            listPph21 = tp.getListPph21();
//            pendapatan.put("pph21",listPph21);
//            pendapatan.put("pph21_layer",tp.getIndex().toString());
//            pendapatan.put("pph21_reminder",tp.getReminderPajak().toString());
//
//            BigDecimal total_pph21_sementara = new BigDecimal(0.00);
//            for(int i=0;i<listPph21.size();i++) {
//                BasicDBObject obj = (BasicDBObject) listPph21.get(i);
//                total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("hasil_sebulan")));
//            }

//            pendapatan.put("update_time", new Timestamp(new Date().getTime()).toString());

//            BigDecimal netto_pendapatan_setahun = new BigDecimal(p.getNetto_pendapatan_setahun()).add(nettoPendapatan);
//            ops.set("netto_pendapatan_setahun", netto_pendapatan_setahun.toString());
////                    p.setNetto_pendapatan_setahun(netto_pendapatan_setahun.toString());
//            if (new BigDecimal(p.getSisa_ptkp()).compareTo(BigDecimal.ZERO) > 0) {
//                if (nettoPendapatan.compareTo(new BigDecimal(p.getSisa_ptkp())) > 0) {
////                            p.setSisa_ptkp("0");
//                    ops.set("sisa_ptkp", "0.00");
//                } else {
//                    BigDecimal sisa_ptkp = new BigDecimal(p.getSisa_ptkp()).subtract(nettoPendapatan);
////                            p.setSisa_ptkp(sisa_ptkp.toString());
//                    ops.set("sisa_ptkp", sisa_ptkp.toString());
//                }
//            }
//
//            BigDecimal total_pkp = new BigDecimal(p.getTotal_pkp()).add(pkp_sebulan);
////                    p.setTotal_pkp(total_pkp.toString());
//            ops.set("total_pkp", total_pkp.toString());
//
//            BigDecimal total_pph21_usu = new BigDecimal(p.getTotal_pph21_usu()).add(total_pph21_sementara);
////                    p.setTotal_pph21_usu(Integer.toString(ExcelReader.ceilCustom(String.format("%.2f",total_pph21_usu.get()))));
//            ops.set("total_pph21_usu", total_pph21_usu.toString());

//                    p.setTimestamp(new Timestamp(new Date().getTime()).toString());
//            ops.set("timestamp", new Timestamp(new Date().getTime()).toString());
            p.setTimestamp(new Timestamp(new Date().getTime()).toString());

//            newArrayListPendapatan.add(pendapatan);
//            ops.push("pendapatan", newArrayListPendapatan);
//            datastore.update(query, ops);
            datastore.save(p);

        }
    }

    private BigDecimal setBasicSalary(boolean pendapatanTetap, SalaryDetail sd){
        if(pendapatanTetap && sd.getPayment().getAsJsonObject().has("basic_salary")){
            return new BigDecimal(sd.getPayment().getAsJsonObject().get("basic_salary").getAsString());
        }else if(pendapatanTetap && sd.getPayment().getAsJsonObject().has("p1")){
            return new BigDecimal(sd.getPayment().getAsJsonObject().get("p1").getAsString());
        }else{
            return new BigDecimal("0.00");
        }
    }

    private Integer setBasicInput(BasicDBObject pendapatan, SalaryDetail sd, String month, String year){
        pendapatan.put("activity_id",sd.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("id").getAsString());
        pendapatan.put("source_of_fund",sd.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("source_of_fund").getAsString());
        pendapatan.put("activity_title",sd.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("title").getAsString());
        pendapatan.put("request_id",sd.getPayment().getAsJsonObject().get("request").getAsJsonObject().get("id").getAsString());
        pendapatan.put("salary_id",sd.getId().toString());
        pendapatan.put("type_id",sd.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsString());
        pendapatan.put("type_title",sd.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("title").getAsString());
        pendapatan.put("unit_id",sd.getUnit().getId().toString());
        pendapatan.put("unit_name",sd.getUnit().getName());

        pendapatan.put("bulan",month);
        pendapatan.put("tahun",year);

//        if(sd.getPayment().getAsJsonObject().has("position") && sd.getPayment().getAsJsonObject().get("position") != null)
//            pendapatan.put("position",sd.getPayment().getAsJsonObject().get("position").getAsString());

        Integer idType = sd.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt();

        // coba nanti dikerjai krn masih belum tau gimana cara memisahkan numeric value dan string value
        /*Iterator<Map.Entry<String, JsonElement>> iterator = sd.getPayment().getAsJsonObject().entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, JsonElement> map = iterator.next();
            map.getValue().
        }*/
        sd.getPayment().getAsJsonObject().remove("type");
        sd.getPayment().getAsJsonObject().remove("request");
        sd.getPayment().getAsJsonObject().remove("activity");
        sd.getPayment().getAsJsonObject().remove("pph21");
        sd.getPayment().getAsJsonObject().remove("pph21_pbm");
        sd.getPayment().getAsJsonObject().remove("position");
        sd.getPayment().getAsJsonObject().remove("course");
//        sd.getPayment().getAsJsonObject().remove("returned");

        return idType;
    }

    public static String callApiUsu(String ep, String method) throws IOException {
//        String endpoint = "https://api.usu.ac.id/0.2/salary_receipts";
        URL obj = new URL(ep);
        HttpURLConnection conn= (HttpURLConnection) obj.openConnection();

        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Authorization", "Bearer "+getSSO("88062916081001","casper14").getToken());
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( true );
        conn.setDoOutput( true );
        conn.setDoInput(true);

//        DataOutputStream wr;
//        wr = new DataOutputStream(conn.getOutputStream());
//        wr.writeBytes(postData);
//        wr.flush();
//        wr.close();
        StringBuffer response = new StringBuffer();
        if(conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
//            System.out.println(inputLine);
            }
            in.close();
        }else{
            response.append("{ \"code\": 404}");
        }
        return response.toString();
    }

    public static String callApiUsu(String ep, String method, JsonArray jsonArray) throws IOException {
//        String endpoint = "https://api.usu.ac.id/0.2/salary_receipts";
        URL obj = new URL(ep);
        HttpsURLConnection conn= (HttpsURLConnection) obj.openConnection();

        conn.setRequestMethod( method );
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer "+getSSO("88062916081001","casper14").getToken());
        conn.setRequestProperty("AppSecret", "simrkausu");
        conn.setUseCaches( false );
        conn.setDoOutput( true );
        conn.setDoInput(true);

        DataOutputStream wr;
        wr = new DataOutputStream(conn.getOutputStream());
//        logger.info(jsonArray.toString());
//        System.out.println(jsonArray);
        wr.writeBytes(jsonArray.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
//            System.out.println(inputLine);
        }
        in.close();
        return response.toString();
    }

    public boolean serviceGetTax(String requestId) {
        Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
        query.or(query.criteria("pendapatan_tetap.request_id").equalIgnoreCase(requestId),
                query.criteria("pendapatan_tdk_tetap.request_id").equalIgnoreCase(requestId));
        List<UserPajak> listResult = query.asList();
        if(listResult != null){
            for(UserPajak up : listResult){
                if (up != null) {
                    if (up.getPendapatan_tdk_tetap() != null) {
                        for(Object object:up.getPendapatan_tdk_tetap()){
                            BasicDBObject pendapatanTdkTetap = (BasicDBObject) object;
                            if(pendapatanTdkTetap.getString("request_id").equalsIgnoreCase(requestId)){
                                //set salary id and total pph21
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("id",Integer.parseInt(pendapatanTdkTetap.getString("salary_id")));
                                BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                if(pendapatanTdkTetap.get("pph21_rev")!=null){
                                    BasicDBList listPph21 = (BasicDBList) pendapatanTdkTetap.get("pph21_rev");
                                    for (int i = 0; i < listPph21.size(); i++) {
                                        BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                                        total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
                                    }
                                }else {
                                    BasicDBList listPph21 = (BasicDBList) pendapatanTdkTetap.get("pph21");
                                    for (int i = 0; i < listPph21.size(); i++) {
                                        BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                                        total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
                                    }
                                }
                                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                                jsonArray.add(jsonObject);
                            }
                        }
                    }
                    if (up.getPendapatan_tetap() != null) {
                        for(Object object:up.getPendapatan_tetap()){
                            BasicDBObject pendapatanTetap = (BasicDBObject) object;
                            if(pendapatanTetap.getString("request_id").equalsIgnoreCase(requestId)){
                                //set salary id and total pph21
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("id",Integer.parseInt(pendapatanTetap.getString("salary_id")));
                                BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                if(pendapatanTetap.get("pph21_rev")!=null){
                                    BasicDBList listPph21 = (BasicDBList) pendapatanTetap.get("pph21_rev");
                                    for (int i = 0; i < listPph21.size(); i++) {
                                        BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                                        total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
                                    }
                                }else {
                                    BasicDBList listPph21 = (BasicDBList) pendapatanTetap.get("pph21");
                                    for (int i = 0; i < listPph21.size(); i++) {
                                        BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                                        total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
                                    }
                                }
                                jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                                jsonArray.add(jsonObject);
                            }
                        }
                    }
                } else {
                    System.out.println("Request id bermasalah : " + requestId);
                }
            }
            return true;
        }else
            return false;

    }

    public void addSourceOfFund() throws IOException {
        RequestInitial resp = null;
        try {
            resp = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/requests?type=2&status=1","GET"), RequestInitial.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestInitial finalResp = resp;
        int countRequest = 1;
        for(Request req : finalResp.getResponse().getRequests()){
            System.out.println("Request ID : "+req.getId());
            Salary salary = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?request_id="+req.getId(), "GET"), Salary.class);
            for(SalaryDetail sd : salary.getResponse().getSalary_receivers()){
                String salaryId = sd.getId().toString();
                Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
                query.or(
                        query.criteria("pendapatan_tetap.salary_id").equalIgnoreCase(salaryId),
                        query.criteria("pendapatan_tdk_tetap.salary_id").equalIgnoreCase(salaryId)
                );
                UserPajak up = query.get();
                if(up != null) {
                    if (up.getPendapatan_tetap() != null) {
                        for (int i = 0; i < up.getPendapatan_tetap().size(); i++) {
                            BasicDBObject obj = (BasicDBObject) up.getPendapatan_tetap().get(i);
                            if (obj.getString("salary_id").equalsIgnoreCase(salaryId)) {
                                String sourceOfFund = sd.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("source_of_fund").getAsString();
                                obj.put("source_of_fund", sourceOfFund);
                            }
                        }
                    }

                    if (up.getPendapatan_tdk_tetap() != null) {
                        for (int i = 0; i < up.getPendapatan_tdk_tetap().size(); i++) {
                            BasicDBObject obj = (BasicDBObject) up.getPendapatan_tdk_tetap().get(i);
                            if (obj.getString("salary_id").equalsIgnoreCase(salaryId)) {
                                String sourceOfFund = sd.getPayment().getAsJsonObject().get("activity").getAsJsonObject().get("source_of_fund").getAsString();
                                obj.put("source_of_fund", sourceOfFund);
                            }
                        }
                    }

                    datastore.save(up);
                }else{
                    System.out.println("Request ID yang gak jelas : "+req.getId());
                    System.out.println("Salary ID yang gak jelas : "+salaryId);
                }
            }
            countRequest++;
        }
        System.out.println("Total Request : "+countRequest);
    }

    public void hitungUlangPajak(){
        RequestInitial resp = null;
        try {
            resp = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/requests?type=2&status=1","GET"), RequestInitial.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*if(resp != null) {
            List<Request> listRequest = resp.getResponse().getRequests();
            for(Request req : listRequest) {

            }
        }*/
        List<Integer> listRequestHapus = new ArrayList<>();
        List<Integer> listRequestTolak = new ArrayList<>();
        Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
//        query.or(query.criteria("pendatapan_tdk_tetap.request_id").notIn(resp.getResponse().getRequests()),
//                query.criteria("pendapatan_tetap_request_id").notIn(resp.getResponse().getRequests()));
//        query.or(query.criteria("pendatapan_tdk_tetap.returned").greaterThan("0"),
//                query.criteria("pendapatan_tetap_returned").greaterThan("0"));
//        query.field("total_pph21_kurang_bayar").greaterThan("0.00");
//                query.field("pendapatan_tetap.returned").greaterThan("0");
//        .field("pendapatan_tdk_tetap").notEqual(null);
//        query.criteria("pendapatan_tdk_tetap.returned").equalIgnoreCase("0");
        RequestInitial finalResp = resp;
        List<UserPajak> list = query.asList().stream().filter(up -> {
            long countPT = 0, countPTT = 0;

            if(up.getPendapatan_tetap() != null) {
                BasicDBList listPT = up.getPendapatan_tetap().stream().filter(pt -> {
                    BasicDBObject ptObject = (BasicDBObject) pt;
                    boolean result = false;
                    for (Request req : finalResp.getResponse().getRequests()) {
                        if (req.getId().toString().equalsIgnoreCase(ptObject.getString("request_id"))) {
                            result = true;
                            break;
                        } else if (ptObject.getString("request_id").equalsIgnoreCase("apbn")) {
                            result = true;
                        }
                    }

                    if (result)
                        return false;
                    else {
                        Integer requestId = Integer.parseInt(ptObject.getString("request_id"));
//                        try {
//                            CekResponse cekResponse = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?request_id="+requestId,"GET"),CekResponse.class);

//                            if(cekResponse.getCode() == 200) {
                                if(!listRequestTolak.contains(requestId))
                                    listRequestTolak.add(requestId);
//                            }else {
//                                if(!listRequestHapus.contains(requestId))
//                                    listRequestHapus.add(requestId);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        return true;
                    }
                }).collect(Collectors.toCollection(BasicDBList::new));

                up.setPendapatan_tetap(listPT);
                countPT = listPT.size();
            }

            if(up.getPendapatan_tdk_tetap() != null) {
                BasicDBList listPTT = up.getPendapatan_tdk_tetap().stream().filter(ptt -> {
                    BasicDBObject pttObject = (BasicDBObject) ptt;
                    boolean result = false;
                    for (Request req : finalResp.getResponse().getRequests()) {
                        if (req.getId().toString().equalsIgnoreCase(pttObject.getString("request_id"))) {
                            result = true;
                            break;
                        }
                    }

                    if (result)
                        return false;
                    else{
                        Integer requestId = Integer.parseInt(pttObject.getString("request_id"));
//                        try {
//                            CekResponse cekResponse = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/salary_receipts?request_id="+requestId,"GET"),CekResponse.class);
//
//                            if(cekResponse.getCode() == 200) {
                                if(!listRequestTolak.contains(requestId))
                                    listRequestTolak.add(requestId);
//                            }else {
//                                if(!listRequestHapus.contains(requestId))
//                                    listRequestHapus.add(requestId);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                        return true;
                    }
                }).collect(Collectors.toCollection(BasicDBList::new));

                up.setPendapatan_tdk_tetap(listPTT);
                countPTT = listPTT.size();
            }

            if(countPT > 0 || countPTT > 0)
                return true;
            else
                return false;
        }).collect(Collectors.toList());

//        for(Integer id : listRequestHapus){
//            deleteSalary(id.toString());
//        }

        for(Integer id : listRequestTolak){
            deleteSalaryByRequest(id.toString());
        }
        /*double totalPkpDouble = list.get(4).getPendapatan_tdk_tetap().stream().filter(c -> {
            BasicDBObject filteredObj = (BasicDBObject) c;
            if (filteredObj.getString("request_id").equalsIgnoreCase("2397")) {
                return false;
            } else {
                return true;
            }
        }).mapToDouble(m -> {
            BasicDBObject map = (BasicDBObject) m;
            return new BigDecimal(map.getString("bruto_pendapatan")).doubleValue();
        }).sum();
        BigDecimal totalPkp = new BigDecimal(totalPkpDouble);*/
//        Iterator iterator = list.iterator();
//        while(iterator.hasNext()){
//            UserPajak userPajak = (UserPajak) iterator.next();
//            System.out.println("Nama : "+userPajak.getFull_name()+" Lebih Bayar : "+userPajak.getTotal_pph21_lebih_bayar());
//        }
        System.out.println(list.size());


    }

    public void deleteSalaryByRequest(String requestId){
        deleteSalaryByRequest(requestId,"request_id");
    }

    public void deleteSalaryByRequest(String requestId, String criteria){
        Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
        query.or(
                query.criteria("pendapatan_tetap."+criteria).equalIgnoreCase(requestId),
                query.criteria("pendapatan_tdk_tetap."+criteria).equalIgnoreCase(requestId)
        );
        List<UserPajak> listResult = query.asList();
        if(listResult != null){
            Iterator iterator = listResult.iterator();
            while(iterator.hasNext()){
                UserPajak userPajak = (UserPajak) iterator.next();
                if(userPajak.getPendapatan_tdk_tetap() != null){
                    BasicDBList listTdkTetap = userPajak.getPendapatan_tdk_tetap();

                    Iterator iteratorTdkTetap = listTdkTetap.iterator();
                    int count = 0;
                    while(iteratorTdkTetap.hasNext()){
                        BasicDBObject obj = (BasicDBObject) iteratorTdkTetap.next();
                        if(obj.getString(criteria).equalsIgnoreCase(requestId)){
                            /**
                             * 1. Hitung pendapatan yang mau dihapus di kurangkan netto setahun Dan Kawan Kawan.
                             * 2. Hitung ulang pendapatan setelahnya jika ada dan save data yg lama ke field baru
                             * 3. Hitung selisih apakah menjadi kurang bayar atau lebih bayar
                             * 4.
                             */

                            if(!userPajak.getId_user().contains("-")) {
                                BigDecimal nettoPendapatanSetahun = new BigDecimal(userPajak.getNetto_pendapatan_setahun());
                                BigDecimal potonganJabatanA1Setahun = new BigDecimal(userPajak.getPotongan_jabatan_A1_setahun());
                                BigDecimal sisaPtkp = new BigDecimal(userPajak.getSisa_ptkp());
                                BigDecimal totalPkp = new BigDecimal(userPajak.getTotal_pkp());
                                BigDecimal reminderPajak = new BigDecimal(userPajak.getReminder_pajak());
                                Integer indexLayerPajak = Integer.parseInt(userPajak.getIndex_layer_pajak());
                                BigDecimal totalPph21Usu = new BigDecimal(userPajak.getTotal_pph21_usu());

                                BigDecimal potonganJabatanHapus;
                                potonganJabatanHapus = new BigDecimal(obj.getString("pot_jabatan"));

                                for (int j = count; j < listTdkTetap.size(); j++) {
                                    BasicDBObject nextObj = (BasicDBObject) listTdkTetap.get(j);
                                    if(!nextObj.getString("type_id").equalsIgnoreCase("49")) {
                                        if (nextObj.getBoolean("batal") == false) {
//                                BigDecimal nettoPendapatan = new BigDecimal(nextObj.getString("netto_pendapatan"));
                                            BigDecimal nettoPendapatanHapus;
                                            if (nextObj.getString("netto_pendapatan") != null)
                                                nettoPendapatanHapus = new BigDecimal(nextObj.getString("netto_pendapatan"));
                                            else
                                                nettoPendapatanHapus = new BigDecimal(nextObj.getString("bruto_pendapatan"));

                                            nettoPendapatanSetahun = nettoPendapatanSetahun.subtract(nettoPendapatanHapus);

                                            BigDecimal pph21Hapus = new BigDecimal("0.00");
                                            BasicDBList listPph21 = (BasicDBList) nextObj.get("pph21");
                                            for (Object objPph21 : listPph21) {
                                                BasicDBObject basicDBObjectPph21 = (BasicDBObject) objPph21;
                                                pph21Hapus = pph21Hapus.add(new BigDecimal(basicDBObjectPph21.getString("_hasil")));
                                            }

                                            totalPph21Usu = totalPph21Usu.subtract(pph21Hapus);

                                            if (sisaPtkp.compareTo(BigDecimal.ZERO) == 0) {
                                                if (totalPkp.subtract(nettoPendapatanHapus).compareTo(BigDecimal.ZERO) <= 0) {
                                                    sisaPtkp = nettoPendapatanHapus.subtract(totalPkp);
                                                    totalPkp = new BigDecimal("0.00");
                                                } else {
                                                    totalPkp = totalPkp.subtract(nettoPendapatanHapus);
                                                    if (indexLayerPajak == 0) {
                                                        if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("50000000")) >= 0) {
                                                            reminderPajak = new BigDecimal("50000000").subtract(totalPkp);
                                                        } else {
                                                            reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                        }
                                                    } else if (indexLayerPajak == 1) {
                                                        if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("200000000")) > 0) {
                                                            reminderPajak = reminderPajak.add(nettoPendapatanHapus).subtract(new BigDecimal("200000000"));
                                                            indexLayerPajak = indexLayerPajak - 1;
                                                        } else {
                                                            reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                        }
                                                    } else if (indexLayerPajak == 2) {
                                                        if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("250000000")) > 0) {
                                                            reminderPajak = reminderPajak.add(nettoPendapatanHapus).subtract(new BigDecimal("250000000"));
                                                            indexLayerPajak = indexLayerPajak - 1;
                                                        } else {
                                                            reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                        }
                                                    } else {
                                                        if (totalPkp.compareTo(new BigDecimal("500000000")) < 0) {
                                                            reminderPajak = new BigDecimal("500000000").subtract(totalPkp);
                                                            indexLayerPajak = indexLayerPajak - 1;
                                                        } else {

                                                        }
                                                    }
                                                }
                                            } else {
                                                sisaPtkp = sisaPtkp.add(nettoPendapatanHapus);
                                            }
                                        }
                                    }else{ // hitung jika jasa medis yg dibatalkan

                                    }
                                }

                                potonganJabatanA1Setahun = potonganJabatanA1Setahun.subtract(potonganJabatanHapus);

                                for (int i = count + 1; i < listTdkTetap.size(); i++) {
                                    BasicDBObject objPajak = (BasicDBObject) listTdkTetap.get(i);
                                    if(!objPajak.getString("type_id").equalsIgnoreCase("49")) {
                                        if (objPajak.getBoolean("batal") == false) {
                                            BigDecimal nettoPendapatan = new BigDecimal(objPajak.getString("netto_pendapatan"));
                                            nettoPendapatanSetahun = nettoPendapatanSetahun.add(nettoPendapatan);
                                            BigDecimal pkp = new BigDecimal("0.00");
                                            if (nettoPendapatan.compareTo(sisaPtkp) >= 0) {
                                                totalPkp = totalPkp.add(nettoPendapatan.subtract(sisaPtkp));
                                                pkp = nettoPendapatan.subtract(sisaPtkp);
                                                sisaPtkp = new BigDecimal("0.00");
                                                userPajak.setSisa_ptkp(sisaPtkp.toString());
                                                userPajak.setTotal_pkp(totalPkp.toString());
                                            } else {
                                                sisaPtkp = sisaPtkp.subtract(nettoPendapatan);
                                                pkp = new BigDecimal("0.00");
                                                userPajak.setSisa_ptkp(sisaPtkp.toString());
                                                userPajak.setTotal_pkp(totalPkp.toString());
                                            }

                                            TarifPajak t = new TarifPajak();
                                            if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                                                if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                                    t.hitungPajak(reminderPajak, pkp, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                                                else
                                                    t.hitungPajak(reminderPajak, pkp, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                                            } else {
                                                t.hitungPajak(reminderPajak, pkp, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                                            }

                                            BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                            for (int j = 0; j < t.getListPph21().size(); j++) {
                                                BasicDBObject objz = (BasicDBObject) t.getListPph21().get(j);
                                                total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(objz.getString("_hasil")));
                                            }

                                            totalPph21Usu = totalPph21Usu.add(total_pph21_sementara);

                                            objPajak.put("pph21_rev", t.getListPph21());
                                            objPajak.put("batal", false);
                                            reminderPajak = t.getReminderPajak();
                                            indexLayerPajak = t.getIndex();

                                            userPajak.setReminder_pajak(reminderPajak.toString());
                                            userPajak.setIndex_layer_pajak(indexLayerPajak.toString());
//                                        userPajak.setTotal_pph21_usu(totalPph21Usu.toString());
                                        }
                                    }else{ // hitung jika jasa medis yg dibatalkan

                                    }
                                }

                                userPajak.setNetto_pendapatan_setahun(nettoPendapatanSetahun.toString());
                                userPajak.setPotongan_jabatan_A1_setahun(potonganJabatanA1Setahun.toString());

                                BigDecimal hasilPajak = new BigDecimal("0.00");
                                BigDecimal hasilPajakRev = new BigDecimal("0.00");
                                for (int i = 0; i < listTdkTetap.size(); i++) {
                                    BasicDBObject o = (BasicDBObject) listTdkTetap.get(i);
                                    if(!o.getString("type_id").equalsIgnoreCase("49")) {
                                        if (o.containsField("pph21_rev") && o.getBoolean("batal") == false) {
                                            BasicDBList pajak = (BasicDBList) o.get("pph21");
                                            Iterator iteratorPajak = pajak.iterator();
                                            while (iteratorPajak.hasNext()) {
                                                BasicDBObject hasil = (BasicDBObject) iteratorPajak.next();
                                                hasilPajak = hasilPajak.add(new BigDecimal(hasil.getString("_hasil")));
                                            }

                                            pajak = (BasicDBList) o.get("pph21_rev");
                                            iteratorPajak = pajak.iterator();
                                            while (iteratorPajak.hasNext()) {
                                                BasicDBObject hasil = (BasicDBObject) iteratorPajak.next();
                                                hasilPajakRev = hasilPajakRev.add(new BigDecimal(hasil.getString("_hasil")));
                                            }
                                        }
                                    }else{ // hitung jika jasa medis yg dibatalkan

                                    }
                                }

                                if(userPajak.getPendapatan_batal() != null ) {
                                    Double pph21Batal = userPajak.getPendapatan_batal().stream().mapToDouble(c -> {
                                        BasicDBObject pph21Obj = (BasicDBObject) c;
                                        BasicDBList listPph21 = (BasicDBList) pph21Obj.get("pph21");
                                        return listPph21.stream().mapToDouble(d -> {
                                            BasicDBObject hasil = (BasicDBObject) d;
                                            return Double.valueOf(hasil.getString("_hasil"));
                                        }).sum();
                                    }).sum();

                                    hasilPajak = hasilPajak.add(new BigDecimal(pph21Batal).setScale(3,RoundingMode.CEILING));
                                }

                                if (hasilPajak.compareTo(hasilPajakRev) > 0) {
                                    BigDecimal selisih = hasilPajak.subtract(hasilPajakRev);
                                    userPajak.setTotal_pph21_lebih_bayar(selisih.toString());
                                    userPajak.setTotal_pph21_kurang_bayar("0.00");
                                } else if (hasilPajak.compareTo(hasilPajakRev) < 0) {
                                    BigDecimal selisih = hasilPajakRev.subtract(hasilPajak);
                                    userPajak.setTotal_pph21_lebih_bayar("0.00");
                                    userPajak.setTotal_pph21_kurang_bayar(selisih.toString());
                                } else {
                                    userPajak.setTotal_pph21_lebih_bayar("0.00");
                                    userPajak.setTotal_pph21_kurang_bayar("0.00");
                                }

                                if (userPajak.getPendapatan_batal() != null) {
                                    BasicDBList listPenBatal = userPajak.getPendapatan_batal();
                                    listPenBatal.add(obj);
                                    userPajak.setPendapatan_batal(listPenBatal);
                                } else {
                                    BasicDBList listBatal = new BasicDBList();
                                    listBatal.add(obj);
                                    userPajak.setPendapatan_batal(listBatal);
                                }

                                iteratorTdkTetap.remove();

                                datastore.save(userPajak);
                            }else{
                                BigDecimal reminderPajak = new BigDecimal(userPajak.getReminder_pajak());
                                Integer indexLayerPajak = Integer.parseInt(userPajak.getIndex_layer_pajak());
                                BigDecimal totalPph21Usu = new BigDecimal(userPajak.getTotal_pph21_usu());

                                if(userPajak.getId_user().contains("MWA-")){
                                    BasicDBList listBruto = (BasicDBList) listTdkTetap.copy();
                                    double totalPkpDouble = listBruto.stream().filter(c -> {
                                        BasicDBObject filteredObj = (BasicDBObject) c;
                                        if (filteredObj.getString("request_id").equalsIgnoreCase(requestId)) {
                                            return false;
                                        } else {
                                            return true;
                                        }
                                    }).mapToDouble(m -> {
                                        BasicDBObject map = (BasicDBObject) m;
                                        return new BigDecimal(map.getString("bruto_pendapatan")).doubleValue();
                                    }).sum();

                                    BigDecimal totalPkp = new BigDecimal(totalPkpDouble);

                                    for (int j = count; j < listTdkTetap.size(); j++) {
                                        BasicDBObject nextObj = (BasicDBObject) listTdkTetap.get(j);
                                        if (nextObj.getBoolean("batal") == false) {

                                            BigDecimal nettoPendapatanHapus = new BigDecimal(nextObj.getString("bruto_pendapatan"));

                                            BigDecimal pph21Hapus = new BigDecimal("0.00");
                                            BasicDBList listPph21 = (BasicDBList) nextObj.get("pph21");
                                            for (Object objPph21 : listPph21) {
                                                BasicDBObject basicDBObjectPph21 = (BasicDBObject) objPph21;
                                                pph21Hapus = pph21Hapus.add(new BigDecimal(basicDBObjectPph21.getString("_hasil")));
                                            }

                                            totalPph21Usu = totalPph21Usu.subtract(pph21Hapus);

                                            if (indexLayerPajak == 0) {
//                                                if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("50000000")) >= 0) {
//                                                    reminderPajak = new BigDecimal("50000000").subtract(totalPkp);
//                                                } else {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus);
//                                                }
                                            } else if (indexLayerPajak == 1) {
                                                if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("200000000")) > 0) {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus).subtract(new BigDecimal("200000000"));
                                                    indexLayerPajak = indexLayerPajak - 1;
                                                } else {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                }
                                            } else if (indexLayerPajak == 2) {
                                                if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("250000000")) > 0) {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus).subtract(new BigDecimal("250000000"));
                                                    indexLayerPajak = indexLayerPajak - 1;
                                                } else {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                }
                                            } else {
                                                if (totalPkp.compareTo(new BigDecimal("500000000")) < 0) {
                                                    reminderPajak = new BigDecimal("500000000").subtract(totalPkp);
                                                    indexLayerPajak = indexLayerPajak - 1;
                                                } else {

                                                }
                                            }
                                        }
                                    }

                                    for (int i = count + 1; i < listTdkTetap.size(); i++) {
                                        BasicDBObject objPajak = (BasicDBObject) listTdkTetap.get(i);
                                        if (objPajak.getBoolean("batal") == false) {
                                            BigDecimal nettoPendapatan = new BigDecimal(objPajak.getString("bruto_pendapatan"));

                                            TarifPajak t = new TarifPajak();
                                            if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                                                if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                                    t.hitungPajak(reminderPajak, nettoPendapatan, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                                                else
                                                    t.hitungPajak(reminderPajak, nettoPendapatan, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                                            } else {
                                                t.hitungPajak(reminderPajak, nettoPendapatan, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                                            }

                                            BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                            for (int j = 0; j < t.getListPph21().size(); j++) {
                                                BasicDBObject objz = (BasicDBObject) t.getListPph21().get(j);
                                                total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(objz.getString("_hasil")));
                                            }

                                            totalPph21Usu = totalPph21Usu.add(total_pph21_sementara);

                                            objPajak.put("pph21_rev", t.getListPph21());
                                            objPajak.put("batal", false);
                                            reminderPajak = t.getReminderPajak();
                                            indexLayerPajak = t.getIndex();

                                            userPajak.setReminder_pajak(reminderPajak.toString());
                                            userPajak.setIndex_layer_pajak(indexLayerPajak.toString());
//                                            userPajak.setTotal_pph21_usu(totalPph21Usu.toString());
                                        }
                                    }

                                    BigDecimal hasilPajak = new BigDecimal("0.00");
                                    BigDecimal hasilPajakRev = new BigDecimal("0.00");
                                    for (int i = 0; i < listTdkTetap.size(); i++) {
                                        BasicDBObject o = (BasicDBObject) listTdkTetap.get(i);
                                        if (o.containsField("pph21_rev") && o.getBoolean("batal") == false) {
                                            BasicDBList pajak = (BasicDBList) o.get("pph21");
                                            Iterator iteratorPajak = pajak.iterator();
                                            while (iteratorPajak.hasNext()) {
                                                BasicDBObject hasil = (BasicDBObject) iteratorPajak.next();
                                                hasilPajak = hasilPajak.add(new BigDecimal(hasil.getString("_hasil")));
                                            }

                                            pajak = (BasicDBList) o.get("pph21_rev");
                                            iteratorPajak = pajak.iterator();
                                            while (iteratorPajak.hasNext()) {
                                                BasicDBObject hasil = (BasicDBObject) iteratorPajak.next();
                                                hasilPajakRev = hasilPajakRev.add(new BigDecimal(hasil.getString("_hasil")));
                                            }
                                        }
                                    }

                                    if(userPajak.getPendapatan_batal() != null ) {
                                        Double pph21Batal = userPajak.getPendapatan_batal().stream().mapToDouble(c -> {
                                            BasicDBObject pph21Obj = (BasicDBObject) c;
                                            BasicDBList listPph21 = (BasicDBList) pph21Obj.get("pph21");
                                            return listPph21.stream().mapToDouble(d -> {
                                                BasicDBObject hasil = (BasicDBObject) d;
                                                return Double.valueOf(hasil.getString("_hasil"));
                                            }).sum();
                                        }).sum();

                                        hasilPajak = hasilPajak.add(new BigDecimal(pph21Batal).setScale(3,RoundingMode.CEILING));
                                    }

                                    if (hasilPajak.compareTo(hasilPajakRev) > 0) {
                                        BigDecimal selisih = hasilPajak.subtract(hasilPajakRev);
                                        userPajak.setTotal_pph21_lebih_bayar(selisih.toString());
                                        userPajak.setTotal_pph21_kurang_bayar("0.00");
                                    } else if (hasilPajak.compareTo(hasilPajakRev) < 0) {
                                        BigDecimal selisih = hasilPajakRev.subtract(hasilPajak);
                                        userPajak.setTotal_pph21_lebih_bayar("0.00");
                                        userPajak.setTotal_pph21_kurang_bayar(selisih.toString());
                                    } else {
                                        userPajak.setTotal_pph21_lebih_bayar("0.00");
                                        userPajak.setTotal_pph21_kurang_bayar("0.00");
                                    }

                                    if (userPajak.getPendapatan_batal() != null) {
                                        BasicDBList listPenBatal = userPajak.getPendapatan_batal();
                                        listPenBatal.add(obj);
                                        userPajak.setPendapatan_batal(listPenBatal);
                                    } else {
                                        BasicDBList listBatal = new BasicDBList();
                                        listBatal.add(obj);
                                        userPajak.setPendapatan_batal(listBatal);
                                    }

                                    iteratorTdkTetap.remove();

                                    datastore.save(userPajak);

                                }else{
                                    BasicDBList listBruto = (BasicDBList) listTdkTetap.copy();
                                    double totalPkpDouble = listBruto.stream().filter(c -> {
                                        BasicDBObject filteredObj = (BasicDBObject) c;
                                        if (filteredObj.getString("request_id").equalsIgnoreCase(requestId)) {
                                            return false;
                                        } else {
                                            return true;
                                        }
                                    }).mapToDouble(m -> {
                                        BasicDBObject map = (BasicDBObject) m;
                                        if(map.getString("unit_id").equalsIgnoreCase("1"))
                                            return new BigDecimal(map.getString("bruto_pendapatan")).doubleValue();
                                        else
                                            return new BigDecimal(map.getString("bruto_pendapatan")).divide(new BigDecimal("2")).doubleValue();
                                    }).sum();

                                    BigDecimal totalPkp = new BigDecimal(totalPkpDouble);

                                    for (int j = count; j < listTdkTetap.size(); j++) {
                                        BasicDBObject nextObj = (BasicDBObject) listTdkTetap.get(j);
                                        if (nextObj.getBoolean("batal") == false) {
                                            BigDecimal nettoPendapatanHapus;

                                            if(nextObj.getString("unit_id").equalsIgnoreCase("1"))
                                                nettoPendapatanHapus = new BigDecimal(nextObj.getString("bruto_pendapatan"));
                                            else
                                                nettoPendapatanHapus = new BigDecimal(nextObj.getString("bruto_pendapatan")).divide(new BigDecimal("2"));

                                            BigDecimal pph21Hapus = new BigDecimal("0.00");
                                            BasicDBList listPph21 = (BasicDBList) nextObj.get("pph21");
                                            for (Object objPph21 : listPph21) {
                                                BasicDBObject basicDBObjectPph21 = (BasicDBObject) objPph21;
                                                pph21Hapus = pph21Hapus.add(new BigDecimal(basicDBObjectPph21.getString("_hasil")));
                                            }

                                            totalPph21Usu = totalPph21Usu.subtract(pph21Hapus);

                                            if (indexLayerPajak == 0) {
//                                                if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("50000000")) >= 0) {
//                                                    reminderPajak = new BigDecimal("50000000").subtract(totalPkp);
//                                                } else {
                                                reminderPajak = reminderPajak.add(nettoPendapatanHapus);
//                                                }
                                            } else if (indexLayerPajak == 1) {
                                                if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("200000000")) > 0) {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus).subtract(new BigDecimal("200000000"));
                                                    indexLayerPajak = indexLayerPajak - 1;
                                                } else {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                }
                                            } else if (indexLayerPajak == 2) {
                                                if (reminderPajak.add(nettoPendapatanHapus).compareTo(new BigDecimal("250000000")) > 0) {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus).subtract(new BigDecimal("250000000"));
                                                    indexLayerPajak = indexLayerPajak - 1;
                                                } else {
                                                    reminderPajak = reminderPajak.add(nettoPendapatanHapus);
                                                }
                                            } else {
                                                if (totalPkp.compareTo(new BigDecimal("500000000")) < 0) {
                                                    reminderPajak = new BigDecimal("500000000").subtract(totalPkp);
                                                    indexLayerPajak = indexLayerPajak - 1;
                                                } else {

                                                }
                                            }
                                        }
                                    }

                                    for (int i = count + 1; i < listTdkTetap.size(); i++) {
                                        BasicDBObject objPajak = (BasicDBObject) listTdkTetap.get(i);
                                        if (objPajak.getBoolean("batal") == false) {
                                            BigDecimal nettoPendapatan;

                                            if(objPajak.getString("unit_id").equalsIgnoreCase("1"))
                                                nettoPendapatan = new BigDecimal(objPajak.getString("bruto_pendapatan"));
                                            else
                                                nettoPendapatan = new BigDecimal(objPajak.getString("bruto_pendapatan")).divide(new BigDecimal("2"));

                                            TarifPajak t = new TarifPajak();
                                            if (userPajak.getNpwp_simsdm() == null || userPajak.getNpwp_simsdm().equalsIgnoreCase("")) {
                                                if (userPajak.getNpwp() == null || userPajak.getNpwp().equalsIgnoreCase(""))
                                                    t.hitungPajak(reminderPajak, nettoPendapatan, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NON_NPWP, false);
                                                else
                                                    t.hitungPajak(reminderPajak, nettoPendapatan, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                                            } else {
                                                t.hitungPajak(reminderPajak, nettoPendapatan, indexLayerPajak, TarifPajak.LAYER_SETAHUN, TarifPajak.TARIF_NPWP, false);
                                            }

                                            BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                            for (int j = 0; j < t.getListPph21().size(); j++) {
                                                BasicDBObject objz = (BasicDBObject) t.getListPph21().get(j);
                                                total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(objz.getString("_hasil")));
                                            }

                                            totalPph21Usu = totalPph21Usu.add(total_pph21_sementara);

                                            objPajak.put("pph21_rev", t.getListPph21());
                                            objPajak.put("batal", false);
                                            reminderPajak = t.getReminderPajak();
                                            indexLayerPajak = t.getIndex();

                                            userPajak.setReminder_pajak(reminderPajak.toString());
                                            userPajak.setIndex_layer_pajak(indexLayerPajak.toString());
//                                            userPajak.setTotal_pph21_usu(totalPph21Usu.toString());
                                        }
                                    }

                                    BigDecimal hasilPajak = new BigDecimal("0.00");
                                    BigDecimal hasilPajakRev = new BigDecimal("0.00");
                                    for (int i = 0; i < listTdkTetap.size(); i++) {
                                        BasicDBObject o = (BasicDBObject) listTdkTetap.get(i);
                                        if (o.containsField("pph21_rev") && o.getBoolean("batal") == false) {
                                            BasicDBList pajak = (BasicDBList) o.get("pph21");
                                            Iterator iteratorPajak = pajak.iterator();
                                            while (iteratorPajak.hasNext()) {
                                                BasicDBObject hasil = (BasicDBObject) iteratorPajak.next();
                                                hasilPajak = hasilPajak.add(new BigDecimal(hasil.getString("_hasil")));
                                            }

                                            pajak = (BasicDBList) o.get("pph21_rev");
                                            iteratorPajak = pajak.iterator();
                                            while (iteratorPajak.hasNext()) {
                                                BasicDBObject hasil = (BasicDBObject) iteratorPajak.next();
                                                hasilPajakRev = hasilPajakRev.add(new BigDecimal(hasil.getString("_hasil")));
                                            }
                                        }
                                    }

                                    if(userPajak.getPendapatan_batal() != null ) {
                                        Double pph21Batal = userPajak.getPendapatan_batal().stream().mapToDouble(c -> {
                                            BasicDBObject pph21Obj = (BasicDBObject) c;
                                            BasicDBList listPph21 = (BasicDBList) pph21Obj.get("pph21");
                                            return listPph21.stream().mapToDouble(d -> {
                                                BasicDBObject hasil = (BasicDBObject) d;
                                                return new BigDecimal(hasil.getString("_hasil")).doubleValue();
                                            }).sum();
                                        }).sum();

                                        hasilPajak = hasilPajak.add(new BigDecimal(pph21Batal).round(new MathContext(3, RoundingMode.HALF_UP)));
                                    }

                                    if (hasilPajak.compareTo(hasilPajakRev) > 0) {
                                        BigDecimal selisih = hasilPajak.subtract(hasilPajakRev);
                                        userPajak.setTotal_pph21_lebih_bayar(selisih.toString());
                                        userPajak.setTotal_pph21_kurang_bayar("0.00");
                                    } else if (hasilPajak.compareTo(hasilPajakRev) < 0) {
                                        BigDecimal selisih = hasilPajakRev.subtract(hasilPajak);
                                        userPajak.setTotal_pph21_lebih_bayar("0.00");
                                        userPajak.setTotal_pph21_kurang_bayar(selisih.toString());
                                    } else {
                                        userPajak.setTotal_pph21_lebih_bayar("0.00");
                                        userPajak.setTotal_pph21_kurang_bayar("0.00");
                                    }

                                    if (userPajak.getPendapatan_batal() != null) {
                                        BasicDBList listPenBatal = userPajak.getPendapatan_batal();
                                        listPenBatal.add(obj);
                                        userPajak.setPendapatan_batal(listPenBatal);
                                    } else {
                                        BasicDBList listBatal = new BasicDBList();
                                        listBatal.add(obj);
                                        userPajak.setPendapatan_batal(listBatal);
                                    }

                                    iteratorTdkTetap.remove();

                                    datastore.save(userPajak);
                                }

                            }
                        }
                        count++;
                    }
                }
            }
        }
    }

    public void deleteSalaryById(String salaryId){
        deleteSalaryByRequest(salaryId,"salary_id");
    }

    public void moveUserId(String oldUserId, String newUserId) {
        /**
         * Get Data lama
         * Ambil :
         *         pendapatan tetap,
         *         pendapatan tidak tetap,
         *         pendapatan batal,
         *         netto pendapatan setahun,
         *         potongan jabatan A2 setahun,
         *         potongan jabatan A1 setahun,
         *         ptkp setahun,
         *         sisa ptkp,
         *         total pkp,
         *         reminder pajak,
         *         index layer pajak,
         *         total pph 21 lebih bayar,
         *         total pph21 kurang bayar
         * Tambahkan total pph21 usu,
         */
        Query<UserPajak> query = datastore.find(UserPajak.class).filter("id_user", oldUserId);
        UserPajak oldData = query.get();
        BasicDBList pendapatan_tetap = null,pendapatan_tdk_tetap=null,pendapatan_batal=null;
        String netto_pendapatan_setahun = "",potongan_jabatan_A2_setahun="",potongan_jabatan_A1_setahun=""
                ,ptkp_setahun="",sisa_ptkp="",total_pkp="",reminder_pajak="",index_layer_pajak="",
                total_pph21_lebih_bayar="",total_pph21_kurang_bayar="";
        BigDecimal total_pph21_usu = new BigDecimal("0.00");
        if (oldData.getPendapatan_tetap() != null)
            pendapatan_tetap = oldData.getPendapatan_tetap();
        /*if (oldData.getPendapatan_tdk_tetap() != null)
            pendapatan_tdk_tetap = oldData.getPendapatan_tdk_tetap();*/
        if (oldData.getPendapatan_batal() != null)
            pendapatan_batal = oldData.getPendapatan_batal();
        netto_pendapatan_setahun = oldData.getNetto_pendapatan_setahun();
        potongan_jabatan_A2_setahun = oldData.getPotongan_jabatan_A2_setahun();
        potongan_jabatan_A1_setahun = oldData.getPotongan_jabatan_A1_setahun();
        ptkp_setahun = oldData.getPtkp_setahun();
        sisa_ptkp = oldData.getSisa_ptkp();
        total_pkp = oldData.getTotal_pkp();
        reminder_pajak = oldData.getReminder_pajak();
        index_layer_pajak = oldData.getIndex_layer_pajak();
        total_pph21_lebih_bayar = oldData.getTotal_pph21_lebih_bayar();
        total_pph21_kurang_bayar = oldData.getTotal_pph21_kurang_bayar();

        total_pph21_usu = new BigDecimal(oldData.getTotal_pph21_usu());

        query = datastore.find(UserPajak.class).filter("id_user",newUserId);
        UserPajak newData = query.get();
        newData.setPendapatan_tetap(pendapatan_tetap);
//        newData.setPendapatan_tdk_tetap(pendapatan_tdk_tetap);
        newData.setPendapatan_batal(pendapatan_batal);
        newData.setNetto_pendapatan_setahun(netto_pendapatan_setahun);
        newData.setPotongan_jabatan_A1_setahun(potongan_jabatan_A1_setahun);
        newData.setPotongan_jabatan_A2_setahun(potongan_jabatan_A2_setahun);
        newData.setPtkp_setahun(ptkp_setahun);
        newData.setSisa_ptkp(sisa_ptkp);
        newData.setTotal_pkp(total_pkp);
        newData.setReminder_pajak(reminder_pajak);
        newData.setIndex_layer_pajak(index_layer_pajak);
        newData.setTotal_pph21_usu(new BigDecimal(newData.getTotal_pph21_usu()).add(total_pph21_usu).toString());
        newData.setTotal_pph21_lebih_bayar(total_pph21_lebih_bayar);
        newData.setTotal_pph21_kurang_bayar(total_pph21_kurang_bayar);
        datastore.save(newData);
    }

    public void movePendapatan(String targetUserId, String t_salaryId, String destUserId){
        Query<UserPajak> queryTarget = datastore.find(UserPajak.class).filter("id_user",targetUserId);
        UserPajak target = queryTarget.get();

        Query<UserPajak> queryDest = datastore.find(UserPajak.class).filter("id_user", destUserId);
        UserPajak dest = queryDest.get();

        BasicDBList targetListPendapatan = target.getPendapatan_tdk_tetap().stream().filter(r -> {
            BasicDBObject b = (BasicDBObject) r;
            if (b.getString("salary_id").equalsIgnoreCase(t_salaryId))
                return true;
            else
                return false;
        }).collect(Collectors.toCollection(BasicDBList::new));

        dest.getPendapatan_tdk_tetap().add(targetListPendapatan.get(0));
        datastore.save(dest);
    }

    public void checkNpwp() throws IOException {
        Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
        query.and(query.criteria("id_user").not().containsIgnoreCase("-"));
        List<UserPajak> list = query.asList();
        for(UserPajak u : list){
            if(u.getNpwp_simsdm() == null){
                System.out.println("#User ID:"+u.getId_user()+" NPWP:"+u.getNpwp_simsdm());
                u.setNpwp(getNpwp(u.getId_user()));
                datastore.save(u);
            }else {
                System.out.println("#User ID:"+u.getId_user()+" NPWP:"+u.getNpwp_simsdm());
                if (u.getNpwp_simsdm().isEmpty()) {
                    u.setNpwp(getNpwp(u.getId_user()));
                    datastore.save(u);
                }
            }
        }
    }

    private String getNpwp(String userId) throws IOException {
        String endpoint = "https://api.usu.ac.id/0.1/users/"+userId+"/ptkp";
        Response response = new Gson().fromJson(callApiUsu(endpoint,"GET"),Response.class);
        UserSimSdm uss = response.getResponse().get(0);
        System.out.println("User ID:"+userId+" NPWP:"+uss.getNpwp());
        return  uss.getNpwp();
    }
}

class CekResponse{
    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

class Response{
    private List<UserSimSdm> response;

    public List<UserSimSdm> getResponse() {
        return response;
    }
}

class Token{
    private String token;

    public String getToken() {
        return token;
    }

}
