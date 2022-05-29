package my.project.sakuraproject.main.animeList;

import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.Presenter;

public class AnimeListPresenter extends Presenter<AnimeListContract.View> implements AnimeListContract.LoadDataCallback {
    private String url;
    private int page;
    private AnimeListContract.View view;
    private AnimeListModel model;

    private String[] params;

    public AnimeListPresenter(String url, int page, AnimeListContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        this.page = page;
        model = new AnimeListModel();
    }

    public AnimeListPresenter(String[] params, AnimeListContract.View view) {
        super(view);
        this.params = params;
        this.view = view;
        model = new AnimeListModel();
    }

    public void loadData(boolean isMain, boolean isMovie,  boolean isImomoe) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        try {
            model.getData(url, page, isMain, isMovie, isImomoe, this);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void loadMaliData(boolean isMain) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        model.getData(params, isMain, this);
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
