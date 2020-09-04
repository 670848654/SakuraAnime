package my.project.sakuraproject.main.home;

import java.util.LinkedHashMap;

import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class HomePresenter extends Presenter<HomeContract.View> implements BasePresenter, HomeContract.LoadDataCallback {
    private HomeContract.View view;
    private HomeModel model;

    public HomePresenter(HomeContract.View view) {
        super(view);
        this.view = view;
        model = new HomeModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.showLoadingView();
        model.getData(this);
    }

    @Override
    public void success(LinkedHashMap map) {
        view.showLoadSuccess(map);
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

