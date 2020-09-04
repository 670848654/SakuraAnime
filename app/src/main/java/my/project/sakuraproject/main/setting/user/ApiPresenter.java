package my.project.sakuraproject.main.setting.user;

import java.util.List;

import my.project.sakuraproject.bean.ApiBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;

public class ApiPresenter extends Presenter<ApiContract.View> implements BasePresenter, ApiContract.LoadDataCallback {
    private ApiContract.View view;
    private ApiModel model;

    public ApiPresenter(ApiContract.View view) {
        super(view);
        this.view = view;
        model = new ApiModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain) {
            view.showLoadingView();
            view.showEmptyVIew();
        }
        model.getData(this);
    }

    @Override
    public void success(List<ApiBean> list) {
        view.showSuccess(list);
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }

    @Override
    public void log(String url) {
        
    }
}
