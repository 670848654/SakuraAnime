package my.project.sakuraproject.bean;
public class AnimeDescRecommendBean {
    // 标题
    private String title;
    // 图片
    private String img;
    // 链接
    private String url;

    public AnimeDescRecommendBean(String title, String img, String url) {
        this.title = title;
        this.img = img;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
