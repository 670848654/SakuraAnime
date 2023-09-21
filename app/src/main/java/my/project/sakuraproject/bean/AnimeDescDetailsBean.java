package my.project.sakuraproject.bean;

import java.io.Serializable;

public class AnimeDescDetailsBean implements Serializable {
    // 标题
    private String title;
    // 链接
    private String url;
    // 集数列表下标，用于silisili弹幕接口
    private Integer index;
    // 是否选中
    private boolean selected;

    private String downloadDataId;

    public AnimeDescDetailsBean(int index, String title, String url, boolean selected) {
        this.index = index;
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

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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
