package usu.fp.model;

public class FpLog {
    private Integer id;
    private Integer unit_id;
    private Integer user_id;
    private String timestamp;
    private Integer type_id;
    private String type_title;

    public Integer getType_id() {
        return type_id;
    }

    public void setType_id(Integer type_id) {
        this.type_id = type_id;
    }

    public String getType_title() {
        return type_title;
    }

    public void setType_title(String type_title) {
        this.type_title = type_title;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUnit_id() {
        return unit_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setUnit_id(Integer unit_id) {
        this.unit_id = unit_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
}

