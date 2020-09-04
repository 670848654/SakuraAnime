package my.project.sakuraproject.main.animeTopic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AnimeTopicModel extends BaseModel implements AnimeTopicContract.Model {
    private List<AnimeListBean> list;

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
                    Document doc = Jsoup.parse(response.body().string());
                    if (hasRedirected(doc))
                        getHtml(Sakura.DOMAIN + getRedirectedStr(doc), isMain, callback);
                    else if (hasRefresh(doc)) getHtml(url, isMain, callback);
                    else {
                        Elements animeList = doc.select("div.dnews > ul > li");
                        if (isMain) {
                            Elements pages = doc.select("div.pages");
                            if (pages.size() > 0)
                                callback.pageCount(Integer.parseInt(doc.getElementById("lastn").text()));
                        }
                        if (animeList.size() > 0) {
                            list = new ArrayList<>();
                            for (int i = 0, size = animeList.size(); i < size; i++) {
                                AnimeListBean bean = new AnimeListBean();
                                bean.setTitle(animeList.get(i).select("p").text());
                                bean.setUrl(animeList.get(i).select("p > a").attr("href"));
                                bean.setImg(animeList.get(i).select("img").attr("src"));
                                list.add(bean);
                            }
                            callback.success(isMain, list);
                        } else callback.error(isMain, Utils.getString(R.string.error_msg));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(isMain, e.getMessage());
                }
            }
        });
    }
}
