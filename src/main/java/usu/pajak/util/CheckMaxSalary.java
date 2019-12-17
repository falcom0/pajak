package usu.pajak.util;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import usu.pajak.model.UserPajak;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CheckMaxSalary {

    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/pajak_2019_rev")); //connect to mongodb
    private static Datastore datastore = new Morphia().mapPackage("usu.pajak.model.UserPajak").createDatastore(client, "pajak_2019_rev");
    public static void main(String[] args) {
        Query<UserPajak> query = datastore.find(UserPajak.class).disableValidation();
        query.and(query.criteria("pendapatan_tdk_tetap.type_title").containsIgnoreCase("Adhoc"),query.criteria("id_user").not().containsIgnoreCase("-"));
        List<UserPajak> userPajakList = query.asList();
        List<UserHighIncome> list = new ArrayList<>();
        for (UserPajak u : userPajakList
             ) {
            UserHighIncome o = new UserHighIncome();
            o.setId_user(Integer.parseInt(u.getId_user()));
            o.setName(u.getFront_degree()+" "+u.getFull_name()+" "+u.getBehind_degree());
            BigDecimal adhoc = u.getPendapatan_tdk_tetap().stream().filter(f -> {
                BasicDBObject l = (BasicDBObject) f;
                if(l.get("type_title").toString().equalsIgnoreCase("Adhoc")){
                    return true;
                }else{
                    return false;
                }
            }).map(n -> {
                BasicDBObject b = (BasicDBObject) n;
                return new BigDecimal(b.getString("netto_TakeHomePay"));
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
            o.setTotal_adhoc(adhoc.doubleValue());
            o.setTotal(adhoc.toString());
            list.add(o);
        }

        List<UserHighIncome> result = list.stream().sorted(Comparator.comparingDouble(UserHighIncome::getTotal_adhoc).reversed()).collect(Collectors.toList());
        System.out.println(result);
    }

    static class UserHighIncome{
        private int id_user;
        private String name;
        private Double total_adhoc;
        private String total;

        public void setId_user(int id_user) {
            this.id_user = id_user;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTotal_adhoc(Double total_adhoc) {
            this.total_adhoc = total_adhoc;
        }

        public Double getTotal_adhoc() {
            return total_adhoc;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }
}
