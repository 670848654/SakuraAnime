package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface FavoriteContract {
    interface Model {
        void getData(int offset, int limit, boolean updateOrder, LoadDataCallback callback);

        void getUpdateInfo(int source, List<AnimeUpdateInfoBean> animeUpdateInfoBeans, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<AnimeListBean> list);

        void completionView(boolean complete);

        void showErrorUpdateInfo(int source);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<AnimeListBean> list);

        void completion(boolean complete);

        void error(int source);
    }

}
