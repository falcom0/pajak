package usu.pajak.model;

import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value="pendapatan_tetaps")
public class PendapatanTetaps extends BasicDBObject {
    @Id
    private ObjectId id;
    @Reference
    private String _idUser;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void set_idUser(String _idUser) {
        this._idUser = _idUser;
    }

    public String get_idUser() {
        return _idUser;
    }
}
