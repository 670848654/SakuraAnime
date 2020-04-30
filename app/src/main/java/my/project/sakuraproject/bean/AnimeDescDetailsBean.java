package my.project.sakuraproject.bean;

import java.io.Serializable;

public class AnimeDescDetailsBean implements Serializable {
    // 标题
    private String title;
    // 链接
    private String url;
    // 是否选中
    private boolean selected;

    public AnimeDescDetailsBean(String title, String url, boolean selected) {
        this.title = title;
        this.url = url;
        this.selected = selected;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
