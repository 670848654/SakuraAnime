package my.project.sakuraproject.main.my;

import java.util.List;

import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class DownloadDataPresenter extends Presenter<DownloadDataContract.View> implements BasePresenter, DownloadDataContract.LoadDataCallback {
    private DownloadDataContract.View view;
    private DownloadDataModel model;
    private String downloadId;
    private int offset;
    private int limit;

    public DownloadDataPresenter(String downloadId, int offset, int limit, DownloadDataContract.View view) {
        super(view);
        this.view = view;
        this.downloadId = downloadId;
        this.offset = offset;
        this.limit = limit;
        model = new DownloadDataModel();
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
        model.getData(downloadId, offset, limit, this);
    }

    @Override
    public void success(List<DownloadDataBean> list) {
        view.showSuccessView(list);
    }
}
