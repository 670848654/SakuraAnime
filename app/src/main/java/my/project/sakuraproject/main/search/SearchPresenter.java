package my.project.sakuraproject.main.search;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class SearchPresenter extends Presenter<SearchContract.View> implements BasePresenter, SearchContract.LoadDataCallback {
    private String url;
    private int page;
    private SearchContract.View view;
    private SearchModel model;

    public SearchPresenter(String url, int page, SearchContract.View view) {
        super(view);
        this.url = url;
        this.page = page;
        this.view = view;
        model = new SearchModel();
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
    public void pageCount(int pageCount) {
        view.getPageCount(pageCount);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void log(String url) {
        view.showLog(url);
    }
}
