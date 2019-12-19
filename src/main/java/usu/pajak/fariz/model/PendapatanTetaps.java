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
    private Integer month;
    private Integer year;
    private BasicDBObject rka_payment;
    private Pajak pajak;
    private Boolean status;
    private Date lastUpdated = new Date();
    @PrePersist
    void prePersist() {lastUpdated = new Date();}

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Pajak getPajak() {
        return pajak;
    }

    public void setPajak(Pajak pajak) {
        this.pajak = pajak;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public BasicDBObject getRka_payment() {
        return rka_payment;
    }

    public void setRka_payment(BasicDBObject rka_payment) {
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

    public void setId_user(String _idUser) {
        this.id_user = _idUser;
    }

    public String getId_user() {
        return id_user;
    }
}
