package usu.pajak.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
//import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.Morphia;
//import org.mongodb.morphia.query.Query;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.Query;
import usu.pajak.model.Group;
import usu.pajak.model.Salary;
import usu.pajak.model.SalaryDetail;
import usu.pajak.model.UserPajak;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CheckMasalahPajakUnbalance {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_rev")); //connect to mongodb
    private List<BasicDBObject> listR = new ArrayList<>();
    private List<SalaryDetail> listL = new ArrayList<>();
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_rev");
    private List<BasicDBObject> list = new ArrayList<>();
    private List<BasicDBObject> dbPph21 = new ArrayList<>();

    public static void main(String[] args) {
        try {
            new CheckMasalahPajakUnbalance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CheckMasalahPajakUnbalance() throws IOException {
        // ambil data dari database berdasarkan bulan dan unit yg terkait dan source of fund NON-PNBP
        /* Unit ID
        Aset : 13
        Kedokteran : 14
         */
        Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
        ApiRka apiRka = new ApiRka();
//        query.and(
//                query.or(query.and(
//                query.criteria("pendapatan_tetap.bulan").containsIgnoreCase(Integer.toString(3)),
//                query.criteria("pendapatan_tetap.source_of_fund").containsIgnoreCase("NON"),
//                query.criteria("pendapatan_tetap.unit_id").containsIgnoreCase("13")),
//                query.and(query.criteria("pendapatan_tdk_tetap.bulan").containsIgnoreCase(Integer.toString(3)),
//                query.criteria("pendapatan_tdk_tetap.unit_id").containsIgnoreCase("13"),
//                query.criteria("pendapatan_tdk_tetap.source_of_fund").containsIgnoreCase("NON")
//                )),
//                query.criteria("id_user").not().containsIgnoreCase("-"));
        query.criteria("id_user").not().containsIgnoreCase("-");
        List<UserPajak> listResult = query.asList();
        getUserFromDatabase(listResult,apiRka);
        String unitId = "27";
        String bulan = "4";
        fromDatabase(listResult,unitId,String.format("%02d",Integer.parseInt(bulan)));
        System.out.println(listResult);
        BigDecimal totalT = list.stream().map(t -> {
            BasicDBObject s = (BasicDBObject) t;
            BasicDBList l = (BasicDBList) s.get("pph21");
            return l.stream().map(e -> {
                BasicDBObject o = (BasicDBObject) e;
                return new BigDecimal(o.getString("_hasil"));
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(totalT.toString());

        BigDecimal totalV = list.stream().map(t -> {
            BasicDBObject s = (BasicDBObject) t;
            BasicDBList l = new BasicDBList();
            if(s.containsField("pph21_rev"))
                l = (BasicDBList) s.get("pph21_rev");
            else
                l = (BasicDBList) s.get("pph21");
            return l.stream().map(e -> {
                BasicDBObject o = (BasicDBObject) e;
                return new BigDecimal(o.getString("_hasil"));
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(totalV.toString());
        // ambil data dari api berdasarkan bulan dan manager id dan source of fund NON-PNBP
        /* Manager ID
        - Aset : 231
        - Kedokteran : 202
        - LIDA :
        - LPPM :
         */

        Salary salary = new Gson().fromJson(
                apiRka.callApiUsu(
                        "https://api.usu.ac.id/0.2/salary_receipts?status=1&unit_id="+unitId+"&source_of_fund=NON-PNBP&month="+bulan+"&year=2019", "GET")
                , Salary.class);
//        salary.getResponse().getSalary_receivers().get(0)
//        salary.getResponse().getSalary_receivers().stream().
        Integer total = salary.getResponse().getSalary_receivers().stream().mapToInt(s -> s.getPayment().getAsJsonObject().get("pph21").getAsInt()).sum();
        System.out.println("Total "+total);
        if(list.size()>=salary.getResponse().getSalary_receivers().size()) {
            listR = list.stream().filter(f -> {
                String salaryId = f.getString("salary_id");
                boolean res = salary.getResponse().getSalary_receivers().stream().anyMatch(m -> {
                    if(m.getId().toString().equalsIgnoreCase(salaryId)){
                        BasicDBObject o = new BasicDBObject();
                        o.put("salary_id",salaryId);
                        BasicDBList bd = (BasicDBList) f.get("pph21");
                        BigDecimal p1 = bd.stream().map(t -> {
                            BasicDBObject s = (BasicDBObject) t;
                            return new BigDecimal(s.getString("_hasil"));
                        }).reduce(BigDecimal.ZERO, BigDecimal::add);
                        o.put("pph21",p1.toString());
                        if(f.containsField("pph21_rev")){
                            BasicDBList ls = (BasicDBList) f.get("pph21_rev");
                            BigDecimal p2 = ls.stream().map(t -> {
                                BasicDBObject s = (BasicDBObject) t;
                                return new BigDecimal(s.getString("_hasil"));
                            }).reduce(BigDecimal.ZERO, BigDecimal::add);
                            o.put("pph21_rev",p2.toString());
                        }
                        BigDecimal apiPph = new BigDecimal(m.getPayment().getAsJsonObject().get("pph21").getAsInt());
                        o.put("api_pph21",apiPph.toString());
                        if(p1.toBigInteger().compareTo(apiPph.toBigInteger())==0){
                            return false;
                        }else{
                            dbPph21.add(o);
                            return true;
                        }
                    }else{
                        return false;
                    }
                });
                return res;
            }).collect(Collectors.toList());
            list.stream().filter(i -> Collections.frequency(list, i) > 1)
                    .collect(Collectors.toSet()).forEach(System.out::println);
        }else {
            listL = salary.getResponse().getSalary_receivers().stream().filter(l -> {
                return !list.stream().anyMatch(m -> m.getString("salary_id").equalsIgnoreCase(l.getId().toString()));
            }).collect(Collectors.toList());

        }
//        listR.forEach(e -> {
//            try {
//                apiRka.callApiUsu("http://localhost:8253/delete-salary?salary_id="+e.getString("salary_id"),"GET");
////                URL obj = new URL("http://localhost:8253/delete-salary?salary_id="+e.getString("salary_id"));
////                HttpURLConnection conn= (HttpURLConnection) obj.openConnection();
////
////                conn.setRequestMethod( "GET" );
//////                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//////                conn.setRequestProperty("Authorization", "Bearer "+getSSO("88062916081001","casper14").getToken());
//////                conn.setRequestProperty("AppSecret", "simrkausu");
////                conn.setUseCaches( false );
////                conn.setDoOutput( true );
////                conn.setDoInput(true);
////                conn.connect();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        });
        System.out.println(salary);
    }

    private void fromDatabase(List<UserPajak> listResult, String unitId, String bulan){
        listResult.stream().forEach(s -> {
            if(s.getPendapatan_tetap()!=null) {
                BasicDBList test = s.getPendapatan_tetap().stream().filter(a -> {
                    BasicDBObject bdo = (BasicDBObject) a;
                    if (bdo.containsField("source_of_fund")) {
                        if (bdo.getString("source_of_fund").equalsIgnoreCase("NON-PNBP") &&
                                bdo.getString("unit_id").equalsIgnoreCase(unitId) &&
                                bdo.getString("bulan").equalsIgnoreCase(bulan) ) {
                            list.add(bdo);
                            return true;
                        }else
                            return false;
                    } else
                        return false;
                }).collect(Collectors.toCollection(BasicDBList::new));
                s.setPendapatan_tetap(test);
            }
            if(s.getPendapatan_tdk_tetap()!=null){
                BasicDBList test = s.getPendapatan_tdk_tetap().stream().filter(a -> {
                    BasicDBObject bdo = (BasicDBObject) a;
                    if (bdo.containsField("source_of_fund")) {
                        if (bdo.getString("source_of_fund").equalsIgnoreCase("NON-PNBP") &&
                                bdo.getString("unit_id").equalsIgnoreCase(unitId) &&
                                bdo.getString("bulan").equalsIgnoreCase(bulan)
                        ) {
                            list.add(bdo);
                            return true;
                        } else {
                            return false;
                        }
                    } else
                        return false;
                }).collect(Collectors.toCollection(BasicDBList::new));
                s.setPendapatan_tdk_tetap(test);
            }
        });
        listResult = listResult.stream().filter(l -> {
            if(l.getPendapatan_tdk_tetap()!=null ) {
                if(l.getPendapatan_tdk_tetap().size() > 0)
                    return true;
                else
                    return false;
            }else if(l.getPendapatan_tetap()!=null){
                if(l.getPendapatan_tetap().size() > 0)
                    return true;
                else
                    return false;
            }else
                return false;
        }).collect(Collectors.toList());
    }

    private void getUserFromDatabase(List<UserPajak> listResult, ApiRka apiRka){
        List<Group> group = new ArrayList<>();
        listResult.forEach(e -> {
            try {
                Salary salary = new Gson().fromJson(
                        apiRka.callApiUsu(
                                "https://api.usu.ac.id/0.2/salary_receipts?status=1&user_id="+e.getId_user(), "GET")
                        , Salary.class);
                if(!(salary.getResponse() == null)) {
                    List<SalaryDetail> listGajiTdkTetap = new ArrayList<>();
                    List<SalaryDetail> listSd = salary.getResponse().getSalary_receivers();
                    List<SalaryDetail> listGajiTetap = listSd.stream().filter(
                            c -> c.getPayment().getAsJsonObject().has("basic_salary")
                    ).collect(Collectors.toList());
                    if (listGajiTetap.size() ==
                            0){
                        listGajiTdkTetap = listSd.stream().filter(
                                c -> c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23
                        ).collect(Collectors.toList());
                    }
                    List<SalaryDetail> listHonor = listSd.stream()
                            .filter(c -> !c.getPayment().getAsJsonObject().has("basic_salary"))
                            .filter(c -> !(c.getPayment().getAsJsonObject().get("type").getAsJsonObject().get("id").getAsInt()==23))
                            .collect(Collectors.toList());
                    final BigDecimal[] totalPendapatan = {BigDecimal.ZERO};
                    final BigInteger[] totalPph21 = {BigInteger.ZERO};
                    listSd.forEach(f -> {
                        if(!group.stream().anyMatch(s -> s.getId().compareTo(f.getUser().getGroup().getId())==0)) {
                            group.add(f.getUser().getGroup());
                        }
                        f.getPayment().getAsJsonObject().remove("type");
                        f.getPayment().getAsJsonObject().remove("request");
                        f.getPayment().getAsJsonObject().remove("activity");
                        f.getPayment().getAsJsonObject().remove("position");
                        f.getPayment().getAsJsonObject().remove("course");
                        f.getPayment().getAsJsonObject().remove("pph21_pbm");
                        Iterator<Map.Entry<String, JsonElement>> t = f.getPayment().getAsJsonObject().entrySet().iterator();
//                        f.getUser().getGroup().getId()
                        while(t.hasNext()){
                            Map.Entry<String, JsonElement> map = t.next();
                            if(map.getKey().equalsIgnoreCase("pph21") && !map.getValue().isJsonNull()){
                                totalPph21[0] = totalPph21[0].add(map.getValue().getAsBigInteger());
                            }else if(map.getKey().equalsIgnoreCase("returned")){
                                totalPendapatan[0] = totalPendapatan[0].subtract(map.getValue().getAsBigDecimal());
                            }else if(!map.getValue().isJsonNull()){
                                totalPendapatan[0] = totalPendapatan[0].add(map.getValue().getAsBigDecimal());
                            }
                        }

                    });
//                    System.out.println(listSd.size());
//                    System.out.println(listGajiTetap.size());
//                    System.out.println(listGajiTdkTetap.size());
//                    System.out.println(listHonor.size());
                }else{
                    System.out.println("Id User yg tidak ada di API:"+e.getId_user());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        System.out.println("Test");
    }
}

