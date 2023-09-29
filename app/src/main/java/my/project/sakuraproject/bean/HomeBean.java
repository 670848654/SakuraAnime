package my.project.sakuraproject.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import my.project.sakuraproject.adapter.HomeAdapter;

public class HomeBean implements MultiItemEntity {
    private String title;
    private String moreUrl;
    private int dataType;
    private List<HomeItemBean> data;

    public HomeBean () {};

    public HomeBean (int dataType, String title, String moreUrl, List<HomeItemBean> data) {
        this.dataType = dataType;
        this.title = title;
        this.moreUrl = moreUrl;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMoreUrl() {
        return moreUrl;
    }

    public void setMoreUrl(String moreUrl) {
        this.moreUrl = moreUrl;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public List<HomeItemBean> getData() {
        return data;
    }

    public void setData(List<HomeItemBean> data) {
        this.data = data;
    }

    @Override
    public int getItemType() {
        return dataType;
    }

    public static class HomeItemBean {
        private String title;
        private String img;
        private String url;
        private String episodes;

        public HomeItemBean () {};

        public HomeItemBean(String title, String img, String url, String episodes) {
            this.title = title;
            this.img = img;
            this.url = url;
            this.episodes = episodes;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getEpisodes() {
            return episodes;
        }

        public void setEpisodes(String episodes) {
            this.episodes = episodes;
        }
    }
}
