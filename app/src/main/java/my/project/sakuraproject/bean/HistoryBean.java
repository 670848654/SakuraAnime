package my.project.sakuraproject.bean;

/**
 * 历史记录实体
 */
public class HistoryBean {
    // 番剧ID
    private String animeId;
    // 历史记录ID
    private String historyId;
    // 番剧标题
    private String title;
    // 番剧图片地址
    private String imgUrl;
    // 详情界面地址
    private String descUrl;
    // 来源 0 yhdm 1 imomoe
    private int source;
    // 播放源（仅用于imomoe）
    private int playSource;
    // 集数名称
    private String dramaNumber;
    // 集数地址
    private String dramaUrl;
    // 最后更新时间
    private String updateTime;
    // 当前播放进度（大于1秒才存）
    private long progress;
    // 视频总长度
    private long duration;

    public String getAnimeId() {
        return animeId;
    }

    public void setAnimeId(String animeId) {
        this.animeId = animeId;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getPlaySource() {
        return playSource;
    }

    public void setPlaySource(int playSource) {
        this.playSource = playSource;
    }

    public String getDramaNumber() {
        return dramaNumber;
    }

    public void setDramaNumber(String dramaNumber) {
        this.dramaNumber = dramaNumber;
    }

    public String getDramaUrl() {
        return dramaUrl;
    }

    public void setDramaUrl(String dramaUrl) {
        this.dramaUrl = dramaUrl;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
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
