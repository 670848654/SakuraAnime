package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface VideoContract {
    interface Model {
        void getData(String title, String url, int source, String playNumber, LoadDataCallback callback);

//        void getVideoUrl(String url, LoadDataCallback callback);

        void getSilisiliVideoUrl(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void cancelDialog();

        void showYhdmVideoSuccessView(List<String> list);

        void showSuccessYhdmDramasView(List<AnimeDescDetailsBean> list);

        void getVideoEmpty();

        void getVideoError();

        void errorDramaView();

        void showSuccessImomoeVideoUrlView(String playUrl);

        void showSuccessImomoeDramasView(List<AnimeDescDetailsBean> bean);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successYhdmVideoUrls(List<String> list);

        void successYhdmDramas(List<AnimeDescDetailsBean> list);

        void error();

        void empty();

        void successImomoeVideoUrl(String playUrl);

        void successImomoeDramas(List<AnimeDescDetailsBean> bean);
    }
}
