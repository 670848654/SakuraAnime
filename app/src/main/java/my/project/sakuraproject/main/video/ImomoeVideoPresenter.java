package my.project.sakuraproject.main.video;

import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class ImomoeVideoPresenter extends Presenter<ImomoeVideoContract.View> implements BasePresenter, ImomoeVideoContract.LoadDataCallback {
    private ImomoeVideoContract.View view;
    private ImomoeVideoModel model;
    private String url;

    public ImomoeVideoPresenter(String url, ImomoeVideoContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        model = new ImomoeVideoModel();
    }

    @Override
    public void loadData(boolean isMain) {
        model.getData(url, this);
    }

    @Override
    public void success(String playUrl) {
        view.getVideoSuccess(playUrl);
    }

    @Override
    public void error() {
        view.getVideoError();
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void log(String url) {

    }
}
