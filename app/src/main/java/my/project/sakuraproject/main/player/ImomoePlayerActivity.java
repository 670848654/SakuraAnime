package my.project.sakuraproject.main.player;

import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fanchen.sniffing.SniffingUICallback;
import com.fanchen.sniffing.SniffingVideo;
import com.fanchen.sniffing.web.SniffingUtil;
import com.google.android.material.button.MaterialButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class ImomoePlayerActivity extends BaseActivity implements JZPlayer.CompleteListener, JZPlayer.TouchListener, SniffingUICallback {
    @BindView(R.id.player)
    JZPlayer player;
    private String witchTitle, url, diliUrl;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private List<List<AnimeDescDetailsBean>> list = new ArrayList<>();
    private List<List<ImomoeVideoUrlBean>> imomoeBeans = new ArrayList<>();
    private DramaAdapter dramaAdapter;
    private AlertDialog alertDialog;
    private String animeTitle;
    @BindView(R.id.nav_view)
    LinearLayout linearLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.anime_title)
    TextView titleView;
    @BindView(R.id.pic_config)
    RelativeLayout picConfig;
    //播放网址
    private String webUrl;
    private boolean isPip = false;

    @BindView(R.id.nav_config_view)
    LinearLayout navConfigView;
    @BindView(R.id.speed)
    TextView speedTextView;
    private String[] speeds = Utils.getArray(R.array.speed_item);
    private int userSpeed = 2;

    private int nowSource = 0; // 当前源
    private int clickIndex; // 当前点击剧集
    @BindView(R.id.spinner)
    AppCompatSpinner spinner;
    private ArrayAdapter<String> spinnerAdapter;


    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_play_imomoe;
    }

    @Override
    protected void init() {
        Sakura.addDestoryActivity(this, "playerImomoe");
        hideGap();
        Bundle bundle = getIntent().getExtras();
        init(bundle);
        initAdapter();
        initUserConfig();
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    private void init(Bundle bundle) {
        //播放地址
        url = bundle.getString("url");
        //集数名称
        witchTitle = bundle.getString("title");
        //番剧名称
        animeTitle = bundle.getString("animeTitle");
        titleView.setText(animeTitle);
        //源地址
        diliUrl = bundle.getString("dili");
        //剧集list
        list = (List<List<AnimeDescDetailsBean>>) bundle.getSerializable("list");
        //播放地址
        imomoeBeans = (List<List<ImomoeVideoUrlBean>>) bundle.getSerializable("playList");
        //当前选择源
        nowSource = bundle.getInt("nowSource");
        //禁止冒泡
        linearLayout.setOnClickListener(view -> {
            return;
        });
        navConfigView.setOnClickListener(view -> {
            return;
        });
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
        if (list.size() > 1) {
            List<String> items = new ArrayList<>();
            for (int i=1; i<list.size()+1; i++) {
                items.add("播放源 " + i);
            }
            spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner, items);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            spinner.setSelection(nowSource);
            spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    dramaAdapter.setNewData(list.get(position));
                    nowSource = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinner.setVisibility(View.VISIBLE);
        }
        player.config.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        System.out.println(url + "========================>");
        player.setListener(this, this, this);
        player.backButton.setOnClickListener(v -> finish());
        // 加载视频失败，嗅探视频
        player.snifferBtn.setOnClickListener(v -> snifferPlayUrl(imomoeBeans.get(nowSource).get(clickIndex)));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) picConfig.setVisibility(View.GONE);
        else picConfig.setVisibility(View.VISIBLE);
        if (gtSdk23()) player.tvSpeed.setVisibility(View.VISIBLE);
        else player.tvSpeed.setVisibility(View.GONE);
        player.setUp(url, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        player.fullscreenButton.setOnClickListener(view -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        player.startButton.performClick();
        player.startVideo();
    }

    public void startPic() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        new Handler().postDelayed(this::enterPicInPic, 500);
    }

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        dramaAdapter = new DramaAdapter(this, list.get(nowSource));
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            setResult(0x20);
            clickIndex = position;
            drawerLayout.closeDrawer(GravityCompat.END);
            AnimeDescDetailsBean bean = list.get(nowSource).get(position);
            Jzvd.releaseAllVideos();
            alertDialog = Utils.getProDialog(ImomoePlayerActivity.this, R.string.parsing);
            MaterialButton materialButton = (MaterialButton) adapter.getViewByPosition(recyclerView, position, R.id.tag_group);
            materialButton.setTextColor(getResources().getColor(R.color.tabSelectedTextColor));
            bean.setSelected(true);
            String fid = DatabaseUtil.getAnimeID(animeTitle);
            DatabaseUtil.addIndex(fid, Sakura.DOMAIN + list.get(nowSource).get(clickIndex).getUrl());
            System.out.println(Sakura.DOMAIN + list.get(nowSource).get(clickIndex).getUrl());
            diliUrl = VideoUtils.getUrl(bean.getUrl());
            witchTitle = animeTitle + " - " + bean.getTitle();
            playAnime(imomoeBeans.get(nowSource).get(clickIndex).getVidOrUrl());
        });
    }

    private void playAnime(String animeUrl) {
        cancelDialog();
        url = animeUrl;
        switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
            case 0:
                //调用播放器
                Jzvd.releaseAllVideos();
                player.currentSpeedIndex = 1;
                player.setUp(url, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
                player.startVideo();
                break;
            case 1:
                Jzvd.releaseAllVideos();
                Utils.selectVideoPlayer(this, url);
                break;
        }
    }

    /**
     * 嗅探视频真实连接
     * @param imomoeVideoUrlBean
     */
    private void snifferPlayUrl(ImomoeVideoUrlBean imomoeVideoUrlBean) {
        alertDialog = Utils.getProDialog(this, R.string.should_be_used_web);
        try {
            webUrl = imomoeVideoUrlBean.getVidOrUrl().contains("http") ? imomoeVideoUrlBean.getVidOrUrl() : String.format(Api.IMOMOE_PARSE_API, imomoeVideoUrlBean.getParam(), imomoeVideoUrlBean.getVidOrUrl(),  URLEncoder.encode(Sakura.DOMAIN + list.get(nowSource).get(clickIndex).getUrl(),"GB2312"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sniffer(webUrl);
    }

    /**
     * 嗅探方法
     * @param url
     */
    private void sniffer(String url) {
        SniffingUtil.get().activity(this).referer(url).callback(this).url(url).start();
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

    @OnClick({R.id.speed_config, R.id.pic_config, R.id.player_config, R.id.browser_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.speed_config:
                setDefaultSpeed();
                break;
            case R.id.pic_config:
                if (gtSdk26()) startPic();
                break;
            case R.id.player_config:
                Utils.selectVideoPlayer(this, url);
                break;
            case R.id.browser_config:
                Utils.viewInChrome(this, diliUrl);
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
        if (!inMultiWindow()) player.goOnPlayOnPause();
        else player.goOnPlayOnResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        if (!inMultiWindow()) player.goOnPlayOnResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (inMultiWindow()) player.goOnPlayOnResume();
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
            player.goOnPlayOnResume();
        } else isPip = false;
    }

    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    @Override
    protected void onDestroy() {
        player.releaseAllVideos();
        super.onDestroy();
    }

    @Override
    public void complete() {
        application.showSuccessToastMsg("播放完毕");
        if (!drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.openDrawer(GravityCompat.END);
    }

    @Override
    public void onSniffingStart(View webView, String url) {

    }

    @Override
    public void onSniffingFinish(View webView, String url) {
        SniffingUtil.get().releaseWebView();
        cancelDialog();
    }

    @Override
    public void onSniffingSuccess(View webView, String url, List<SniffingVideo> videos) {
        List<String> urls = Utils.ridRepeat(videos);
        if (urls.size() > 1)
            VideoUtils.showMultipleVideoSources(this,
                    urls,
                    (dialog, index) -> playAnime(urls.get(index)),
                    (dialog, which) -> dialog.dismiss(),
                    1);
        else playAnime(urls.get(0));
    }

    @Override
    public void onSniffingError(View webView, String url, int errorCode) {
        Sakura.getInstance().showToastMsg(Utils.getString(R.string.open_web_view));
        VideoUtils.openDefaultWebview(this, webUrl);
        finish();
    }

    @Override
    public void touch() {
        hideNavBar();
    }
}
