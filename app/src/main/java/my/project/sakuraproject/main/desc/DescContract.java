package my.project.sakuraproject.main.desc;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;


public interface DescContract {
    interface Model {
        void getData(String url, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccessMainView(List<MultiItemEntity> list);

        void showSuccessDescView(AnimeListBean bean);

        void showSuccessFavorite(boolean favorite);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void successMain(List<MultiItemEntity> list);

        void successDesc(AnimeListBean bean);

        void isFavorite(boolean favorite);
    }
}
