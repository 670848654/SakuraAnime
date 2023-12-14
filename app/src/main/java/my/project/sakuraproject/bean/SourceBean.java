package my.project.sakuraproject.bean;

/**
 * 开源相关
 */
public class SourceBean {
    private String title;
    private String author;
    private String desc;
    private String url;

    public SourceBean(String title, String author, String desc, String url) {
        this.title = title;
        this.author = author;
        this.desc = desc;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
