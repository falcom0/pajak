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
    private String id_user; //ok
    private String npwp; //ok
    private String npwp_simsdm;
    private String front_degree;
    private String full_name; // ok
    private String behind_degree;
    private String nip_simsdm; // ok
    private String nip_gpp; // ok
    private Group group;
//    private BasicDBObject unit;

//    @Reference
//    private BasicDBList pendapatan_tetap;
//    @Reference
//    private BasicDBList pendapatan_tdk_tetap; //ok
//    private BasicDBList pendapatan_batal;
    /**
     * isi pendapatan_tdk_tetap:
     * 0. activity_id
     * 0. request_id
     * 0. salary_id
     * 0. type_id
     * 0. type_title
     * 0. unit_id
     * 1. bulan
     * 2. tahun
     * 3. semua jenis pendapatan_tdk_tetap
     * 3. bruto_pendapatan
     * 4. semua jenis potongan bkn pajak
     * 5. semua jenis potongan pajak
     * 7. netto take home pay
     * 6. netto pendapatan_tdk_tetap pajak
     * 9. ptkp sebulan
     * 10. pkp sebulan
     * 11. sisa ptkp sebulan
     * 12. pph21 - Array
     *      12a. tarif
     *      12b. pkp
     *      12c. hasil
     * 13. update time
     */
    private BigDecimal netto_pendapatan_setahun; //ok
    private BigDecimal potongan_jabatan_A2_setahun;
    private BigDecimal potongan_jabatan_A1_setahun;
    private BigDecimal ptkp_setahun; //ok
    private BigDecimal sisa_ptkp; // ok
    private BigDecimal total_pkp;
    private BigDecimal reminder_pajak;
    private Integer index_layer_pajak;
    private BigDecimal reminder_pajak_jasa_medis;
    private Integer index_layer_pajak_jasa_medis;
    private BigDecimal total_pph21_usu;
    private BigDecimal total_pph21_usu_dibayar; //ok
    private BigDecimal total_pph21_pribadi; //ok
    private BigDecimal total_pph21_lebih_bayar;
    private BigDecimal total_pph21_kurang_bayar;
    private Date lastUpdate = new Date();
    @PrePersist
    void prePersist() {lastUpdate = new Date();}

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public BigDecimal getReminder_pajak_jasa_medis() {
        return reminder_pajak_jasa_medis;
    }

    public Integer getIndex_layer_pajak_jasa_medis() {
        return index_layer_pajak_jasa_medis;
    }

    public void setIndex_layer_pajak_jasa_medis(Integer index_layer_pajak_jasa_medis) {
        this.index_layer_pajak_jasa_medis = index_layer_pajak_jasa_medis;
    }

    public void setReminder_pajak_jasa_medis(BigDecimal reminder_pajak_jasa_medis) {
        this.reminder_pajak_jasa_medis = reminder_pajak_jasa_medis;
    }

    public void setTotal_pph21_kurang_bayar(BigDecimal total_pph21_kurang_bayar) {
        this.total_pph21_kurang_bayar = total_pph21_kurang_bayar;
    }

    public void setTotal_pph21_lebih_bayar(BigDecimal total_pph21_lebih_bayar) {
        this.total_pph21_lebih_bayar = total_pph21_lebih_bayar;
    }

    public BigDecimal getTotal_pph21_lebih_bayar() {
        return total_pph21_lebih_bayar;
    }

    public BigDecimal getTotal_pph21_kurang_bayar() {
        return total_pph21_kurang_bayar;
    }

    public void setPotongan_jabatan_A1_setahun(BigDecimal potongan_jabatan_A1_setahun) {
        this.potongan_jabatan_A1_setahun = potongan_jabatan_A1_setahun;
    }

    public void setPotongan_jabatan_A2_setahun(BigDecimal potongan_jabatan_A2_setahun) {
        this.potongan_jabatan_A2_setahun = potongan_jabatan_A2_setahun;
    }

    public BigDecimal getPotongan_jabatan_A1_setahun() {
        return potongan_jabatan_A1_setahun;
    }

    public BigDecimal getPotongan_jabatan_A2_setahun() {
        return potongan_jabatan_A2_setahun;
    }

    public void setIndex_layer_pajak(Integer index_layer_pajak) {
        this.index_layer_pajak = index_layer_pajak;
    }

    public void setReminder_pajak(BigDecimal reminder_pajak) {
        this.reminder_pajak = reminder_pajak;
    }

    public Integer getIndex_layer_pajak() {
        return index_layer_pajak;
    }

    public BigDecimal getReminder_pajak() {
        return reminder_pajak;
    }

    public String getNpwp_simsdm() {
        return npwp_simsdm;
    }

    public void setNpwp_simsdm(String npwp_simsdm) {
        this.npwp_simsdm = npwp_simsdm;
    }

    public BigDecimal getTotal_pph21_usu_dibayar() {
        return total_pph21_usu_dibayar;
    }

    public void setTotal_pph21_usu_dibayar(BigDecimal total_pph21_usu_dibayar) {
        this.total_pph21_usu_dibayar = total_pph21_usu_dibayar;
    }

    public void setTotal_pkp(BigDecimal total_pkp) {
        this.total_pkp = total_pkp;
    }

    public BigDecimal getTotal_pkp() {
        return total_pkp;
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

    public BigDecimal getPtkp_setahun() {
        return ptkp_setahun;
    }

    public void setPtkp_setahun(BigDecimal ptkp_setahun) {
        this.ptkp_setahun = ptkp_setahun;
    }

    public BigDecimal getNetto_pendapatan_setahun() {
        return netto_pendapatan_setahun;
    }

    public void setNetto_pendapatan_setahun(BigDecimal netto_pendapatan_setahun) {
        this.netto_pendapatan_setahun = netto_pendapatan_setahun;
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

    public void setSisa_ptkp(BigDecimal sisa_ptkp) {
        this.sisa_ptkp = sisa_ptkp;
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

    public void setTotal_pph21_pribadi(BigDecimal total_pph21_pribadi) {
        this.total_pph21_pribadi = total_pph21_pribadi;
    }

    public void setTotal_pph21_usu(BigDecimal total_pph21_usu) {
        this.total_pph21_usu = total_pph21_usu;
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

    public BigDecimal getSisa_ptkp() {
        return sisa_ptkp;
    }

    public BigDecimal getTotal_pph21_pribadi() {
        return total_pph21_pribadi;
    }

    public BigDecimal getTotal_pph21_usu() {
        return total_pph21_usu;
    }
}
