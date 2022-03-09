package my.project.sakuraproject.bean;

import java.io.Serializable;

/**
 * 动漫更新列表实体
 */
public class AnimeUpdateInfoBean implements Serializable {
    private String title; // 番剧标题
    private String playNumber; // 最新一集播放剧集地址
    private int source; // 来源 0 yhdm 1 imomoe

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(String playNumber) {
        this.playNumber = playNumber;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }
}
