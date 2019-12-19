package usu.pajak.fariz.model;

import com.mongodb.BasicDBObject;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PrePersist;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

import java.util.Date;

@Entity(value="pendapatan_tetaps")
public class PendapatanTetaps {
    @Id
    private ObjectId id;
    @Reference
    private String id_user;
    private Integer salary_id;
    private Unit unit;
    private BasicDBObject rka_payment;
    private Pajak pajak;
    private Date lastUpdated = new Date();
    @PrePersist
    void prePersist() {lastUpdated = new Date();}

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public BasicDBObject getDetails() {
        return rka_payment;
    }

    public void setDetails(BasicDBObject rka_payment) {
        this.rka_payment = rka_payment;
    }

    public Integer getSalary_id() {
        return salary_id;
    }

    public void setSalary_id(Integer salary_id) {
        this.salary_id = salary_id;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void set_idUser(String _idUser) {
        this.id_user = _idUser;
    }

    public String get_idUser() {
        return id_user;
    }
}
