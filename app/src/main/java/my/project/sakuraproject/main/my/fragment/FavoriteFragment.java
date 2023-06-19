package my.project.sakuraproject.main.my.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.FavoriteListAdapter;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.UpdateImgBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.main.my.FavoriteContract;
import my.project.sakuraproject.main.my.FavoritePresenter;
import my.project.sakuraproject.main.my.UpdateImgContract;
import my.project.sakuraproject.main.my.UpdateImgPresenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

@Deprecated
public class FavoriteFragment extends MyLazyFragment<FavoriteContract.View, FavoritePresenter> implements FavoriteContract.View, UpdateImgContract.View {
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    CoordinatorLayout msg;
    private FavoriteListAdapter adapter;
    private List<AnimeListBean> favoriteList = new ArrayList<>();
    private int limit = 100;
    private int favoriteCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private boolean updateOrder;
    private View view;
    private List<AnimeUpdateInfoBean> animeUpdateInfoBeans;

    public FavoriteFragment(List<AnimeUpdateInfoBean> beans) {
        animeUpdateInfoBeans = beans;
    }

    @Override
    protected FavoritePresenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_my, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        msg = getActivity().findViewById(R.id.msg);
        initAdapter();
        loadFavoriteData();
        return view;
    }

    private void initAdapter() {
        adapter = new FavoriteListAdapter(getActivity(), favoriteList);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            AnimeListBean bean = (AnimeListBean) adapter.getItem(position);
            Bundle bundle = new Bundle();
            bundle.putString("name", bean.getTitle());
            bundle.putString("url", bean.getUrl());
            startActivityForResult(new Intent(getActivity(), DescActivity.class).putExtras(bundle), 3000);
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            View v = adapter.getViewByPosition(mRecyclerView, position, R.id.img);
            final PopupMenu popupMenu = new PopupMenu(getActivity(), v);
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
        if (Utils.checkHasNavigationBar(getActivity())) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
        mRecyclerView.setAdapter(adapter);
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.loadMoreComplete();
    }

    private void loadFavoriteData() {
        updateOrder = (Boolean) SharedPreferencesUtils.getParam(getActivity(), "check_favorite_update", true);
        favoriteCount = DatabaseUtil.queryFavoriteCount();
        isMain = true;
        favoriteList.clear();
        adapter.setNewData(favoriteList);
        loading.setVisibility(View.VISIBLE);
        if (favoriteCount > 0 && updateOrder) {
//            application.showSnackbarMsg(msg, Utils.getString(R.string.check_favorite_update));
            mPresenter = new FavoritePresenter(0, animeUpdateInfoBeans, this);
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
        CustomToast.showToast(getActivity(), Utils.getString(R.string.join_error), CustomToast.SUCCESS);
        if (favoriteList.size() <= 0) {
            setRecyclerViewEmpty();
            errorTitle.setText(Utils.getString(R.string.empty_favorite));
            adapter.setEmptyView(errorView);
        }
    }

    @Override
    public void showSuccessView(List<AnimeListBean> list) {
        if (getActivity() != null) {
            setLoadState(true);
            getActivity().runOnUiThread(() -> {
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
    }

    @Override
    public void completionView(boolean complete) {
        if (!complete) {
            mPresenter = new FavoritePresenter(1, animeUpdateInfoBeans, this);
            mPresenter.loadUpdateInfo();
        } else {
            int favoriteUpdateCount = DatabaseUtil.queryFavoriteUpdateCount();
            getActivity().runOnUiThread(() -> {
                if (Utils.isTopActivity(getActivity()) && isFragmentVisible) {
//                    application.showSnackbarMsg(msg, favoriteUpdateCount >0 ? "你的追番列表共有" + favoriteUpdateCount + "个更新" : "你的追番列表暂无更新~");
                    CustomToast.showToast(getActivity(), favoriteUpdateCount >0 ? "你的追番列表共有" + favoriteUpdateCount + "个更新" : "你的追番列表暂无更新~", CustomToast.DEFAULT);
                }
            });
            mPresenter = new FavoritePresenter(favoriteList.size(), limit, updateOrder, this);
            mPresenter.loadData(isMain);
        }
    }

    @Override
    public void showErrorUpdateInfo(int source) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (Utils.isTopActivity(getActivity()) && isFragmentVisible) {
                    CustomToast.showToast(getActivity(), source == 0 ? "Yhdm源获取更新失败！" : "Imomoe源获取更新失败！", CustomToast.ERROR);
//                application.showSnackbarMsg(msg, source == 0 ? "Yhdm源获取更新失败！" : "Imomoe源获取更新失败！");
                }
            });
        }
    }

    @Override
    public void showLoadingView() {
        getActivity().runOnUiThread(() -> {
            adapter.setNewData(favoriteList);
        });
    }

    @Override
    public void showLoadErrorView(String msg) {
        setLoadState(false);
        getActivity().runOnUiThread(() -> {
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
        getActivity().runOnUiThread(() -> {
            adapter.setEmptyView(emptyView);
        });
    }

    @Override
    public void showLog(String url) {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 1) {
            loadFavoriteData();
        }
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        String config = getActivity().getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad())
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 3 : 5));
        else {
            if (isInMagicWindow)
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
            else
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 5 : 8));
        }
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgBean updateImgBean) {
        if (!isFragmentVisible) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgBean.getOldImgUrl(), updateImgBean.getDescUrl(), this);
        updateImgPresenter.loadData();
    }

    @Override
    public void showSuccessImg(String oldImgUrl, String imgUrl) {
        if (!isFragmentVisible) return;
        getActivity().runOnUiThread(() -> {
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
        if (!isFragmentVisible) return;
        getActivity().runOnUiThread(() -> CustomToast.showToast(getActivity(), msg, CustomToast.ERROR));
    }
}
