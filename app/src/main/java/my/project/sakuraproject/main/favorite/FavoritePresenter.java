package my.project.sakuraproject.main.favorite;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class FavoritePresenter extends Presenter<FavoriteContract.View> implements BasePresenter, FavoriteContract.LoadDataCallback {
    private FavoriteContract.View view;
    private FavoriteModel model;
    private int offset;
    private int limit;

    public FavoritePresenter(int offset, int limit, FavoriteContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        model = new FavoriteModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        model.getData(offset, limit, this);
    }

    @Override
    public void success(List<AnimeListBean> list) {
        view.showSuccessView(list);
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }

    @Override
    public void log(String url) {

    }
}
