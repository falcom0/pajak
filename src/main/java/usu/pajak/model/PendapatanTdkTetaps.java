package usu.pajak.model;

import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

@Entity(value="pendapatan_tdk_tetaps")
public class PendapatanTdkTetaps extends BasicDBObject {
    @Id
    private ObjectId id;
    @Reference
    private String _idUser;

    @Override
    public Object put(String key, Object value) {
        return super.put(key, value);
    }

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
