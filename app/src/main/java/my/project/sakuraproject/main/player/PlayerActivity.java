package my.project.sakuraproject.main.player;

import android.app.PictureInPictureParams;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.main.webview.DefaultWebActivity;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class PlayerActivity extends BaseActivity implements VideoContract.View, JZPlayer.CompleteListener {
    @BindView(R.id.player)
    JZPlayer player;
    private String witchTitle, url, diliUrl;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private List<AnimeDescBean> list = new ArrayList<>();
    private DramaAdapter dramaAdapter;
    private ProgressDialog p;
    private String animeTitle;
    @BindView(R.id.nav_view)
    LinearLayout linearLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.anime_title)
    TextView titleView;
    @BindView(R.id.pic)
    TextView pic;
    private VideoPresenter presenter;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_play;
    }

    @Override
    protected void init() {
        Sakura.addDestoryActivity(this, "player");
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        hideGap();
        Bundle bundle = getIntent().getExtras();
        init(bundle);
        initAdapter();
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
        list = (List<AnimeDescBean>) bundle.getSerializable("list");
        //禁止冒泡
        linearLayout.setOnClickListener(view -> {
            return;
        });
        linearLayout.getBackground().mutate().setAlpha(150);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        player.setListener(this, this);
        player.backButton.setOnClickListener(v -> finish());
//        if (Utils.isPad()) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            pic.setVisibility(View.GONE);
        } else {
            pic.setVisibility(View.VISIBLE);
        }
//        } else
//            pic.setVisibility(View.GONE);
        player.setUp(url, witchTitle, Jzvd.SCREEN_WINDOW_FULLSCREEN);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @OnClick(R.id.pic)
    public void startPic() {
        drawerLayout.closeDrawer(GravityCompat.END);
        new Handler().postDelayed(this::enterPicInPic, 500);
    }

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        dramaAdapter = new DramaAdapter(this, list);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            setResult(0x20);
            drawerLayout.closeDrawer(GravityCompat.END);
            final AnimeDescBean bean = (AnimeDescBean) adapter.getItem(position);
            switch (bean.getType()) {
                case "play":
                    p = Utils.getProDialog(PlayerActivity.this, R.string.parsing);
                    Button v = (Button) adapter.getViewByPosition(recyclerView, position, R.id.tag_group);
                    v.setBackgroundResource(R.drawable.button_selected);
                    v.setTextColor(getResources().getColor(R.color.item_selected_color));
                    bean.setSelect(true);
                    diliUrl = VideoUtils.getUrl(bean.getUrl());
                    witchTitle = animeTitle + " - " + bean.getTitle();
                    presenter = new VideoPresenter(animeTitle, diliUrl, PlayerActivity.this);
                    presenter.loadData(true);
                    break;
            }

        });
    }

    public void goToPlay(List<String> list) {
        new Handler().postDelayed(() -> {
            if (list.size() == 1) oneSource(list);
            else multipleSource(list);
        }, 200);
    }

    /**
     * 只有一个播放地址
     *
     * @param list
     */
    private void oneSource(List<String> list) {
        hideNavBar();
        playAnime(VideoUtils.getVideoUrl(list.get(0)));
    }

    /**
     * 多个播放地址
     *
     * @param list
     */
    private void multipleSource(List<String> list) {
        VideoUtils.showMultipleVideoSources(this,
                list,
                (dialog, index) -> {
                    hideNavBar();
                    playAnime(VideoUtils.getVideoUrl(list.get(index)));
                });
    }

    private void playAnime(String animeUrl) {
        url = animeUrl;
        if (Patterns.WEB_URL.matcher(animeUrl.replace(" ", "")).matches()) {
            if (animeUrl.contains("jx.618g.com")) {
                url = animeUrl.replaceAll("http://jx.618g.com/\\?url=", "");
                VideoUtils.openWebview(false, this, witchTitle, animeTitle, url, diliUrl, this.list);
            } else if (url.contains(".mp4") || url.contains(".m3u8")) {
                switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
                    case 0:
                        //调用播放器
                        Jzvd.releaseAllVideos();
                        player.setUp(url, witchTitle, Jzvd.SCREEN_WINDOW_FULLSCREEN);
                        player.startVideo();
                        break;
                    case 1:
                        Jzvd.releaseAllVideos();
                        Utils.selectVideoPlayer(PlayerActivity.this, url);
                        break;
                }
            }else {
                Sakura.getInstance().showToastMsg(Utils.getString(R.string.should_be_used_web));
                startActivity(new Intent(PlayerActivity.this, DefaultWebActivity.class).putExtra("url", url));
                this.finish();
            }
        }  else {
            Sakura.getInstance().showToastMsg(Utils.getString(R.string.maybe_can_not_play));
            startActivity(new Intent(PlayerActivity.this, DefaultWebActivity.class).putExtra("url",String.format(Api.PARSE_API, url)));
        }
    }

    @OnClick({R.id.select_player, R.id.open_in_browser})
    public void onClick(TextView view) {
        switch (view.getId()) {
            case R.id.select_player:
                Utils.selectVideoPlayer(this, url);
                break;
            case R.id.open_in_browser:
                Utils.viewInChrome(this, diliUrl);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        else finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!inMultiWindow()) JzvdStd.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        if (!inMultiWindow()) JzvdStd.goOnPlayOnResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        JzvdStd.releaseAllVideos();
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
        if (isInPictureInPictureMode) player.startPIP();
        else player.exitPIP();
    }

    @Override
    public void cancelDialog() {
        Utils.cancelProDialog(p);
    }

    @Override
    public void getVideoSuccess(List<String> list) {
        runOnUiThread(() -> {
            hideNavBar();
            goToPlay(list);
        });
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> {
            hideNavBar();
            VideoUtils.showErrorInfo(PlayerActivity.this, diliUrl);

        });
    }

    @Override
    public void getVideoError() {
        //网络出错
        runOnUiThread(() -> {
            hideNavBar();
            application.showToastMsg(Utils.getString(R.string.error_700));
        });
    }

    @Override
    public void showSuccessDramaView(List<AnimeDescBean> dramaList) {
        list = dramaList;
        runOnUiThread(() -> dramaAdapter.setNewData(list));

    }

    @Override
    public void errorDramaView() {
        runOnUiThread(() -> application.showToastMsg(Utils.getString(R.string.get_drama_error)));
    }

    @Override
    protected void onDestroy() {
        if (null != presenter) presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void complete() {
        application.showToastMsg("播放完毕");
        if (!drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.openDrawer(GravityCompat.END);
    }
}
