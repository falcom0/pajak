package usu.pajak.fariz.model;

import java.math.BigInteger;

public class User{
    private BigInteger id;
    private String full_name;
    private String nip_nik;
    private String npwp;
    private String front_degree;
    private String behind_degree;
    private Group group;

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getFront_degree() {
        return front_degree;
    }

    public String getBehind_degree() {
        return behind_degree;
    }

    public BigInteger getId() {
        return id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getNip_nik() {
        return nip_nik;
    }

    public String getNpwp() {
        return npwp.replaceAll("-","").replaceAll(".","").replaceAll(",","").replaceAll(" ","").trim();
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public void setFront_degree(String front_degree) {
        this.front_degree = front_degree;
    }

    public void setBehind_degree(String behind_degree) {
        this.behind_degree = behind_degree;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setNpwp(String npwp) {
        this.npwp = npwp;
    }

    public void setNip_nik(String nip_nik) {
        this.nip_nik = nip_nik;
    }
}