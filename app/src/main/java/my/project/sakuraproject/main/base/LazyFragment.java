package my.project.sakuraproject.main.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public abstract class LazyFragment extends Fragment {
    private boolean isFragmentVisible;
    private boolean isPrepared;
    private boolean isFirstLoad = true;
    private boolean forceLoad = false;
    protected int position = 0;
    protected boolean isPortrait;
    protected int change;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration mConfiguration = getResources().getConfiguration();
        change = mConfiguration.orientation;
        if (change == mConfiguration.ORIENTATION_LANDSCAPE) isPortrait = false;
        else if (change == mConfiguration.ORIENTATION_PORTRAIT) isPortrait = true;
        isFirstLoad = true;
        View view = initViews(inflater, container, savedInstanceState);
        isPrepared = true;
        lazyLoad();
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    protected void onVisible() {
        isFragmentVisible = true;
        lazyLoad();
    }

    protected void onInvisible() {
        isFragmentVisible = false;
    }

    protected void lazyLoad() {
        if (isPrepared() && isFragmentVisible()) {
            if (forceLoad || isFirstLoad()) {
                forceLoad = false;
                isFirstLoad = false;
                initData();
            }
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isPrepared = false;
    }

    protected abstract View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    protected abstract void initData();

    public boolean isPrepared() {
        return isPrepared;
    }


    public boolean isFirstLoad() {
        return isFirstLoad;
    }

    public boolean isFragmentVisible() {
        return isFragmentVisible;
    }
}
