package usu.pajak.temp;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.bson.types.ObjectId;
//import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.Morphia;
import usu.pajak.model.Group;
import usu.pajak.model.Pendapatan;
import usu.pajak.model.UserUsu;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class TestPajakNew {
    private static MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017/new_pajak_usu")); //connect to mongodb
    private static Datastore dsUser = new Morphia().mapPackage("usu.pajak.model.UserUsu").createDatastore(client, "new_pajak_usu");
    private static Datastore dsPendapatan = new Morphia().mapPackage("usu.pajak.model.Pendapatan").createDatastore(client, "new_pajak_usu");

    public static void main(String[] args) {
        UserUsu userUsu = new UserUsu();
        userUsu.set_id(new ObjectId());
        userUsu.setUser_id(3984);
        userUsu.setGroup(new Group(5,"Honorer"));
        userUsu.setPtkp_setahun(new BigDecimal("54000000.00"));
        userUsu.setCreated_at(new Timestamp(new Date().getTime()));
        userUsu.setUpdated_at(new Timestamp(new Date().getTime()));
        dsUser.save(userUsu);

        Pendapatan pend = new Pendapatan();
        pend.set_id(new ObjectId());
        pend.setUser_usu_id(userUsu.get_id());
        pend.setUser_id(userUsu.getUser_id());
        dsPendapatan.save(pend);


    }
}
