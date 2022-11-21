package my.project.sakuraproject.main.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.MainAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.MainBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.custom.GridSpaceItemDecoration;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.about.AboutActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.my.DownloadActivity;
import my.project.sakuraproject.main.my.FavoriteActivity;
import my.project.sakuraproject.main.my.HistoryActivity;
import my.project.sakuraproject.main.my.fragment.MyLazyFragment;
import my.project.sakuraproject.main.setting.SettingActivity;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyFragment extends MyLazyFragment {
    private View view;
    @BindView(R.id.video_layout)
    LinearLayout videoView;
    @BindView(R.id.other_layout)
    LinearLayout otherView;
    @BindView(R.id.video_list)
    RecyclerView videoRvList;
    @BindView(R.id.other_list)
    RecyclerView otherRvList;
    private MainAdapter videoAdapter;
    private List<MainBean> videoList = new ArrayList<>();
    private MainAdapter otherAdapter;
    private List<MainBean> otherList = new ArrayList<>();
    protected GridSpaceItemDecoration gridSpaceItemDecoration;
    private String[] sourceItems = Utils.getArray(R.array.source);
    private AlertDialog alertDialog;
    private String downloadUrl = "";

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_my_new, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        initAdapter();
        return view;
    }

    private void initAdapter() {
        videoList.add(new MainBean("追番列表", 1, R.drawable.outline_movie_filter_white_48dp, DatabaseUtil.queryFavoriteCount()));
        videoList.add(new MainBean("历史播放记录", 2, R.drawable.baseline_history_white_48dp, DatabaseUtil.queryHistoryCount()));
        videoList.add(new MainBean("视频下载列表", 3, R.drawable.baseline_download_white_48dp, DatabaseUtil.queryAllDownloadCount()));
        videoRvList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        gridSpaceItemDecoration = new GridSpaceItemDecoration(4, Utils.dpToPx(getActivity(), 8), true);
        videoRvList.addItemDecoration(gridSpaceItemDecoration);
        videoAdapter = new MainAdapter(getActivity(), videoList);
        videoAdapter.setOnItemClickListener((adapter, view, position) -> {
            switch (videoList.get(position).getType()) {
                case 1:
                    startActivity(new Intent(getActivity(), FavoriteActivity.class));
                    break;
                case 2:
                    startActivity(new Intent(getActivity(), HistoryActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(getActivity(), DownloadActivity.class));
                    break;
            }
        });
        videoRvList.setAdapter(videoAdapter);

        otherList.add(new MainBean("更换站点", 0, R.drawable.baseline_sync_alt_white_48dp, 0));
        otherList.add(new MainBean(Utils.getTheme() ? "亮色主题" : "暗色主题", 1, R.drawable.baseline_style_white_48dp, 0));
        otherList.add(new MainBean("版本 "+Utils.getASVersionName(), 2, R.drawable.outline_cloud_sync_white_48dp, 0));
        otherList.add(new MainBean("设置", 3, R.drawable.ic_settings_white_48dp, 0));
        otherList.add(new MainBean("关于", 4, R.drawable.baseline_android_white_48dp, 0));
        otherRvList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        gridSpaceItemDecoration = new GridSpaceItemDecoration(4, Utils.dpToPx(getActivity(), 8), true);
        otherRvList.addItemDecoration(gridSpaceItemDecoration);
        otherAdapter = new MainAdapter(getActivity(), otherList);
        otherAdapter.setOnItemClickListener((adapter, view, position) -> {
            switch (otherList.get(position).getType()) {
                case 0:
                    setDefaultSource();
                    break;
                case 1:
                    EventBus.getDefault().post(new Refresh(-1));
                    break;
                case 2:
                    if (downloadUrl.isEmpty())
                        checkUpdate();
                    else
                        viewInBrowser();
                    break;
                case 3:
                    startActivity(new Intent(getActivity(), SettingActivity.class));
                    break;
                case 4:
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    break;
            }
        });
        otherRvList.setAdapter(otherAdapter);
    }

    private void setDefaultSource() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.select_source));
        int selected = Utils.isImomoe() ? 1 : 0;
        builder.setSingleChoiceItems(sourceItems, selected, (dialog, index) -> {
            switch (index) {
                case 0:
                    SharedPreferencesUtils.setParam(getActivity(), "isImomoe", false);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getActivity(), "isImomoe", true);
                    break;
            }
            setDomain();
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setDomain() {
        Sakura.setApi();
        EventBus.getDefault().post(new Refresh(0));
    }

    public void checkUpdate() {
        alertDialog = Utils.getProDialog(getActivity(), R.string.check_update_text);
        new Handler().postDelayed(() -> new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Utils.cancelDialog(alertDialog);
                    CustomToast.showToast(getActivity(), Utils.getString(R.string.ck_network_error_start), CustomToast.ERROR);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String newVersion = obj.getString("tag_name");
                    if (newVersion.equals(Utils.getASVersionName()))
                        getActivity().runOnUiThread(() -> {
                            Utils.cancelDialog(alertDialog);
                            CustomToast.showToast(getActivity(), Utils.getString(R.string.no_new_version), CustomToast.SUCCESS);
                        });
                    else {
                        downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        String body = obj.getString("body");
                        getActivity().runOnUiThread(() -> {
                            Utils.cancelDialog(alertDialog);
                            Utils.findNewVersion(getActivity(),
                                    newVersion,
                                    body,
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        viewInBrowser();
                                    },
                                    (dialog, which) -> dialog.dismiss()
                            );
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
//                    application.showErrorToastMsg(Utils.getString(R.string.ck_error_start));
                    CustomToast.showToast(getActivity(), Utils.getString(R.string.ck_error_start), CustomToast.ERROR);
                    Utils.cancelDialog(alertDialog);
                }
            }
        }), 1000);
    }

    private void viewInBrowser() {
        Utils.putTextIntoClip(downloadUrl);
        CustomToast.showToast(getActivity(), Utils.getString(R.string.url_copied), CustomToast.SUCCESS);
        Utils.viewInBrowser(getActivity(), downloadUrl);
    }

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        Log.e("ref", "刷新列表");
        videoList.get(0).setNumber(DatabaseUtil.queryFavoriteCount());
        videoList.get(1).setNumber(DatabaseUtil.queryHistoryCount());
        videoList.get(2).setNumber(DatabaseUtil.queryAllDownloadCount());
        videoAdapter.setNewData(videoList);
    }

    private void setRecyclerViewView() {
        String config = this.getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad()) {
            videoRvList.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 4 : 6));

            otherRvList.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 4 : 6));
        } else {
            if (isInMagicWindow) {
                videoRvList.setLayoutManager(new GridLayoutManager(getActivity(), 6));

                otherRvList.setLayoutManager(new GridLayoutManager(getActivity(), 6));
            } else {
                videoRvList.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 8 : 16));

                otherRvList.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 8 : 16));
            }
        }
    }
}
