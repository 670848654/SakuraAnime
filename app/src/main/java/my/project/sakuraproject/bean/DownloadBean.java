package my.project.sakuraproject.bean;

public class DownloadBean {
    private String downloadId;
    private String animeTitle;
    private String imgUrl;
    private String descUrl;
    private int source;
    private int downloadDataSize;
    private String filesSize;
    private int noCompleteSize;

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public String getAnimeTitle() {
        return animeTitle;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getDescUrl() {
        return descUrl;
    }

    public void setDescUrl(String descUrl) {
        this.descUrl = descUrl;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDownloadDataSize() {
        return downloadDataSize;
    }

    public void setDownloadDataSize(int downloadDataSize) {
        this.downloadDataSize = downloadDataSize;
    }

    public String getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(String filesSize) {
        this.filesSize = filesSize;
    }

    public int getNoCompleteSize() {
        return noCompleteSize;
    }

    public void setNoCompleteSize(int noCompleteSize) {
        this.noCompleteSize = noCompleteSize;
    }
}
