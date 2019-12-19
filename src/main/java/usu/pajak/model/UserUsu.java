package usu.pajak.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import org.bson.types.ObjectId;
//import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;
//import org.mongodb.morphia.annotations.Indexed;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity(value="user_usus")
public class UserUsu {
    @Id
    private ObjectId _id;
    @Indexed
    private int user_id;
    private String full_name;
    private String npwp_gpp;
    private String npwp_simsdm;
    private String nip_gpp;
    private String nip_simsdm;
    private Group group;
    private BigDecimal ptkp_setahun;
    private BigDecimal sisa_ptkp;
    private BigDecimal total_pkp;
    private BigDecimal pot_jabatan_setahun;
    private BigDecimal netto_pendapatan_setahun;
    private BigDecimal pph21_dibayar_usu;
    private BigDecimal pph21_database;
    private BigDecimal pph21_lebih_bayar;
    private BigDecimal pph21_kurang_bayar;
    private Timestamp created_at;
    private Timestamp updated_at;

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setNetto_pendapatan_setahun(BigDecimal netto_pendapatan_setahun) {
        this.netto_pendapatan_setahun = netto_pendapatan_setahun;
    }

    public void setNip_gpp(String nip_gpp) {
        this.nip_gpp = nip_gpp;
    }

    public void setNip_simsdm(String nip_simsdm) {
        this.nip_simsdm = nip_simsdm;
    }

    public void setNpwp_gpp(String npwp_gpp) {
        this.npwp_gpp = npwp_gpp;
    }

    public void setNpwp_simsdm(String npwp_simsdm) {
        this.npwp_simsdm = npwp_simsdm;
    }

    public void setPot_jabatan_setahun(BigDecimal pot_jabatan_setahun) {
        this.pot_jabatan_setahun = pot_jabatan_setahun;
    }

    public void setPph21_database(BigDecimal pph21_database) {
        this.pph21_database = pph21_database;
    }

    public void setPph21_dibayar_usu(BigDecimal pph21_dibayar_usu) {
        this.pph21_dibayar_usu = pph21_dibayar_usu;
    }

    public void setPph21_kurang_bayar(BigDecimal pph21_kurang_bayar) {
        this.pph21_kurang_bayar = pph21_kurang_bayar;
    }

    public void setPph21_lebih_bayar(BigDecimal pph21_lebih_bayar) {
        this.pph21_lebih_bayar = pph21_lebih_bayar;
    }

    public void setPtkp_setahun(BigDecimal ptkp_setahun) {
        this.ptkp_setahun = ptkp_setahun;
    }

    public void setSisa_ptkp(BigDecimal sisa_ptkp) {
        this.sisa_ptkp = sisa_ptkp;
    }

    public void setTotal_pkp(BigDecimal total_pkp) {
        this.total_pkp = total_pkp;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public BigDecimal getNetto_pendapatan_setahun() {
        return netto_pendapatan_setahun;
    }

    public BigDecimal getPot_jabatan_setahun() {
        return pot_jabatan_setahun;
    }

    public BigDecimal getPph21_database() {
        return pph21_database;
    }

    public BigDecimal getPph21_dibayar_usu() {
        return pph21_dibayar_usu;
    }

    public BigDecimal getPph21_kurang_bayar() {
        return pph21_kurang_bayar;
    }

    public BigDecimal getPph21_lebih_bayar() {
        return pph21_lebih_bayar;
    }

    public BigDecimal getPtkp_setahun() {
        return ptkp_setahun;
    }

    public BigDecimal getSisa_ptkp() {
        return sisa_ptkp;
    }

    public BigDecimal getTotal_pkp() {
        return total_pkp;
    }

    public Group getGroup() {
        return group;
    }

    public int getUser_id() {
        return user_id;
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getNip_gpp() {
        return nip_gpp;
    }

    public String getNip_simsdm() {
        return nip_simsdm;
    }

    public String getNpwp_gpp() {
        return npwp_gpp;
    }

    public String getNpwp_simsdm() {
        return npwp_simsdm;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }
}
