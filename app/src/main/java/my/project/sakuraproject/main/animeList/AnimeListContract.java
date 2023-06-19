package my.project.sakuraproject.main.animeList;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface AnimeListContract {
    interface Model {
        void getData(String url, int page, boolean isMain, boolean isMovie, boolean isImomoe, boolean isToptic, List<String> siliParams, LoadDataCallback callback) throws UnsupportedEncodingException;

        void getData(String[] params, boolean isMain, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(boolean isMain, List<AnimeListBean> list);

        void showErrorView(boolean isMain, String msg);

        void getPageCountSuccessView(int count);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(boolean isMain, List<AnimeListBean> list);

        void error(boolean isMain, String msg);

        void pageCount(int count);
    }
}
