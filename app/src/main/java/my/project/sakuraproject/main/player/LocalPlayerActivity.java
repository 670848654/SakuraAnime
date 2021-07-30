package my.project.sakuraproject.main.player;

import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.bean.RefreshDownloadData;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;

public class LocalPlayerActivity extends BaseActivity implements JZPlayer.CompleteListener, JZPlayer.TouchListener,
        JZPlayer.ShowOrHideChangeViewListener, JZPlayer.OnProgressListener, JZPlayer.PlayingListener, JZPlayer.PauseListener {
    @BindView(R.id.player)
    JZPlayer player;
    private String playPath, animeTitle,  dramaTitle;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private List<DownloadDataBean> downloadDataBeans = new ArrayList<>();
    List<AnimeDescDetailsBean> dramaList = new ArrayList<>();
    private DramaAdapter dramaAdapter;
    @BindView(R.id.nav_view)
    LinearLayout linearLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.pic_config)
    RelativeLayout picConfig;
    //播放网址
    private boolean isPip = false;

    @BindView(R.id.nav_config_view)
    LinearLayout navConfigView;
    @BindView(R.id.speed)
    TextView speedTextView;
    private String[] speeds = Utils.getArray(R.array.speed_item);
    private int userSpeed = 2;
    @BindView(R.id.hide_progress)
    SwitchCompat hideProgressSc;
    private int clickIndex;
    private boolean hasPreVideo = false;
    private boolean hasNextVideo = false;
    @BindView(R.id.play_next_video)
    SwitchCompat playNextVideoSc;
    private boolean playNextVideo;
    private String downloadDataId;
    private long playPosition;
    private long videoDuration;
    private boolean hasPosition = false;
    private long userSavePosition = 0;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_local_play;
    }

    @Override
    protected void init() {
        Sakura.addDestoryActivity(this, "player");
        hideGap();
        Bundle bundle = getIntent().getExtras();
        playPath = bundle.getString("playPath");
        animeTitle = bundle.getString("animeTitle");
        dramaTitle = bundle.getString("dramaTitle");
        downloadDataBeans = (List<DownloadDataBean>) bundle.getSerializable("downloadDataBeans");
        initAdapter();
        initPlayer();
        initUserConfig();
    }

    private void initPlayer() {
        //禁止冒泡
        linearLayout.setOnClickListener(view -> {
            return;
        });
        navConfigView.setOnClickListener(view -> {
            return;
        });
        setPlayerPreNextTag();
        linearLayout.getBackground().mutate().setAlpha(150);
        navConfigView.getBackground().mutate().setAlpha(150);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    player.goOnPlayOnPause();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                    player.goOnPlayOnResume();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        player.config.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        player.openDrama.setOnClickListener(view -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        player.setListener(this, true,this, this, this, this, this, this);
        player.WIFI_TIP_DIALOG_SHOWED = true;
        player.backButton.setOnClickListener(v -> finish());
        player.preVideo.setOnClickListener(v -> {
            clickIndex--;
            changePlayUrl(clickIndex);
        });
        player.nextVideo.setOnClickListener(v -> {
            clickIndex++;
            changePlayUrl(clickIndex);
        });
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) picConfig.setVisibility(View.GONE);
        else picConfig.setVisibility(View.VISIBLE);
        if (gtSdk23()) player.tvSpeed.setVisibility(View.VISIBLE);
        else player.tvSpeed.setVisibility(View.GONE);
        player.fullscreenButton.setOnClickListener(view -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        toPlay(playPath, dramaTitle);
    }

    public void startPic() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(this::enterPicInPic, 500);
    }

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        for (DownloadDataBean downloadDataBean : downloadDataBeans) {
            if (downloadDataBean.getComplete() == 1)
                dramaList.add(new AnimeDescDetailsBean(downloadDataBean.getPlayNumber(), downloadDataBean.getPath(), false, downloadDataBean.getId()));
        }
        for (int i=0,size=dramaList.size(); i<size; i++) {
            if (dramaList.get(i).getUrl().equals(playPath)) {
                clickIndex = i;
                downloadDataId = dramaList.get(i).getDownloadDataId();
                break;
            }
        }
        dramaAdapter = new DramaAdapter(this, dramaList);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
//            setResult(0x20);
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
    }

    private void setPlayerPreNextTag() {
        hasPreVideo = clickIndex != 0;
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, dramaList.get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != dramaList.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, dramaList.get(clickIndex+1).getTitle()) : "");
    }

    private void changePlayUrl(int position) {
        saveProgress();
        clickIndex = position;
        setPlayerPreNextTag();
        AnimeDescDetailsBean bean = dramaAdapter.getItem(position);
        Jzvd.releaseAllVideos();
        downloadDataId = bean.getDownloadDataId();
        toPlay(bean.getUrl(), bean.getTitle());
    }

    private void toPlay(String path, String dramaTitle) {
        player.playingShow();
        player.currentSpeedIndex = 1;
        player.localVideoPath = path;
        player.setUp(Uri.fromFile(new File(path)).toString(), animeTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        player.startVideo();
        userSavePosition = DatabaseUtil.queryDownloadDataProgressById(downloadDataId);
        player.seekToInAdvance = userSavePosition;//跳转到指定的播放进度
        player.startButton.performClick();//响应点击事件
        hasPosition = userSavePosition > 0;
    }

    private void initUserConfig() {
        switch ((Integer) SharedPreferencesUtils.getParam(this, "user_speed", 15)) {
            case 5:
                setUserSpeedConfig(speeds[0], 0);
                break;
            case 10:
                setUserSpeedConfig(speeds[1], 1);
                break;
            case 15:
                setUserSpeedConfig(speeds[2], 2);
                break;
            case 30:
                setUserSpeedConfig(speeds[3], 3);
                break;
        }
        hideProgressSc.setChecked((Boolean) SharedPreferencesUtils.getParam(this, "hide_progress", false));
        hideProgressSc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setParam(this, "hide_progress", isChecked);
        });
        playNextVideo = (Boolean) SharedPreferencesUtils.getParam(this, "play_next_video", false);
        playNextVideoSc.setChecked(playNextVideo);
        playNextVideoSc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setParam(this, "play_next_video", isChecked);
            playNextVideo = isChecked;
        });
    }

    private void setUserSpeedConfig(String text, int speed) {
        speedTextView.setText(text);
        userSpeed = speed;
    }

    private void setDefaultSpeed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Utils.getString(R.string.set_user_speed));
        builder.setSingleChoiceItems(speeds, userSpeed, (dialog, which) -> {
            switch (which) {
                case 0:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 5);
                    setUserSpeedConfig(speeds[0], which);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 10);
                    setUserSpeedConfig(speeds[1], which);
                    break;
                case 2:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 15);
                    setUserSpeedConfig(speeds[2], which);
                    break;
                case 3:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 30);
                    setUserSpeedConfig(speeds[3], which);
                    break;
            }
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveProgress() {
        if (Utils.videoHasComplete(playPosition, videoDuration)) {
            playPosition = 0;
            DatabaseUtil.updateDownloadDataProgressById(playPosition, videoDuration, downloadDataId);
        }
        else
            DatabaseUtil.updateDownloadDataProgressById(playPosition > 2000 ? playPosition : 0, videoDuration, downloadDataId);
        EventBus.getDefault().post(new RefreshDownloadData(downloadDataId, playPosition > 2000 ? playPosition : 0, videoDuration));
    }

    @OnClick({R.id.speed_config, R.id.pic_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.speed_config:
                setDefaultSpeed();
                break;
            case R.id.pic_config:
                if (gtSdk26()) startPic();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        if (!inMultiWindow()) player.goOnPlayOnResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPip) finish();
    }

    /**
     * 是否为分屏模式
     *
     * @return
     */
    public boolean inMultiWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return this.isInMultiWindowMode();
        else return false;
    }

    /**
     * Android 8.0 画中画
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPicInPic() {
//        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        // 设置宽高比例值，第一个参数表示分子，第二个参数表示分母
        // 下面的10/5=2，表示画中画窗口的宽度是高度的两倍
//        Rational aspectRatio = new Rational(10,5);
        // 设置画中画窗口的宽高比例
//        builder.setAspectRatio(aspectRatio);
        // 进入画中画模式，注意enterPictureInPictureMode是Android8.0之后新增的方法
//        enterPictureInPictureMode(builder.build());
        PictureInPictureParams builder = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(builder);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            player.startPIP();
            isPip = true;
        } else isPip = false;
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        if (isInMultiWindowMode)
            player.goOnPlayOnResume();
    }

    @Override
    protected void onDestroy() {
        saveProgress();
        player.releaseAllVideos();
        super.onDestroy();
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    @Override
    public void playing() {
        if (hasPosition) {
            new Handler().postDelayed(() -> {
                CustomToast.showToast(this, "已定位到上次观看位置 " + JZUtils.stringForTime(userSavePosition), CustomToast.DEFAULT);
                hasPosition = false;
            }, 500);
        }
    }

    @Override
    public void pause() {
        saveProgress();
    }

    @Override
    public void complete() {
        saveProgress();
        if (hasNextVideo && playNextVideo) {
//            application.showSuccessToastMsg("开始播放下一集");
            CustomToast.showToast(this, "开始播放下一集", CustomToast.SUCCESS);
            clickIndex++;
            changePlayUrl(clickIndex);
        } else {
//            application.showSuccessToastMsg("全部播放完毕");
            CustomToast.showToast(this, "全部播放完毕", CustomToast.SUCCESS);
            if (!drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void touch() {
        hideNavBar();
    }

    @Override
    public void showOrHideChangeView() {
        player.preVideo.setVisibility(hasPreVideo ? View.VISIBLE : View.GONE);
        player.nextVideo.setVisibility(hasNextVideo ? View.VISIBLE : View.GONE);
    }

    @Override
    public void getPosition(long position, long duration) {
        playPosition = position;
        videoDuration = duration;
    }
}
