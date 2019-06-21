package my.project.sakuraproject.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

public class AnimeDescBean implements MultiItemEntity, Serializable {
    //布局TYPE
    private int typeLevel;
    //标题
    private String title;
    //地址
    private String url;
    //type
    private String type;
    //是否能被选中
    private boolean select;
    //图片地址
    private String img;

    /**
     * 按钮
     *
     * @param typeLevel
     * @param select
     * @param title
     * @param url
     * @param type
     */
    public AnimeDescBean(int typeLevel, boolean select, String title, String url, String type) {
        this.typeLevel = typeLevel;
        this.select = select;
        this.title = title;
        this.url = url;
        this.type = type;
    }

    /**
     * 推荐
     *
     * @param typeLevel
     * @param title
     * @param url
     */
    public AnimeDescBean(int typeLevel, String title, String url, String img, String type) {
        this.typeLevel = typeLevel;
        this.title = title;
        this.url = url;
        this.img = img;
        this.type = type;
    }

    public int getTypeLevel() {
        return typeLevel;
    }

    public void setTypeLevel(int typeLevel) {
        this.typeLevel = typeLevel;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public int getItemType() {
        return typeLevel;
    }
}
