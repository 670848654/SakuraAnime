package my.project.sakuraproject.main.my;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.task.DownloadTask;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
import my.project.sakuraproject.adapter.DownloadListAdapter;
import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.bean.DownloadEvent;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.UpdateImgBean;
import my.project.sakuraproject.config.M3U8DownloadConfig;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.util.Utils;

public class DownloadActivity extends BaseActivity<DownloadContract.View, DownloadPresenter> implements DownloadContract.View, UpdateImgContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    CoordinatorLayout msg;
    private int limit = 10;
    private int downloadCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private DownloadListAdapter adapter;
    private List<DownloadBean> downloadList = new ArrayList<>();
    private UpdateImgPresenter updateImgPresenter;
    private M3U8VodOption m3U8VodOption; // 下载m3u8配置

    @Override
    protected DownloadPresenter createPresenter() {
        return new DownloadPresenter(downloadList.size(), limit, this);
    }

    @Override
    protected void loadData() {
        downloadCount = DatabaseUtil.queryDownloadCount();
        loading.setVisibility(View.VISIBLE);
        mPresenter.loadData(isMain);
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
        Aria.download(this).register();
        List<DownloadEntity> list = Aria.download(this).getAllNotCompleteTask();
        if (list != null && list.size() > 0) {
            for (DownloadEntity downloadEntity : list) {
                DatabaseUtil.deleteAbsentTask(this, downloadEntity.getId());
            }
        }
        /*if (!Utils.isServiceRunning(Sakura.getInstance(), "my.project.sakuraproject.services.DownloadService"))
            new Handler().postDelayed(() -> checkNotCompleteDownloadTask(), 200);*/
        List<DownloadEntity> downloadEntities = Aria.download(this).getDRunningTask();
        if (downloadEntities == null || downloadEntities.size() == 0) {
            checkNotCompleteDownloadTask();
        }
    }

    private void checkNotCompleteDownloadTask() {
        List<DownloadEntity> list = Aria.download(this).getAllNotCompleteTask();
        if (list != null && list.size() > 0) {
            Utils.showAlert(this,
                    "下载任务操作",
                    String.format("你有%s个未完成的下载任务，是否继续下载？", list.size()+""),
                    false,
                    Utils.getString(R.string.download_positive),
                    Utils.getString(R.string.download_negative),
                    null,
                    (dialogInterface, i) -> {
                        startService(new Intent(this, DownloadService.class));
                        setM3U8VodOption();
                        for (DownloadEntity entity : list) {
                            if (entity.getUrl().contains("m3u8")) {
                                Log.e("恢复下载M3U8", "....");
                                Aria.download(this).load(entity.getId()).ignoreCheckPermissions().m3u8VodOption(m3U8VodOption).resume();
                            } else {
                                Aria.download(this).load(entity.getId()).ignoreCheckPermissions().resume();
                                Log.e("恢复下载MP4", "....");
                            }
                        }
                    },
                    (dialogInterface, i) -> dialogInterface.dismiss(),
                    null);
        }
    }

    private void setM3U8VodOption() {
        // m3u8下载配置
        m3U8VodOption = new M3U8VodOption();
        m3U8VodOption.ignoreFailureTs();
        m3U8VodOption.setUseDefConvert(false);
        m3U8VodOption.setBandWidthUrlConverter(new M3U8DownloadConfig.BandWidthUrlConverter());
        m3U8VodOption.setVodTsUrlConvert(new M3U8DownloadConfig.VodTsUrlConverter());
        m3U8VodOption.setMergeHandler(new M3U8DownloadConfig.TsMergeHandler());
    }

    public void initToolbar() {
        toolbar.setTitle("下载列表");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initAdapter() {
        adapter = new DownloadListAdapter(this, downloadList);
//        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Bundle bundle = new Bundle();
            bundle.putString("animeTitle", downloadList.get(position).getAnimeTitle());
            bundle.putString("downloadId", downloadList.get(position).getDownloadId());
            startActivity(new Intent(this, DownloadDataActivity.class).putExtras(bundle));
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            View v = adapter.getViewByPosition(mRecyclerView, position, R.id.title);
            final PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.download_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.go_to_desc:
                        Bundle bundle = new Bundle();
                        bundle.putString("name", downloadList.get(position).getAnimeTitle());
                        bundle.putString("url", downloadList.get(position).getDescUrl());
                        startActivity(new Intent(this, DescActivity.class).putExtras(bundle));
                        break;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (downloadList.size() >= downloadCount) {
                adapter.loadMoreEnd();
            } else {
                if (isErr) {
                    isMain = false;
                    mPresenter = new DownloadPresenter(downloadList.size(), limit, this);
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

    private void loadDownloadData() {
        isMain = true;
        downloadList.clear();
        mPresenter = createPresenter();
        loadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 3)
            loadDownloadData();
    }

    @Download.onTaskRunning
    protected void running(DownloadTask downloadTask) {
//        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        for (int i = 0, size = downloadList.size(); i < size; i++) {
            List<Object> list = DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId());
            String title = (String) list.get(0);
            if (downloadList.get(i).getAnimeTitle().equals(title)) {
                TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                if (number != null) {
                    String speed = downloadTask.getConvertSpeed() == null ? "0kb/s" : downloadTask.getConvertSpeed();
                    number.setText("正在下载" + downloadTask.getTaskName().replaceAll(".mp4", "").replaceAll(".m3u8", "")  + "\n" + speed);
                }
                TextView fileSize = (TextView) adapter.getViewByPosition(i, R.id.file_size);
                if (fileSize != null) {
                    if (fileSize.getVisibility() != View.VISIBLE) fileSize.setVisibility(View.VISIBLE);
                    fileSize.setText("大小:" +Utils.getNetFileSizeDescription(downloadTask.getEntity().getFileSize()));
                }
                ProgressBar p = (ProgressBar) adapter.getViewByPosition(i, R.id.bottom_progress);
                if (p != null) {
                    if (p.getVisibility() != View.VISIBLE) p.setVisibility(View.VISIBLE);
                    p.setProgress(downloadTask.getPercent());
                }
            }
        }
    }

    @Download.onTaskStop
    protected void stop(DownloadTask downloadTask) {
        adapter.notifyDataSetChanged();
    }

    /*@Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
//        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadList.size(); i < size; i++) {
                String title = (String) DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId()).get(0);
                if (downloadList.get(i).getAnimeTitle().equals(title)) {
                    downloadList.get(i).setFilesSize(DatabaseUtil.queryDownloadFilesSize(downloadList.get(i).getDownloadId()));
                    downloadList.get(i).setNoCompleteSize(DatabaseUtil.queryDownloadNotCompleteCount(downloadList.get(i).getDownloadId()));
                    adapter.notifyItemChanged(i);
                    DatabaseUtil.updateDownloadSuccess((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
                    break;
                }
            }
        }, 1000);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != updateImgPresenter) updateImgPresenter.detachView();
        Aria.download(this).unRegister();
        EventBus.getDefault().unregister(this);
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
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            adapter.setNewData(downloadList);
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
    public void showSuccessView(List<DownloadBean> list) {
        if (mActivityFinish) return;
        setLoadState(true);
        runOnUiThread(() -> {
            if (isMain) {
                loading.setVisibility(View.GONE);
                downloadList = list;
                if (downloadList.size() > 0)
                    setRecyclerViewView();
                else
                    setRecyclerViewEmpty();
                adapter.setNewData(downloadList);
            } else
                adapter.addData(list);
        });
    }

    @Override
    public void showSuccessImg(String oldImgUrl, String imgUrl) {
        if (mActivityFinish) return;
        runOnUiThread(() -> {
            for (int i=0,size=downloadList.size(); i<size; i++) {
                if (downloadList.get(i).getImgUrl().contains(oldImgUrl)) {
                    downloadList.get(i).setImgUrl(imgUrl);
                    adapter.notifyItemChanged(i);
                    DatabaseUtil.updateImg(downloadList.get(i).getDownloadId(), imgUrl, 2);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgBean updateImgBean) {
        if (mActivityFinish) return;
        updateImgPresenter = new UpdateImgPresenter(updateImgBean.getOldImgUrl(), updateImgBean.getDescUrl(), this);
        updateImgPresenter.loadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent downloadEvent) {
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadList.size(); i < size; i++) {
                if (downloadList.get(i).getAnimeTitle().equals(downloadEvent.getTitle())) {
                    downloadList.get(i).setFilesSize(DatabaseUtil.queryDownloadFilesSize(downloadList.get(i).getDownloadId()));
                    downloadList.get(i).setNoCompleteSize(DatabaseUtil.queryDownloadNotCompleteCount(downloadList.get(i).getDownloadId()));
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }, 1000);
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

}
