package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.HistoryBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class HistoryPresenter extends Presenter<HistoryContract.View> implements BasePresenter, HistoryContract.LoadDataCallback {
    private HistoryContract.View view;
    private HistoryModel model;
    private int offset;
    private int limit;

    public HistoryPresenter(int offset, int limit, HistoryContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        model = new HistoryModel();
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }

    @Override
    public void log(String url) {

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
    public void success(List<HistoryBean> list) {
        view.showSuccessView(list);
    }
}
