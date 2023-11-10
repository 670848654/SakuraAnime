package my.project.sakuraproject.main.my;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.HistoryListAdapter;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.HistoryBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.UpdateImgBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class HistoryActivity extends BaseActivity<HistoryContract.View, HistoryPresenter> implements HistoryContract.View,
        UpdateImgContract.View, VideoContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    private HistoryListAdapter adapter;
    private List<HistoryBean> historyBeans = new ArrayList<>();
    CoordinatorLayout msg;
    private int limit = 10;
    private int historyCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private AlertDialog alertDialog;
    private VideoPresenter videoPresenter;
    private UpdateImgPresenter updateImgPresenter;
    private String animeId;
    private String animeTitle;
    private String dramaUrl;
    private String dramaTitle;
    private int playSource;
    private int source;
    private List<AnimeDescDetailsBean> yhdmDramasBeans;
    private int clickIndex = 0;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected HistoryPresenter createPresenter() {
        return new HistoryPresenter(historyBeans.size(), limit, this);
    }

    @Override
    protected void loadData() {
        loading.setVisibility(View.VISIBLE);
        mPresenter.loadData(isMain);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_my_list;
    }

    @Override
    protected void init() {
        historyCount = DatabaseUtil.queryHistoryCount();
        EventBus.getDefault().register(this);
        initToolbar();
        initAdapter();
        initFab();
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.history_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initAdapter() {
        adapter = new HistoryListAdapter(this, historyBeans);
//        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            animeId = historyBeans.get(position).getAnimeId();
            animeTitle = historyBeans.get(position).getTitle();
            dramaUrl = historyBeans.get(position).getDramaUrl();
            dramaTitle = historyBeans.get(position).getDramaNumber();
            playSource = historyBeans.get(position).getPlaySource();
            source = historyBeans.get(position).getSource();
            videoPresenter = new VideoPresenter(
                    animeTitle,
                    dramaUrl,
                    playSource,
                    historyBeans.get(position).getDramaNumber(),
                    this
            );
            alertDialog = Utils.getProDialog(this, R.string.get_anime_info);
            videoPresenter.loadData(true);
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            switch (view.getId()) {
                case R.id.desc_view:
                    Bundle bundle = new Bundle();
                    bundle.putString("name", historyBeans.get(position).getTitle());
                    bundle.putString("url", historyBeans.get(position).getDescUrl());
                    startActivityForResult(new Intent(this, DescActivity.class).putExtras(bundle), 3000);
                    break;
                case R.id.delete_view:
                    showDeleteHistoryDialog(position, historyBeans.get(position).getHistoryId(), false);
                    break;
            }
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return false;
            View v = adapter.getViewByPosition(mRecyclerView, position, R.id.title);
            final PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.delete_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.desc:
                        Bundle bundle = new Bundle();
                        bundle.putString("name", historyBeans.get(position).getTitle());
                        bundle.putString("url", historyBeans.get(position).getDescUrl());
                        startActivityForResult(new Intent(this, DescActivity.class).putExtras(bundle), 3000);
                        break;
                    case R.id.delete:
                        showDeleteHistoryDialog(position, historyBeans.get(position).getHistoryId(), false);
                        break;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (historyBeans.size() >= historyCount) {
                adapter.loadMoreEnd();
            } else {
                if (isErr) {
                    isMain = false;
                    mPresenter = new HistoryPresenter(historyBeans.size(), limit, this);
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

    private void initFab() {
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 15);
            fab.setLayoutParams(params);
        }
        fab.setOnClickListener(view -> showDeleteHistoryDialog(0, null, true));
    }

    private void loadHistoryData() {
        isMain = true;
        historyBeans.clear();
        setRecyclerViewView();
        mPresenter = createPresenter();
        loadData();
    }

    /**
     * 删除历史记录弹窗
     * @param position
     * @param historyId
     * @param isAll
     */
    private void showDeleteHistoryDialog(int position, String historyId, boolean isAll) {
        Utils.showAlert(this,
                Utils.getString(R.string.other_operation),
                isAll ? Utils.getString(R.string.delete_all_history) : Utils.getString(R.string.delete_single_history),
                true,
                getString(R.string.page_positive),
                getString(R.string.page_negative),
                null,
                (dialogInterface, i) -> deleteHistory(position, historyId, isAll),
                (dialogInterface, i) -> dialogInterface.dismiss(),
                null);
    }

    /**
     * 删除历史记录
     * @param position
     * @param historyId
     * @param isAll
     */
    private void deleteHistory(int position, String historyId, boolean isAll) {
        DatabaseUtil.deleteHistory(historyId, isAll);
        historyCount = DatabaseUtil.queryHistoryCount();
        if (!isAll)
            adapter.remove(position);
        else
            historyBeans.clear();
        if (historyBeans.size() <= 0) {
            fab.setVisibility(View.GONE);
            setRecyclerViewEmpty();
            adapter.setNewData(historyBeans);
            errorTitle.setText(Utils.getString(R.string.empty_history));
            adapter.setEmptyView(errorView);
        }
        EventBus.getDefault().post(new Refresh(99));
    }

    /**
     * 播放视频
     *
     * @param animeUrl
     */
    private void playAnime(String animeUrl) {
        cancelDialog();
        switch ((Integer) SharedPreferencesUtils.getParam(this, "player", 0)) {
            case 0:
                //调用播放器
                /*switch (source) {
                    case 0:
                        // yhdm
                        VideoUtils.openPlayer(true, this, animeTitle + " - " + dramaTitle, animeUrl, animeTitle, dramaUrl, yhdmDramasBeans, clickIndex, animeId);
                        break;
                    case 1:
                        // imomoe
                        VideoUtils.openImomoePlayer(true, this, animeTitle + " - " +dramaTitle, animeUrl, animeTitle, dramaUrl, imomoeDramasBeans, imomoeVideoUrlBeans, playSource, clickIndex, animeId);
                        break;
                }*/
                VideoUtils.openPlayer(true, this, animeTitle + " - " + dramaTitle, animeUrl, animeTitle, dramaUrl, yhdmDramasBeans, clickIndex, animeId, playSource, source == 1);

                break;
            case 1:
                Utils.selectVideoPlayer(this, animeUrl);
                break;
        }
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
            adapter.setNewData(historyBeans);
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
                fab.setVisibility(View.GONE);
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
    public void showSuccessView(List<HistoryBean> list) {
        if (mActivityFinish) return;
        setLoadState(true);
        runOnUiThread(() -> {
            if (isMain) {
                loading.setVisibility(View.GONE);
                historyBeans = list;
                if (historyBeans.size() > 0) {
                    setRecyclerViewView();
                    fab.setVisibility(View.VISIBLE);
                }
                else
                    setRecyclerViewEmpty();
                adapter.setNewData(historyBeans);
            } else
                adapter.addData(list);
        });
    }

    @Override
    public void showSuccessImg(String oldImgUrl, String imgUrl) {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            for (int i=0,size=historyBeans.size(); i<size; i++) {
                if (historyBeans.get(i).getImgUrl().contains(oldImgUrl)) {
                    historyBeans.get(i).setImgUrl(imgUrl);
                    adapter.notifyItemChanged(i);
                    DatabaseUtil.updateImg(historyBeans.get(i).getAnimeId(), imgUrl, 1);
                    break;
                }
            }
        });
    }

    @Override
    public void showErrorImg(String msg) {
        if (mActivityFinish) return;
        runOnUiThread(() -> CustomToast.showToast(this, msg, CustomToast.ERROR));

    }

    @Override
    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    @Override
    public void showYhdmVideoSuccessView(List<String> list) {
        if (mActivityFinish) return;
        this.runOnUiThread(() -> {
            if (list.size() == 1)
                playAnime(list.get(0));
            else
                VideoUtils.showMultipleVideoSources(this,
                        list,
                        (dialog, index) -> playAnime(list.get(index)),
                        (dialog, which) -> {
                            cancelDialog();
                            dialog.dismiss();
                        }, 1, false);
        });
    }

    @Override
    public void showSuccessYhdmDramasView(List<AnimeDescDetailsBean> list) {
        yhdmDramasBeans = list;
        for (int i=0,size=list.size(); i<size; i++) {
            if (list.get(i).getUrl().equals(dramaUrl)) {
                clickIndex = i;
                break;
            }
        }
    }

    @Override
    public void getVideoEmpty() {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
//            application.showToastMsg(Utils.getString(R.string.open_web_view));
//            CustomToast.showToast(this, Utils.getString(R.string.open_web_view), CustomToast.WARNING);
//            VideoUtils.openDefaultWebview(this, dramaUrl.contains("/voddetail/") ? BaseModel.getDomain(true) + dramaUrl : BaseModel.getDomain(false) + dramaUrl);
            VideoUtils.showErrorInfo(this, dramaUrl, false);
        });
    }

    @Override
    public void getVideoError() {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
//            application.showErrorToastMsg(Utils.getString(R.string.error_700));
//            CustomToast.showToast(this, Utils.getString(R.string.loading_video__failed), CustomToast.ERROR);
            VideoUtils.showErrorInfo(this, dramaUrl, false);
        });
    }

    @Override
    public void errorDramaView() {

    }

    @Override
    public void showSuccessImomoeVideoUrlView(String playUrl) {
        playAnime(playUrl);
    }

    @Override
    public void showSuccessImomoeDramasView(List<AnimeDescDetailsBean> bean) {
        yhdmDramasBeans = bean;
        for (int i=0,size=bean.size(); i<size; i++) {
            if (bean.get(i).getUrl().equals(dramaUrl)) {
                clickIndex = i;
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != videoPresenter) videoPresenter.detachView();
        if (null != updateImgPresenter) updateImgPresenter.detachView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 2) {
            loadHistoryData();
        }
    }

    private void setRecyclerViewEmpty() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    private void setRecyclerViewView() {
        position = mRecyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad())
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, isPortrait ? 1 : 2));
        else {
            if (isInMagicWindow)
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            else
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgBean updateImgBean) {
        if (mActivityFinish) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgBean.getOldImgUrl(), updateImgBean.getDescUrl(), this);
        updateImgPresenter.loadData();
    }
}
