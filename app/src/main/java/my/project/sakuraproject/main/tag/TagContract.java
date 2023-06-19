package my.project.sakuraproject.main.tag;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface TagContract {
    interface Model {
        void getData(String url, String[] siliParams, LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showTagSuccessView(boolean isSilisili, List<TagBean> list);

        void showDefaultSiliAnimeList(List<AnimeListBean> animeListBeans, int pageCount);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(boolean isSilisili, List<TagBean> list);

        void siliAnimeList(List<AnimeListBean> animeListBeans, int pageCount);
    }
}
