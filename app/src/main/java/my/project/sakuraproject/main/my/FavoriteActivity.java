package my.project.sakuraproject.main.my;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.FavoriteListAdapter;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.UpdateImgBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class FavoriteActivity extends BaseActivity<FavoriteContract.View, FavoritePresenter> implements FavoriteContract.View, UpdateImgContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    CoordinatorLayout msg;
    private FavoriteListAdapter adapter;
    private List<AnimeListBean> favoriteList = new ArrayList<>();
    private int limit = 10;
    private int favoriteCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private boolean updateOrder;
    private UpdateImgPresenter updateImgPresenter;
    
    @Override
    protected FavoritePresenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_my_list;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        initToolbar();
        initAdapter();
        loadFavoriteData();
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.favorite_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initAdapter() {
        adapter = new FavoriteListAdapter(this, favoriteList);
//        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            AnimeListBean bean = (AnimeListBean) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", bean.getTitle());
            bundle.putString("url", bean.getUrl());
            startActivityForResult(new Intent(this, DescActivity.class).putExtras(bundle), 3000);
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            View v = adapter.getViewByPosition(mRecyclerView, position, R.id.img);
            final PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.favorite_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.remove_favorite:
                        removeFavorite(position);
                        break;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (favoriteList.size() >= favoriteCount) {
                adapter.loadMoreEnd();
            } else {
                if (isErr) {
                    isMain = false;
                    mPresenter = new FavoritePresenter(favoriteList.size(), limit, updateOrder, this);
                    mPresenter.loadData(isMain);
                } else {
                    isErr = true;
                    adapter.loadMoreFail();
                }
            }
        }, 500), mRecyclerView);
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        mRecyclerView.setAdapter(adapter);
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.loadMoreComplete();
    }

    private void loadFavoriteData() {
        updateOrder = (Boolean) SharedPreferencesUtils.getParam(this, "check_favorite_update", true);
        favoriteCount = DatabaseUtil.queryFavoriteCount();
        isMain = true;
        favoriteList.clear();
        adapter.setNewData(favoriteList);
        loading.setVisibility(View.VISIBLE);
        if (favoriteCount > 0 && updateOrder) {
//            application.showSnackbarMsg(msg, Utils.getString(R.string.check_favorite_update));
            mPresenter = new FavoritePresenter(0, application.animeUpdateInfoBeans, this);
            mPresenter.loadUpdateInfo();
        } else {
            mPresenter = new FavoritePresenter(favoriteList.size(), limit, updateOrder, this);
            mPresenter.loadData(isMain);
        }
    }

    /**
     * 移除收藏
     */
    private void removeFavorite(int position) {
        DatabaseUtil.deleteFavorite(favoriteList.get(position).getAnimeId());
        adapter.remove(position);
        favoriteCount = DatabaseUtil.queryFavoriteCount();
//        application.showSnackbarMsg(msg, Utils.getString(R.string.join_error));
        CustomToast.showToast(this, Utils.getString(R.string.join_error), CustomToast.SUCCESS);
        if (favoriteList.size() <= 0) {
            setRecyclerViewEmpty();
            errorTitle.setText(Utils.getString(R.string.empty_favorite));
            adapter.setEmptyView(errorView);
        }
        EventBus.getDefault().post(new Refresh(99));
    }

    @Override
    protected void initBeforeView() {

    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    @Override
    public void showLoadingView() {
        runOnUiThread(() -> {
            adapter.setNewData(favoriteList);
        });
    }

    @Override
    public void showLoadErrorView(String msg) {
        if (mActivityFinish) return;
        setLoadState(false);
        runOnUiThread(() -> {
            if (isMain) {
                setRecyclerViewEmpty();
                loading.setVisibility(View.GONE);
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            adapter.setEmptyView(emptyView);
        });
    }

    @Override
    public void showLog(String url) {

    }

    @Override
    public void showSuccessView(List<AnimeListBean> list) {
        if (mActivityFinish) return;
        setLoadState(true);
        runOnUiThread(() -> {
            if (isMain) {
                loading.setVisibility(View.GONE);
                favoriteList = list;
                if (favoriteList.size() > 0)
                    setRecyclerViewView();
                else
                    setRecyclerViewEmpty();
                adapter.setNewData(favoriteList);
            } else
                adapter.addData(list);
        });
    }

    @Override
    public void completionView(boolean complete) {
        if (mActivityFinish) return;
        if (!complete) {
            mPresenter = new FavoritePresenter(1, application.animeUpdateInfoBeans, this);
            mPresenter.loadUpdateInfo();
        } else {
            int favoriteUpdateCount = DatabaseUtil.queryFavoriteUpdateCount();
            runOnUiThread(() -> CustomToast.showToast(this, favoriteUpdateCount >0 ? "你的追番列表共有" + favoriteUpdateCount + "个更新" : "你的追番列表暂无更新~", CustomToast.DEFAULT));
            mPresenter = new FavoritePresenter(favoriteList.size(), limit, updateOrder, this);
            mPresenter.loadData(isMain);
        }
    }

    @Override
    public void showErrorUpdateInfo(int source) {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            CustomToast.showToast(this, source == 0 ? "Yhdm源获取更新失败！" : "Imomoe源获取更新失败！", CustomToast.ERROR);
        });
    }

    @Override
    public void showSuccessImg(String oldImgUrl, String imgUrl) {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            for (int i=0,size=favoriteList.size(); i<size; i++) {
                if (favoriteList.get(i).getImg().contains(oldImgUrl)) {
                    favoriteList.get(i).setImg(imgUrl);
                    adapter.notifyItemChanged(i);
                    DatabaseUtil.updateImg(favoriteList.get(i).getAnimeId(), imgUrl, 0);
                    break;
                }
            }
        });
    }

    @Override
    public void showErrorImg(String msg) {
        if (mActivityFinish) return;
        this.runOnUiThread(() -> CustomToast.showToast(this, msg, CustomToast.ERROR));
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad())
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 3 : 5));
        else {
            if (isInMagicWindow)
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            else
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 5 : 8));
        }
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 1) {
            loadFavoriteData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgBean updateImgBean) {
        updateImgPresenter = new UpdateImgPresenter(updateImgBean.getOldImgUrl(), updateImgBean.getDescUrl(), this);
        updateImgPresenter.loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != updateImgPresenter) updateImgPresenter.detachView();
        EventBus.getDefault().unregister(this);
    }
}
