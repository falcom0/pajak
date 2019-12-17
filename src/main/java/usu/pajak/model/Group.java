package usu.pajak.model;

public class Group{
    private Integer id;
    private String title;

    public Group(){

    }

    public Group(Integer id, String title){
        this.id = id;
        this.title = title;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Integer getId() {
        return id;
    }
}
