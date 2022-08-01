package my.project.sakuraproject.bean;

public class DownloadEvent {
    private String title;
    private String drama;
    private String filePath;
    private long fileSize;

    public DownloadEvent(String title, String drama, String filePath, long fileSize) {
        this.title = title;
        this.drama = drama;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDrama() {
        return drama;
    }

    public void setDrama(String drama) {
        this.drama = drama;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
