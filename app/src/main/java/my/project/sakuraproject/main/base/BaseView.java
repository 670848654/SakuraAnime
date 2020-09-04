package my.project.sakuraproject.main.base;

public interface BaseView {
    //显示加载中视图
    void showLoadingView();
    //显示加载失败视图
    void showLoadErrorView(String msg);
    //空布局
    void showEmptyVIew();

    void showLog(String url);
}
