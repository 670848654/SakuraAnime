package my.project.sakuraproject.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.HomeAdapter;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeDramasBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.TagBean;

/**
 * http://www.yhdm.io/ 站点解析类
 */
public class YhdmJsoupUtils {

    /** 星期数组 **/
    private static final String[] TABS = Utils.getArray(R.array.week_array);

    /**
     * 是否包含定时跳转
     * @param source
     * @return
     */
    public static boolean hasRefresh(String source) {
        Document document = Jsoup.parse(source);
        Element meta = document.select("meta[http-equiv=refresh]").first();
        return meta != null;
    }

    /**
     * 是否包含重定向
     * @param source
     * @return
     */
    public static boolean hasRedirected(String source) {
        Document document = Jsoup.parse(source);
        if (document.html().contains("You have verified successfully"))
            return true;
        else
            return false;
    }

    /**
     * 获取重定向地址
     * @param source
     * @return
     */
    public static String getRedirectedStr(String source) {
        Document document = Jsoup.parse(source);
        return document.select("a").attr("href");
    }

    /**
     * 获取总页数
     * @param source
     * @return
     */
    public static int getPageCount(String source) {
        Document document = Jsoup.parse(source);
        Elements pages = document.select("div.pages");
        return pages.size() > 0 ? Integer.parseInt(document.getElementById("lastn").text()) : 0;
    }

    /**************************************  获取首页相关信息解析方法开始  **************************************/
    public static List<HomeBean> getHomeAllData(String source) {
        Document document = Jsoup.parse(source);
        List<HomeBean> homeBeanList = new ArrayList<>();
        // banner 数据
        Elements bannerEle = document.select("div.hero-wrap > ul.heros > li");
        List<HomeBean.HomeItemBean> bannerItems = new ArrayList<>();
        HomeBean bannerBean = new HomeBean();
        if (Utils.isPad()) {
            // 平板不显示banner轮播
            bannerBean.setTitle("动漫推荐");
            bannerBean.setMoreUrl("");
            bannerBean.setDataType(HomeAdapter.TYPE_LEVEL_2);
        } else
            bannerBean.setDataType(HomeAdapter.TYPE_LEVEL_1);
        for (Element element : bannerEle) {
            HomeBean.HomeItemBean itemBean = new HomeBean.HomeItemBean();
            itemBean.setTitle(element.select("a").attr("title"));
            itemBean.setUrl(element.select("a").attr("href"));
            itemBean.setImg(element.select("img").attr("src"));
            itemBean.setEpisodes(element.getElementsByTag("em").text());
            bannerItems.add(itemBean);
        }
        bannerBean.setData(bannerItems);
        homeBeanList.add(bannerBean);
        Elements titles = document.select("div.firs > div.dtit");
        Log.e("titles", titles.size() + "");
        Elements data = document.select("div.firs > div.img");

        for (int i=0,size=titles.size(); i<size; i++) {
            HomeBean homeBean = new HomeBean();
            homeBean.setDataType(HomeAdapter.TYPE_LEVEL_2);
            String title = titles.get(i).select("h2 > a").text();
            String moreUrl = titles.get(i).select("h2 > a").attr("href");
            homeBean.setTitle(title);
            homeBean.setMoreUrl(moreUrl);
            List<HomeBean.HomeItemBean> homeItemBeanList = new ArrayList<>();
            Elements animes = data.get(i).select("ul > li");
            for (Element anime : animes) {
                Elements animeInfo = anime.select("a");
                HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
                String animeTitle = animeInfo.get(1).text();
                String url = animeInfo.get(1).attr("href");
                String img = animeInfo.get(0).select("img").attr("src");
                String episodes = animeInfo.size() == 3 ? animeInfo.get(2).text() : "";
                homeItemBean.setTitle(animeTitle);
                homeItemBean.setUrl(url);
                homeItemBean.setImg(img);
                homeItemBean.setEpisodes(episodes);
                homeItemBeanList.add(homeItemBean);
            }
            homeBean.setData(homeItemBeanList);
            homeBeanList.add(homeBean);
        }
        return homeBeanList;
    }

    /**
     * 获取首页最近更新信息
     * @param source
     * @return
     */
    public static List<AnimeUpdateInfoBean> getHomeUpdateInfo(String source) {
        List<AnimeUpdateInfoBean> animeUpdateInfoBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements data = document.select("div.firs > div.img");
        Elements animes = data.get(0).select("ul > li");
        for (Element anime : animes) {
            Elements animeInfo = anime.select("a");
            AnimeUpdateInfoBean animeUpdateInfoBean = new AnimeUpdateInfoBean();
            animeUpdateInfoBean.setSource(0);
            for (Element e : animeInfo) {
                boolean hasUrl = false;
                if (e.attr("href").contains("/show/")) {
                    animeUpdateInfoBean.setTitle(e.text());
                } else if (e.attr("href").contains("/v/")) {
                    hasUrl = true;
                    animeUpdateInfoBean.setPlayNumber(e.attr("href"));
                }
                if (hasUrl)
                    animeUpdateInfoBeans.add(animeUpdateInfoBean);
            }
        }
        return animeUpdateInfoBeans;
    }

    /**************************************  新番时间表解析方法开始  **************************************/
    /**
     * 首页新番时间表解析方法
     * @param source
     * @throws JSONException
     */
    public static LinkedHashMap getHomeData(String source) throws JSONException {
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.tlist > ul");
        LinkedHashMap homeMap = new LinkedHashMap();
        JSONObject weekObj = new JSONObject();
        if (elements.size() > 0) {
            for (int i=0,size=TABS.length; i<size; i++) {
                weekObj.put(TABS[i], setWeekJsonArray(elements.get(i).select("li")));
            }
            Log.e("week", weekObj.toString());
            homeMap.put("success", weekObj.length() > 0 ? true : false);
            homeMap.put("week", weekObj);
        } else
            homeMap.put("success", false);
        return homeMap;
    }

    /**
     * 新番时间表JSON封装
     *
     * @param els
     * @throws JSONException
     */
    public static JSONArray setWeekJsonArray(Elements els) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0, size = els.size(); i < size; i++) {
                JSONObject object = new JSONObject();
                if (els.get(i).select("a").size() > 1) {
                    object.put("title", els.get(i).select("a").get(1).text());
                    object.put("url", els.get(i).select("a").get(1).attr("href"));
                    object.put("drama", els.get(i).select("a").get(0).text());
                    object.put("dramaUrl", els.get(i).select("a").get(0).attr("href"));
                } else {
                    object.put("title", els.get(i).select("a").get(0).text());
                    object.put("url", els.get(i).select("a").get(0).attr("href"));
                }
                jsonArray.put(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    /**
     * 获取今日番剧更新
     * @param source
     * @return
     */
    /*
    public static List<AnimeUpdateInfoBean> getUpdateInfoList(String source) {
        List<AnimeUpdateInfoBean> animeUpdateInfoBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.tlist > ul > li");
        for (int i=0,size=elements.size(); i<size; i++) {
            Elements aList = elements.get(i).select("a");
            if (aList.size() > 1) {
                AnimeUpdateInfoBean animeUpdateInfoBean = new AnimeUpdateInfoBean();
                animeUpdateInfoBean.setSource(0);
                animeUpdateInfoBean.setTitle(elements.get(i).select("a").get(1).text());
                animeUpdateInfoBean.setPlayNumber(elements.get(i).select("a").get(0).attr("href"));
                animeUpdateInfoBeans.add(animeUpdateInfoBean);
                Log.e("YHDM", animeUpdateInfoBean.getTitle() + " > " + animeUpdateInfoBean.getPlayNumber());
            }
        }
        return animeUpdateInfoBeans;
    }
    */
    /**************************************  新番时间表解析方法结束  **************************************/

    /**************************************  番剧列表&&电影列表&&搜索列表解析方法开始  **************************************/
    /**
     *  获取番剧列表集合
     * @param source
     * @param isMovie 是否是动漫电影界面
     * @return
     */
    public static List<AnimeListBean> getAnimeList(String source, boolean isMovie) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements;
        if (isMovie) {
            elements = document.select("div.imgs > ul > li");
            if (elements.size() > 0) {
                for (int i = 0, size = elements.size(); i < size; i++) {
                    AnimeListBean bean = new AnimeListBean();
                    bean.setTitle(elements.get(i).select("p > a").text());
                    bean.setUrl(elements.get(i).select("p > a").attr("href"));
                    bean.setImg(elements.get(i).select("img").attr("src"));
                    animeListBeans.add(bean);
                }
            }
        } else {
            elements = document.select("div.lpic > ul > li");
            if (elements.size() > 0) {
                for (int i=0,size=elements.size(); i < size; i++) {
                    AnimeListBean bean = new AnimeListBean();
                    bean.setTitle(elements.get(i).select("h2").text());
                    bean.setUrl(elements.get(i).select("h2 > a").attr("href"));
                    bean.setImg(elements.get(i).select("img").attr("src"));
                    bean.setDesc(elements.get(i).select("p").text());
                    animeListBeans.add(bean);
                }
            }
        }
         return animeListBeans;
    }
    /**************************************  番剧列表&&电影列表&&搜索列表解析方法结束  **************************************/

    /**************************************  动漫专题解析方法开始  **************************************/
    /**
     * 获取动漫专题列表
     * @param source
     * @return
     */
    public static List<AnimeListBean> getAnimeTopicList(String source) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.dnews > ul > li");
        if (elements.size() > 0) {
            for (int i = 0, size = elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).select("p").text());
                bean.setUrl(elements.get(i).select("p > a").attr("href"));
                bean.setImg(elements.get(i).select("img").attr("src"));
                animeListBeans.add(bean);
            }
        }
        return animeListBeans;
    }
    /**************************************  动漫专题解析方法结束  **************************************/

    /**************************************  动漫分类解析方法开始  **************************************/
    /**
     * 获取动漫分类列表
     * @param source
     * @return
     */
    public static List<TagBean> getTagList(String source) {
        Document document = Jsoup.parse(source);
        Elements titles = document.select("div.dtit");
        Elements items = document.select("div.link");
        List<TagBean> tagBeans = new ArrayList<>();
        if (titles.size() == items.size()) {
            for (int i=1,tagSize=titles.size(); i<tagSize; i++) {
                TagBean tagBean = new TagBean();
                tagBean.setTitle(titles.get(i).text());
                Elements itemElements = items.get(i).select("a");
                List<TagBean.TagSelectBean> tagSelectBeans = new ArrayList<>();
                for (int j = 0, itemSize = itemElements.size(); j < itemSize; j++) {
                    TagBean.TagSelectBean tagSelectBean = new TagBean.TagSelectBean();
                    tagSelectBean.setTagTitle(tagBean.getTitle() + " - " + itemElements.get(j).text());
                    tagSelectBean.setTitle(itemElements.get(j).text());
                    tagSelectBean.setUrl(itemElements.get(j).attr("href"));
                    tagSelectBeans.add(tagSelectBean);
                }
                tagBean.setTagSelectBeans(tagSelectBeans);
                tagBeans.add(tagBean);
            }
        }
        return tagBeans;
    }
    /**************************************  动漫分类解析方法结束  **************************************/

    /**************************************  动漫详情解析方法开始  **************************************/
    /**
     * 获取番剧详情信息
     * @param source
     * @param url
     * @return
     */
    public static AnimeListBean getAinmeInfo(String source, String url) {
        AnimeListBean animeListBean = new AnimeListBean();
        Document document = Jsoup.parse(source);
        animeListBean.setTitle(document.select("h1").text());
        animeListBean.setDesc(document.select("div.info").text());
        animeListBean.setScore(document.select("div.score > em").text());
        if (document.select("div.sinfo > p").size() > 1)
            animeListBean.setUpdateTime(document.select("div.sinfo > p").get(1).text().isEmpty() ? Utils.getString(R.string.no_update) : document.select("div.sinfo > p").get(1).text());
        else
            animeListBean.setUpdateTime(document.select("div.sinfo > p").get(0).text().isEmpty() ? Utils.getString(R.string.no_update) : document.select("div.sinfo > p").get(0).text());
        //番剧图片
        animeListBean.setImg(document.select("div.thumb > img").attr("src"));
        //番剧地址
        animeListBean.setUrl(url);
        Elements tagElements = new Elements();
        tagElements.addAll(document.select("div.sinfo > span").get(0).select("a"));
        tagElements.addAll(document.select("div.sinfo > span").get(1).select("a"));
        tagElements.addAll(document.select("div.sinfo > span").get(2).select("a"));
        tagElements.addAll(document.select("div.sinfo > span").get(4).select("a"));
        List<String> tagTitles = new ArrayList<>();
        List<String> tagUrls = new ArrayList<>();
        for (int i=0,size=tagElements.size(); i<size; i++) {
            tagTitles.add(tagElements.get(i).text().toUpperCase());
            tagUrls.add(tagElements.get(i).attr("href"));
        }
        animeListBean.setTagTitles(tagTitles);
        animeListBean.setTagUrls(tagUrls);
        return animeListBean;
    }

    /**
     * 获取番剧播放列表、多季列表、推荐列表
     * @param source
     * @param dramaStr
     * @return 如果没有播放列表则返回null
     */
    public static AnimeDescListBean getAnimeDescList(String source, String dramaStr) {
        AnimeDescListBean animeDescListBean = new AnimeDescListBean();
        Document document = Jsoup.parse(source);
        List<AnimeDramasBean> animeDramasBeans = new ArrayList<>();
        AnimeDramasBean animeDramasBean = new AnimeDramasBean();
        animeDramasBean.setListTitle("默认播放列表");
        Elements dramaElements = document.select("div.movurl > ul > li"); //剧集列表
        if (dramaElements.size() > 0) {
            /** 封装剧集 **/
            List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
            boolean select;
            for (int i = 0, size = dramaElements.size(); i < size; i++) {
                String name = dramaElements.get(i).select("a").text();
                String watchUrl = dramaElements.get(i).select("a").attr("href");
                if (dramaStr.contains(watchUrl)) select = true;
                else select = false;
                animeDescDramasBeans.add(new AnimeDescDetailsBean(i+1, name, watchUrl, select));
            }
            animeDramasBean.setAnimeDescDetailsBeanList(animeDescDramasBeans);
            animeDramasBeans.add(animeDramasBean);
            animeDescListBean.setAnimeDramasBeans(animeDramasBeans);
            /** 封装多季 **/
            Elements multiElements = document.select("div.img > ul > li"); //多季
            if (multiElements.size() > 0) {
                List<AnimeDescRecommendBean> animeDescMultiBeans = new ArrayList<>();
                for (int i = 0, size = multiElements.size(); i < size; i++) {
                    String title = multiElements.get(i).select("p.tname > a").text();
                    String img = multiElements.get(i).select("img").attr("src");
                    String url = multiElements.get(i).select("p.tname > a").attr("href");
                    animeDescMultiBeans.add(new AnimeDescRecommendBean(title, img, url));
                }
                animeDescListBean.setAnimeDescMultiBeans(animeDescMultiBeans);
            }
            /** 封装推荐 **/
            Elements recommendElements = document.select("div.pics > ul > li"); //相关推荐
            if (recommendElements.size() > 0) {
                List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
                for (int i = 0, size = recommendElements.size(); i < size; i++) {
                    String title = recommendElements.get(i).select("h2 > a").text();
                    String img = recommendElements.get(i).select("img").attr("src");
                    String url = recommendElements.get(i).select("h2 > a").attr("href");
                    animeDescRecommendBeans.add(new AnimeDescRecommendBean(title, img, url));
                }
                animeDescListBean.setAnimeDescRecommendBeans(animeDescRecommendBeans);
            }
            return animeDescListBean;
        } else
            return null;
    }
    /**************************************  动漫详情解析方法结束  **************************************/

    /**************************************  选集解析方法开始  **************************************/
    /**
     * 获取番剧所有剧集（用于 PlayerActivity 选集）
     * @param source
     * @param dataBaseDrama 用户已观看过的url
     * @return
     */
    public static List<AnimeDescDetailsBean> getAllDrama(String source, String dataBaseDrama) {
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.movurls > ul > li");
        List<AnimeDescDetailsBean> animeDescDetailsBeans = new ArrayList<>();
        try {
            String dramaTitle;
            String dramaUrl;
            for (int i = 0, size = elements.size(); i < size; i++) {
                dramaUrl = elements.get(i).select("a").attr("href");
                dramaTitle = elements.get(i).select("a").text();
                if (dataBaseDrama.contains(dramaUrl)) // 是否已经观看
                    animeDescDetailsBeans.add(new AnimeDescDetailsBean(i+1, dramaTitle, dramaUrl, true));
                else
                    animeDescDetailsBeans.add(new AnimeDescDetailsBean(i+1, dramaTitle, dramaUrl, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return animeDescDetailsBeans;
        }
        return animeDescDetailsBeans;
    }

    public static List<String> getVideoUrlList(String source) {
        List<String> urls = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.playbo > a");
        if (elements.size() > 0) {
            for (int i=0,size=elements.size(); i<size; i++) {
                urls.add(VideoUtils.getVideoUrl(elements.get(i).attr("onClick")));
            }
            return urls;
        }
        else
            return urls;
    }
    /**************************************  选集解析方法结束  **************************************/

    /**************************************  最近更新动漫解析方法开始  **************************************/
    public static List<AnimeUpdateInfoBean> getUpdateInfoList(String source, List<AnimeUpdateInfoBean> animeUpdateInfoBeans) {
        if (animeUpdateInfoBeans == null)
            animeUpdateInfoBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.topli > ul > li");
        for (int i=0,size=elements.size(); i<size; i++) {
            Elements aList = elements.get(i).select("a");
            AnimeUpdateInfoBean animeUpdateInfoBean = new AnimeUpdateInfoBean();
            animeUpdateInfoBean.setSource(0);
            for (Element e : aList) {
                boolean hasUrl = false;
                if (e.attr("href").contains("/show/")) {
                    animeUpdateInfoBean.setTitle(e.text());
                } else if (e.attr("href").contains("/v/")) {
                    hasUrl = true;
                    animeUpdateInfoBean.setPlayNumber(e.attr("href"));
                }
                if (hasUrl)
                    animeUpdateInfoBeans.add(animeUpdateInfoBean);
            }
        }
        return animeUpdateInfoBeans;
    }
    /**************************************  最近更新动漫解析方法结束  **************************************/

    /**************************************  最近更新动漫解析方法开始  **************************************/
    public static List<AnimeUpdateBean> getUpdateInfoList2(String source) {
        List<AnimeUpdateBean> animeUpdateBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.topli > ul > li");
        for (Element li : elements) {
            Elements aList = li.select("a");
            AnimeUpdateBean animeUpdateBean = new AnimeUpdateBean();
            animeUpdateBean.setNumber(li.select("i").text());
            Elements span = li.select("span > a");
            if (span.size() > 0) {
                animeUpdateBean.setRegion(aList.get(0).text());
                animeUpdateBean.setTitle(aList.get(1).text());
                animeUpdateBean.setUrl(aList.get(1).attr("href"));
                animeUpdateBean.setEpisodes(aList.get(2).text());
            } else {
                animeUpdateBean.setRegion("");
                animeUpdateBean.setTitle(aList.get(0).text());
                animeUpdateBean.setUrl(aList.get(0).attr("href"));
                animeUpdateBean.setEpisodes(aList.get(1).text());
            }
            animeUpdateBean.setUpdateTime(li.select("em").text());
            animeUpdateBeans.add(animeUpdateBean);
        }
        return animeUpdateBeans;
    }
    /**************************************  最近更新动漫解析方法结束  **************************************/

    /**************************************  更新图片方法开始  **************************************/
    /**
     * 获取番剧图片
     * @param source
     * @return
     */
    public static String getAinmeImg(String source) {
        Document document = Jsoup.parse(source);
        return document.select("div.thumb > img").attr("src");
    }
    /**************************************  更新图片方法结束  **************************************/
}
