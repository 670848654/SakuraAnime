package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.bean.YhdmVideoUrlBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface DownloadVideoContract {
    interface Model {
        void getData(String url, String playNumber, int source, DownloadVideoContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showYhdmVideoSuccessView(YhdmVideoUrlBean yhdmViideoUrlBean, String playNumber);

        void getVideoError(String playNumber);

        void showSuccessImomoeVideoUrlsView(String url, String playNumber);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successYhdmVideoUrls(YhdmVideoUrlBean yhdmViideoUrlBean, String playNumber);

        void error(String playNumber);

        void successImomoeVideoUrls(String url, String playNumber);
    }
}
