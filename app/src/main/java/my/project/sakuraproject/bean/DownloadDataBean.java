package my.project.sakuraproject.bean;

import java.io.Serializable;

public class DownloadDataBean implements Serializable {
    private String id;
    private String animeTitle;
    private String animeImg;
    private String playNumber;
    private int complete;
    private String path;
    private long fileSize;
    private int source;
    private int imomoeSource;
    private long taskId;
    private long progress;
    private long duration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnimeTitle() {
        return animeTitle;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public String getAnimeImg() {
        return animeImg;
    }

    public void setAnimeImg(String animeImg) {
        this.animeImg = animeImg;
    }

    public String getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(String playNumber) {
        this.playNumber = playNumber;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getImomoeSource() {
        return imomoeSource;
    }

    public void setImomoeSource(int imomoeSource) {
        this.imomoeSource = imomoeSource;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
