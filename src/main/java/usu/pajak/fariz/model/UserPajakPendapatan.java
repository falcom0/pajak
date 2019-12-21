package usu.pajak.fariz.model;

import java.math.BigDecimal;

public class UserPajakPendapatan {
    private BigDecimal netto_pendapatan_setahun;
    private BigDecimal bruto_jasa_setahun;
    private BigDecimal biaya_jabatan_setahun;
    private BigDecimal jaminan_pensiun_ht_setahun;
    private BigDecimal ptkp_setahun;
    private BigDecimal sisa_ptkp;
    private BigDecimal total_pkp;
    private BigDecimal total_pkp_jasa;

    public BigDecimal getJaminan_pensiun_ht_setahun() {
        return jaminan_pensiun_ht_setahun;
    }

    public void setJaminan_pensiun_ht_setahun(BigDecimal jaminan_pensiun_ht_setahun) {
        this.jaminan_pensiun_ht_setahun = jaminan_pensiun_ht_setahun;
    }

    public BigDecimal getBruto_jasa_setahun() {
        return bruto_jasa_setahun;
    }

    public void setBruto_jasa_setahun(BigDecimal bruto_jasa_setahun) {
        this.bruto_jasa_setahun = bruto_jasa_setahun;
    }

    public BigDecimal getTotal_pkp_jasa() {
        return total_pkp_jasa;
    }

    public void setTotal_pkp_jasa(BigDecimal total_pkp_jasa) {
        this.total_pkp_jasa = total_pkp_jasa;
    }

    public BigDecimal getNetto_pendapatan_setahun() {
        return netto_pendapatan_setahun;
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

    public BigDecimal getBiaya_jabatan_setahun() {
        return biaya_jabatan_setahun;
    }

    public void setBiaya_jabatan_setahun(BigDecimal biaya_jabatan_setahun) {
        this.biaya_jabatan_setahun = biaya_jabatan_setahun;
    }

    public void setNetto_pendapatan_setahun(BigDecimal netto_pendapatan_setahun) {
        this.netto_pendapatan_setahun = netto_pendapatan_setahun;
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
}
