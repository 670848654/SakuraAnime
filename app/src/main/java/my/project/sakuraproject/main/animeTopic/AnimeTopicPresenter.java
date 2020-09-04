package my.project.sakuraproject.main.animeTopic;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class AnimeTopicPresenter extends Presenter<AnimeTopicContract.View> implements BasePresenter, AnimeTopicContract.LoadDataCallback {
    private String url;
    private int page;
    private AnimeTopicContract.View view;
    private AnimeTopicModel model;

    public AnimeTopicPresenter(String url, int page, AnimeTopicContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        this.page = page;
        model = new AnimeTopicModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        model.getData(url, page, isMain, this);
    }

    @Override
    public void success(boolean isMain, List<AnimeListBean> list) {
        view.showSuccessView(isMain, list);
    }

    @Override
    public void error(boolean isMain, String msg) {
        view.showErrorView(isMain, msg);
    }

    @Override
    public void pageCount(int count) {
        view.getPageCountSuccessView(count);
    }

    @Override
    public void error(String msg) {
    }

    @Override
    public void log(String url) {
        view.showLog(url);
    }
}
