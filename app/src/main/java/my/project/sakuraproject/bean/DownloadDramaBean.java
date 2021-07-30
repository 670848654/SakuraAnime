package my.project.sakuraproject.bean;

public class DownloadDramaBean {
    // 标题
    private String title;
    // 链接
    private String url;
    // 是否选中
    private boolean selected;

    private boolean hasDownload;

    private boolean shouldParse;

    private String yhdmUrl;

    private String imomoeParma;

    private String imomoeVid;

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

    public boolean isHasDownload() {
        return hasDownload;
    }

    public void setHasDownload(boolean hasDownload) {
        this.hasDownload = hasDownload;
    }

    public boolean isShouldParse() {
        return shouldParse;
    }

    public void setShouldParse(boolean shouldParse) {
        this.shouldParse = shouldParse;
    }

    public String getYhdmUrl() {
        return yhdmUrl;
    }

    public void setYhdmUrl(String yhdmUrl) {
        this.yhdmUrl = yhdmUrl;
    }

    public String getImomoeParma() {
        return imomoeParma;
    }

    public void setImomoeParma(String imomoeParma) {
        this.imomoeParma = imomoeParma;
    }

    public String getImomoeVid() {
        return imomoeVid;
    }

    public void setImomoeVid(String imomoeVid) {
        this.imomoeVid = imomoeVid;
    }
}
