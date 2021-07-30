package my.project.sakuraproject.main.video;

import java.util.List;

import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.bean.YhdmViideoUrlBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class DownloadVideoPresenter extends Presenter<DownloadVideoContract.View> implements BasePresenter, DownloadVideoContract.LoadDataCallback {
    private DownloadVideoContract.View view;
    private DownloadVideoModel downloadModel;
    private String url;
    private int source;
    private String playNumber;

    public DownloadVideoPresenter(String url, int source, String playNumber, DownloadVideoContract.View view) {
        super(view);
        this.url = url;
        this.source = source;
        this.playNumber = playNumber;
        this.view = view;
        downloadModel = new DownloadVideoModel();
    }


    @Override
    public void loadData(boolean isMain) {
        downloadModel.getData(url, playNumber, source,this);
    }

    @Override
    public void successYhdmVideoUrls(YhdmViideoUrlBean yhdmViideoUrlBean, String playNumber) {
        view.showYhdmVideoSuccessView(yhdmViideoUrlBean, playNumber);
    }

    @Override
    public void error(String playNumber) {
        view.getVideoError(playNumber);
    }

    @Override
    public void successImomoeVideoUrls(List<List<ImomoeVideoUrlBean>> bean, String playNumber) {
        view.showSuccessImomoeVideoUrlsView(bean, playNumber);
    }


    @Override
    public void log(String url) {}
}
