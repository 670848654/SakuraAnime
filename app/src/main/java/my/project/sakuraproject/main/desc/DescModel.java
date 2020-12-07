package my.project.sakuraproject.main.desc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DescModel extends BaseModel implements DescContract.Model {
    private String fid;
    private String dramaStr;
    private AnimeDescListBean animeDescListBean = new AnimeDescListBean();
    private AnimeListBean animeListBean = new AnimeListBean();

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
                    Document doc = Jsoup.parse(response.body().string());
                    if (hasRedirected(doc))
                        getHtml(Sakura.DOMAIN + getRedirectedStr(doc), callback);
                    else if (hasRefresh(doc)) getHtml(url, callback);
                    else {
                        String animeTitle = doc.select("h1").text();
                        //是否收藏
                        callback.isFavorite(DatabaseUtil.checkFavorite(animeTitle));
                        //创建番剧索引
                        DatabaseUtil.addAnime(animeTitle);
                        fid = DatabaseUtil.getAnimeID(animeTitle);
                        dramaStr = DatabaseUtil.queryAllIndex(fid);
                        //番剧名称
                        animeListBean.setTitle(animeTitle);
                        //番剧简介
                        animeListBean.setDesc(doc.select("div.info").text());
                        Elements descElements = new Elements();
                        descElements.addAll(doc.select("div.sinfo > span").get(0).select("a"));
                        descElements.addAll(doc.select("div.sinfo > span").get(1).select("a"));
                        descElements.addAll(doc.select("div.sinfo > span").get(2).select("a"));
                        descElements.addAll(doc.select("div.sinfo > span").get(4).select("a"));
                        setTags(descElements);
                        animeListBean.setScore(doc.select("div.score > em").text());
                        if (doc.select("div.sinfo > p").size() > 1)
                            animeListBean.setUpdateTime(doc.select("div.sinfo > p").get(1).text().isEmpty() ? Utils.getString(R.string.no_update) : doc.select("div.sinfo > p").get(1).text());
                         else
                            animeListBean.setUpdateTime(doc.select("div.sinfo > p").get(0).text().isEmpty() ? Utils.getString(R.string.no_update) : doc.select("div.sinfo > p").get(0).text());
                        //番剧图片
                        animeListBean.setImg(doc.select("div.thumb > img").attr("src"));
                        //番剧地址
                        animeListBean.setUrl(url);
                        callback.successDesc(animeListBean);
                        //剧集列表
                        Elements detail = doc.select("div.movurl > ul > li");
                        //多季
                        Elements multi = doc.select("div.img > ul > li");
                        //相关推荐
                        Elements recommend = doc.select("div.pics > ul > li");
                        if (detail.size() > 0) {
                            setPlayData(detail);
                            if (multi.size() > 0) setMulti(multi);
                            if (recommend.size() > 0) setRecommend(recommend);
                            callback.successMain(animeDescListBean);
                        } else {
                            callback.error(Utils.getString(R.string.no_playlist_error));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void setTags(Elements elements) {
        List<String> tagTitles = new ArrayList<>();
        List<String> tagUrls = new ArrayList<>();
        for (int i = 0, size = elements.size(); i < size; i++) {
            tagTitles.add(elements.get(i).text().toUpperCase());
            tagUrls.add(elements.get(i).attr("href"));
        }
        animeListBean.setTagTitles(tagTitles);
        animeListBean.setTagUrls(tagUrls);
    }

    private void setPlayData(Elements els) {
        List<AnimeDescDetailsBean> animeDescDetailsBeans = new ArrayList<>();
        boolean select;
        for (int i = 0, size = els.size(); i < size; i++) {
            String name = els.get(i).select("a").text();
            String watchUrl = els.get(i).select("a").attr("href");
            if (dramaStr.contains(watchUrl)) select = true;
            else select = false;
            animeDescDetailsBeans.add(new AnimeDescDetailsBean(name, watchUrl, select));
        }
        animeDescListBean.setAnimeDescDetailsBeans(animeDescDetailsBeans);
    }

    private void setMulti(Elements els) {
        List<AnimeDescRecommendBean> animeDescMultiBeans = new ArrayList<>();
        for (int i = 0, size = els.size(); i < size; i++) {
            String title = els.get(i).select("p.tname > a").text();
            String img = els.get(i).select("img").attr("src");
            String url = els.get(i).select("p.tname > a").attr("href");
            animeDescMultiBeans.add(new AnimeDescRecommendBean(title, img, url));
        }
        animeDescListBean.setAnimeDescMultiBeans(animeDescMultiBeans);
    }

    private void setRecommend(Elements els) {
        List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
        for (int i = 0, size = els.size(); i < size; i++) {
            String title = els.get(i).select("h2 > a").text();
            String img = els.get(i).select("img").attr("src");
            String url = els.get(i).select("h2 > a").attr("href");
            animeDescRecommendBeans.add(new AnimeDescRecommendBean(title, img, url));
        }
        animeDescListBean.setAnimeDescRecommendBeans(animeDescRecommendBeans);
    }
}
