package my.project.sakuraproject.main.my;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DownloadDataListAdapter;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.bean.DownloadEvent;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.RefreshDownloadData;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.player.LocalPlayerActivity;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.util.Utils;

public class DownloadDataActivity extends BaseActivity<DownloadDataContract.View, DownloadDataPresenter> implements DownloadDataContract.View {
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private List<DownloadDataBean> downloadDataBeans = new ArrayList<>();
    private DownloadDataListAdapter adapter;
    private String downloadId;
    private String animeTitle;
    private int limit = 10;
    private int downloadDataCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private AlertDialog.Builder builder;
    private static final String[] DOWNLOAD_STR = new String[]{"继续任务", "暂停任务", "删除任务"};
    private static final String[] COMPLETE_STR = new String[]{"使用内置播放器播放", "使用外部播放器播放", "删除任务"};
    private static final String[] DOWNLOAD_ERROR_STR = new String[]{"尝试重新下载", "删除任务"};
    private File downloadDir;
    private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/SakuraAnime/Downloads/%s/%s/";;

    @Override
    protected DownloadDataPresenter createPresenter() {
        return new DownloadDataPresenter(downloadId, downloadDataBeans.size(),limit, this);
    }

    @Override
    protected void loadData() {
        downloadDataBeans.clear();
        downloadDataCount = DatabaseUtil.queryDownloadDataCount(downloadId);
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_anime;
    }

    @Override
    protected void init() {
//        Slidr.attach(this, Utils.defaultInit());
        EventBus.getDefault().register(this);
        Aria.download(this).register();
        Bundle bundle = getIntent().getExtras();
        downloadId = bundle.getString("downloadId");
        animeTitle = bundle.getString("animeTitle");
        initToolbar();
        initSwipe();
        initAdapter();
    }

    private void initToolbar() {
        toolbar.setTitle(animeTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initSwipe() {
        mSwipe.setEnabled(false);
    }

    private void initAdapter() {
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.isPad() ? 2 : 1));
        adapter = new DownloadDataListAdapter(this, downloadDataBeans);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            long taskId = downloadDataBeans.get(position).getTaskId();
            switch (downloadDataBeans.get(position).getComplete()) {
                case 0:
                    // 等待下载状态
                    builder = new AlertDialog.Builder(this, R.style.DialogStyle)
                            .setItems(DOWNLOAD_STR, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                switch (i) {
                                    case 0:
                                        // 继续下载
                                        Aria.download(this).load(taskId).ignoreCheckPermissions().resume();
                                        downloadDataBeans.get(position).setComplete(0);
                                        startService(new Intent(this, DownloadService.class));
                                        break;
                                    case 1:
                                        // 暂停任务
                                        Aria.download(this).load(taskId).ignoreCheckPermissions().stop();
                                        downloadDataBeans.get(position).setComplete(0);
                                        adapter.notifyItemChanged(position);
                                        stopService(new Intent(this, DownloadService.class));
                                        break;
                                    case 2:
                                        // 删除任务
                                        showDeleteDataDialog(downloadDataBeans.get(position), position);
                                        break;
                                }
                            });
                    break;
                case 1:
                    builder = new AlertDialog.Builder(this, R.style.DialogStyle)
                            .setItems(COMPLETE_STR, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                switch (i) {
                                    case 0:
                                        Bundle bundle = new Bundle();
                                        bundle.putString("playPath",  downloadDataBeans.get(position).getPath());
                                        bundle.putString("animeTitle", animeTitle);
                                        bundle.putString("dramaTitle", downloadDataBeans.get(position).getPlayNumber());
                                        bundle.putSerializable("downloadDataBeans", (Serializable) downloadDataBeans);
                                        startActivity(new Intent(this, LocalPlayerActivity.class).putExtras(bundle));
                                        break;
                                    case 1:
                                        Utils.selectVideoPlayer(this, downloadDataBeans.get(position).getPath());
                                        break;
                                    case 2:
                                        showDeleteDataDialog(downloadDataBeans.get(position), position);
                                        break;
                                }
                            });
                    break;
                case 2:
                    builder = new AlertDialog.Builder(this, R.style.DialogStyle)
                            .setItems(DOWNLOAD_ERROR_STR, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                                switch (i) {
                                    case 0:
                                        Aria.download(this).load(taskId).ignoreCheckPermissions().resume();
                                        downloadDataBeans.get(position).setComplete(0);
                                        DatabaseUtil.updateDownloadState(taskId);
                                        break;
                                    case 1:
                                        showDeleteDataDialog(downloadDataBeans.get(position), position);
                                        break;
                                }
                            });
                    break;
            }
            builder.create().show();
        });
        if (Utils.checkHasNavigationBar(this)) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        adapter.setLoadMoreView(new CustomLoadMoreView());
        adapter.setOnLoadMoreListener(() -> mRecyclerView.postDelayed(() -> {
            if (downloadDataBeans.size() >= downloadDataCount) {
                //数据全部加载完毕
                adapter.loadMoreEnd();
            } else {
                if (isErr) {
                    //成功获取更多数据
                    isMain = false;
                    mPresenter = createPresenter();
                    mPresenter.loadData(isMain);
                } else {
                    //获取更多数据失败
                    isErr = true;
                    adapter.loadMoreFail();
                }
            }
        }, 500), mRecyclerView);
        mRecyclerView.setAdapter(adapter);
    }

    private void showDeleteDataDialog(DownloadDataBean bean, int position) {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_remove_download, null);
        MaterialCheckBox checkBox = view.findViewById(R.id.remove_file_select);
        builder.setTitle(Utils.getString(R.string.other_operation));
        builder.setPositiveButton(Utils.getString(R.string.page_positive), (dialog, which) -> deleteData(checkBox.isChecked(), bean, position));
        builder.setNegativeButton(Utils.getString(R.string.page_negative), (dialog, which) -> dialog.dismiss());
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }

    private void deleteData(boolean removeFile, DownloadDataBean bean, int position) {
        DatabaseUtil.deleteDownloadData(bean.getId());
        String downloadPath = String.format(DOWNLOAD_PATH, (bean.getSource() == 0 ? "YHDM" : "SILISILI"), bean.getAnimeTitle());
        downloadDir = new File(downloadPath);
        if (bean.getTaskId() == -1) {
            // -1直接删除
            adapter.remove(position);
            CustomToast.showToast(this, "已删除该剧集任务", CustomToast.SUCCESS);
        } else {
            if (!downloadDir.exists()) downloadDir.mkdirs(); // 如果从文件管理器中手动删除整个目录 Aria会报错，在这里重建目录
            // 获取所有下载任务
            List<DownloadEntity> list = Aria.download(this).getTaskList();
            // 判断任务列表是否存在，当应用卸载重装时为NULL会报错
            if (list != null && list.size() > 0) {
                for (DownloadEntity entity : list) {
                    // 未下载完成
                    if (bean.getTaskId() != -99 && bean.getTaskId() == entity.getId()) {
                        // 从Aria数据库中删除任务
                        Aria.download(this).load(entity.getId()).ignoreCheckPermissions().cancel(false);
                        break;
                        // 已下载完成通过path对比
                    } else if (bean.getPath().equals(entity.getFilePath().replaceAll("m3u8", "mp4"))) {
                        // 从Aria数据库中删除任务
                        Aria.download(this).load(entity.getId()).ignoreCheckPermissions().cancel(false);
                        break;
                    }
                }
            }
            // 是否删除文件
            deleteDownloadData(removeFile, bean, position);
        }
        downloadDataCount = DatabaseUtil.queryDownloadDataCount(downloadId);
        if (downloadDataBeans.size() == 0) {
            shouldDeleteDownloadDir();
            DatabaseUtil.deleteDownload(downloadId);
            EventBus.getDefault().post(new Refresh(3));
            finish();
        }
    }

    /**
     * 删除数据
     * @param removeFile
     * @param bean
     * @param position
     */
    private void deleteDownloadData(boolean removeFile, DownloadDataBean bean, int position) {
        // 如果已完成的任务
        if (bean.getComplete() == 1 && removeFile) {
            File mp4File = new File(bean.getPath());
            if (mp4File.exists()) mp4File.delete();
            File m3u8File = new File(bean.getPath().replaceAll("mp4", "m3u8"));
            if (m3u8File.exists()) m3u8File.delete();
        }
        CustomToast.showToast(this, "已删除该剧集任务", CustomToast.SUCCESS);
        adapter.remove(position);
    }

    /**
     * 是否应该删除下载主目录
     */
    private void shouldDeleteDownloadDir() {
        try {
            if (downloadDir.list().length == 0) // 文件夹下没有任何文件才删除主目录
                downloadDir.delete();
        } catch (Exception e) {}
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        adapter.loadMoreComplete();
    }

    private void loadDownloadData() {
        isMain = true;
        mPresenter = createPresenter();
        loadData();
    }

    @Download.onTaskRunning
    protected void running(DownloadTask downloadTask) {
        String title = (String) DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId()).get(0);
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(title) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
                TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                if (number != null)
                    number.setText(downloadTask.getConvertSpeed() == null ? "0kb/s" : downloadTask.getConvertSpeed());
                TextView state = (TextView) adapter.getViewByPosition(i, R.id.state);
                if (state != null) {
                    state.setText(Html.fromHtml("<font color='#ff4081'>下载中</font>"));
                }
                TextView fileSize = (TextView) adapter.getViewByPosition(i, R.id.file_size);
                if (fileSize != null) {
                    if (fileSize.getVisibility() != View.VISIBLE)
                        fileSize.setVisibility(View.VISIBLE);
                    fileSize.setText(Utils.getNetFileSizeDescription(downloadTask.getEntity().getFileSize()));
                }
                ProgressBar p = (ProgressBar) adapter.getViewByPosition(i, R.id.bottom_progress);
                if (p != null) {
                    if (p.getVisibility() != View.VISIBLE) p.setVisibility(View.VISIBLE);
                    p.setProgress(downloadTask.getPercent());
                }
            }
        }
    }

    /*@Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        Log.e("Activity onTaskComplete", downloadTask.getTaskName() + "，下载完成");
        String title = (String) DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId()).get(0);
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(title) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
                downloadDataBeans.get(i).setComplete(1);
                String path = downloadTask.getFilePath();
                if (path.contains("m3u8")) {
                    path = path.replaceAll("m3u8", "mp4");
                    File file = new File(path);
                    downloadDataBeans.get(i).setFileSize(file == null ? 0 : file.length());
                    downloadDataBeans.get(i).setPath(path);
                } else {
                    downloadDataBeans.get(i).setFileSize(downloadTask.getFileSize());
                    downloadDataBeans.get(i).setPath(downloadTask.getFilePath());
                }
                adapter.notifyItemChanged(i);
                DatabaseUtil.updateDownloadSuccess((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
                break;
            }
        }
    }*/

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        Log.e("Service onTaskCancel", downloadTask.getTaskName() + "，取消下载");
        shouldDeleteDownloadDir();
//        List<Object> objects = DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId());
        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask) {
        Log.e("Service onTaskCancel", downloadTask.getTaskName() + "，下载失败");
        List<Object> objects = DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId());
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(objects.get(0)) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
                downloadDataBeans.get(i).setComplete(2);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        Aria.download(this).unRegister();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void initBeforeView() {
//        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    @Override
    public void showSuccessView(List<DownloadDataBean> list) {
        setLoadState(true);
        runOnUiThread(() -> {
            if (isMain) {
                downloadDataBeans = list;
                if (downloadDataBeans.size() >0)
                    setRecyclerViewView();
                else
                    setRecyclerViewEmpty();
                adapter.setNewData(downloadDataBeans);
            } else
                adapter.addData(list);
        });
    }

    @Override
    public void showLoadingView() {
        runOnUiThread(()-> adapter.setNewData(downloadDataBeans));
    }

    @Override
    public void showLoadErrorView(String msg) {
        setLoadState(false);
        runOnUiThread(() -> {
            if (isMain) {
                setRecyclerViewEmpty();
                errorTitle.setText(msg);
                adapter.setEmptyView(errorView);
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        runOnUiThread(() -> adapter.setEmptyView(emptyView));
    }

    @Override
    public void showLog(String url) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshDownloadData refreshDownloadData) {
        for (int i=0,size=downloadDataBeans.size(); i<size; i++) {
            if (downloadDataBeans.get(i).getId().equals(refreshDownloadData.getId())) {
                downloadDataBeans.get(i).setProgress(refreshDownloadData.getPlayPosition());
                downloadDataBeans.get(i).setDuration(refreshDownloadData.getVideoDuration());
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 3) {
            mPresenter = new DownloadDataPresenter(downloadId, 0,limit, this);
            loadData();
        }
    }
*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent downloadEvent) {
        new Handler().postDelayed(() -> {
            for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
                if (downloadDataBeans.get(i).getAnimeTitle().equals(downloadEvent.getTitle()) && downloadEvent.getDrama().contains(downloadDataBeans.get(i).getPlayNumber())) {
                    downloadDataBeans.get(i).setComplete(1);
                    String path = downloadEvent.getFilePath();
                    if (path.contains("m3u8")) {
                        path = path.replaceAll("m3u8", "mp4");
                        File file = new File(path);
                        downloadDataBeans.get(i).setFileSize(file == null ? 0 : file.length());
                        downloadDataBeans.get(i).setPath(path);
                    } else {
                        downloadDataBeans.get(i).setFileSize(downloadEvent.getFileSize());
                        downloadDataBeans.get(i).setPath(downloadEvent.getFilePath());
                    }
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }, 1000);
    }

    @Override
    protected void setConfigurationChanged() {
        if (downloadDataBeans.size() == 0) return;
        setRecyclerViewView();
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
