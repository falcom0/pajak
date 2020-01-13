package usu.pajak.util;

public class UserSimSdm{
    private String id;
    private String full_name;
    private String npwp;
    private String nip;
    private TypeSimSdm type;
    private String gender;
    private String marital_status;
    private Boolean has_couple;
    private Integer num_of_children;

    public String getNpwp() {
        return npwp.replaceAll("\\D","");
    }

    public String getMarital_status() {
        return marital_status;
    }

    public String getId() {
        return id;
    }

    public String getGender() {
        return gender;
    }

    public String getFull_name() {
        return full_name;
    }

    public TypeSimSdm getType() {
        return type;
    }

    public Boolean getHas_couple() {
        return has_couple;
    }

    public String getNip() {
        return nip;
    }

    public Integer getNum_of_children() {
        return num_of_children;
    }
}

class TypeSimSdm {
    private String id;
    private String title;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
