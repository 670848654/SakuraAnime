package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class VideoPresenter extends Presenter<VideoContract.View> implements BasePresenter, VideoContract.LoadDataCallback {
    private VideoContract.View view;
    private VideoModel playModel;
    private String title;
    private String url;

    public VideoPresenter(String title, String url, VideoContract.View view) {
        super(view);
        this.title = title;
        this.url = url;
        this.view = view;
        playModel = new VideoModel();
    }

    @Override
    public void loadData(boolean isMain) {
        playModel.getData(title, url, this);
    }

    @Override
    public void success(List<String> list) {
        view.getVideoSuccess(list);
    }

    @Override
    public void error() {
        view.cancelDialog();
        view.getVideoError();
    }

    @Override
    public void empty() {
        view.cancelDialog();
        view.getVideoEmpty();
    }

    @Override
    public void successDrama(List<AnimeDescDetailsBean> list) {
        if (list.size() > 0)
            view.showSuccessDramaView(list);
        else
            view.errorDramaView();
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void log(String url) {
        view.showLog(url);
    }
}
