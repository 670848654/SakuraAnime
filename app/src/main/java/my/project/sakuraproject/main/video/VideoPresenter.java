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
    private int source;
    private String playNumber;

    public VideoPresenter(String title, String url, int source, String playNumber, VideoContract.View view) {
        super(view);
        this.title = title;
        this.url = url;
        this.source = source;
        this.playNumber = playNumber;
        this.view = view;
        playModel = new VideoModel();
    }

    public VideoPresenter(String url, VideoContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        playModel = new VideoModel();
    }

    @Override
    public void loadData(boolean isMain) {
        playModel.getData(title, url, source, playNumber, this);
    }

    public void tryGetSilisiliPlayUrl() {
        playModel.getSilisiliVideoUrl(url, this);
    }

    /*public void loadVideoUrls() {
        playModel.getVideoUrl(url, this);
    }*/

    @Override
    public void successYhdmVideoUrls(List<String> list) {
        view.showYhdmVideoSuccessView(list);
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
    public void successYhdmDramas(List<AnimeDescDetailsBean> list) {
        if (list.size() > 0)
            view.showSuccessYhdmDramasView(list);
        else
            view.errorDramaView();
    }

    @Override
    public void successImomoeVideoUrl(String playUrl) {
        if (!playUrl.isEmpty())
            view.showSuccessImomoeVideoUrlView(playUrl);
        else
            view.errorDramaView();
    }

    @Override
    public void successImomoeDramas(List<AnimeDescDetailsBean> bean) {
        if (bean.size() > 0)
            view.showSuccessImomoeDramasView(bean);
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
