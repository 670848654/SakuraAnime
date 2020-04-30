package my.project.sakuraproject.main.desc;

import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;


public interface DescContract {
    interface Model {
        void getData(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessMainView(AnimeDescListBean bean);

        void showSuccessDescView(AnimeListBean bean);

        void showSuccessFavorite(boolean favorite);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successMain(AnimeDescListBean bean);

        void successDesc(AnimeListBean bean);

        void isFavorite(boolean favorite);
    }
}
