package my.project.sakuraproject.bean;

public class MainBean {
    private String title;
    private int type;
    private int img;
    private int number;

    public MainBean(String title, int type, int img, int number) {
        this.type = type;
        this.title = title;
        this.img = img;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
