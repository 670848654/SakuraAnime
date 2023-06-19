package my.project.sakuraproject.main.rank;

import java.util.List;

import my.project.sakuraproject.bean.SiliSiliRankBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class RankPresenter extends Presenter<RankContract.View> implements BasePresenter, RankContract.LoadDataCallback {
    private RankContract.View view;
    private RankModel model;

    public RankPresenter(RankContract.View view) {
        super(view);
        this.view = view;
        model = new RankModel();
    }

    @Override
    public void loadData(boolean isMain) {
        view.showLoadingView();
        model.getData(this);
    }

    @Override
    public void success(List<SiliSiliRankBean> siliSiliRankBeans) {
        view.showSuccess(siliSiliRankBeans);
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

