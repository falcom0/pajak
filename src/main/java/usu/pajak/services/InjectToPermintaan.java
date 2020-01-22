package usu.pajak.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import usu.pajak.model.UserPajak;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static usu.pajak.services.ApiRka.callApiUsu;

public class InjectToPermintaan {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_rev")); //connect to mongodb
    private static Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_rev");

    public static void main(String[] args){
        RequestInitial resp = null;
        try {
            resp = new Gson().fromJson(callApiUsu("https://api.usu.ac.id/0.2/requests?month=3&type=2&status=1","GET"), RequestInitial.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(resp != null){
            List<Request> listRequest = resp.getResponse().getRequests();

            for(Request req : listRequest) {
                JsonArray jsonArray = new JsonArray();

                Query<UserPajak> query = datastore.createQuery(UserPajak.class).disableValidation()
                        .filter("pendapatan_tdk_tetap.request_id", req.getId().toString());
//                        .filter("pendapatan_tetap.request_id", req.getId().toString());
                List<UserPajak> p = query.asList();

                for(UserPajak up : p){
                    if (up != null) {
                        if (up.getPendapatan_tdk_tetap() != null) {
                            for(Object object:up.getPendapatan_tdk_tetap()){
                                BasicDBObject pendapatanTdkTetap = (BasicDBObject) object;
                                if(pendapatanTdkTetap.getString("request_id").equalsIgnoreCase(req.getId().toString())){
                                    //set salary id and total pph21
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.addProperty("id",Integer.parseInt(pendapatanTdkTetap.getString("salary_id")));
                                    BasicDBList listPph21 = (BasicDBList) pendapatanTdkTetap.get("pph21");
                                    BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                    for(int i=0;i<listPph21.size();i++) {
                                        BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                                        total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
                                    }
                                    jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                                    jsonArray.add(jsonObject);
                                }
                            }
                        }
                    } else {
                        System.out.println("Request id bermasalah : " + req.getId().toString());
                    }
                }

                query = datastore.createQuery(UserPajak.class).disableValidation()
//                        .filter("pendapatan_tdk_tetap.request_id", req.getId().toString());
                        .filter("pendapatan_tetap.request_id", req.getId().toString());
                p = query.asList();

                for(UserPajak up : p){
                    if (up != null) {
                        if (up.getPendapatan_tetap() != null) {
                            for(Object object:up.getPendapatan_tetap()){
                                BasicDBObject pendapatanTetap = (BasicDBObject) object;
                                if(pendapatanTetap.getString("request_id").equalsIgnoreCase(req.getId().toString())){
                                    //set salary id and total pph21
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.addProperty("id",Integer.parseInt(pendapatanTetap.getString("salary_id")));
                                    BasicDBList listPph21 = (BasicDBList) pendapatanTetap.get("pph21");
                                    BigDecimal total_pph21_sementara = new BigDecimal(0.00);
                                    for(int i=0;i<listPph21.size();i++) {
                                        BasicDBObject obj = (BasicDBObject) listPph21.get(i);
                                        total_pph21_sementara = total_pph21_sementara.add(new BigDecimal(obj.getString("_hasil")));
                                    }
                                    jsonObject.addProperty("pph21",total_pph21_sementara.toBigInteger());
                                    jsonArray.add(jsonObject);
                                }
                            }
                        }
                    } else {
                        System.out.println("Request id bermasalah : " + req.getId().toString());
                    }
                }

                //update ke api put
                System.out.println("Request id : "+req.getId());
                System.out.println(jsonArray.toString());
                /*try {
                    callApiUsu(
                            "https://api.usu.ac.id/0.2/salary_receipts", "PUT", jsonArray);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }
}

class RequestInitial{
    private int code;
    private String status;
    private ResponseApi response;

    public ResponseApi getResponse() {
        return response;
    }
}

class ResponseApi{
    private List<Request> requests;

    public List<Request> getRequests() {
        return requests;
    }
}

class Request{
    private Integer id;

    public Integer getId() {
        return id;
    }
}