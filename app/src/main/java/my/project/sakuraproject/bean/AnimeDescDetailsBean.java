package my.project.sakuraproject.bean;

import java.io.Serializable;

public class AnimeDescDetailsBean implements Serializable {
    // 标题
    private String title;
    // 链接
    private String url;
    // 是否选中
    private boolean selected;

    private String downloadDataId;

    public AnimeDescDetailsBean(String title, String url, boolean selected) {
        this.title = title;
        this.url = url;
        this.selected = selected;
    }

    // 本地播放器使用
    public AnimeDescDetailsBean(String title, String url, boolean selected, String downloadDataId) {
        this.title = title;
        this.url = url;
        this.selected = selected;
        this.downloadDataId = downloadDataId;
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

    public String getDownloadDataId() {
        return downloadDataId;
    }

    public void setDownloadDataId(String downloadDataId) {
        this.downloadDataId = downloadDataId;
    }
}
