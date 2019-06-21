package my.project.sakuraproject.bean;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import my.project.sakuraproject.adapter.TagAdapter;

public class TagHeaderBean extends AbstractExpandableItem<TagBean> implements MultiItemEntity {
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TagHeaderBean(String title) {
        this.title = title;
    }

    @Override
    public int getLevel() {
        return TagAdapter.TYPE_LEVEL_0;
    }

    @Override
    public int getItemType() {
        return 0;
    }
}
