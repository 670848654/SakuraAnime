package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class DownloadPresenter extends Presenter<DownloadContract.View> implements BasePresenter, DownloadContract.LoadDataCallback {
    private DownloadContract.View view;
    private DownloadModel model;
    private int offset;
    private int limit;

    public DownloadPresenter(int offset, int limit, DownloadContract.View view) {
        super(view);
        this.view = view;
        this.offset = offset;
        this.limit = limit;
        model = new DownloadModel();
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
    public void success(List<DownloadBean> list) {
        view.showSuccessView(list);
    }
}
