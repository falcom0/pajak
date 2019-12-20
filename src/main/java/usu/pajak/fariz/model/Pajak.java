package usu.pajak.fariz.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.math.BigDecimal;

public class Pajak {

    private BigDecimal total_pendapatan_rka;
    private BigDecimal jkk;
    private BigDecimal jkm;
    private BigDecimal bpjs_kesehatan;
    private BigDecimal bruto_pendapatan;
    private BigDecimal biaya_jabatan;
    private BigDecimal netto_pendapatan;
    private BigDecimal netto_take_homepay;
    private BasicDBList pph21;
    private UserPajakTax _recordCalTax;

    public BasicDBList getPph21() {
        return pph21;
    }

    public UserPajakTax get_recordCalTax() {
        return _recordCalTax;
    }

    public void set_recordCalTax(UserPajakTax _recordCalTax) {
        this._recordCalTax = _recordCalTax;
    }

    public void setPph21(BasicDBList pph21) {
        this.pph21 = pph21;
    }

    public BigDecimal getNetto_take_homepay() {
        return netto_take_homepay;
    }

    public void setNetto_take_homepay(BigDecimal netto_take_homepay) {
        this.netto_take_homepay = netto_take_homepay;
    }

    public void setTotal_pendapatan_rka(BigDecimal total_pendapatan_rka) {
        this.total_pendapatan_rka = total_pendapatan_rka;
    }

    public BigDecimal getTotal_pendapatan_rka() {
        return total_pendapatan_rka;
    }

    public void setBiaya_jabatan(BigDecimal biaya_jabatan) {
        this.biaya_jabatan = biaya_jabatan;
    }

    public void setBpjs_kesehatan(BigDecimal bpjs_kesehatan) {
        this.bpjs_kesehatan = bpjs_kesehatan;
    }

    public void setBruto_pendapatan(BigDecimal bruto_pendapatan) {
        this.bruto_pendapatan = bruto_pendapatan;
    }

    public void setJkk(BigDecimal jkk) {
        this.jkk = jkk;
    }

    public void setJkm(BigDecimal jkm) {
        this.jkm = jkm;
    }

    public void setNetto_pendapatan(BigDecimal netto_pendapatan) {
        this.netto_pendapatan = netto_pendapatan;
    }

    public BigDecimal getBiaya_jabatan() {
        return biaya_jabatan;
    }

    public BigDecimal getBruto_pendapatan() {
        return bruto_pendapatan;
    }

    public BigDecimal getBpjs_kesehatan() {
        return bpjs_kesehatan;
    }

    public BigDecimal getJkm() {
        return jkm;
    }

    public BigDecimal getJkk() {
        return jkk;
    }

    public BigDecimal getNetto_pendapatan() {
        return netto_pendapatan;
    }
}
