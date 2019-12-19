package usu.pajak.util;

import com.google.gson.Gson;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
//import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.Morphia;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import usu.pajak.model.UserPajak;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CheckingSalahPtkp {
//    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_rev")); //connect to mongodb
//    private static Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_rev");
    public static void main(String arg[]){
        MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_rev"));
        Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_rev");
        List<UserPajak> listBenar = datastore.createQuery(UserPajak.class).asList();

        /*int count = 0;
        for(UserPajak p : listBenar){
            BasicDBList listTetap = p.getPendapatan_tetap();
            if(listTetap != null) {
                for (int i = 0; i < listTetap.size(); i++) {
                    BasicDBObject objTetap = (BasicDBObject) listTetap.get(i);
                    if (!objTetap.getString("activity_id").equalsIgnoreCase("apbn")) {
                        count++;
                    }
                }
            }

            if(p.getPendapatan_tdk_tetap() != null)
                count += p.getPendapatan_tdk_tetap().size();
        }

        System.out.println(count);*/
        client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_salah"));
        datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_salah");
        List<UserPajak> listSalah = datastore.createQuery(UserPajak.class).asList();

        int count = 1;
        for(int i = 0; i < listSalah.size(); i++){
            UserPajak userPajakSalah = listSalah.get(i);
            List<UserPajak> dataYgDiUji = listBenar.stream().filter(c -> c.getId_user().equalsIgnoreCase(userPajakSalah.getId_user())).collect(Collectors.toList());
            if(dataYgDiUji!=null)
                if(dataYgDiUji.size()>0){
                    UserPajak userPajakBenar = dataYgDiUji.get(0);
                    BigDecimal pajakSalah,pajakBenar;
                    pajakSalah = new BigDecimal(userPajakSalah.getTotal_pph21_usu()).subtract(new BigDecimal(userPajakSalah.getTotal_pph21_usu_dibayar()));
                    pajakBenar = new BigDecimal(userPajakBenar.getTotal_pph21_usu()).subtract(new BigDecimal(userPajakBenar.getTotal_pph21_usu_dibayar()));
                    if(pajakSalah.compareTo(pajakBenar) == 0){

                    }else{
                        System.out.println(count+". Pajak Tidak Sama user_id : "+userPajakSalah.getId_user());
                        System.out.println(count+". full_name : "+userPajakSalah.getFull_name());
                        count++;
                        String jsonSalah = new Gson().toJson(userPajakSalah);
                        String jsonBenar = new Gson().toJson(userPajakBenar);
                        System.out.println(jsonSalah);
                        System.out.println(jsonBenar);
                        System.out.println("");
                        System.out.println("");
                    }
                }else{
//                    System.out.println("Tidak ditemukan user_id : "+userPajakSalah.getId_user());
                }
            else{
//                System.out.println("Tidak ditemukan user_id : "+userPajakSalah.getId_user());
            }

        }
    }
}
