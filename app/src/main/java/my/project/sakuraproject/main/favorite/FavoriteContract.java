package my.project.sakuraproject.main.favorite;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface FavoriteContract {
    interface Model {
        void getData(LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<AnimeListBean> list);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<AnimeListBean> list);
    }

}
