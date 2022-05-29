package my.project.sakuraproject.bean;

import java.io.Serializable;
import java.util.List;

public class MaliTagBean implements Serializable {
    private String title;
    private List<MaliTagList> maliTagLists;

    public MaliTagBean (String title, List<MaliTagList> maliTagLists) {
        this.title = title;
        this.maliTagLists = maliTagLists;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MaliTagList> getMaliTagLists() {
        return maliTagLists;
    }

    public void setMaliTagLists(List<MaliTagList> maliTagLists) {
        this.maliTagLists = maliTagLists;
    }

    public static class MaliTagList implements Serializable {
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

        public MaliTagList() {};

        public MaliTagList(String title, String itemTitle, String itemUrl) {
            this.title = title;
            this.itemTitle = itemTitle;
            this.itemUrl = itemUrl;
        }
    }
}
