package usu.pajak.fariz.model;

import java.math.BigDecimal;

public class UserPajakPendapatan {
    private BigDecimal netto_pendapatan_setahun; //-
    private BigDecimal biaya_jabatan_setahun; //-
    private BigDecimal ptkp_setahun; //-
    private BigDecimal sisa_ptkp; // -
    private BigDecimal total_pkp;// -

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
