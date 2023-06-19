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
    private List<String> siliParams;

    public AnimeListPresenter(String url, List<String> siliParams, int page, AnimeListContract.View view) {
        super(view);
        this.url = url;
        this.siliParams = siliParams;
        this.page = page;
        this.view = view;
        model = new AnimeListModel();
    }

    public void loadData(boolean isMain, boolean isMovie,  boolean isImomoe, boolean isToptic) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        try {
            model.getData(url, page, isMain, isMovie, isImomoe, isToptic, siliParams, this);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
