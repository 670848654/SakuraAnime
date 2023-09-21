package my.project.sakuraproject.main.video;

import com.alibaba.fastjson.JSONObject;

import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class DanmuPresenter extends Presenter<DanmuContract.View> implements BasePresenter, DanmuContract.LoadDataCallback {
    private String title;
    private String drama;
    private DanmuModel model;
    private DanmuContract.View view;

    public DanmuPresenter(String title, String drama, DanmuContract.View view) {
        super(view);
        this.title = title;
        this.drama = drama;
        this.view = view;
        model = new DanmuModel();
    }

    public void loadDanmu() {
        model.getDanmu(title, drama, this);
    }

    @Override
    public void loadData(boolean isMain) {

    }

    @Override
    public void successDanmu(JSONObject danmus) {
        view.showSuccessDanmuView(danmus);
    }

    @Override
    public void successDanmuXml(String content) {
        view.showSuccessDanmuXmlView(content);
    }

    @Override
    public void errorDanmu(String msg) {
        view.showErrorDanmuView(msg);
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void log(String url) {

    }
}
