package my.project.sakuraproject.main.animeTopic;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AnimeTopicModel implements AnimeTopicContract.Model {
    @Override
    public void getData(String url, int page, boolean isMain, AnimeTopicContract.LoadDataCallback callback) {
        if (page != 1)
            url = url.contains(Sakura.DOMAIN) ? url + page + ".html" : Sakura.DOMAIN + url + page + ".html";
        getHtml(url, isMain, callback);
    }

    private void getHtml(String url, boolean isMain, AnimeTopicContract.LoadDataCallback callback) {
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
                        getHtml(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), isMain, callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        getHtml(url, isMain, callback);
                    else {
                        if (isMain)
                            callback.pageCount(YhdmJsoupUtils.getPageCount(source));
                        List<AnimeListBean> animeListBeans = YhdmJsoupUtils.getAnimeTopicList(source);
                        if (animeListBeans.size() > 0)
                            callback.success(isMain, animeListBeans);
                        else
                            callback.error(isMain, Utils.getString(R.string.error_msg));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(isMain, e.getMessage());
                }
            }
        });
    }
}
