package usu.pajak.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
//import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.Morphia;
//import org.mongodb.morphia.query.UpdateOperations;
import usu.pajak.model.UserPajak;
import usu.pajak.services.ApiRka;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class TestMain {
//    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://fariz:Laru36Dema@clusterasetmongo-shard-00-00-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-01-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-02-t3kc1.mongodb.net:27017/test?ssl=true&replicaSet=ClusterAsetMongo-shard-0&authSource=admin&retryWrites=true")); //connect to mongodb
//private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://fariz:Laru36Dema@172.30.100.75:27017/pajak?authSource=admin")); //connect to mongodb
//    private static Datastore datastore = new Morphia().createDatastore(client, "pajak");

    public static void main(String[] args) throws IOException{
//        final Query<UserPajak> query = datastore.createQuery(UserPajak.class).filter("id_user","832");
//        BasicDBList testLagi = new BasicDBList();
//        BasicDBObject obj = new BasicDBObject();
//        obj.put("halo","come on");
//        testLagi.add(obj);
//        UpdateOperations ops = datastore
//                .createUpdateOperations(UserPajak.class)
//                .push("pendapatan", testLagi);
//        datastore.update(query, (UpdateOperations<Query<UserPajak>>) ops);
//        UserPajak up = new UserPajak();
//        up.setId_user("0");
//        datastore.save(up);
//        new PnsApbn();

        /*BigDecimal x = new BigDecimal("150");
        BigDecimal y = new BigDecimal("200");

        if(x.subtract(y).compareTo(BigDecimal.ZERO) <= 0){
            System.out.println("Did it");
        }else
            System.out.println("No, you didn't");*/
        /*String [] name_unit = new String[]{"Fakultas Teknik","Farmasi","Keperawatan","Fakultas Kedokteran","FASILKOMTI","FEB"};
        String[] work_unit = new String[]{"17","24","26","14","44","18"};
        for(int i=0;i<work_unit.length;i++) {
            String result = callApi("https://api.usu.ac.id/0.1/users?unit_id=" + work_unit[i] + "&type_id=0,2", "GET", true);
            Result fromJson = new Gson().fromJson(result,Result.class);
            Iterator iterator = fromJson.getData().iterator();
            while(iterator.hasNext()){
                JsonObject obj = (JsonObject) iterator.next();
                if(obj.get("status").getAsString().equalsIgnoreCase("Tidak Aktif"))
                    iterator.remove();
            }
            System.out.println(name_unit[i]+" Jumlah Dosen : "+fromJson.getData().size());
        }*/
        Logger logger = Logger.getLogger("ApiPajak");
//        new ApiRka(logger).hitungUlangPajak();
//        new ApiRka(logger).addSourceOfFund();
//        new ApiRka(logger).moveUserId("5450","5887");
        new ApiRka(logger).checkNpwp();

//        new ApiRka(logger).deleteSalary("3531");
//        new ApiRka(logger).deleteSalary("3514");
    }

    private static String callApi(String ep, String method, boolean ssl) throws IOException {
        URL obj = new URL(ep);

        if(ssl) {
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
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
        }else {
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
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
    /*private MongoClient client = new MongoClient(new MongoClientURI("mongodb://fariz:Laru36Dema@clusterasetmongo-shard-00-00-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-01-t3kc1.mongodb.net:27017,clusterasetmongo-shard-00-02-t3kc1.mongodb.net:27017/test?ssl=true&replicaSet=ClusterAsetMongo-shard-0&authSource=admin&retryWrites=true")); //connect to mongodb
    private Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019");

    public TestMain(){
        List<UserPajak> listResult = datastore.createQuery(UserPajak.class).disableValidation()
                .filter("pendapatan.pph21.hasil", ".000").asList();
        UpdateOperations<UserPajak> ops = datastore.createUpdateOperations(UserPajak.class);
        for(UserPajak up : listResult){
            BasicDBList list = up.getPendapatan_tdk_tetap();
            for(Object o:list){
                BasicDBObject test = (BasicDBObject) o;
                ops.set("hasil","0.000");
            }
        }
    }*/
}

class Result{
    private List<Object> data;

    public List<Object> getData() {
        return data;
    }
}
