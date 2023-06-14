package my.project.sakuraproject.main.tag;

import java.util.List;

import my.project.sakuraproject.bean.MaliTagBean;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface TagContract {
    interface Model {
        void getData(LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessView(List<TagBean> list);

        void showMaliSuccessView(List<MaliTagBean> list);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<TagBean> list);

        void maliSuccess(List<MaliTagBean> list);
    }
}
