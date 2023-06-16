package my.project.sakuraproject.main.video;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoModel extends BaseModel implements VideoContract.Model {
    private final static Pattern PLAY_DATA_PATTERN = Pattern.compile("\\[(.*)\\]");
    private boolean isImomoe;
    @Override
    public void getData(String title, String url, int source, String playNumber, VideoContract.LoadDataCallback callback) {
        isImomoe = url.contains("/vodplay/");
        if (isImomoe)
            parserImomoe(title, BaseModel.getDomain(true) + url, source, playNumber, callback);
        else
            parserYhdm(title, BaseModel.getDomain(false) + url, source, playNumber, callback);
    }

    private void parserYhdm(String title, String url, int playSource, String playNumber, VideoContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, false);
                if (YhdmJsoupUtils.hasRedirected(source))
                    parserYhdm(title, Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), playSource, playNumber, callback);
                else if (YhdmJsoupUtils.hasRefresh(source))
                    parserYhdm(title, url, playSource, playNumber, callback);
                else {
                    /*String fid = DatabaseUtil.getAnimeID(title, 0);
                    DatabaseUtil.addIndex(fid, url, playSource, playNumber);
                    String dataBaseDrama = DatabaseUtil.queryAllIndex(fid);
                    callback.successYhdmDramas(YhdmJsoupUtils.getAllDrama(source, dataBaseDrama));*/
                    List<String> urls = YhdmJsoupUtils.getVideoUrlList(source);
                    if (urls.size() > 0)
                        callback.successYhdmVideoUrls(urls);
                    else
                        callback.empty();
                }
            }
        });
    }

    private void parserImomoe(String title, String url, int playSource, String playNumber, VideoContract.LoadDataCallback callback) {
        callback.log(url);
        Log.e("url", url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, true);
                /*
                String fid = DatabaseUtil.getAnimeID(title, 1);
                DatabaseUtil.addIndex(fid, url, playSource, playNumber);
                String dataBaseDrama = DatabaseUtil.queryAllIndex(fid);
                List<AnimeDescDetailsBean> bean = ImomoeJsoupUtils.getAllDrama(source, dataBaseDrama);
                if (bean.size() == 0) {
                    callback.error();
                } else {
                    callback.successImomoeDramas(bean);
                    String playUrl = ImomoeJsoupUtils.getImomoePlayUrl(source);
                    if (!playUrl.isEmpty())
                        callback.successImomoeVideoUrl(playUrl);
                    else
                        callback.empty();
                }*/
                String playUrl = ImomoeJsoupUtils.getImomoePlayUrl(source);
                if (!playUrl.isEmpty())
                    callback.successImomoeVideoUrl(playUrl);
                else
                    callback.empty();
            }
        });
    }
}
