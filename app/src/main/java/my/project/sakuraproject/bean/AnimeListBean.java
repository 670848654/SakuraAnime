package my.project.sakuraproject.bean;

public class AnimeListBean {
    private String title;
    private String url;
    private String desc;
    private String img;

    public AnimeListBean() {
    }

    public AnimeListBean(String title, String url, String desc, String img) {
        this.title = title;
        this.url = url;
        this.desc = desc;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
