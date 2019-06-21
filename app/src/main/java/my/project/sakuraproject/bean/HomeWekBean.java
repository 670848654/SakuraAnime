package my.project.sakuraproject.bean;

public class HomeWekBean {
    private String title;
    private String url;
    private String drama;
    private String dramaUrl;

    public HomeWekBean(String title, String url, String drama, String dramaUrl) {
        this.title = title;
        this.url = url;
        this.drama = drama;
        this.dramaUrl = dramaUrl;
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

    public String getDrama() {
        return drama;
    }

    public void setDrama(String drama) {
        this.drama = drama;
    }

    public String getDramaUrl() {
        return dramaUrl;
    }

    public void setDramaUrl(String dramaUrl) {
        this.dramaUrl = dramaUrl;
    }
}
