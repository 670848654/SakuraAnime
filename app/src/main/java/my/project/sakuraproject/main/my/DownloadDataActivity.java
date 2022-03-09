package my.project.sakuraproject.main.my;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DownloadDataListAdapter;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.RefreshDownloadData;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.player.LocalPlayerActivity;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
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
    private int limit = 100;
    private int downloadDataCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private String downloadDataId;
    private PopupMenu popupMenu;

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
        Slidr.attach(this, Utils.defaultInit());
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
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            if (downloadDataBeans.get(position).getComplete() != 1) {
                CustomToast.showToast(this, "视频下载完成后才可播放！", CustomToast.WARNING);
                return;
            }
            String path = downloadDataBeans.get(position).getPath();
            popupMenu = new PopupMenu(this, adapter.getViewByPosition(position, R.id.title));
            popupMenu.getMenu().add(android.view.Menu.NONE, 0, 0, "使用内置播放器播放");
            popupMenu.getMenu().add(android.view.Menu.NONE, 1, 1, "使用外置播放器播放");
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        Bundle bundle = new Bundle();
                        bundle.putString("playPath", path);
                        bundle.putString("animeTitle", animeTitle);
                        bundle.putString("dramaTitle", downloadDataBeans.get(position).getPlayNumber());
                        bundle.putSerializable("downloadDataBeans", (Serializable) downloadDataBeans);
                        startActivity(new Intent(this, LocalPlayerActivity.class).putExtras(bundle));
                        break;
                    case 1:
                        Utils.selectVideoPlayer(this, path);
                        break;
                }
                return true;
            });
            popupMenu.show();
        });
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            switch (view.getId()) {
                case R.id.delete_view:
                    showDeleteDataDialog(downloadDataBeans.get(position), position);
                    break;
            }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogStyle);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_remove_download, null);
        AppCompatCheckBox checkBox = view.findViewById(R.id.remove_file_select);
        builder.setPositiveButton(Utils.getString(R.string.page_positive), (dialog, which) -> deleteData(checkBox.isChecked(), bean, position));
        builder.setNegativeButton(Utils.getString(R.string.page_negative), (dialog, which) -> dialog.dismiss());
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }

    private void deleteData(boolean removeFile, DownloadDataBean bean, int position) {
        downloadDataId = bean.getId();
        if (bean.getTaskId() == -1) {
            // -1直接删除
            adapter.remove(position);
            DatabaseUtil.deleteDownloadData(bean.getId());
            CustomToast.showToast(this, "已删除该剧集任务", CustomToast.SUCCESS);
        } else {
            String path = bean.getPath();
            if (path != null && !path.isEmpty()) {
                path = path.substring(0, path.lastIndexOf('/'));
                File file = new File(path);
                if (!file.exists()) file.mkdirs(); // 如果从文件管理器中手动删除整个目录 Aria会报错，在这里重建目录
            }
            // 获取所有下载任务
            List<DownloadEntity> list = Aria.download(this).getTaskList();
            for (DownloadEntity entity : list) {
                if (bean.getTaskId() == entity.getId()) {
                    // 如果是m3u8且已完成的任务
                    if (bean.getComplete() == 1) {
                        File f = new File(bean.getPath());
                        if (f.exists()) f.delete();
                    }
                    Aria.download(this).load(bean.getTaskId()).cancel(removeFile);
                    CustomToast.showToast(this, "已删除该剧集任务", CustomToast.SUCCESS);
                    adapter.remove(position);
                    break;
                }
            }
        }
        downloadDataCount = DatabaseUtil.queryDownloadDataCount(downloadId);
        if (downloadDataBeans.size() <= 0) {
            /*setRecyclerViewView();
            DatabaseUtil.deleteDownload(downloadId);
            adapter.setNewData(downloadDataBeans);
            errorTitle.setText(Utils.getString(R.string.empty_download));
            adapter.setEmptyView(errorView);*/
            DatabaseUtil.deleteDownload(downloadId);
            EventBus.getDefault().post(new Refresh(3));
            finish();
        }
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
        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(obj.getString("title")) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
                TextView number = (TextView) adapter.getViewByPosition(i, R.id.number);
                if (number != null)
                    number.setText(downloadTask.getConvertSpeed() == null ? "0kb/s" : downloadTask.getConvertSpeed());
                TextView state = (TextView) adapter.getViewByPosition(i, R.id.state);
                if (state != null)
                    state.setText(Html.fromHtml("<font color='#ff4081'>下载中</font>"));
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

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(obj.getString("title")) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
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
                break;
            }
        }
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        Log.e("onTaskCancel", downloadTask.getTaskName() + "，取消下载");
        List<Object> objects = DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId());
        DatabaseUtil.deleteDownloadData(downloadDataId);
        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask) {
        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        for (int i = 0, size = downloadDataBeans.size(); i < size; i++) {
            if (downloadDataBeans.get(i).getAnimeTitle().equals(obj.getString("title")) && downloadTask.getTaskName().contains(downloadDataBeans.get(i).getPlayNumber())) {
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
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    @Override
    public void showSuccessView(List<DownloadDataBean> list) {
        setLoadState(true);
        runOnUiThread(() -> {
            if (isMain) {
                downloadDataBeans = list;
                setRecyclerViewView();
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
                setRecyclerViewView();
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

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isPad()) setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        if (downloadDataBeans.size() == 0) return;
        setRecyclerViewView();
    }

    private void setRecyclerViewView() {
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (downloadDataBeans.size() == 0) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            return;
        }
        if (!Utils.isPad()) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        }
        else {
            if (isInMagicWindow) {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            }
        }
    }
}
