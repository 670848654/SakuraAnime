package my.project.sakuraproject.main.search;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface SearchContract {
    interface Model {
        void getData(String url, int page, boolean isMain, String wd, boolean isSiliTag,  LoadDataCallback callback) throws UnsupportedEncodingException;
    }

    interface View extends BaseView {
        void showSuccessView(boolean isMain, List<AnimeListBean> list);

        void showErrorView(boolean isMain, String msg);

        void getPageCount(int pageCount);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(boolean isMain, List<AnimeListBean> list);

        void error(boolean isMain, String msg);

        void pageCount(int pageCount);
    }
}
