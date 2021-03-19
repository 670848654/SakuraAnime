package my.project.sakuraproject.main.desc;

import java.io.IOException;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DescModel implements DescContract.Model {
    private String fid;
    private String dramaStr;

    @Override
    public void getData(String url, DescContract.LoadDataCallback callback) {
        getHtml(url, callback);
    }

    private void getHtml(String url, DescContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = response.body().string();
                    if (YhdmJsoupUtils.hasRedirected(source))
                        getHtml(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        getHtml(url, callback);
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
}
