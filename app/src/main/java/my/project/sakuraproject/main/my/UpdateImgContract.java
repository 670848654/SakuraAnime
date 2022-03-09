package my.project.sakuraproject.main.my;

import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface UpdateImgContract {
    interface Model {
        void getData(String oldImgUrl, String descUrl, UpdateImgContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessImg(String oldImgUrl, String imgUrl);

        void showErrorImg(String msg);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(String oldImgUrl, String imgUrl);
    }
}
