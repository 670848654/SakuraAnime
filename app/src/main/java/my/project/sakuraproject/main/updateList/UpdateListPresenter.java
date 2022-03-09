package my.project.sakuraproject.main.updateList;

import java.util.List;

import my.project.sakuraproject.bean.AnimeUpdateBean;
import my.project.sakuraproject.main.base.Presenter;

public class UpdateListPresenter extends Presenter<UpdateListContract.View> implements UpdateListContract.LoadDataCallback {
    private String url;
    private UpdateListContract.View view;
    private UpdateListModel model;

    public UpdateListPresenter(String url, UpdateListContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        model = new UpdateListModel();
    }

    public void loadData(boolean isImomoe) {
        view.showLoadingView();
        view.showEmptyVIew();
        model.getData(url, isImomoe, this);
    }

    @Override
    public void success(List<AnimeUpdateBean> list) {
        view.showSuccessView(list);
    }

    @Override
    public void error(String msg) {
        view.showErrorView(msg);
    }

    @Override
    public void log(String url) {
        view.showLog(url);
    }
}
