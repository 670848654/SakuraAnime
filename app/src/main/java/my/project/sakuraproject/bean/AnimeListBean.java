package my.project.sakuraproject.bean;

import java.util.List;

public class AnimeListBean {
    private String title;
    private String url;
    private String desc;
    private String sy;
    private String dq;
    private String lx;
    private String bq;
    private String img;
    private List<String> tagTitles;
    private List<String> tagUrls;
    private String score;
    private String updateTime;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<String> getTagTitles() {
        return tagTitles;
    }

    public void setTagTitles(List<String> tagTitles) {
        this.tagTitles = tagTitles;
    }

    public List<String> getTagUrls() {
        return tagUrls;
    }

    public void setTagUrls(List<String> tagUrls) {
        this.tagUrls = tagUrls;
    }

    public AnimeListBean() {
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getSy() {
        return sy;
    }

    public void setSy(String sy) {
        this.sy = sy;
    }

    public String getDq() {
        return dq;
    }

    public void setDq(String dq) {
        this.dq = dq;
    }

    public String getLx() {
        return lx;
    }

    public void setLx(String lx) {
        this.lx = lx;
    }

    public String getBq() {
        return bq;
    }

    public void setBq(String bq) {
        this.bq = bq;
    }
}
