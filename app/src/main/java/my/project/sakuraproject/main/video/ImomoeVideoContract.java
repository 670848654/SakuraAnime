package my.project.sakuraproject.main.video;

import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface ImomoeVideoContract {
    interface Model {
        void getData(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void cancelDialog();

        void getVideoSuccess(String playUrl);

        void getVideoError();
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(String playUrl);

        void error();
    }
}
