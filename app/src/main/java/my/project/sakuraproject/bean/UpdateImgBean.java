package my.project.sakuraproject.bean;

public class UpdateImgBean {
    private String oldImgUrl;
    private String descUrl;

    public UpdateImgBean(String oldImgUrl, String descUrl) {
        this.oldImgUrl = oldImgUrl;
        this.descUrl = descUrl;
    }

    public String getOldImgUrl() {
        return oldImgUrl;
    }

    public void setOldImgUrl(String oldImgUrl) {
        this.oldImgUrl = oldImgUrl;
    }

    public String getDescUrl() {
        return descUrl;
    }

    public void setDescUrl(String descUrl) {
        this.descUrl = descUrl;
    }
}
