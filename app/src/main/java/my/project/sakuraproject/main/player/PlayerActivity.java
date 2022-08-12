package my.project.sakuraproject.main.player;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import androidx.core.view.GravityCompat;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.jzvd.JZUtils;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.Event;
import my.project.sakuraproject.custom.CustomDanmakuParser;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.video.DanmuContract;
import my.project.sakuraproject.main.video.DanmuPresenter;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.sniffing.SniffingUICallback;
import my.project.sakuraproject.sniffing.SniffingVideo;
import my.project.sakuraproject.sniffing.web.SniffingUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class PlayerActivity extends BasePlayerActivity implements VideoContract.View, SniffingUICallback, DanmuContract.View {
    private boolean isMaliMali = false;
    private DanmuPresenter danmuPresenter;

    @Override
    protected boolean isLocalVideo() {
        return false;
    }

    @Override
    protected void setActivityName() {
        Sakura.addDestoryActivity(this, "player");;
    }

    @Override
    protected void setBundleData(Bundle bundle) {
        url = bundle.getString("url");
        witchTitle = bundle.getString("title");
        animeTitle = bundle.getString("animeTitle");
        dramaUrl = bundle.getString("dramaUrl");
        yhdmDescList = (List<AnimeDescDetailsBean>) bundle.getSerializable("list");
        clickIndex = bundle.getInt("clickIndex");
        animeId = bundle.getString("animeId");
        isMaliMali = bundle.getBoolean("isMaliMali");
    }

    @Override
    protected void setPreNextData() {
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, yhdmDescList.get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != yhdmDescList.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, yhdmDescList.get(clickIndex+1).getTitle()) : "");
    }

    @Override
    protected void snifferVideo() {
        alertDialog = Utils.getProDialog(PlayerActivity.this, R.string.should_be_sniffer);
        webUrl = url;
        /*
        if (Patterns.WEB_URL.matcher(animeUrl.replace(" ", "")).matches()) {
            if (animeUrl.contains("jx.618g.com")) {
                cancelDialog();
                url = animeUrl.replaceAll("http://jx.618g.com/\\?url=", "");
                VideoUtils.openWebview(false, this, witchTitle, animeTitle, url, BaseModel.getDomain(false) + dramaUrl, this.list);
            } else sniffer(webUrl, true);
        } else sniffer(webUrl, false);
        */
        if (webUrl.contains("jx.618g.com")) {
            cancelDialog();
            VideoUtils.openDefaultWebview(this, webUrl);
        } else if (webUrl.contains("html")) {
            VideoUtils.showParseAlert(this, (dialog, index) -> {
                dialog.dismiss();
                url = String.format(Api.PARSE_INTERFACE[index], url);
                if (index == Api.PARSE_INTERFACE.length -1) {
                    VideoUtils.openDefaultWebview(this, url);
                    this.finish();
                } else {
                    SniffingUtil.get().activity(this).referer(url).callback(this).url(url).start();
                    Toast.makeText(this, Utils.getString(R.string.select_parse_interface_msg), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            url = String.format(Api.PARSE_API, url);
            SniffingUtil.get().activity(this).referer(url).callback(this).url(url).start();
        }
    }

    @Override
    protected void playVideo() {
        checkPlayUrl(url);
    }

    @Override
    protected void setAdapter() {
        dramaAdapter = new DramaAdapter(this, yhdmDescList);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
    }

    @Override
    protected AnimeDescDetailsBean setAnimeDescDetailsBean(int position) {
        alertDialog = Utils.getProDialog(this, R.string.parsing);
        EventBus.getDefault().post(new Event(false, -1, position));
        return dramaAdapter.getItem(position);
    }

    @Override
    protected void changeVideo(String title) {
        videoPresenter = new VideoPresenter(animeTitle, dramaUrl, 0, title, this);
        videoPresenter.loadData(true);
    }

    @Override
    protected void getDanmu() {
        if (player.openDanmuConfig) {
            danmuPresenter = new DanmuPresenter(animeTitle, witchTitle.split("-")[1].trim(), this);
            danmuPresenter.loadDanmu();
        }
    }

    private BaseDanmakuParser createParser(InputStream stream) {
        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }
        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_ACFUN);
        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new CustomDanmakuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    @Override
    protected void initCustomData() {}

    @Override
    public void cancelDialog() {Utils.cancelDialog(alertDialog);}

    @Override
    public void showYhdmVideoSuccessView(List<String> list) {
        runOnUiThread(() -> {
            hideNavBar();
            cancelDialog();
            Log.e("playUrl", list.toString());
            if (list.size() == 1)
                checkPlayUrl(list.get(0));
            else
                VideoUtils.showMultipleVideoSources(this,
                        list,
                        (dialog, index) -> checkPlayUrl(list.get(index)),
                        null,
                        1, true);
        });
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> {
            hideNavBar();
            VideoUtils.showErrorInfo(this, BaseModel.getDomain(false) + dramaUrl);
        });
    }

    @Override
    public void getVideoError() {
        //网络出错
        runOnUiThread(() -> {
            hideNavBar();
//            application.showErrorToastMsg(Utils.getString(R.string.error_700));
            VideoUtils.showPlayerNetworkErrorDialog(this, (dialog, index) -> videoPresenter.loadData(true));
        });
    }

    @Override
    public void showSuccessYhdmDramasView(List<AnimeDescDetailsBean> dramaList) {
        yhdmDescList = dramaList;
        runOnUiThread(() -> dramaAdapter.setNewData(yhdmDescList));
    }

    @Override
    public void errorDramaView() {
        runOnUiThread(() -> {
//            application.showErrorToastMsg(Utils.getString(R.string.get_drama_error));
            CustomToast.showToast(this, Utils.getString(R.string.get_drama_error), CustomToast.ERROR);
        });
    }

    @Override
    public void showSuccessImomoeVideoUrlView(String playUrl) {
        runOnUiThread(() -> {
            hideNavBar();
            cancelDialog();
            play(playUrl);
        });
    }

    @Override
    public void showSuccessImomoeDramasView(List<AnimeDescDetailsBean> bean) {
        yhdmDescList = bean;
        runOnUiThread(() -> dramaAdapter.setNewData(yhdmDescList));
    }

    @Override
    public void onSniffingStart(View webView, String url) {

    }

    @Override
    public void onSniffingFinish(View webView, String url) {
        SniffingUtil.get().releaseWebView();
        cancelDialog();
        hideNavBar();
    }

    @Override
    public void onSniffingSuccess(View webView, String url, int position, List<SniffingVideo> videos) {
        List<String> urls = Utils.ridRepeat(videos);
        if (urls.size() == 0)
            GetRealPlayingAddressError(url);
        else if (urls.size() > 1)
            VideoUtils.showMultipleVideoSources(this,
                    urls,
                    (dialog, index) -> play(urls.get(index)),
                    null,
                    1, true);
        else play(urls.get(0));
    }

    @Override
    public void onSniffingError(View webView, String url, int position, int errorCode) {
        GetRealPlayingAddressError(url);
    }

    /**
     * 嗅探播放地址失败
     */
    private void GetRealPlayingAddressError(String url) {
//        Sakura.getInstance().showToastMsg(Utils.getString(R.string.open_web_view));
        CustomToast.showToast(this, Utils.getString(R.string.open_web_view), CustomToast.WARNING);
        VideoUtils.openDefaultWebview(this, url);
        finish();
    }


    @Override
    public void showLoadingView() {

    }

    @Override
    public void showLoadErrorView(String msg) {

    }

    @Override
    public void showEmptyVIew() {

    }

    @Override
    public void showLog(String url) {
//        runOnUiThread(() -> application.showToastShortMsg(url));
    }

    private void checkPlayUrl(String url) {
        if (!url.contains("$"))
            play(url);
        else
            snifferPlayUrl(url);
    }

    /**
     * 嗅探视频真实连接
     * @param animeUrl
     */
    private void snifferPlayUrl(String animeUrl) {
        alertDialog = Utils.getProDialog(PlayerActivity.this, R.string.should_be_sniffer);
        webUrl = animeUrl;
        /*
        if (Patterns.WEB_URL.matcher(animeUrl.replace(" ", "")).matches()) {
            if (animeUrl.contains("jx.618g.com")) {
                cancelDialog();
                url = animeUrl.replaceAll("http://jx.618g.com/\\?url=", "");
                VideoUtils.openWebview(false, this, witchTitle, animeTitle, url, BaseModel.getDomain(false) + dramaUrl, this.list);
            } else sniffer(webUrl, true);
        } else sniffer(webUrl, false);
        */
        if (webUrl.contains("jx.618g.com")) {
            cancelDialog();
            VideoUtils.openDefaultWebview(this, webUrl);
        } else {
            url = String.format(Api.PARSE_API, url);
            SniffingUtil.get().activity(this).referer(url).callback(this).url(url).start();
        }
    }

    @Override
    public void showSuccessDanmuView(JSONObject danmus) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (player.loadError) return;
                JSONArray jsonArray = danmus.getJSONObject("data").getJSONArray("data");
                Toast.makeText(this, "查询弹幕API成功，共"+danmus.getJSONObject("data").getInteger("total")+"条弹幕~", Toast.LENGTH_SHORT).show();
                player.danmuInfoView.setText("已加载"+ danmus.getJSONObject("data").getInteger("total") + "条弹幕！");
                player.danmuInfoView.setVisibility(View.VISIBLE);
                InputStream result = new ByteArrayInputStream(jsonArray.toString().getBytes(StandardCharsets.UTF_8));
                player.danmakuParser = createParser(result);
                player.createDanmu();
                if (player.danmakuView.isPrepared()) {
                    player.danmakuView.restart();
                }
                player.danmakuView.prepare(player.danmakuParser, player.danmakuContext);
                if (userSavePosition > 0) {
                    new Handler().postDelayed(() -> {
                        // 一秒后定位弹幕时间为用户上次观看位置
                        player.seekDanmu(userSavePosition);
                    }, 1000);
                }
            }
        });
    }

    @Override
    public void showErrorDanmuView(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (danmuPresenter != null)
            danmuPresenter.detachView();
    }
}
