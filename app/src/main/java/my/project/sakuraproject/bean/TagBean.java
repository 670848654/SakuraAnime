package my.project.sakuraproject.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import my.project.sakuraproject.adapter.TagAdapter;

public class TagBean implements MultiItemEntity {
    private String title;
    private String url;
    private String type;
    private String desc;
    private boolean select;
    private String witchTitle;
    private String witchUrl;

    public String getWitchTitle() {
        return witchTitle;
    }

    public void setWitchTitle(String witchTitle) {
        this.witchTitle = witchTitle;
    }

    public String getWitchUrl() {
        return witchUrl;
    }

    public void setWitchUrl(String witchUrl) {
        this.witchUrl = witchUrl;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public TagBean(String title, String url, String witchTitle, String witchUrl) {
        this.title = title;
        this.url = url;
        this.witchTitle = witchTitle;
        this.witchUrl = witchUrl;
    }

    public TagBean(String title, String url, String desc, boolean state) {
        this.title = title;
        this.url = url;
        this.desc = desc;
    }

    public TagBean(boolean select, String title, String url, String type) {
        this.select = select;
        this.title = title;
        this.url = url;
        this.type = type;
    }

    @Override
    public int getItemType() {
        return TagAdapter.TYPE_LEVEL_1;
    }
}
