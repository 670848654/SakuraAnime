package my.project.sakuraproject.main.home.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import androidx.fragment.app.Fragment;
import butterknife.Unbinder;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.main.base.Presenter;

public abstract class BaseFragment<V, P extends Presenter<V>> extends Fragment {
    protected P mPresenter;
    protected View errorView, emptyView;
    protected TextView errorTitle;
    protected Sakura application;
    protected Unbinder mUnBinder;
    protected int position = 0;
    protected boolean isPortrait;
    protected int change;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration mConfiguration = getResources().getConfiguration();
        change = mConfiguration.orientation;
        if (change == mConfiguration.ORIENTATION_LANDSCAPE) isPortrait = false;
        else if (change == mConfiguration.ORIENTATION_PORTRAIT) isPortrait = true;
        mPresenter = createPresenter();
        initCustomViews();
        if (application == null) application = (Sakura) getActivity().getApplication();
        View view = initViews(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        loadData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
        EventBus.getDefault().unregister(this);
        mUnBinder.unbind();
    }

    public void initCustomViews() {
        errorView = getLayoutInflater().inflate(R.layout.base_error_view, null);
        errorTitle = errorView.findViewById(R.id.title);
        emptyView = getLayoutInflater().inflate(R.layout.base_emnty_view, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 防止两次调用
        if (newConfig.orientation == change) return;
        change = newConfig.orientation;
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setConfigurationChanged();
    }

    protected abstract void setConfigurationChanged();

    protected abstract View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected abstract P createPresenter();

    protected abstract void loadData();

    public abstract void onEvent(Refresh refresh);
}
