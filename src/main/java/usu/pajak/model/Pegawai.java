package usu.pajak.model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity(value="pegawai")
public class Pegawai {
    @Id
    private ObjectId id;
    private Long id_user;
    private String full_name;
    private String npwp;
    private List<LinkedHashMap<String, String>> pendapatan;
    private String total_pendapatan;
    private String ptkp;
    private String sisa_ptkp;
    private String pkp;
    private List<Map.Entry<String,String>> pph21;
    private String total_pph21;
    private Integer tahun;
    private String timestamp;

    public Integer getTahun() {
        return tahun;
    }

    public void setTahun(Integer tahun) {
        this.tahun = tahun;
    }

    public String getPkp() {
        return pkp;
    }

    public void setPkp(String pkp) {
        this.pkp = pkp;
    }

    public String getNpwp() {
        return npwp;
    }

    public void setNpwp(String npwp) {
        this.npwp = npwp;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Long getId_user() {
        return id_user;
    }

    public String getPtkp() {
        return ptkp;
    }

    public String getSisa_ptkp() {
        return sisa_ptkp;
    }

    public String getTotal_pendapatan() {
        return total_pendapatan;
    }

    public String getTotal_pph21() {
        return total_pph21;
    }

    public List<LinkedHashMap<String, String>> getPendapatan() {
        return pendapatan;
    }

    public List<Map.Entry<String, String>> getPph21() {
        return pph21;
    }

    public ObjectId getId() {
        return id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setPendapatan(List<LinkedHashMap<String, String>> pendapatan) {
        this.pendapatan = pendapatan;
    }

    public void setPph21(List<Map.Entry<String, String>> pph21) {
        this.pph21 = pph21;
    }

    public void setPtkp(String ptkp) {
        this.ptkp = ptkp;
    }

    public void setSisa_ptkp(String sisa_ptkp) {
        this.sisa_ptkp = sisa_ptkp;
    }

    public void setTotal_pendapatan(String total_pendapatan) {
        this.total_pendapatan = total_pendapatan;
    }

    public void setTotal_pph21(String total_pph21) {
        this.total_pph21 = total_pph21;
    }
}

