package my.project.sakuraproject.main.desc;

import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;


public class DescPresenter extends Presenter<DescContract.View> implements BasePresenter, DescContract.LoadDataCallback {
    private String url;
    private DescContract.View view;
    private DescModel model;

    public DescPresenter(String url, DescContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        model = new DescModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.showLoadingView();
        model.getData(url, this);
    }

    @Override
    public void successMain(AnimeDescListBean bean) {
        view.showSuccessMainView(bean);
    }

    @Override
    public void successDesc(AnimeListBean bean) {
        view.showSuccessDescView(bean);
    }

    @Override
    public void isFavorite(boolean favorite) {
        view.showSuccessFavorite(favorite);
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
