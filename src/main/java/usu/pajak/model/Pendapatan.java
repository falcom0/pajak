package usu.pajak.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.sql.Timestamp;

@Entity(value="pendapatans")
public class Pendapatan {
    @Id
    private ObjectId _id;
    @Reference
    private ObjectId user_usu_id;
    private int user_id;
    private BasicDBObject activity;
    private BasicDBObject request;
    private BasicDBObject salary;
    private BasicDBObject type; /* Gaji Tetap=0, Adhoc=1 */
    private BasicDBObject unit;
    private BasicDBObject cheque;
    private BasicDBObject payment_detail;
    private BasicDBList pph21;
    private String source_of_fund;
    private boolean batal = false;
    private Timestamp created_at;
    private Timestamp updated_at;

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setActivity(BasicDBObject activity) {
        this.activity = activity;
    }

    public void setBatal(boolean batal) {
        this.batal = batal;
    }

    public void setCheque(BasicDBObject cheque) {
        this.cheque = cheque;
    }

    public void setPayment_detail(BasicDBObject payment_detail) {
        this.payment_detail = payment_detail;
    }

    public void setPph21(BasicDBList pph21) {
        this.pph21 = pph21;
    }

    public void setRequest(BasicDBObject request) {
        this.request = request;
    }

    public void setSalary(BasicDBObject salary) {
        this.salary = salary;
    }

    public void setSource_of_fund(String source_of_fund) {
        this.source_of_fund = source_of_fund;
    }

    public void setType(BasicDBObject type) {
        this.type = type;
    }

    public void setUnit(BasicDBObject unit) {
        this.unit = unit;
    }

    public void setUser_usu_id(ObjectId user_usu_id) {
        this.user_usu_id = user_usu_id;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public ObjectId get_id() {
        return _id;
    }

    public int getUser_id() {
        return user_id;
    }

    public BasicDBList getPph21() {
        return pph21;
    }

    public BasicDBObject getActivity() {
        return activity;
    }

    public BasicDBObject getCheque() {
        return cheque;
    }

    public BasicDBObject getPayment_detail() {
        return payment_detail;
    }

    public BasicDBObject getRequest() {
        return request;
    }

    public BasicDBObject getSalary() {
        return salary;
    }

    public BasicDBObject getType() {
        return type;
    }

    public BasicDBObject getUnit() {
        return unit;
    }

    public ObjectId getUser_usu_id() {
        return user_usu_id;
    }

    public String getSource_of_fund() {
        return source_of_fund;
    }
}
