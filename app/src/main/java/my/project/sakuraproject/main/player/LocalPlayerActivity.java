package my.project.sakuraproject.main.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import androidx.core.view.GravityCompat;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.DownloadDataBean;
import my.project.sakuraproject.services.DLNAService;
import my.project.sakuraproject.util.Utils;

public class LocalPlayerActivity extends BasePlayerActivity {

    @Override
    protected boolean isLocalVideo() {
        return true;
    }

    @Override
    protected void setActivityName() {
        Sakura.addDestoryActivity(this, "player");;
    }

    @Override
    protected void setBundleData(Bundle bundle) {
        playPath = bundle.getString("playPath");
        animeTitle = bundle.getString("animeTitle");
        dramaTitle = bundle.getString("dramaTitle");
        downloadDataBeans = (List<DownloadDataBean>) bundle.getSerializable("downloadDataBeans");
    }

    @Override
    protected void initCustomData() {
        startService(new Intent(this, DLNAService.class));
        otherView.setVisibility(View.GONE);
        player.snifferBtn.setVisibility(View.GONE);
    }

    @Override
    protected void setPreNextData() {
        hasPreVideo = clickIndex != 0;
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, dramaList.get(clickIndex-1).getTitle()) : "");
        hasNextVideo = clickIndex != dramaList.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, dramaList.get(clickIndex+1).getTitle()) : "");
    }

    @Override
    protected void snifferVideo() {

    }

    @Override
    protected void playVideo() {
        toPlay(playPath, dramaTitle);
    }

    @Override
    protected void setAdapter() {
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
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
    }

    @Override
    protected AnimeDescDetailsBean setAnimeDescDetailsBean(int position) {
        return dramaAdapter.getItem(position);
    }


    @Override
    protected void changeVideo(String title) {

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
}
