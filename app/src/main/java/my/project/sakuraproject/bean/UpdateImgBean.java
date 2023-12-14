package my.project.sakuraproject.bean;

/**
 * 更新图片地址
 * <p>
 * 当图片加载失败时通过详情地址获取新的图片更新数据库
 * </p>
 */
public class UpdateImgBean {
    private String oldImgUrl; // 老图片地址
    private String descUrl; // 详情地址

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
