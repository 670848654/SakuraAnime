package my.project.sakuraproject.main.player;

import android.app.PictureInPictureParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.bean.RefreshDownloadData;
import my.project.sakuraproject.custom.BiliDanmukuParser;
import my.project.sakuraproject.custom.CustomDanmakuParser;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.video.DanmuContract;
import my.project.sakuraproject.main.video.DanmuPresenter;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.services.DLNAService;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public abstract class BasePlayerActivity extends BaseActivity implements JZPlayer.CompleteListener, JZPlayer.TouchListener,
        JZPlayer.ShowOrHideChangeViewListener,  JZPlayer.OnProgressListener, JZPlayer.PlayingListener, JZPlayer.PauseListener, JZPlayer.OnQueryDanmuListener, DanmuContract.View {
    // 通用属性
    @BindView(R.id.player)
    JZPlayer player;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView; // 剧集列表
    @BindView(R.id.nav_view)
    LinearLayout linearLayout;
    @BindView(R.id.nav_config_view)
    LinearLayout navConfigView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    /*@BindView(R.id.pic_config)
    RelativeLayout picConfig;*/
    @BindView(R.id.hide_progress)
    SwitchMaterial hideProgressSc;
    @BindView(R.id.other_view)
    LinearLayout otherView;
    protected String
            animeTitle, // 番剧名称
            witchTitle, // 观看集数
            url, // 播放地址
            dramaUrl; // 源地址
    protected DramaAdapter dramaAdapter;
    protected AlertDialog alertDialog;
    //播放网址
    protected String webUrl;
    @BindView(R.id.speed)
    TextView speedTextView;
    protected String[] speedsStrItems = Utils.getArray(R.array.speed_item);
    protected int[] speedsIntItems = Utils.getIntArray(R.array.speed_set_item);
    protected int userSpeed = 2;
    protected int clickIndex; // 当前点击剧集
    protected boolean hasPreVideo = false;
    protected boolean hasNextVideo = false;
    protected String animeId;
    protected long playPosition;
    protected long videoDuration;
    protected boolean hasPosition = false;
    protected long userSavePosition = 0;
    @BindView(R.id.play_next_video)
    SwitchMaterial playNextVideoSc;
    protected boolean playNextVideo;
    @BindView(R.id.spinner)
    TextView spinner;
    protected PopupMenu popupMenu;
    private boolean isLocalVideo;
    // YHDM源相关属性
    protected List<AnimeDescDetailsBean> yhdmDescList = new ArrayList<>();
    protected VideoPresenter videoPresenter;
    // SILISILI源相关属性
    protected int nowSource = 0; // 当前源
    // Local相关属性
    protected List<AnimeDescDetailsBean> dramaList = new ArrayList<>();
    protected List<DownloadDataBean> downloadDataBeans = new ArrayList<>();
    protected String playPath,  dramaTitle;
    protected String downloadDataId;

    protected DanmuPresenter danmuPresenter;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_player;
    }


    @Override
    protected void init() {
        isLocalVideo = isLocalVideo();
        player.isLocalVideo = isLocalVideo;
        setActivityName();
        Bundle bundle = getIntent().getExtras();
        setBundleData(bundle);
        //禁止冒泡
        player.setOnClickListener(view -> {return;});
        linearLayout.setOnClickListener(view -> {return;});
        navConfigView.setOnClickListener(view -> {return;});
        initCustomData();
        initAdapter();
        initPlayerPreNextTag();
        initPlayerView();
        initNavConfigView();
        initUserConfig();
    }

    protected abstract boolean isLocalVideo();

    protected abstract void setPreNextData();

    private void initNavConfigView() {
        linearLayout.getBackground().mutate().setAlpha(150);
        navConfigView.getBackground().mutate().setAlpha(150);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) player.goOnPlayOnPause();
            }
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) player.goOnPlayOnResume();
            }
            @Override
            public void onDrawerStateChanged(int newState) {}
        });
    }

    protected void initPlayerPreNextTag() {
        hasPreVideo = clickIndex != 0;
        setPreNextData();
    }

    private void initPlayerView() {
        player.configView.setOnClickListener(v -> {
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
        player.setListener(this, this, this, this, this, this, this, this);
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
        /*if (isLocalVideo)
            player.danmuView.setVisibility(View.GONE);*/
        // 加载视频失败，嗅探视频
        player.snifferBtn.setOnClickListener(v -> snifferVideo());
        if (gtSdk23()) player.tvSpeedView.setVisibility(View.VISIBLE);
        else player.tvSpeedView.setVisibility(View.GONE);
        player.selectDramaView.setOnClickListener(view -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        player.pipView.setOnClickListener(view -> {
            if (gtSdk26()) startPic();
        });
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        player.playingShow();
        playVideo();
    }

    protected abstract void snifferVideo();

    protected abstract void playVideo();

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        setAdapter();
    }

    protected abstract void setAdapter();

    @OnClick(R.id.spinner)
    public void showMenu() {
        popupMenu.show();
    }

    private void initUserConfig() {
        switch ((Integer) SharedPreferencesUtils.getParam(this, "user_speed", 15)) {
            case 5:
                setUserSpeedConfig(0);
                break;
            case 10:
                setUserSpeedConfig(1);
                break;
            case 15:
                setUserSpeedConfig(2);
                break;
            case 30:
                setUserSpeedConfig(3);
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

    private void setUserSpeedConfig(int userSet) {
        SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", speedsIntItems[userSet]);
        speedTextView.setText(speedsStrItems[userSet]);
        userSpeed = userSet;
    }

//    @OnClick({R.id.speed_config, R.id.pic_config, R.id.player_config, R.id.browser_config})
    @OnClick({R.id.speed_config, R.id.player_config, R.id.browser_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.speed_config:
                setDefaultSpeed();
                break;
            /*case R.id.pic_config:
                if (gtSdk26()) startPic();
                break;*/
            case R.id.player_config:
                Utils.selectVideoPlayer(this, url);
                break;
            case R.id.browser_config:
                Utils.viewInChrome(this, dramaUrl);
                break;
        }
    }

    private void setDefaultSpeed() {
        Utils.showSingleChoiceAlert(this,
                Utils.getString(R.string.set_user_speed),
                speedsStrItems,
                true,
                userSpeed,
                (dialogInterface, i) -> {
                    setUserSpeedConfig(i);
                    dialogInterface.dismiss();
                }
        );
    }

    protected void changePlayUrl(int position) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        if (isLocalVideo) {
            changeLocalPlayUrl(position);
            return;
        }
        clickIndex = position;
        initPlayerPreNextTag();
        AnimeDescDetailsBean bean = setAnimeDescDetailsBean(position);
        Jzvd.releaseAllVideos();
        MaterialButton materialButton = (MaterialButton) dramaAdapter.getViewByPosition(recyclerView, position, R.id.tag_group);
        materialButton.setTextColor(getResources().getColor(R.color.tabSelectedTextColor));
        bean.setSelected(true);
        saveProgress();
        dramaUrl = bean.getUrl();
        witchTitle = animeTitle + " - " + bean.getTitle();
        player.playingShow();
        changeVideo(bean.getTitle());
    }

    protected void changeLocalPlayUrl(int position) {
        saveProgress();
        clickIndex = position;
        initPlayerPreNextTag();
        AnimeDescDetailsBean bean = setAnimeDescDetailsBean(position);
        Jzvd.releaseAllVideos();
        downloadDataId = bean.getDownloadDataId();
        toPlay(bean.getUrl(), bean.getTitle());
    }

    protected abstract AnimeDescDetailsBean setAnimeDescDetailsBean(int position);

    protected abstract void changeVideo(String title);

    private void saveProgress() {
        if (isLocalVideo) {
            if (Utils.videoHasComplete(playPosition, videoDuration)) {
                playPosition = 0;
                DatabaseUtil.updateDownloadDataProgressById(playPosition, videoDuration, downloadDataId);
            }
            else
                DatabaseUtil.updateDownloadDataProgressById(playPosition > 2000 ? playPosition : 0, videoDuration, downloadDataId);
            EventBus.getDefault().post(new RefreshDownloadData(downloadDataId, playPosition > 2000 ? playPosition : 0, videoDuration));
        } else {
            if (Utils.videoHasComplete(playPosition, videoDuration)) {
                playPosition = 0;
                DatabaseUtil.updateHistory(animeId, dramaUrl, playPosition, videoDuration);
            }
            else
                DatabaseUtil.updateHistory(animeId, dramaUrl, playPosition > 2000 ? playPosition : 0, videoDuration);
        }
    }

    /*private void playAnime(String animeUrl) {
        baseCncelDialog();
        url = animeUrl;
        switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
            case 0:
                //调用播放器
                play(url);
                break;
            case 1:
                Jzvd.releaseAllVideos();
                Utils.selectVideoPlayer(this, url);
                break;
        }
    }*/

    /**
     * 播放视频
     * @param playUrl
     */
    protected void play(String playUrl) {
        Jzvd.releaseAllVideos();
        /*player.currentSpeedIndex = 1;
        player.displayIndex = 1;*/
//        player.setUp(playUrl, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        player.setUp(playUrl, witchTitle, Jzvd.SCREEN_FULLSCREEN, VideoUtils.getUserPlayerKernel(this));
        player.startVideo();
        userSavePosition = DatabaseUtil.getPlayPosition(animeId, dramaUrl);
        player.seekToInAdvance = userSavePosition;//跳转到指定的播放进度
        player.startButton.performClick();//响应点击事件
        hasPosition = userSavePosition > 0;
        getDanmu();
    }

    private void getDanmu() {
        // 2023年9月19日16:18:50 不再支持本地自动获取弹幕，可以手动
        if (isLocalVideo)
            return;
        if (player.openDanmuConfig) {
            if (!isSiliSili()) // YHDM源也可以使用弹幕接口，但有局限性
                Toast.makeText(this, "当前为YHDM源，使用SILISILI弹幕接口对应集数可能不一致！", Toast.LENGTH_LONG).show();
            danmuPresenter = new DanmuPresenter(animeTitle, String.valueOf(yhdmDescList.get(clickIndex).getIndex()),this);
            danmuPresenter.loadDanmu();
        }
    }

    protected void toPlay(String path, String dramaTitle) {
        player.playingShow();
        /*player.currentSpeedIndex = 1;
        player.displayIndex = 1;*/
        player.localVideoPath = path;
//        player.setUp(Uri.fromFile(new File(path)).toString(), animeTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        String playPath = Uri.fromFile(new File(path)).toString();
        try {
            playPath = URLDecoder.decode(playPath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.setUp(playPath, animeTitle + " - " + dramaTitle, Jzvd.SCREEN_FULLSCREEN, VideoUtils.getUserPlayerKernel(this));
        player.startVideo();
        userSavePosition = DatabaseUtil.queryDownloadDataProgressById(downloadDataId);
        player.seekToInAdvance = userSavePosition;//跳转到指定的播放进度
        player.startButton.performClick();//响应点击事件
        hasPosition = userSavePosition > 0;
        getDanmu();
    }


    protected abstract void initCustomData();

    protected abstract void setActivityName();

    protected abstract void setBundleData(Bundle bundle);

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else finish();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            player.startPIP();
            player.hideDanmmu();
        } else {
            if (getWindow().getDecorView().getVisibility() == View.VISIBLE)
                player.showDanmmu();
            else
                finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        if (isInMultiWindowMode)
            player.goOnPlayOnResume();
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
        saveProgress();
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startPic() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(this::enterPicInPic, 500);
    }

    /**
     * Android 8.0 画中画
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPicInPic() {
        PictureInPictureParams builder = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(builder);
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    public void playing() {
        if (hasPosition) {
            CustomToast.showToast(this, "已定位到上次观看位置 " + JZUtils.stringForTime(userSavePosition), CustomToast.DEFAULT);
            hasPosition = false;
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
            player.releaseDanMu();
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
    public void finish() {
        if (null != videoPresenter) videoPresenter.detachView();
        player.releaseAllVideos();
        super.finish();
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

    private BaseDanmakuParser createParser(boolean json, InputStream stream) {
        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }
        ILoader loader = DanmakuLoaderFactory.create(json ? DanmakuLoaderFactory.TAG_ACFUN : DanmakuLoaderFactory.TAG_BILI);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = json ? new CustomDanmakuParser() : new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    @Override
    public void showSuccessDanmuXmlView(String content) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (player.loadError) return;
                try {
//                    Toast.makeText(this, "弹幕接口响应正常", Toast.LENGTH_LONG).show();
                    player.danmuInfoView.setText("弹幕接口响应正常");
                    player.danmuInfoView.setVisibility(View.VISIBLE);
                    InputStream result = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                    player.danmakuParser = createParser(false, result);
                    player.createDanmu();
                    if (player.danmakuView.isPrepared()) {
                        player.danmakuView.restart();
                    }
                    player.danmakuView.prepare(player.danmakuParser, player.danmakuContext);
                    if (!player.open_danmu) {
                        player.hideDanmmu();
                    }
                    if (userSavePosition > 0) {
                        new Handler().postDelayed(() -> {
                            // 一秒后定位弹幕时间为用户上次观看位置
                            player.seekDanmu(userSavePosition);
                        }, 1000);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void showSuccessDanmuView(JSONObject danmus) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (player.loadError) return;
                try {
                    JSONArray jsonArray = danmus.getJSONObject("data").getJSONArray("data");
                    int total = danmus.getJSONObject("data").getInteger("total");
                    if (total == 0)
                        Toast.makeText(this, "未能查询到当前番剧弹幕信息，请检查番剧名称或集数名称是否异常，使用手动查询弹幕功能尝试~", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, "查询弹幕API成功，共"+total+"条弹幕~", Toast.LENGTH_SHORT).show();
                    player.danmuInfoView.setText("已加载"+ danmus.getJSONObject("data").getInteger("total") + "条弹幕！");
                    player.danmuInfoView.setVisibility(View.VISIBLE);
                    InputStream result = new ByteArrayInputStream(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                    player.danmakuParser = createParser(true, result);
                    player.createDanmu();
                    if (player.danmakuView.isPrepared()) {
                        player.danmakuView.restart();
                    }
                    player.danmakuView.prepare(player.danmakuParser, player.danmakuContext);
                    if (!player.open_danmu) {
                        player.hideDanmmu();
                    }
                    if (userSavePosition > 0) {
                        new Handler().postDelayed(() -> {
                            // 一秒后定位弹幕时间为用户上次观看位置
                            player.seekDanmu(userSavePosition);
                        }, 1000);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void showErrorDanmuView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
//        handler.removeCallbacksAndMessages(null);
        stopService(new Intent(this, DLNAService.class));
        if (!isLocalVideo) {
            EventBus.getDefault().post(new Refresh(1));
            EventBus.getDefault().post(new Refresh(2));
        }
        player.releaseDanMu();
        player.releaseAllVideos();
        player.danmakuView = null;
        if (danmuPresenter != null)
            danmuPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void queryDamu(String queryDanmuTitle, String queryDanmuDrama) {
        player.releaseDanMu();
        player.danmuInfoView.setVisibility(View.GONE);
        danmuPresenter = new DanmuPresenter(queryDanmuTitle, queryDanmuDrama, this);
        danmuPresenter.loadDanmu();
    }

    protected boolean isSiliSili() {
        return dramaUrl.contains("vodplay");
    }

    /*@OnClick(R.id.order)
    public void setOrder() {
        String nowClickUrl = "";
        if (isLocalVideo) {
            nowClickUrl = dramaList.get(clickIndex).getUrl();
            Collections.reverse(dramaList);
            for (int i=0,size=dramaList.size(); i<size; i++) {
                if (dramaList.get(i).getUrl().equals(nowClickUrl)) {
                    clickIndex = i;
                    break;
                }
            }
            dramaAdapter.setNewData(dramaList);
        } else {
            nowClickUrl = yhdmDescList.get(clickIndex).getUrl();
            Collections.reverse(yhdmDescList);
            for (int i=0,size=yhdmDescList.size(); i<size; i++) {
                if (yhdmDescList.get(i).getUrl().equals(nowClickUrl)) {
                    clickIndex = i;
                    break;
                }
            }
            dramaAdapter.setNewData(yhdmDescList);
        }
        setPreNextData();
    }*/
}
