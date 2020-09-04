package my.project.sakuraproject.main.video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.VideoUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoModel extends BaseModel implements VideoContract.Model {
    private List<String> videoUrlList = new ArrayList<>();

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
                Document doc = Jsoup.parse(response.body().string());
                if (hasRedirected(doc))
                    getHtml(title, Sakura.DOMAIN + getRedirectedStr(doc), callback);
                else if (hasRefresh(doc)) getHtml(title, url, callback);
                else {
                    String fid = DatabaseUtil.getAnimeID(title);
                    DatabaseUtil.addIndex(fid, url);
                    callback.successDrama(getAllDrama(fid, doc.select("div.movurls > ul > li")));
                    Elements playList = doc.select("div.playbo > a");
                    if (playList.size() > 0) {
                        for (int i = 0, size = playList.size(); i < size; i++) {
                            videoUrlList.add(VideoUtils.getVideoUrl(playList.get(i).attr("onClick")));
                        }
                        callback.success(videoUrlList);
                    } else {
                        callback.empty();
                    }
                }
            }
        });
    }

    private List<AnimeDescDetailsBean> getAllDrama(String fid, Elements dramaList) {
        List<AnimeDescDetailsBean> list = new ArrayList<>();
        try {
            String dataBaseDrama = DatabaseUtil.queryAllIndex(fid);
            String dramaTitle;
            String dramaUrl;
            for (int i = 0, size = dramaList.size(); i < size; i++) {
                dramaUrl = dramaList.get(i).select("a").attr("href");
                dramaTitle = dramaList.get(i).select("a").text();
                if (dataBaseDrama.contains(dramaUrl))
                    list.add(new AnimeDescDetailsBean(dramaTitle, dramaUrl, true));
                else
                    list.add(new AnimeDescDetailsBean(dramaTitle, dramaUrl, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        }
        return list;
    }
}
