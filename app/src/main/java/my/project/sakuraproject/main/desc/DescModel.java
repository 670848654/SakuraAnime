package my.project.sakuraproject.main.desc;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.bean.AnimeHeaderBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.config.AnimeType;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DescModel implements DescContract.Model {
    private String fid;
    private List<MultiItemEntity> list;
    private String dramaStr;

    @Override
    public void getData(String url, DescContract.LoadDataCallback callback) {
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    list = new ArrayList<>();
                    Document doc = Jsoup.parse(response.body().string());
                    String animeTitle = doc.select("h1").text();
                    //是否收藏
                    callback.isFavorite(DatabaseUtil.checkFavorite(animeTitle));
                    //创建番剧索引
                    DatabaseUtil.addAnime(animeTitle);
                    fid = DatabaseUtil.getAnimeID(animeTitle);
                    dramaStr = DatabaseUtil.queryAllIndex(fid);
                    AnimeListBean bean = new AnimeListBean();
                    //番剧名称
                    bean.setTitle(animeTitle);
                    //番剧简介
                    bean.setDesc(doc.select("div.info").text());
                    //番剧图片
                    bean.setImg(doc.select("div.thumb > img").attr("src"));
                    //番剧地址
                    bean.setUrl(url);
                    callback.successDesc(bean);
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
                        callback.successMain(list);
                    } else {
                        callback.error(Utils.getString(R.string.no_playlist_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void setPlayData(Elements els) {
        AnimeHeaderBean animeHeaderBean = new AnimeHeaderBean(Utils.getString(R.string.online));
        boolean select;
        for (int i = 0, size = els.size(); i < size; i++) {
            String name = els.get(i).select("a").text();
            String watchUrl = els.get(i).select("a").attr("href");
            if (dramaStr.contains(watchUrl)) select = true;
            else select = false;
            animeHeaderBean.addSubItem(
                    new AnimeDescBean(
                            AnimeType.TYPE_LEVEL_1,
                            select,
                            name,
                            watchUrl,
                            "play")
            );
        }
        list.add(animeHeaderBean);
    }

    private void setMulti(Elements els) {
        AnimeHeaderBean animeHeaderBean = new AnimeHeaderBean(Utils.getString(R.string.multi));
        for (int i = 0, size = els.size(); i < size; i++) {
            animeHeaderBean.addSubItem(
                    new AnimeDescBean(
                            AnimeType.TYPE_LEVEL_3,
                            els.get(i).select("p.tname > a").text(),
                            els.get(i).select("p.tname > a").attr("href"),
                            els.get(i).select("img").attr("src"),
                            "multi"));
        }
        list.add(animeHeaderBean);
    }

    private void setRecommend(Elements els) {
        AnimeHeaderBean animeHeaderBean = new AnimeHeaderBean(Utils.getString(R.string.recommend));
        for (int i = 0, size = els.size(); i < size; i++) {
            animeHeaderBean.addSubItem(
                    new AnimeDescBean(
                            AnimeType.TYPE_LEVEL_2,
                            els.get(i).select("h2 > a").text(),
                            els.get(i).select("h2 > a").attr("href"),
                            els.get(i).select("img").attr("src"),
                            "recommend"));
        }
        list.add(animeHeaderBean);
    }
}
