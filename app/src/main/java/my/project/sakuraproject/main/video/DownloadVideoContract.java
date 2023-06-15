package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface DownloadVideoContract {
    interface Model {
        void getData(String url, String playNumber, int source, DownloadVideoContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showYhdmVideoSuccessView(List<String> urls, String playNumber);

        void getVideoError(String playNumber);

        void showSuccessImomoeVideoUrlsView(String url, String playNumber);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successYhdmVideoUrls(List<String> urls, String playNumber);

        void error(String playNumber);

        void successImomoeVideoUrls(String url, String playNumber);
    }
}
