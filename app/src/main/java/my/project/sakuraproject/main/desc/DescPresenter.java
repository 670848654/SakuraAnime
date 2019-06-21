package my.project.sakuraproject.main.desc;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BasePresenter;
import my.project.sakuraproject.main.base.Presenter;


public class DescPresenter extends Presenter<DescContract.View> implements BasePresenter, DescContract.LoadDataCallback {
    private String url;
    private DescContract.View view;
    private DescModel model;

    public DescPresenter(String url, DescContract.View view) {
        super(view);
        this.url = url;
        this.view = view;
        model = new DescModel();
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.showLoadingView();
        model.getData(url, this);
    }

    @Override
    public void successMain(List<MultiItemEntity> list) {
        view.showSuccessMainView(list);
    }

    @Override
    public void successDesc(AnimeListBean bean) {
        view.showSuccessDescView(bean);
    }

    @Override
    public void isFavorite(boolean favorite) {
        view.showSuccessFavorite(favorite);
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }
}
