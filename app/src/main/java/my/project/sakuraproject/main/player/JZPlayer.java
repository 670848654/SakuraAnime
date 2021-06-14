package my.project.sakuraproject.main.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import cn.jzvd.JZDataSource;
import cn.jzvd.JZUtils;
import cn.jzvd.JzvdStd;
import my.project.sakuraproject.R;
import my.project.sakuraproject.cling.ui.DLNAActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;

public class JZPlayer extends JzvdStd {
    private Context context;
    private CompleteListener listener;
    private TouchListener touchListener;
    private ImageView ibLock;
    private boolean locked = false;
    public ImageView fastForward, quickRetreat, config, airplay;
    public TextView tvSpeed, snifferBtn, openDrama;
    public int currentSpeedIndex = 1;

    public JZPlayer(Context context) { super(context); }

    public JZPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Context context, CompleteListener listener, TouchListener touchListener) {
        this.context = context;
        this.listener = listener;
        this.touchListener = touchListener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.jz_layout_std;
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
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.std_lock:
                if (locked) {
                    // 已经上锁，再次点击解锁
                    changeUiToPlayingShow();
                    ibLock.setImageResource(R.drawable.player_btn_locking);
                    Toast.makeText(context, "屏幕锁定关闭", Toast.LENGTH_SHORT).show();
                } else {
                    // 上锁
                    changeUiToPlayingClear();
                    ibLock.setImageResource(R.drawable.player_btn_locking_pre);
                    Toast.makeText(context, "屏幕锁定开启", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.quick_retreat:
                //当前时间
                long quickRetreatCurrentPositionWhenPlaying = getCurrentPositionWhenPlaying();
                //快退（15S）
                long quickRetreatProgress = quickRetreatCurrentPositionWhenPlaying - (Integer) SharedPreferencesUtils.getParam(context, "user_speed", 15) * 1000;
                if (quickRetreatProgress > 0) mediaInterface.seekTo(quickRetreatProgress);
                else mediaInterface.seekTo(0);
                break;
            case R.id.tvSpeed:
                if (currentSpeedIndex == 7) currentSpeedIndex = 0;
                else currentSpeedIndex += 1;
                mediaInterface.setSpeed(getSpeedFromIndex(currentSpeedIndex));
                tvSpeed.setText("倍数X" + getSpeedFromIndex(currentSpeedIndex));
                break;
            case R.id.airplay:
                Bundle bundle = new Bundle();
                bundle.putString("playUrl", jzDataSource.getCurrentUrl().toString());
                bundle.putLong("duration", getDuration());
                Log.e("duration", getDrawingTime() + "");
                Log.e("playUrl", jzDataSource.getCurrentUrl().toString());
                context.startActivity(new Intent(context, DLNAActivity.class).putExtras(bundle));
                break;
        }
    }

    private float getSpeedFromIndex(int index) {
        float ret = 0f;
        switch (index) {
            case 0:
                ret = 0.5f;
                break;
            case 1:
                ret = 1.0f;
                break;
            case 2:
                ret = 1.25f;
                break;
            case 3:
                ret = 1.5f;
                break;
            case 4:
                ret = 1.75f;
                break;
            case 5:
                ret = 2.0f;
                break;
            case 6:
                ret = 2.5f;
                break;
            case 7:
                ret = 3.0f;
                break;
        }
        return ret;
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
        }
        if (screen == SCREEN_FULLSCREEN)
            ibLock.setVisibility(ibLock.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void playingShow() {
        setAllControlsVisiblity(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE,
                View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);
        ibLock.setVisibility(GONE);
        fastForward.setVisibility(GONE);
        quickRetreat.setVisibility(GONE);
        config.setVisibility(GONE);
        airplay.setVisibility(GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchListener.touch();
        switch (event.getAction()) {
            // 用户滑动屏幕的操作，返回true来屏蔽音量、亮度、进度的滑动功能
            case MotionEvent.ACTION_MOVE:
                if (locked)
                    return true;
        }
        return super.onTouch(v, event);
    }

    //这里是播放的时候屏幕上面UI消失  只显示下面底部的进度条UI
    @Override
    public void changeUiToPlayingClear() {
        super.changeUiToPlayingClear();
        ibLock.setVisibility(View.INVISIBLE);
        fastForward.setVisibility(INVISIBLE);
        quickRetreat.setVisibility(INVISIBLE);
        config.setVisibility(INVISIBLE);
        airplay.setVisibility(INVISIBLE);
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
        });
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
        fullscreenButton.setImageResource(R.drawable.baseline_view_selections_white_48dp);
        tvSpeed.setText("倍数X" + getSpeedFromIndex(currentSpeedIndex));
    }

    public interface  CompleteListener{
        void complete();
    }

    public interface TouchListener {
        void touch();
    }

    @Override
    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        super.setUp(jzDataSource, screen, mediaInterfaceClass);
        batteryTimeLayout.setVisibility(GONE);
    }

    public void startPIP(){ changeUiToPlayingClear(); }

    @Override
    public void onAutoCompletion() {
        onStateAutoComplete();
        listener.complete();
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
}
