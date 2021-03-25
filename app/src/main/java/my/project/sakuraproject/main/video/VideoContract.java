package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface VideoContract {
    interface Model {
        void getData(String title, String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void cancelDialog();

        void getVideoSuccess(List<String> list);

        void getVideoEmpty();

        void getVideoError();

        void showSuccessDramaView(List<AnimeDescDetailsBean> list);

        void errorDramaView();

        void showSuccessImomoeDramaView(List<List<ImomoeVideoUrlBean>> bean);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<String> list);

        void error();

        void empty();

        void successDrama(List<AnimeDescDetailsBean> list);

        void successImomoeDrama(List<List<ImomoeVideoUrlBean>> bean);
    }
}
