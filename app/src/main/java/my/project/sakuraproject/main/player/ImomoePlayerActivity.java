package my.project.sakuraproject.main.player;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import androidx.core.view.GravityCompat;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.Event;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.main.video.ImomoeVideoContract;
import my.project.sakuraproject.main.video.ImomoeVideoPresenter;
import my.project.sakuraproject.sniffing.SniffingUICallback;
import my.project.sakuraproject.sniffing.SniffingVideo;
import my.project.sakuraproject.sniffing.web.SniffingUtil;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class ImomoePlayerActivity extends BasePlayerActivity implements ImomoeVideoContract.View, SniffingUICallback {

    @Override
    protected boolean isLocalVideo() {
        return false;
    }

    @Override
    protected void setActivityName() {
        Sakura.addDestoryActivity(this, "playerImomoe");;
    }

    @Override
    protected void setBundleData(Bundle bundle) {
        url = bundle.getString("url");
        witchTitle = bundle.getString("title");
        animeTitle = bundle.getString("animeTitle");
        dramaUrl = bundle.getString("dramaUrl");
        imomoeDescList = (List<List<AnimeDescDetailsBean>>) bundle.getSerializable("list");
        clickIndex = bundle.getInt("clickIndex");
        imomoeVideoUrls = (List<List<ImomoeVideoUrlBean>>) bundle.getSerializable("playList");
        nowSource = bundle.getInt("nowSource");
        animeId = bundle.getString("animeId");
    }

    @Override
    protected void initCustomData() {
        if (imomoeDescList.size() > 1) {
            popupMenu = new PopupMenu(this, spinner);
            for (int i=1; i<imomoeDescList.size()+1; i++) {
                popupMenu.getMenu().add(android.view.Menu.NONE, i, i, "播放源 " + i);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                dramaAdapter.setNewData(imomoeDescList.get(item.getItemId()-1));
                nowSource = item.getItemId()-1;
                spinner.setText("播放源 " + item.getItemId());
                initPlayerPreNextTag();
                return true;
            });
            spinner.setText("播放源 " + (nowSource+1));
            spinner.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void setPreNextData() {
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, imomoeDescList.get(nowSource).get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != imomoeDescList.get(nowSource).size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, imomoeDescList.get(nowSource).get(clickIndex+1).getTitle()) : "");
    }

    @Override
    protected void snifferVideo() {
        alertDialog = Utils.getProDialog(this, R.string.should_be_used_web);
        if (url.contains("http")) {
            webUrl = url;
            SniffingUtil.get().activity(this).referer(webUrl).callback(this).url(webUrl).start();
        } else {
            try {
                webUrl = String.format(Api.IMOMOE_PARSE_API,
                        imomoeVideoUrls.get(nowSource).get(clickIndex).getParam(),
                        url,
                        URLEncoder.encode(BaseModel.getDomain(true) +  imomoeDescList.get(nowSource).get(clickIndex).getUrl(),
                                "GB2312"));
                SniffingUtil.get().activity(this).referer(webUrl).callback(this).url(webUrl).start();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void playVideo() {
        checkPlayUrl();
    }

    private void checkPlayUrl() {
        if (url.contains("http"))
            play(url);
        else {
            alertDialog = Utils.getProDialog(this, R.string.should_be_sniffer);
            try {
                webUrl = String.format(Api.IMOMOE_PARSE_API,
                        imomoeVideoUrls.get(nowSource).get(clickIndex).getParam(),
                        url,
                        URLEncoder.encode(Sakura.DOMAIN +  imomoeDescList.get(nowSource).get(clickIndex).getUrl(),
                                "GB2312"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            imomoeVideoPresenter = new ImomoeVideoPresenter(webUrl, this);
            imomoeVideoPresenter.loadData(true);
        }
    }

    @Override
    protected void setAdapter() {
        dramaAdapter = new DramaAdapter(this, imomoeDescList.get(nowSource));
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
    }

    @Override
    protected AnimeDescDetailsBean setAnimeDescDetailsBean(int position) {
        EventBus.getDefault().post(new Event(true, nowSource, clickIndex));
        url = imomoeVideoUrls.get(nowSource).get(clickIndex).getVidOrUrl();
        String fid = DatabaseUtil.getAnimeID(animeTitle, 1);
        DatabaseUtil.addIndex(
                fid,
                Sakura.DOMAIN + imomoeDescList.get(nowSource).get(clickIndex).getUrl(),
                nowSource,
                imomoeDescList.get(nowSource).get(clickIndex).getTitle());
        return imomoeDescList.get(nowSource).get(position);
    }

    @Override
    protected void changeVideo(String title) {
        checkPlayUrl();
    }

    @Override
    public void cancelDialog() {Utils.cancelDialog(alertDialog);}

    @Override
    public void getVideoSuccess(String playUrl) {
        runOnUiThread(() -> {
            cancelDialog();
            play(playUrl);
        });
    }

    @Override
    public void getVideoError() {
        runOnUiThread(() -> {
            cancelDialog();
            snifferVideo();
        });
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
}
