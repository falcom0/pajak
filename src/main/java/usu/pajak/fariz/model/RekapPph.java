package usu.pajak.fariz.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

//import com.mongodb.DBObject;
//import org.mongodb.morphia.annotations.Entity;
//import org.mongodb.morphia.annotations.Id;

import java.math.BigDecimal;
import java.math.BigInteger;

@Entity(value = "TotalPPh21")
public class RekapPph {
    private String _id;
    private BigInteger total_pph21_rka;
    private UserPajak user;
    private BigDecimal usu;
    private BigDecimal jasa;
    private BigDecimal pns;
    private BigDecimal kegiatan;
    private BigDecimal total_pph21_system;
    private BigDecimal total_pph21_sudah_dibayar;
    private BigDecimal hasil;

    public BigDecimal getKegiatan() {
        return kegiatan;
    }

    public BigDecimal getJasa() {
        return jasa;
    }

    public BigDecimal getUsu() {
        return usu;
    }

    public BigDecimal getPns() {
        return pns;
    }

    public BigDecimal getHasil() {
        return hasil;
    }

    public BigDecimal getTotal_pph21_sudah_dibayar() {
        return total_pph21_sudah_dibayar;
    }

    public BigDecimal getTotal_pph21_system() {
        return total_pph21_system;
    }

    public BigInteger getTotal_pph21_rka() {
        return total_pph21_rka;
    }

    public String get_id() {
        return _id;
    }

    public UserPajak getUser() {
        return user;
    }
}
