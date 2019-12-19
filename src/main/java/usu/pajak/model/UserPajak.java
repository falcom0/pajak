package usu.pajak.model;

import com.mongodb.BasicDBList;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;
//import org.mongodb.morphia.annotations.*;

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
    @Reference
    private BasicDBList pendapatan_tetap;
    @Reference
    private BasicDBList pendapatan_tdk_tetap; //ok
    private BasicDBList pendapatan_batal;
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
    private String netto_pendapatan_setahun; //ok
    private String potongan_jabatan_A2_setahun;
    private String potongan_jabatan_A1_setahun;
    private String ptkp_setahun; //ok
    private String sisa_ptkp; // ok
    private String total_pkp;
    private String reminder_pajak;
    private String index_layer_pajak;
    private String reminder_pajak_jasa_medis;
    private String index_layer_pajak_jasa_medis;
    private String total_pph21_usu;
    private String total_pph21_usu_dibayar; //ok
    private String total_pph21_pribadi; //ok
    private String total_pph21_lebih_bayar;
    private String total_pph21_kurang_bayar;
    private String timestamp; //ok

    public String getReminder_pajak_jasa_medis() {
        return reminder_pajak_jasa_medis;
    }

    public String getIndex_layer_pajak_jasa_medis() {
        return index_layer_pajak_jasa_medis;
    }

    public void setIndex_layer_pajak_jasa_medis(String index_layer_pajak_jasa_medis) {
        this.index_layer_pajak_jasa_medis = index_layer_pajak_jasa_medis;
    }

    public void setReminder_pajak_jasa_medis(String reminder_pajak_jasa_medis) {
        this.reminder_pajak_jasa_medis = reminder_pajak_jasa_medis;
    }

    public BasicDBList getPendapatan_batal() {
        return pendapatan_batal;
    }

    public void setPendapatan_batal(BasicDBList pendapatan_batal) {
        this.pendapatan_batal = pendapatan_batal;
    }

    public void setTotal_pph21_kurang_bayar(String total_pph21_kurang_bayar) {
        this.total_pph21_kurang_bayar = total_pph21_kurang_bayar;
    }

    public void setTotal_pph21_lebih_bayar(String total_pph21_lebih_bayar) {
        this.total_pph21_lebih_bayar = total_pph21_lebih_bayar;
    }

    public String getTotal_pph21_lebih_bayar() {
        return total_pph21_lebih_bayar;
    }

    public String getTotal_pph21_kurang_bayar() {
        return total_pph21_kurang_bayar;
    }

    public void setPotongan_jabatan_A1_setahun(String potongan_jabatan_A1_setahun) {
        this.potongan_jabatan_A1_setahun = potongan_jabatan_A1_setahun;
    }

    public void setPotongan_jabatan_A2_setahun(String potongan_jabatan_A2_setahun) {
        this.potongan_jabatan_A2_setahun = potongan_jabatan_A2_setahun;
    }

    public String getPotongan_jabatan_A1_setahun() {
        return potongan_jabatan_A1_setahun;
    }

    public String getPotongan_jabatan_A2_setahun() {
        return potongan_jabatan_A2_setahun;
    }

    public void setIndex_layer_pajak(String index_layer_pajak) {
        this.index_layer_pajak = index_layer_pajak;
    }

    public void setReminder_pajak(String reminder_pajak) {
        this.reminder_pajak = reminder_pajak;
    }

    public String getIndex_layer_pajak() {
        return index_layer_pajak;
    }

    public String getReminder_pajak() {
        return reminder_pajak;
    }

    public BasicDBList getPendapatan_tetap() {
        return pendapatan_tetap;
    }

    public void setPendapatan_tetap(BasicDBList pendapatan_tetap) {
        this.pendapatan_tetap = pendapatan_tetap;
    }

    public String getNpwp_simsdm() {
        return npwp_simsdm;
    }

    public void setNpwp_simsdm(String npwp_simsdm) {
        this.npwp_simsdm = npwp_simsdm;
    }

    public String getTotal_pph21_usu_dibayar() {
        return total_pph21_usu_dibayar;
    }

    public void setTotal_pph21_usu_dibayar(String total_pph21_usu_dibayar) {
        this.total_pph21_usu_dibayar = total_pph21_usu_dibayar;
    }

    public void setTotal_pkp(String total_pkp) {
        this.total_pkp = total_pkp;
    }

    public String getTotal_pkp() {
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

    public String getPtkp_setahun() {
        return ptkp_setahun;
    }

    public void setPtkp_setahun(String ptkp_setahun) {
        this.ptkp_setahun = ptkp_setahun;
    }

    public String getNetto_pendapatan_setahun() {
        return netto_pendapatan_setahun;
    }

    public void setNetto_pendapatan_setahun(String netto_pendapatan_setahun) {
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

    public void setSisa_ptkp(String sisa_ptkp) {
        this.sisa_ptkp = sisa_ptkp;
    }

    public void setPendapatan_tdk_tetap(BasicDBList pendapatan_tdk_tetap) {
        this.pendapatan_tdk_tetap = pendapatan_tdk_tetap;
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

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTotal_pph21_pribadi(String total_pph21_pribadi) {
        this.total_pph21_pribadi = total_pph21_pribadi;
    }

    public void setTotal_pph21_usu(String total_pph21_usu) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public String getId_user() {
        return id_user;
    }

    public BasicDBList getPendapatan_tdk_tetap() {
        return pendapatan_tdk_tetap;
    }

    public String getSisa_ptkp() {
        return sisa_ptkp;
    }

    public String getTotal_pph21_pribadi() {
        return total_pph21_pribadi;
    }

    public String getTotal_pph21_usu() {
        return total_pph21_usu;
    }
}
