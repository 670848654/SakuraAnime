package my.project.sakuraproject.bean;

public class RefreshDownloadData {
    private String id;
    private long playPosition;
    private long videoDuration;

    public RefreshDownloadData(String id, long playPosition, long videoDuration) {
        this.id = id;
        this.playPosition = playPosition;
        this.videoDuration = videoDuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPlayPosition() {
        return playPosition;
    }

    public void setPlayPosition(long playPosition) {
        this.playPosition = playPosition;
    }

    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }
}
