package my.project.sakuraproject.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import androidx.annotation.DrawableRes;
import my.project.sakuraproject.adapter.HomeAdapter;

public class HomeHeaderBean implements MultiItemEntity {
    public static final int TYPE_XFSJB = 0;
    public static final int TYPE_DMFL = 1;
    public static final int TYPE_DMDY = 2;
    public static final int TYPE_DMZT = 3;
    public static final int TYPE_JCB = 4;
    public static final int TYPE_DMFL_SILISILI_XFRM = 5;
    public static final int TYPE_DMFL_SILISILI_XFGM = 6;
    public static final int TYPE_DMFL_SILISILI_WJDM = 7;
    public static final int TYPE_DMFL_SILISILI_JCB  = 8;
    public static final int TYPE_DMFL_SILISILI_PHB  = 9;
    public static final int TYPE_DMFL_SILISILI_ZT  = 10;

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
