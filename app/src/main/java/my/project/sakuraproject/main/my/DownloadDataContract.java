package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface DownloadDataContract {
    interface Model {
        void getData(String downloadId, int offset, int limit, DownloadDataContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<DownloadDataBean> list);

    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<DownloadDataBean> list);
    }
}
