package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.bean.YhdmViideoUrlBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface DownloadVideoContract {
    interface Model {
        void getData(String url, String playNumber, int source, DownloadVideoContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showYhdmVideoSuccessView(YhdmViideoUrlBean yhdmViideoUrlBean, String playNumber);

        void getVideoError(String playNumber);

        void showSuccessImomoeVideoUrlsView(List<List<ImomoeVideoUrlBean>> bean, String playNumber);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successYhdmVideoUrls(YhdmViideoUrlBean yhdmViideoUrlBean, String playNumber);

        void error(String playNumber);

        void successImomoeVideoUrls(List<List<ImomoeVideoUrlBean>> bean, String playNumber);
    }
}
