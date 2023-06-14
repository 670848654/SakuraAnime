package my.project.sakuraproject.bean;

import androidx.annotation.DrawableRes;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import my.project.sakuraproject.adapter.HomeAdapter;

public class HomeHeaderBean implements MultiItemEntity {
    public static final int TYPE_XFSJB = 0;
    public static final int TYPE_DMFL = 1;
    public static final int TYPE_DMDY = 2;
    public static final int TYPE_DMZT = 3;
    public static final int TYPE_JCB = 4;
    public static final int TYPE_DMFL_MALIMALI_TAG = 5;
    public static final int TYPE_DMFL_MALIMALI_JAPAN = 6;
    public static final int TYPE_DMFL_MALIMALI_CHINA = 7;
    public static final int TYPE_DMFL_MALIMALI_EUROPE  = 8;

    private List<HeaderDataBean> data;

    public HomeHeaderBean (List<HeaderDataBean> data) {
        this.data = data;
    }

    public List<HeaderDataBean> getData() {
        return data;
    }

    public void setData(List<HeaderDataBean> data) {
        this.data = data;
    }

    public static class HeaderDataBean  {
        private String title;
        @DrawableRes
        private int img;
        private int type;

        public HeaderDataBean(String title,  int img, int type) {
            this.title = title;
            this.img = img;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getImg() {
            return img;
        }

        public void setImg(int img) {
            this.img = img;
        }
    }

    @Override
    public int getItemType() {
        return HomeAdapter.TYPE_LEVEL_0;
    }

}
