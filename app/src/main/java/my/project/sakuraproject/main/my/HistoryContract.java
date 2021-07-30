package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.HistoryBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface HistoryContract {
    interface Model {
        void getData(int offset, int limit, HistoryContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<HistoryBean> list);

    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<HistoryBean> list);
    }
}
