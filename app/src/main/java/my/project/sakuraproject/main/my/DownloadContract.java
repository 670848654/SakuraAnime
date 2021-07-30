package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface DownloadContract {
    interface Model {
        void getData(int offset, int limit, DownloadContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<DownloadBean> list);

    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<DownloadBean> list);
    }
}
