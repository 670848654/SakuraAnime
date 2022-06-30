package my.project.sakuraproject.main.home;

import java.util.LinkedHashMap;
import java.util.List;

import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface HomeContract {
    interface Model {
        void getData(boolean isWeek, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showLoadSuccess(LinkedHashMap map);

        void showHomeLoadSuccess(List<HomeBean> beans);

        void showUpdateInfoSuccess(List<AnimeUpdateInfoBean> animeUpdateInfoBeans);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(LinkedHashMap map);

        void homeSuccess(List<HomeBean> beans);

        void updateInfoSuccess(List<AnimeUpdateInfoBean> animeUpdateInfoBeans);
    }
}
