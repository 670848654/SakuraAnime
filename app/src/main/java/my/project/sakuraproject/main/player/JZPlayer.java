package my.project.sakuraproject.main.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZUtils;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import master.flame.danmaku.BuildConfig;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.cling.ui.DLNAActivity;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.custom.LongPressEventView;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class JZPlayer extends JzvdStd {
    float starX, startY;
    private Context context;
    private CompleteListener listener;
    private TouchListener touchListener;
    private ShowOrHideChangeViewListener showOrHideChangeViewListener;
    private OnProgressListener onProgressListener;
    private PlayingListener playingListener;
    private PauseListener pauseListener;
    private ImageView ibLock;
    private boolean locked = false;
    public ImageView fastForward, quickRetreat, config, airplay;
    public TextView tvSpeed, snifferBtn, openDrama, preVideo, nextVideo, display;
    public int currentSpeedIndex = 1;
    public float speedRet = 1.0f;
    public boolean isLocalVideo;
    public String localVideoPath;
    private LocalVideoDLNAServer localVideoDLNAServer;
    public int displayIndex = 1;
    public TextView selectDramaView;
    private RelativeLayout longPressBgView;
    private boolean isLongClick = false;
    public ImageView pipView;
    // 弹幕
    public boolean openDanmuConfig; // 是否启用弹幕功能
    public DanmakuView danmakuView;
    public DanmakuContext danmakuContext;
    public BaseDanmakuParser danmakuParser;
    public ImageView danmuView;
    public TextView danmuInfoView;
    public boolean open_danmu = true;
    public boolean loadError = false;

    public JZPlayer(Context context) { super(context); }

    public JZPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Context context, CompleteListener listener,
                            TouchListener touchListener, ShowOrHideChangeViewListener showOrHideChangeViewListener,
                            OnProgressListener onProgressListener, PlayingListener playingListener, PauseListener pauseListener) {
        this.context = context;
        this.listener = listener;
        this.touchListener = touchListener;
        this.onProgressListener = onProgressListener;
        this.playingListener = playingListener;
        this.pauseListener = pauseListener;
        this.showOrHideChangeViewListener = showOrHideChangeViewListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.custom_jz_layout_std;
    }

    @Override
    public void init(Context context) {
        super.init(context);
        // 获取自定义添加的控件
        ibLock = findViewById(R.id.std_lock);
        ibLock.setOnClickListener(this);
        quickRetreat = findViewById(R.id.quick_retreat);
        quickRetreat.setOnClickListener(this);
        fastForward = findViewById(R.id.fast_forward);
        fastForward.setOnClickListener(this);
        config = findViewById(R.id.config);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvSpeed.setOnClickListener(this);
        airplay = findViewById(R.id.airplay);
        airplay.setOnClickListener(this);
        snifferBtn = findViewById(R.id.sniffer_btn);
        openDrama = findViewById(R.id.open_drama_list);
        preVideo = findViewById(R.id.pre_video);
        nextVideo = findViewById(R.id.next_video);
        display = findViewById(R.id.display);
        display.setOnClickListener(this);
        selectDramaView = findViewById(R.id.select_drama);
        danmuInfoView = findViewById(R.id.danmu_info);
        pipView = findViewById(R.id.pip);
        danmuView = findViewById(R.id.danmu);
        danmuView.setOnClickListener(this);
        openDanmuConfig = (Boolean) SharedPreferencesUtils.getParam(context, "open_danmu", true);
        if (!openDanmuConfig) {
            danmuView.setVisibility(GONE);
            danmuInfoView.setVisibility(GONE);
        }
        danmakuView = findViewById(R.id.jz_danmu);
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行,可设置多种类型限制行数
        danmakuContext = DanmakuContext.create();
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.2f)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair).setDanmakuMargin(40);
        longPressBgView = findViewById(R.id.long_press_bg);
        LongPressEventView viewLongPress = findViewById(R.id.surface_container);
        viewLongPress.setLongPressEventListener(new LongPressEventView.LongPressEventListener() {
            @Override
            public void onLongClick(View v) {
                isLongClick = true;
                //震动反馈
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                if (mediaInterface != null) {
                    mediaInterface.setSpeed(2.0f);
                    longPressBgView.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onDisLongClick(View v) {
                if (mediaInterface != null) {
                    mediaInterface.setSpeed(speedRet);
                    longPressBgView.setVisibility(GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.std_lock:
                ibLock.setTag(1);
                if (locked) {
                    // 已经上锁，再次点击解锁
                    changeUiToPlayingShow();
                    ibLock.setImageResource(R.drawable.player_btn_locking);
                    CustomToast.showToast(context, "屏幕锁定关闭", CustomToast.SUCCESS);
                } else {
                    // 上锁
                    changeUiToPlayingClear();
                    ibLock.setImageResource(R.drawable.player_btn_locking_pre);
                    CustomToast.showToast(context, "屏幕锁定开启", CustomToast.SUCCESS);
//                    Drawable up = ContextCompat.getDrawable(context,R.drawable.player_btn_locking_pre);
//                    Drawable drawableUp= DrawableCompat.wrap(up);
//                    DrawableCompat.setTint(drawableUp, ContextCompat.getColor(context,R.color.colorAccent));
//                    ibLock.setImageDrawable(drawableUp);
                }
                locked = !locked;
                break;
            case R.id.fast_forward:
                //总时间长度
                long duration = getDuration();
                //当前时间
                long currentPositionWhenPlaying = getCurrentPositionWhenPlaying();
                //快进（15S）
                long fastForwardProgress = currentPositionWhenPlaying + (Integer) SharedPreferencesUtils.getParam(context, "user_speed", 15) * 1000;
                if (duration > fastForwardProgress) mediaInterface.seekTo(fastForwardProgress);
                else mediaInterface.seekTo(duration);
                seekDanmu(currentPositionWhenPlaying);
                break;
            case R.id.quick_retreat:
                //当前时间
                long quickRetreatCurrentPositionWhenPlaying = getCurrentPositionWhenPlaying();
                //快退（15S）
                long quickRetreatProgress = quickRetreatCurrentPositionWhenPlaying - (Integer) SharedPreferencesUtils.getParam(context, "user_speed", 15) * 1000;
                if (quickRetreatProgress > 0) mediaInterface.seekTo(quickRetreatProgress);
                else mediaInterface.seekTo(0);
                seekDanmu(quickRetreatProgress);
                break;
            case R.id.tvSpeed:
                if (currentSpeedIndex == 7) currentSpeedIndex = 0;
                else currentSpeedIndex += 1;
                mediaInterface.setSpeed(getSpeedFromIndex(currentSpeedIndex));
                tvSpeed.setText(currentSpeedIndex == 1 ? "倍速" : "倍速X" + getSpeedFromIndex(currentSpeedIndex));
                break;
            case R.id.airplay:
                if (!Utils.isWifi(context)) {
                    CustomToast.showToast(context, "投屏需要连接Wifi，确保与投屏设备网络环境一致~", CustomToast.WARNING);
                    return;
                }
                if (isLocalVideo) {
                    // 本地投屏
                    if (localVideoDLNAServer != null) localVideoDLNAServer.stop();
                    localVideoDLNAServer = new LocalVideoDLNAServer(8080, localVideoPath);
                    try {
                        localVideoDLNAServer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Bundle bundle = new Bundle();
                String videoUrl = jzDataSource.getCurrentUrl().toString().replaceAll("\\\\", "");
                bundle.putString("playUrl", isLocalVideo ? Utils.getLocalIpAddress(context) : videoUrl);
                bundle.putLong("duration", getDuration());
//                CustomToast.showToast(context, videoUrl, CustomToast.SUCCESS);
                context.startActivity(new Intent(context, DLNAActivity.class).putExtras(bundle));
                break;
            case R.id.display:
                if (displayIndex == 4) displayIndex = 1;
                else displayIndex += 1;
                display.setText(getDisplayIndex(displayIndex));
                break;
            case R.id.danmu:
                if (danmakuView == null)
                    return;
                if (open_danmu) {
                    open_danmu = false;
                    // 关闭弹幕
                    danmuView.setImageDrawable(context.getResources().getDrawable(R.drawable.outline_subtitles_off_white_48dp));
                    hideDanmmu();
                } else {
                    open_danmu = true;
                    // 打开弹幕
                    danmuView.setImageDrawable(context.getResources().getDrawable(R.drawable.outline_subtitles_white_48dp));
                    showDanmmu();
                }
                break;
        }
    }

    private float getSpeedFromIndex(int index) {
        switch (index) {
            case 0:
                speedRet = 0.5f;
                break;
            case 1:
                speedRet = 1.0f;
                break;
            case 2:
                speedRet = 1.25f;
                break;
            case 3:
                speedRet = 1.5f;
                break;
            case 4:
                speedRet = 1.75f;
                break;
            case 5:
                speedRet = 2.0f;
                break;
            case 6:
                speedRet = 2.5f;
                break;
            case 7:
                speedRet = 3.0f;
                break;
        }
        return speedRet;
    }

    private String getDisplayIndex(int index) {
        String text = "";
        switch (index) {
            case 1:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER);
                text = "自适应比例";
                break;
            case 2:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT);
                text = "拉伸全屏";
                break;
            case 3:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP);
                text = "裁剪全屏";
                break;
            case 4:
                Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL);
                text = "原始大小";
                break;
        }
        return text;
    }

    //这里是播放的时候点击屏幕出现的UI
    @Override
    public void changeUiToPlayingShow() {
        // 此处做锁屏功能的按钮显示，判断是否锁屏状态，并且需要注意当前屏幕状态
        if (!locked) {
            super.changeUiToPlayingShow();
            fastForward.setVisibility(VISIBLE);
            quickRetreat.setVisibility(VISIBLE);
            config.setVisibility(VISIBLE);
            airplay.setVisibility(VISIBLE);
            fullscreenButton.setVisibility(GONE);
            showOrHideChangeViewListener.showOrHideChangeView();
        }
        if (screen == SCREEN_FULLSCREEN)
            ibLock.setVisibility(ibLock.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void playingShow() {
        setAllControlsVisiblity(View.GONE, View.GONE, View.GONE,
                View.VISIBLE, View.GONE, View.GONE, View.GONE);
        ibLock.setVisibility(GONE);
        fastForward.setVisibility(GONE);
        quickRetreat.setVisibility(GONE);
        config.setVisibility(GONE);
        airplay.setVisibility(GONE);
        preVideo.setVisibility(GONE);
        nextVideo.setVisibility(GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchListener.touch();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                starX = event.getX();
                startY = event.getY();
                if (locked) {
                    return true;
                }
                break;
            // 用户滑动屏幕的操作，返回true来屏蔽音量、亮度、进度的滑动功能
            case MotionEvent.ACTION_MOVE:
                if (locked)
                    return true;
            case MotionEvent.ACTION_UP:
                if (locked) {
                    //&& Math.abs(Math.abs(event.getX() - starX)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()  && Math.abs(Math.abs(event.getY() - startY)) > ViewConfiguration.get(getContext()).getScaledTouchSlop()
                    if (event.getX() == starX || event.getY() == startY) {
                        startDismissControlViewTimer();
                        onClickUiToggle();
                    }
                    return true;
                }
                break;
        }
        return super.onTouch(v, event);
    }

    @Override
    public void onClickUiToggle() {
        super.onClickUiToggle();
        if (isLongClick) {
            changeUiToPlayingClear();
            isLongClick = false;
        }
        if (!locked) {
            if (bottomContainer.getVisibility() == View.VISIBLE) {
                ibLock.setVisibility(View.VISIBLE);
            } else {
                ibLock.setVisibility(View.GONE);
            }
        } else {
            if ((int) ibLock.getTag() == 1) {
                bottomProgressBar.setVisibility(GONE);
                ibLock.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        if (locked) {
            bottomContainer.setVisibility(GONE);
            topContainer.setVisibility(GONE);
            startButton.setVisibility(GONE);
        }
    }

    //这里是播放的时候屏幕上面UI消失  只显示下面底部的进度条UI
    @Override
    public void changeUiToPlayingClear() {
        if ((Boolean) SharedPreferencesUtils.getParam(context, "hide_progress", false))
            setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                    View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);// 全屏播放时隐藏底部进度条
        else
            super.changeUiToPlayingClear();
        ibLock.setVisibility(View.INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        config.setVisibility(INVISIBLE);
        airplay.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    // 点击暂停按钮执行的回调
    @Override
    public void onStatePause() {
        super.onStatePause();
        ibLock.setVisibility(View.INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        config.setVisibility(INVISIBLE);
        airplay.setVisibility(VISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
        pauseListener.pause();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    public void releaseDanMu() {
        if (danmakuView != null) danmakuView.release();
//        danmakuView = null;
    }

    @Override
    public void onStateError() {
        super.onStateError();
        loadError = true;
        if (danmakuView != null) danmakuView.release();
    }

    //这里是暂停的时候点击屏幕消失的UI,只显示下面底部的进度条UI
    @Override
    public void changeUiToPauseClear() {
        super.changeUiToPauseClear();
        ibLock.setVisibility(View.INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        config.setVisibility(INVISIBLE);
        airplay.setVisibility(VISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    //这里是出错的UI
    @Override
    public void changeUiToError() {
        super.changeUiToError();
        ibLock.setVisibility(View.INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        config.setVisibility(INVISIBLE);
        airplay.setVisibility(INVISIBLE);
        preVideo.setVisibility(INVISIBLE);
        nextVideo.setVisibility(INVISIBLE);
    }

    // 点击屏幕会出现所有控件，一定时间后消失的回调
    @Override
    public void dissmissControlView() {
        super.dissmissControlView();
        // 需要在UI线程进行隐藏
        post(() -> {
            ibLock.setVisibility(View.INVISIBLE);
            fastForward.setVisibility(INVISIBLE);
            quickRetreat.setVisibility(INVISIBLE);
            config.setVisibility(INVISIBLE);
            airplay.setVisibility(state == STATE_ERROR ? INVISIBLE : VISIBLE);
            if ((Boolean) SharedPreferencesUtils.getParam(context, "hide_progress", false))
                bottomProgressBar.setVisibility(View.INVISIBLE);// 全屏播放时隐藏底部进度条
            preVideo.setVisibility(INVISIBLE);
            nextVideo.setVisibility(INVISIBLE);
        });
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
//        fullscreenButton.setImageResource(R.drawable.baseline_view_selections_white_48dp);
//        tvSpeed.setText("倍数X" + getSpeedFromIndex(currentSpeedIndex));
    }

    public interface PlayingListener {
        void playing();
    }

    public interface PauseListener {
        void pause();
    }

    public interface CompleteListener {
        void complete();
    }

    public interface TouchListener {
        void touch();
    }

    public interface ShowOrHideChangeViewListener {
        void showOrHideChangeView();
    }

    public interface OnProgressListener {
        void getPosition(long position, long duration);
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        batteryTimeLayout.setVisibility(GONE);
    }

    public void startPIP(){ changeUiToPlayingClear(); }

    @Override
    public void onAutoCompletion() {
        super.onStateAutoComplete();
        listener.complete();
        /*danmakuView.stop();
        danmakuView.clear();
        danmakuView.clearDanmakusOnScreen();*/
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        playingListener.playing();
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.resume();
        }
    }

    @Override
    public void showWifiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogStyle);
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), (dialog, which) -> {
            dialog.dismiss();
            WIFI_TIP_DIALOG_SHOWED = true;
            if (state == STATE_PAUSE) {
                startButton.performClick();
            } else {
                startVideo();
            }

        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), (dialog, which) -> {
            dialog.dismiss();
            releaseAllVideos();
            ViewGroup vg = (ViewGroup) (JZUtils.scanForActivity(getContext())).getWindow().getDecorView();
            vg.removeView(this);
            if (mediaInterface != null) mediaInterface.release();
            CURRENT_JZVD = null;
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    @Override
    public void onSeekComplete() {
        super.onSeekComplete();
        seekDanmu(getCurrentPositionWhenPlaying());
    }

    public void seekDanmu(long time) {
        if (danmakuView != null) {
            danmakuView.clearDanmakusOnScreen();
            danmakuView.seekTo(time);
            showDanmmu();
        }
    }

    @Override
    public void onProgress(int progress, long position, long duration) {
        super.onProgress(progress, position, duration);
        onProgressListener.getPosition(position, duration);
    }

    @Override
    public void reset() {
        super.reset();
        if (localVideoDLNAServer != null) localVideoDLNAServer.stop();
    }

    public void showDanmmu() {
        if (danmuView != null)
            danmakuView.show();
    }

    public void hideDanmmu() {
        if (danmuView != null)
            danmakuView.hide();
    }

    public void createDanmu() {
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                danmakuView.start();
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {
                // 弹幕倍速设置
                timer.update(getCurrentPositionWhenPlaying());
            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuView.showFPS(BuildConfig.DEBUG);
        danmakuView.enableDanmakuDrawingCache(true);
    }
}
