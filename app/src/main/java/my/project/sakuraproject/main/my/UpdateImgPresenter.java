package my.project.sakuraproject.main.my;

import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class UpdateImgPresenter extends Presenter<UpdateImgContract.View> implements BasePresenter, UpdateImgContract.LoadDataCallback {
    private UpdateImgContract.View view;
    private UpdateImgModel model;
    private String oldImgUrl;
    private String descUrl;

    public UpdateImgPresenter(String oldImgUrl, String descUrl, UpdateImgContract.View view) {
        super(view);
        this.view = view;
        this.oldImgUrl = oldImgUrl;
        this.descUrl = descUrl;
        model = new UpdateImgModel();
    }

    @Override
    public void loadData(boolean isMain) {

    }

    public void loadData() {
        model.getData(oldImgUrl, descUrl, this);
    }

    @Override
    public void success(String oldImgUrl, String imgUrl) {
        view.showSuccessImg(oldImgUrl, imgUrl);
    }

    @Override
    public void error(String msg) {
        view.showErrorImg(msg);
    }

    @Override
    public void log(String url) {

    }
}
