package usu.pajak.fariz.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.Date;
//import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;
//import org.mongodb.morphia.annotations.Indexed;
//import org.mongodb.morphia.annotations.Reference;

@Entity(value="user_pajaks")
public class UserPajak {
    @Id
    private ObjectId id;
    @Indexed
    private String id_user;
    private String npwp;
    private String npwp_simsdm;
    private String front_degree;
    private String full_name;
    private String behind_degree;
    private String nip_simsdm;
    private String nip_gpp;
    private Group group;
    private UserPajakPendapatan total_pendapatan;
    private UserPajakTax setting_pajak;
    private UserPajakPPH pph21;
    private Date lastUpdate = new Date();
    private BigDecimal totalNettoPendapatan;
    private BigDecimal totalPph21;

    public BigDecimal getTotalNettoPendapatan() {
        return totalNettoPendapatan;
    }

    public BigDecimal getTotalPph21() {
        return totalPph21;
    }

    public void setTotalPph21(BigDecimal totalPph21) {
        this.totalPph21 = totalPph21;
    }

    public void setTotalNettoPendapatan(BigDecimal totalNettoPendapatan) {
        this.totalNettoPendapatan = totalNettoPendapatan;
    }

    @PrePersist
    void prePersist() {lastUpdate = new Date();}

    public UserPajakPPH getPph21() {
        return pph21;
    }

    public UserPajakTax getSetting_pajak() {
        return setting_pajak;
    }

    public UserPajakPendapatan getTotal_pendapatan() {
        return total_pendapatan;
    }

    public void setTotal_pendapatan(UserPajakPendapatan total_pendapatan) {
        this.total_pendapatan = total_pendapatan;
    }

    public void setSetting_pajak(UserPajakTax setting_pajak) {
        this.setting_pajak = setting_pajak;
    }

    public void setPph21(UserPajakPPH pph21) {
        this.pph21 = pph21;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getNpwp_simsdm() {
        return npwp_simsdm;
    }

    public void setNpwp_simsdm(String npwp_simsdm) {
        this.npwp_simsdm = npwp_simsdm;
    }

    public void setBehind_degree(String behind_degree) {
        this.behind_degree = behind_degree;
    }

    public void setFront_degree(String front_degree) {
        this.front_degree = front_degree;
    }

    public String getBehind_degree() {
        return behind_degree;
    }

    public String getFront_degree() {
        return front_degree;
    }

    public String getNip_gpp() {
        return nip_gpp;
    }

    public String getNip_simsdm() {
        return nip_simsdm;
    }

    public void setNip_gpp(String nip_gpp) {
        this.nip_gpp = nip_gpp;
    }

    public void setNip_simsdm(String nip_simsdm) {
        this.nip_simsdm = nip_simsdm;
    }

    public void setNpwp(String npwp) {
        this.npwp = npwp;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getNpwp() {
        return npwp;
    }

    public String getFull_name() {
        return full_name;
    }

    public ObjectId getId() {
        return id;
    }

    public String getId_user() {
        return id_user;
    }
}
