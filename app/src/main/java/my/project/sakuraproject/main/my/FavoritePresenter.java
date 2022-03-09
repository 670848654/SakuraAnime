package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class FavoritePresenter extends Presenter<FavoriteContract.View> implements BasePresenter, FavoriteContract.LoadDataCallback {
    private FavoriteContract.View view;
    private FavoriteModel model;
    private int offset;
    private int limit;
    private boolean updateOrder;
    private int source;
    private List<AnimeUpdateInfoBean> animeUpdateInfoBeans;

    public FavoritePresenter(int offset, int limit, boolean updateOrder, FavoriteContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        this.updateOrder = updateOrder;
        model = new FavoriteModel();
    }

    public FavoritePresenter(int source, List<AnimeUpdateInfoBean> animeUpdateInfoBeans, FavoriteContract.View view) {
        super(view);
        this.view = view;
        this.source = source;
        this.animeUpdateInfoBeans = animeUpdateInfoBeans;
        model = new FavoriteModel();
    }

    public void loadUpdateInfo() {
        model.getUpdateInfo(source, animeUpdateInfoBeans, this);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        model.getData(offset, limit, updateOrder, this);
    }

    @Override
    public void success(List<AnimeListBean> list) {
        view.showSuccessView(list);
    }

    @Override
    public void completion(boolean complete) {
        view.completionView(complete);
    }

    @Override
    public void error(int source) {
        view.showErrorUpdateInfo(source);
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }

    @Override
    public void log(String url) {

    }
}
