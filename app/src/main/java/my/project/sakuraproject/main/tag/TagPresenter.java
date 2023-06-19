package my.project.sakuraproject.main.tag;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class TagPresenter extends Presenter<TagContract.View> implements BasePresenter, TagContract.LoadDataCallback {
    private String url;
    private TagContract.View view;
    private TagModel model;
    private String[] siliParams;

    public TagPresenter(String url, String[] siliParams, TagContract.View view) {
        super(view);
        this.url = url;
        this.siliParams = siliParams;;
        this.view = view;
        model = new TagModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        model.getData(url, siliParams, this);
    }

    @Override
    public void success(boolean isSilisili, List<TagBean> list) {
        view.showTagSuccessView(isSilisili, list);
    }

    @Override
    public void siliAnimeList(List<AnimeListBean> animeListBeans, int pageCount) {
        view.showDefaultSiliAnimeList(animeListBeans, pageCount);
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }

    @Override
    public void log(String url) {
        view.showLog(url);
    }
}
