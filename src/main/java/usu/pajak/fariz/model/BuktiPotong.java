package usu.pajak.fariz.model;

import java.math.BigInteger;

public class BuktiPotong {
    private String h_01=""; //no.bukti potong
    private String[] h_02 = new String[]{"01","12"}; //masa
    private String[] h_03 = new String[]{"84.662.828.7","121","000"}; //npwp usu
    private String h_04 = "UNIVERSITAS SUMATERA UTARA";
    private String a_01; //npwp pegawai
    private String a_02; //nik
    private String a_03; //nama
    private String a_04; //alamat
    private Boolean a_05 = false; //jenis kelamin laki-laki
    private Boolean a_06 = false; //jenis kelamin perempuan
    private Integer a_07; //jumlah tanggungan Kawin
    private Integer a_08 ; //jumlah tanggungan Tidak Kawin
    private Integer a_09; //jumlah tanggungan HB
    private String a_10 = "Pegawai"; //jabatan
    private boolean a_11; //karyawan asing
    private String a_12; // kode domisili negara
    private String kode_pajak;
    private BigInteger bruto = BigInteger.ZERO;
    private BigInteger dpp = BigInteger.ZERO;
    private boolean tdk_npwp = false;
    private Integer tarif = 0;
    private BigInteger pph_dipotong = BigInteger.ZERO;
    private BigInteger b_01 = BigInteger.ZERO; //gaji
    private BigInteger b_02 = BigInteger.ZERO; //tunjangan_pph
    private BigInteger b_03 = BigInteger.ZERO;//tunjangan lainnya termasuk SK
    private BigInteger b_04 = BigInteger.ZERO;//honorarium dan imbalan lain
    private BigInteger b_05 = BigInteger.ZERO;//premi asuransi
    private BigInteger b_06 = BigInteger.ZERO;//penerimaan natura
    private BigInteger b_07 = BigInteger.ZERO;//bonus/THR
    private BigInteger b_08 = BigInteger.ZERO; // total bruto
    private BigInteger b_09 = BigInteger.ZERO; // biaya jabatan
    private BigInteger b_10 = BigInteger.ZERO; // iuran pensiun
    private BigInteger b_11 = BigInteger.ZERO; // total pengurang
    private BigInteger b_12 = BigInteger.ZERO; // total neto
    private BigInteger b_13 = BigInteger.ZERO; // penghasilan neto masa sebelumnya
    private BigInteger b_14 = BigInteger.ZERO; // neto utk menghitung pph pasal 21
    private BigInteger b_15 = BigInteger.ZERO; // ptkp
    private BigInteger b_16 = BigInteger.ZERO; // neto - ptkp (pkp)
    private BigInteger b_17 = BigInteger.ZERO; // pph21 dari pkp
    private BigInteger b_18 = BigInteger.ZERO; // pph21 yg telah dipotong sebelumnya
    private BigInteger b_19 = BigInteger.ZERO; // pph21 yg terhutang
    private BigInteger b_20 = BigInteger.ZERO; // pph21 yg telah dilunasi
    private String[] c_01 = new String[]{"25.118.795.1","122","000"}; // npwp pemotong
    private String c_02 = "AHMAD IKBAL"; // nama pemotong
    private String[] c_03 = new String[]{"12","02","2020"}; // tanggal


    public void setBruto(BigInteger bruto) {
        this.bruto = bruto;
    }

    public BigInteger getBruto() {
        return bruto;
    }

    public void setDpp(BigInteger dpp) {
        this.dpp = dpp;
    }

    public BigInteger getDpp() {
        return dpp;
    }

    public void setKode_pajak(String kode_pajak) {
        this.kode_pajak = kode_pajak;
    }

    public void setPph_dipotong(BigInteger pph_dipotong) {
        this.pph_dipotong = pph_dipotong;
    }

    public void setTarif(Integer tarif) {
        this.tarif = tarif;
    }

    public void setTdk_npwp(boolean tdk_npwp) {
        this.tdk_npwp = tdk_npwp;
    }

    public boolean getTdk_npwp(){
        return this.tdk_npwp;
    }

    public BigInteger getPph_dipotong() {
        return pph_dipotong;
    }

    public Integer getTarif() {
        return tarif;
    }

    public String getKode_pajak() {
        return kode_pajak;
    }

    public void setB_02(BigInteger b_02) {
        this.b_02 = b_02;
    }

    public BigInteger getB_02() {
        return b_02;
    }

    public void setA_01(String a_01) {
        this.a_01 = a_01;
    }

    public void setA_02(String a_02) {
        this.a_02 = a_02;
    }

    public void setA_03(String a_03) {
        this.a_03 = a_03;
    }

    public void setA_04(String a_04) {
        this.a_04 = a_04;
    }

    public void setA_05(boolean a_05) {
        this.a_05 = a_05;
    }

    public void setA_06(boolean a_06) {
        this.a_06 = a_06;
    }
                                                                                                                                                                                                                                                                                                                                    
    public void setA_07(Integer a_07) {
        this.a_07 = a_07;
    }

    public void setA_08(Integer a_08) {
        this.a_08 = a_08;
    }

    public void setA_09(Integer a_09) {
        this.a_09 = a_09;
    }

    public void setA_10(String a_10) {
        this.a_10 = a_10;
    }

    public void setA_11(boolean a_11) {
        this.a_11 = a_11;
    }

    public void setA_12(String a_12) {
        this.a_12 = a_12;
    }

    public void setB_01(BigInteger b_01) {
        this.b_01 = b_01;
    }

    public void setB_03(BigInteger b_03) {
        this.b_03 = b_03;
    }

    public void setB_04(BigInteger b_04) {
        this.b_04 = b_04;
    }

    public void setB_05(BigInteger b_05) {
        this.b_05 = b_05;
    }

    public void setB_06(BigInteger b_06) {
        this.b_06 = b_06;
    }

    public void setB_07(BigInteger b_07) {
        this.b_07 = b_07;
    }

    public void setB_08(BigInteger b_08) {
        this.b_08 = b_08;
    }

    public void setB_09(BigInteger b_09) {
        this.b_09 = b_09;
    }

    public void setB_10(BigInteger b_10) {
        this.b_10 = b_10;
    }

    public void setB_11(BigInteger b_11) {
        this.b_11 = b_11;
    }

    public void setB_12(BigInteger b_12) {
        this.b_12 = b_12;
    }

    public void setB_13(BigInteger b_13) {
        this.b_13 = b_13;
    }

    public void setB_14(BigInteger b_14) {
        this.b_14 = b_14;
    }

    public void setB_15(BigInteger b_15) {
        this.b_15 = b_15;
    }

    public void setB_16(BigInteger b_16) {
        this.b_16 = b_16;
    }

    public void setB_17(BigInteger b_17) {
        this.b_17 = b_17;
    }

    public void setB_18(BigInteger b_18) {
        this.b_18 = b_18;
    }

    public void setB_19(BigInteger b_19) {
        this.b_19 = b_19;
    }

    public void setB_20(BigInteger b_20) {
        this.b_20 = b_20;
    }

    public void setC_02(String c_02) {
        this.c_02 = c_02;
    }

    public void setH_01(String h_01) {
        this.h_01 = h_01;
    }

    public void setH_02(String[] h_02) {
        this.h_02 = h_02;
    }

    public void setH_03(String[] h_03) {
        this.h_03 = h_03;
    }

    public void setH_04(String h_04) {
        this.h_04 = h_04;
    }

    public BigInteger getB_01() {
        return b_01;
    }

    public BigInteger getB_03() {
        return b_03;
    }

    public BigInteger getB_04() {
        return b_04;
    }

    public BigInteger getB_05() {
        return b_05;
    }

    public BigInteger getB_06() {
        return b_06;
    }

    public BigInteger getB_07() {
        return b_07;
    }

    public BigInteger getB_08() {
        return b_08;
    }

    public BigInteger getB_09() {
        return b_09;
    }

    public BigInteger getB_10() {
        return b_10;
    }

    public BigInteger getB_11() {
        return b_11;
    }

    public BigInteger getB_12() {
        return b_12;
    }

    public BigInteger getB_13() {
        return b_13;
    }

    public BigInteger getB_14() {
        return b_14;
    }

    public BigInteger getB_15() {
        return b_15;
    }

    public BigInteger getB_16() {
        return b_16;
    }

    public BigInteger getB_17() {
        return b_17;
    }

    public BigInteger getB_18() {
        return b_18;
    }

    public BigInteger getB_19() {
        return b_19;
    }

    public BigInteger getB_20() {
        return b_20;
    }

    public Boolean getA_05() {
        return a_05;
    }

    public Boolean getA_06() {
        return a_06;
    }

    public Integer getA_07() {
        return a_07;
    }

    public Integer getA_08() {
        return a_08;
    }

    public Integer getA_09() {
        return a_09;
    }

    public String getA_01() {
        return a_01;
    }

    public String getA_02() {
        return a_02;
    }

    public String getA_03() {
        return a_03;
    }

    public String getA_04() {
        return a_04;
    }

    public String getA_10() {
        return a_10;
    }

    public String getA_12() {
        return a_12;
    }

    public void setC_01(String[] c_01) {
        this.c_01 = c_01;
    }

    public String[] getC_01() {
        return c_01;
    }

    public void setA_06(Boolean a_06) {
        this.a_06 = a_06;
    }

    public void setA_05(Boolean a_05) {
        this.a_05 = a_05;
    }

    public String getC_02() {
        return c_02;
    }

    public String[] getC_03() {
        return c_03;
    }

    public void setC_03(String[] c_03) {
        this.c_03 = c_03;
    }

    public String getH_01() {
        return h_01;
    }

    public String getH_04() {
        return h_04;
    }

    public String[] getH_02() {
        return h_02;
    }

    public String[] getH_03() {
        return h_03;
    }
}
