package usu.pajak.fariz.model;

import java.math.BigDecimal;

public class UserPajakPPH {
    private BigDecimal usu;
    private BigDecimal pns;
    private BigDecimal pribadi;
    private BigDecimal lebih_bayar;
    private BigDecimal kurang_bayar;

    public BigDecimal getKurang_bayar() {
        return kurang_bayar;
    }

    public BigDecimal getLebih_bayar() {
        return lebih_bayar;
    }

    public BigDecimal getPns() {
        return pns;
    }

    public BigDecimal getPribadi() {
        return pribadi;
    }

    public BigDecimal getUsu() {
        return usu;
    }

    public void setKurang_bayar(BigDecimal kurang_bayar) {
        this.kurang_bayar = kurang_bayar;
    }

    public void setLebih_bayar(BigDecimal lebih_bayar) {
        this.lebih_bayar = lebih_bayar;
    }

    public void setPns(BigDecimal pns) {
        this.pns = pns;
    }

    public void setPribadi(BigDecimal pribadi) {
        this.pribadi = pribadi;
    }

    public void setUsu(BigDecimal usu) {
        this.usu = usu;
    }
}
