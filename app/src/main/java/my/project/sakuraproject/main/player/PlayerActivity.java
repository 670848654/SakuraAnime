package my.project.sakuraproject.main.player;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.core.view.GravityCompat;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.Event;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.sniffing.SniffingUICallback;
import my.project.sakuraproject.sniffing.SniffingVideo;
import my.project.sakuraproject.sniffing.web.SniffingUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class PlayerActivity extends BasePlayerActivity implements VideoContract.View, SniffingUICallback {

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
        nowSource = bundle.getInt("nowSource");
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
//            VideoUtils.openDefaultWebview(this, webUrl);
            Utils.viewInChrome(this, webUrl);
        } else if (webUrl.contains("html")) {
            VideoUtils.showParseAlert(this, (dialog, index) -> {
                dialog.dismiss();
                url = String.format(Api.PARSE_INTERFACE[index], url);
                if (index == Api.PARSE_INTERFACE.length -1) {
//                    VideoUtils.openDefaultWebview(this, url);
                    Utils.viewInChrome(this, webUrl);
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
        EventBus.getDefault().post(new Event(isSiliSili(), nowSource, position));
        return dramaAdapter.getItem(position);
    }

    @Override
    protected void changeVideo(String title) {
        videoPresenter = new VideoPresenter(animeTitle, dramaUrl, nowSource, title, this);
        videoPresenter.loadData(true);
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
            player.onStateError();
            hideNavBar();
            VideoUtils.showErrorInfo(this, dramaUrl, true);
        });
    }

    @Override
    public void getVideoError() {
        //网络出错
        runOnUiThread(() -> {
            player.onStateError();
            hideNavBar();
//            application.showErrorToastMsg(Utils.getString(R.string.error_700));
//            VideoUtils.showPlayerNetworkErrorDialog(this, (dialog, index) -> videoPresenter.loadData(true));
            VideoUtils.showErrorInfo(this, dramaUrl, true);
        });
    }

    @Override
    public void showSuccessYhdmDramasView(List<AnimeDescDetailsBean> dramaList) {
        if (yhdmDescList.size() == 0) {
            yhdmDescList = dramaList;
            runOnUiThread(() -> dramaAdapter.setNewData(yhdmDescList));
        }
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
            checkPlayUrl(playUrl);
        });
    }

    @Override
    public void showSuccessImomoeDramasView(List<AnimeDescDetailsBean> bean) {
        if (yhdmDescList.size() == 0) {
            yhdmDescList = bean;
            runOnUiThread(() -> dramaAdapter.setNewData(yhdmDescList));
        }
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
//        VideoUtils.openDefaultWebview(this, url);
        Utils.viewInChrome(this, url);
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
        if (isSiliSili()) {
            // 当为silisili源时校验
            // 2023年10月13日16:13:43 新版解析不需要校验
            /*if (!url.contains("http")) {
                // 尝试获取真实的播放地址
                Toast.makeText(this, "不是播放地址，尝试第二套解析方式", Toast.LENGTH_SHORT).show();
                videoPresenter = new VideoPresenter(url, this);
                videoPresenter.tryGetSilisiliPlayUrl();
            } else*/
                play(url);
        } else {
            // yhdm
            if (!url.contains("$"))
                play(url);
            else
                snifferPlayUrl(url);
        }
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
//            VideoUtils.openDefaultWebview(this, webUrl);
            Utils.viewInChrome(this, webUrl);
        } else {
            url = String.format(Api.PARSE_API, url);
            SniffingUtil.get().activity(this).referer(url).callback(this).url(url).start();
        }
    }
}
