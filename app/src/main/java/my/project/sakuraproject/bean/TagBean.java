package my.project.sakuraproject.bean;

import java.util.List;

public class TagBean {
    private String title;
    private List<TagSelectBean> tagSelectBeans;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TagSelectBean> getTagSelectBeans() {
        return tagSelectBeans;
    }

    public void setTagSelectBeans(List<TagSelectBean> tagSelectBeans) {
        this.tagSelectBeans = tagSelectBeans;
    }

    public static class TagSelectBean {
        private String tagTitle;
        private String title;
        private String url;
        private boolean selected;

        public String getTagTitle() {
            return tagTitle;
        }

        public void setTagTitle(String tagTitle) {
            this.tagTitle = tagTitle;
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

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
