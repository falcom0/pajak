package usu.pajak.model;

import com.mongodb.BasicDBList;

public class DetailTax {
    private Double nettoPendapatanSebelumnya;
    private BasicDBList pph21Sebelumnya;
    private Double brutoPendapatanSekarang;
    private Double potonganJabatan;
    private Double potonganPensiun;
    private Double nettoPendapatanSekarang;
    private Double nettoPendapatanSekarangDisetahunkan;
    private Double ptkp;
    private BasicDBList pph21;

    public Double getNettoPendapatanSekarangDisetahunkan() {
        return nettoPendapatanSekarangDisetahunkan;
    }

    public void setNettoPendapatanSekarangDisetahunkan(Double nettoPendapatanSekarangDisetahunkan) {
        this.nettoPendapatanSekarangDisetahunkan = nettoPendapatanSekarangDisetahunkan;
    }

    public void setPph21Sebelumnya(BasicDBList pph21Sebelumnya) {
        this.pph21Sebelumnya = pph21Sebelumnya;
    }

    public BasicDBList getPph21Sebelumnya() {
        return pph21Sebelumnya;
    }

    public void setPtkp(Double ptkp) {
        this.ptkp = ptkp;
    }

    public void setPph21(BasicDBList pph21) {
        this.pph21 = pph21;
    }

    public void setBrutoPendapatanSekarang(Double brutoPendapatanSekarang) {
        this.brutoPendapatanSekarang = brutoPendapatanSekarang;
    }

    public void setNettoPendapatanSekarang(Double nettoPendapatanSekarang) {
        this.nettoPendapatanSekarang = nettoPendapatanSekarang;
    }

    public void setNettoPendapatanSebelumnya(Double nettoPendapatanSebelumnya) {
        this.nettoPendapatanSebelumnya = nettoPendapatanSebelumnya;
    }

    public void setPotonganJabatan(Double potonganJabatan) {
        this.potonganJabatan = potonganJabatan;
    }

    public void setPotonganPensiun(Double potonganPensiun) {
        this.potonganPensiun = potonganPensiun;
    }

    public Double getNettoPendapatanSebelumnya() {
        return nettoPendapatanSebelumnya;
    }

    public BasicDBList getPph21() {
        return pph21;
    }

    public Double getBrutoPendapatanSekarang() {
        return brutoPendapatanSekarang;
    }

    public Double getNettoPendapatanSekarang() {
        return nettoPendapatanSekarang;
    }

    public Double getPotonganJabatan() {
        return potonganJabatan;
    }

    public Double getPotonganPensiun() {
        return potonganPensiun;
    }

    public Double getPtkp() {
        return ptkp;
    }
}
