package my.project.sakuraproject.main.updateList;

import java.util.List;

import my.project.sakuraproject.bean.AnimeUpdateBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface UpdateListContract {
    interface Model {
        void getData(String url, boolean isImomoe, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<AnimeUpdateBean> list);

        void showErrorView(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<AnimeUpdateBean> list);

        void error(String msg);
    }
}
