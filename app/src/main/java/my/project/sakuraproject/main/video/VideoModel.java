package my.project.sakuraproject.main.video;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoModel implements VideoContract.Model {
    @Override
    public void getData(String title, String url, VideoContract.LoadDataCallback callback) {
        getHtml(title, url, callback);
    }

    private void getHtml(String title, String url, VideoContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = response.body().string();
                if (YhdmJsoupUtils.hasRedirected(source))
                    getHtml(title, Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                else if (YhdmJsoupUtils.hasRefresh(source))
                    getHtml(title, url, callback);
                else {
                    String fid = DatabaseUtil.getAnimeID(title);
                    DatabaseUtil.addIndex(fid, url);
                    String dataBaseDrama = DatabaseUtil.queryAllIndex(fid);
                    callback.successDrama(YhdmJsoupUtils.getAllDrama(source, dataBaseDrama));
                    List<String> urls = YhdmJsoupUtils.getVideoUrlList(source);
                    if (urls.size() > 0)
                        callback.success(urls);
                    else
                        callback.empty();
                }
            }
        });
    }
}
