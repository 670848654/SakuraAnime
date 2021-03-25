package my.project.sakuraproject.main.desc;

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
        if (isImomoe())
            parserImomoe(url, callback);
        else
            parserYhdm(url, callback);
    }

    private void parserYhdm(String url, DescContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(url, callback);
                    else {
                        AnimeListBean animeListBean = YhdmJsoupUtils.getAinmeInfo(source, url);
                        String animeTitle = animeListBean.getTitle();
                        //是否收藏
                        callback.isFavorite(DatabaseUtil.checkFavorite(animeTitle));
                        //创建番剧索引
                        DatabaseUtil.addAnime(animeTitle);
                        fid = DatabaseUtil.getAnimeID(animeTitle);
                        dramaStr = DatabaseUtil.queryAllIndex(fid);
                        callback.successDesc(animeListBean);
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
                    String source = getBody(response);
                    AnimeListBean animeListBean = ImomoeJsoupUtils.getAinmeInfo(source, url);
                    String animeTitle = animeListBean.getTitle();
                    //是否收藏
                    callback.isFavorite(DatabaseUtil.checkFavorite(animeTitle));
                    //创建番剧索引
                    DatabaseUtil.addAnime(animeTitle);
                    fid = DatabaseUtil.getAnimeID(animeTitle);
                    dramaStr = DatabaseUtil.queryAllIndex(fid);
                    callback.successDesc(animeListBean);
                    AnimeDescListBean animeDescListBean = ImomoeJsoupUtils.getAnimeDescList(source, dramaStr);
                    if (animeDescListBean != null)
                        callback.successMain(animeDescListBean);
                    else
                        callback.emptyDram(Utils.getString(R.string.no_playlist_error));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }
}
