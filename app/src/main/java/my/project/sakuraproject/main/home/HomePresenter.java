package my.project.sakuraproject.main.home;

import java.util.LinkedHashMap;
import java.util.List;

import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class HomePresenter extends Presenter<HomeContract.View> implements BasePresenter, HomeContract.LoadDataCallback {
    private boolean isWeek;
    private HomeContract.View view;
    private HomeModel model;

    public HomePresenter(boolean isWeek, HomeContract.View view) {
        super(view);
        this.isWeek = isWeek;
        this.view = view;
        model = new HomeModel();
    }

    /*public HomePresenter(boolean isWeek, HomeContract.View view) {
        super(view);
        this.isWeek = isWeek;
        this.view = view;
        model = new HomeModel();
    }*/

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.showLoadingView();
        model.getData(isWeek, this);
    }

    @Override
    public void success(LinkedHashMap map) {
        view.showLoadSuccess(map);
    }

    @Override
    public void homeSuccess(List<HomeBean> beans) {
        view.showHomeLoadSuccess(beans);
    }

    @Override
    public void updateInfoSuccess(List<AnimeUpdateInfoBean> animeUpdateInfoBeans) {
        view.showUpdateInfoSuccess(animeUpdateInfoBeans);
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

