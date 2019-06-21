package my.project.sakuraproject.main.home;

import java.util.LinkedHashMap;

import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface HomeContract {
    interface Model {
        void getData(LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showLoadSuccess(LinkedHashMap map);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(LinkedHashMap map);
    }
}
