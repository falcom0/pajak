package usu.pajak.fariz.model;

import com.mongodb.BasicDBObject;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PrePersist;
import dev.morphia.annotations.Reference;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.Date;
//import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;
//import org.mongodb.morphia.annotations.Reference;

@Entity(value="pendapatan_tdk_tetaps")
public class PendapatanTdkTetaps {
    @Id
    private ObjectId id;
    private String id_user;
    private Integer salary_id;
    private String type;
    private Unit unit;
    private Integer month;
    private Integer year;
    private BasicDBObject rka_payment;
    private Pajak pajak;
    private Boolean status;
    private Date lastUpdated = new Date();
    @PrePersist
    void prePersist() {lastUpdated = new Date();}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public BigDecimal getNettoPendapatan(){
        return pajak.getNetto_pendapatan();
    }

    public BigDecimal getPph21(){
        return pajak.getPph21().stream().map(e -> {
            BasicDBObject basicDBObject = (BasicDBObject) e;
            Decimal128 decimal128 = (Decimal128)basicDBObject.get("_hasil");
            return decimal128.bigDecimalValue();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
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
