package my.project.sakuraproject.main.setting.user;

import java.util.List;

import my.project.sakuraproject.bean.ApiBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface ApiContract {
    interface Model {
        void getData(LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccess(List<ApiBean> list);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<ApiBean> list);
    }
}
