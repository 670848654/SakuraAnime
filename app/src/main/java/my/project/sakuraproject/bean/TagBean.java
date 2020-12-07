package my.project.sakuraproject.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import my.project.sakuraproject.adapter.TagAdapter;

public class TagBean implements MultiItemEntity {
    private String title;
    private String itemTitle;
    private String itemUrl;
    private boolean selected;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemUrl() {
        return itemUrl;
    }

    public void setItemUrl(String itemUrl) {
        this.itemUrl = itemUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public TagBean(String title, String itemTitle, String itemUrl) {
        this.title = title;
        this.itemTitle = itemTitle;
        this.itemUrl = itemUrl;
    }

    @Override
    public int getItemType() {
        return TagAdapter.TYPE_LEVEL_1;
    }
}
