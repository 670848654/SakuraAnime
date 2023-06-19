package my.project.sakuraproject.main.my.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DownloadListAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.DownloadBean;
import my.project.sakuraproject.bean.DownloadEvent;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.UpdateImgBean;
import my.project.sakuraproject.custom.CustomLoadMoreView;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.main.my.DownloadContract;
import my.project.sakuraproject.main.my.DownloadDataActivity;
import my.project.sakuraproject.main.my.DownloadPresenter;
import my.project.sakuraproject.main.my.UpdateImgContract;
import my.project.sakuraproject.main.my.UpdateImgPresenter;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.util.Utils;

@Deprecated
public class DownloadFragment extends MyLazyFragment<DownloadContract.View, DownloadPresenter> implements DownloadContract.View, UpdateImgContract.View {
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    CoordinatorLayout msg;
    private int limit = 100;
    private int downloadCount = 0;
    private boolean isMain = true;
    protected boolean isErr = true;
    private DownloadListAdapter adapter;
    private List<DownloadBean> downloadList = new ArrayList<>();
    private View view;

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
        Aria.download(this).register();
        return view;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (!Utils.isServiceRunning(Sakura.getInstance(), "my.project.sakuraproject.services.DownloadService"))
            new Handler().postDelayed(() -> checkNotCompleteDownloadTask(), 200);
    }

    private void initAdapter() {
        adapter = new DownloadListAdapter(getActivity(), downloadList);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            Bundle bundle = new Bundle();
            bundle.putString("animeTitle", downloadList.get(position).getAnimeTitle());
            bundle.putString("downloadId", downloadList.get(position).getDownloadId());
            startActivity(new Intent(getActivity(), DownloadDataActivity.class).putExtras(bundle));
        });
        adapter.setOnItemLongClickListener((adapter, view, position) -> {
            View v = adapter.getViewByPosition(mRecyclerView, position, R.id.title);
            final PopupMenu popupMenu = new PopupMenu(getActivity(), v);
            popupMenu.getMenuInflater().inflate(R.menu.download_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.go_to_desc:
                        Bundle bundle = new Bundle();
                        bundle.putString("name", downloadList.get(position).getAnimeTitle());
                        bundle.putString("url", downloadList.get(position).getDescUrl());
                        startActivity(new Intent(getActivity(), DescActivity.class).putExtras(bundle));
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
        if (Utils.checkHasNavigationBar(getActivity())) mRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
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

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 3)
            loadDownloadData();
    }

    @Download.onTaskRunning
    protected void running(DownloadTask downloadTask) {
//        JSONObject obj = JSONObject.parseObject(Aria.download(this).load(downloadTask.getEntity().getId()).getExtendField());
        for (int i = 0, size = downloadList.size(); i < size; i++) {
            String title = (String) DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId()).get(0);
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
    public void onDestroy() {
        Aria.download(this).unRegister();
        super.onDestroy();
    }

    @Override
    public void showSuccessView(List<DownloadBean> list) {
        setLoadState(true);
        getActivity().runOnUiThread(() -> {
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
    public void showLoadingView() {
        getActivity().runOnUiThread(() -> {
            adapter.setNewData(downloadList);
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
    public void showLog(String url) {}

    private void checkNotCompleteDownloadTask() {
        List<DownloadEntity> list = Aria.download(this).getAllNotCompleteTask();
        if (list != null && list.size() > 0) {
            AlertDialog alertDialog;
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
            builder.setMessage(String.format("你有%s个未完成的下载任务，是否继续下载？", list.size()+""));
            builder.setPositiveButton(Utils.getString(R.string.download_positive), (dialog, which) -> getActivity().startService(new Intent(getContext(), DownloadService.class)));
            builder.setNegativeButton(Utils.getString(R.string.download_negative), (dialog, which) -> dialog.dismiss());
//        builder.setNeutralButton(Utils.getString(R.string.remove_download_file), (dialog, which) -> deleteData(checkBox.isChecked(), bean, position));
            builder.setCancelable(false);
            alertDialog = builder.create();
            alertDialog.show();
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
            mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 1 : 2));
        else {
            if (isInMagicWindow)
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            else
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        }
        mRecyclerView.getLayoutManager().scrollToPosition(position);
    }

    @Override
    public void showSuccessImg(String oldImgUrl, String imgUrl) {
        if (!isFragmentVisible) return;
        getActivity().runOnUiThread(() -> {
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
        if (!isFragmentVisible) return;
        getActivity().runOnUiThread(() -> CustomToast.showToast(getActivity(), msg, CustomToast.ERROR));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateImgBean updateImgBean) {
        if (!isFragmentVisible) return;
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
}
