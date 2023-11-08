package my.project.sakuraproject.main.desc;

import android.util.Log;

import java.io.IOException;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DescModel extends BaseModel implements DescContract.Model {
    private String fid;
    private String dramaStr;

    @Override
    public void getData(String url, DescContract.LoadDataCallback callback) {
        if (url.contains("/voddetail/"))
            parserImomoe(getDomain(true) + url, callback);
        else
            parserYhdm(getDomain(false) + url, callback);
    }

    private void parserYhdm(String url, DescContract.LoadDataCallback callback) {
        callback.log(url);
        Log.e("url", url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getHtmlBody(response, false);
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(url, callback);
                    else {
                        AnimeListBean animeListBean = YhdmJsoupUtils.getAinmeInfo(source, url);
                        String animeTitle = animeListBean.getTitle();
                        //创建番剧索引
                        DatabaseUtil.addAnime(animeTitle, 0);
                        fid = DatabaseUtil.getAnimeID(animeTitle, 0);
                        // 添加历史记录
                        DatabaseUtil.addOrUpdateHistory(fid, animeListBean.getUrl(), animeListBean.getImg());
                        //是否收藏
                        callback.isFavorite(DatabaseUtil.checkFavorite(fid));
                        dramaStr = DatabaseUtil.queryAllIndex(fid, false, 0);
                        callback.successDesc(animeListBean);
                        callback.isImomoe(false);
                        callback.getAnimeId(fid);
                        AnimeDescListBean animeDescListBean = YhdmJsoupUtils.getAnimeDescList(source, dramaStr);
                        if (animeDescListBean != null)
                            callback.successMain(animeDescListBean);
                        else
                            callback.emptyDram(Utils.getString(R.string.no_playlist_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void parserImomoe(String url, DescContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getHtmlBody(response, true);
                    AnimeListBean animeListBean = ImomoeJsoupUtils.getAinmeInfo(source, url);
                    String animeTitle = animeListBean.getTitle();
                    if (animeTitle == null || animeTitle.isEmpty()) {
                        callback.error("地址解析失败，可能该番剧的地址变更导致，请使用搜索！");
                    } else {
                        //创建番剧索引
                        DatabaseUtil.addAnime(animeTitle, 1);
                        fid = DatabaseUtil.getAnimeID(animeTitle, 1);
                        Log.e("fid", fid);
                        // 添加历史记录
                        DatabaseUtil.addOrUpdateHistory(fid, animeListBean.getUrl(), animeListBean.getImg());
                        //是否收藏
                        callback.isFavorite(DatabaseUtil.checkFavorite(fid));
                        dramaStr = DatabaseUtil.queryAllIndex(fid, false, 0);
                        Log.e("dramaStr", dramaStr);
                        callback.successDesc(animeListBean);
                        callback.isImomoe(true);
                        callback.getAnimeId(fid);
                        AnimeDescListBean animeDescListBean = ImomoeJsoupUtils.getAnimeDescList(source, dramaStr);
                        if (animeDescListBean != null)
                            callback.successMain(animeDescListBean);
                        else if (animeDescListBean.getAnimeDramasBeans().size() == 0)
                            callback.emptyDram(Utils.getString(R.string.no_playlist_error));
                        else
                            callback.emptyDram(Utils.getString(R.string.no_playlist_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }
}
