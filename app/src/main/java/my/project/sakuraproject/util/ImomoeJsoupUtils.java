package my.project.sakuraproject.util;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeDramasBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.SiliSiliRankBean;
import my.project.sakuraproject.bean.TagBean;

public class ImomoeJsoupUtils {
    private final static Pattern IMG_PATTERN = Pattern.compile("http(.*)");
    /** 星期数组 **/
    private static final String[] TABS = Utils.getArray(R.array.week_array);

    /**
     * 获取搜索列表的页数
     * 2023年6月16日16:27:14 新增
     * @param source
     * @return
     */
    public static int getSearchPageCount(String source) {
        Document document = Jsoup.parse(source);
        Elements pageUl = document.select("ul.list-page > li");
        if (pageUl.size() > 0) {
            String pageCount = pageUl.get(pageUl.size()-2).text().replaceAll("\\.", "");
            return Integer.parseInt(pageCount);
        }
        else
            return 1;
    }

    /**
     * 获取总页数
     * 2023年6月19日14:41:13 修改
     * @param source
     * @return
     */
    public static int getPageCount(String source) {
        Document document = Jsoup.parse(source);
        Elements pageA = document.select("div.page > a");
        if (pageA.size() > 0) {
            String pageCount = pageA.get(pageA.size()-2).text().replaceAll("\\.", "");
            return Integer.parseInt(pageCount);
        } else
            return 1;
    }

    /**************************************  获取首页相关信息解析方法开始  **************************************/
    /**
     * 获取首页展示数据
     * 2023年6月16日 测试源 SILISILI
     * @param source
     * @return
     */
    public static List<HomeBean> getHomeAllData(String source) {
        Document document = Jsoup.parse(source);
        List<HomeBean> homeBeanList = new ArrayList<>();
        HomeBean homeBean;
        // 推荐
        Elements recommendLi = document.select("div.focus").select("div.swiper-slide");
        if (recommendLi.size() == 0)
            return homeBeanList;
        recommendLi.select("div.swiper-slide-votitle > span").remove();
        homeBean = new HomeBean();
        homeBean.setTitle("动漫推荐");
        homeBean.setMoreUrl("");
        List<HomeBean.HomeItemBean> recommendItemBeanList = new ArrayList<>();
        for (Element recommend : recommendLi) {
            HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
            String animeTitle = recommend.select("div.swiper-slide-votitle").text();
            String url = recommend.select("a").attr("href");
            String img = getImg(recommend.select("a").attr("style"));
            String episodes = "";
            for (Element div : recommend.select("div")) {
                if (div.attr("style").contains("ff5c7ca6"))
                {
                    episodes = div.text();
                    break;
                }
            }

            homeItemBean.setTitle(animeTitle);
            homeItemBean.setUrl(url);
            homeItemBean.setImg(img);
            homeItemBean.setEpisodes(episodes);
            recommendItemBeanList.add(homeItemBean);
        }
        homeBean.setData(recommendItemBeanList);
        homeBeanList.add(homeBean);
        // 今日热门
        Elements hotTodayLi = document.select("div.index_slide_r > div.sliderlist > div.sliderli");
        homeBean = new HomeBean();
        homeBean.setTitle("今日热门");
        homeBean.setMoreUrl("");
        List<HomeBean.HomeItemBean> hotTodayItemBeanList = new ArrayList<>();
        for (Element hotToday : hotTodayLi) {
            HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
            String animeTitle = hotToday.select("div.list-body").text();
            String url = hotToday.select("a").attr("href");
            String img = getImg(hotToday.select("i.thumb").attr("style"));
            String episodes = hotToday.select("time.d-inline-block").text();
            homeItemBean.setTitle(animeTitle);
            homeItemBean.setUrl(url);
            homeItemBean.setImg(img);
            homeItemBean.setEpisodes(episodes);
            hotTodayItemBeanList.add(homeItemBean);
        }
        homeBean.setData(hotTodayItemBeanList);
        homeBeanList.add(homeBean);
        // 更新动态
        Elements updateLi = document.select("article.article");
        updateLi.select("span.arc_v2").remove();
        homeBean = new HomeBean();
        homeBean.setTitle("更新动态");
        homeBean.setMoreUrl("");
        List<HomeBean.HomeItemBean> updateItemBeanList = new ArrayList<>();
        for (Element update : updateLi) {
            HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
            String animeTitle = update.select("h2.entry-title").text();
            String url = update.select("h2.entry-title > a").attr("href");
            String img = update.select("img.scrollLoading").attr("data-url");
            String episodes = update.select("div.entry-meta").text();
            homeItemBean.setTitle(animeTitle);
            homeItemBean.setUrl(url);
            homeItemBean.setImg(img);
            homeItemBean.setEpisodes(episodes);
            updateItemBeanList.add(homeItemBean);
        }
        homeBean.setData(updateItemBeanList);
        homeBeanList.add(homeBean);
        return homeBeanList;
    }

    /**************************************  新番时间表解析方法开始  **************************************/

    public static LinkedHashMap getHomeData(String source) throws JSONException {
        LinkedHashMap homeMap = new LinkedHashMap();
        JSONObject weekObj = new JSONObject();
        Document document = Jsoup.parse(source);
        Elements weekElements = document.select("div.week_item").select("ul.tab-content");
        Element sunday = weekElements.get(0);
        weekElements.remove(0);
        weekElements.add(sunday);
        if (weekElements.size() > 0) {
            for (int i=0,size=TABS.length; i<size; i++) {
                weekObj.put(TABS[i], setWeekJsonArray(weekElements.get(i).select("li")));
            }
            Log.e("week", weekObj.toString());
            homeMap.put("success", weekObj.length() > 0 ? true : false);
            homeMap.put("week", weekObj);
        }
        else
            homeMap.put("success", false);
        return homeMap;
    }

    /**
     * 新番时间表JSON封装
     * 2023年6月16日16:15:32 修改
     * @param li
     * @return
     */
    private static JSONArray setWeekJsonArray(Elements li) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0, size = li.size(); i < size; i++) {
                JSONObject object = new JSONObject();
                object.put("title", li.get(i).select("a.item-cover").attr("title"));
                object.put("url", li.get(i).select("a.item-cover").attr("href"));
                object.put("drama", li.get(i).select("p.num").text());
                object.put("dramaUrl", "");
                jsonArray.put(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    /**************************************  新番时间表解析方法结束  **************************************/

    /**************************************  动漫分类解析方法开始  **************************************/
    /**
     * 获取动漫分类列表
     * 2023年6月19日11:45:17 修改
     * @param source
     * @return
     */
    public static List<TagBean> getTagList(String source, String[] url) {
        List<TagBean> tagList = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements uls = document.select("ul.stui-screen__list");
        for (Element ul : uls) {
            String tagTitle = ul.select("li").get(0).text();
            Elements as = ul.select("li > a");
            List<TagBean.TagSelectBean> tagSelectBeans = new ArrayList<>();
            for (Element a : as) {
                String itemUrl = a.attr("href")
                        .replaceAll(url[0], "")
                        .replaceAll(url[1], "")
                        .replaceAll(url[2], "");
                if (!itemUrl.isEmpty()) {
                    TagBean.TagSelectBean tagSelectBean = new TagBean.TagSelectBean();
                    tagSelectBean.setTagTitle(a.text());
                    tagSelectBean.setTitle(a.text());
                    if (!itemUrl.startsWith("/")) itemUrl = "/" + itemUrl;
                    if (itemUrl.endsWith("/")) itemUrl = itemUrl.substring(0, itemUrl.length() -1);
                    tagSelectBean.setUrl(itemUrl);
                    tagSelectBeans.add(tagSelectBean);
                }
            }
            if (tagSelectBeans.size() > 0) {
                TagBean tagBean = new TagBean();
                tagBean.setTitle(tagTitle);
                tagBean.setTagSelectBeans(tagSelectBeans);
                tagList.add(tagBean);
            }
        }
        return tagList;
    }
    /**************************************  动漫分类解析方法结束  **************************************/

    /**************************************  番剧列表&&搜索列表解析方法开始  **************************************/
    /**
     *  获取番剧列表集合(搜索界面)
     *  2023年6月16日16:29:57 修改
     * @param source
     * @return
     */
    public static List<AnimeListBean> getSearchAnimeList(String source) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("article.post-list");
        if (elements.size() > 0) {
            for (int i = 0, size = elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).select("div.search-image").select("a").attr("title"));
                bean.setUrl(elements.get(i).select("div.search-image").select("a").attr("href"));
                bean.setImg(getImg(elements.get(i).select("div.search-image").select("img").attr("srcset")));
                bean.setDesc(elements.get(i).select("div.entry-summary").text());
                animeListBeans.add(bean);
            }
        }
        return animeListBeans;
    }

    /**
     * 获取番剧列表集合(分类界面)
     * 2023年6月19日17:37:54 修改
     * @param source
     * @return
     */
    public static List<AnimeListBean> getAnimeList(String source, boolean isToptic) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        if (isToptic) {
            Elements elements = document.select("div.topic-item").select("a");
            if (elements.size() > 0) {
                for (Element a : elements) {
                    AnimeListBean bean = new AnimeListBean();
                    bean.setTitle(a.select("div.list-body").text());
                    bean.setUrl(a.attr("href"));
                    bean.setImg(getImg(a.select("i").attr("style")));
                    bean.setDesc("");
                    animeListBeans.add(bean);
                }
            }
        } else {
            Elements elements = document.select("article.article");
            if (elements.size() > 0) {
                for (int i = 0, size = elements.size(); i < size; i++) {
                    AnimeListBean bean = new AnimeListBean();
                    Element header = elements.get(i).getElementsByTag("header").get(0);
                    header.select("span").remove();
                    bean.setTitle(header.text());
                    bean.setUrl(elements.get(i).select("div.entry-media > a").attr("href"));
                    bean.setImg(getImg(elements.get(i).select("div.entry-media > a > img").attr("src")));
                    bean.setDesc(elements.get(i).select("div.entry-summary > p").text());
                    animeListBeans.add(bean);
                }
            }
        }
        return animeListBeans;
    }
    /**************************************  番剧列表&&电影列表&&搜索列表解析方法结束  **************************************/

    /**************************************  动漫详情解析方法开始  **************************************/
    /**
     * 获取番剧详情信息
     * 2023年6月16日14:36:28 修改
     * @param source
     * @param url
     * @return
     */
    public static AnimeListBean getAinmeInfo(String source, String url) {
        Log.e("url", url);
        AnimeListBean animeListBean = new AnimeListBean();
        Document document = Jsoup.parse(source);
        animeListBean.setTitle(document.select("h1.entry-title").text());
        //番剧图片
        animeListBean.setImg(getImg(document.select("div.v_sd_l > img").attr("src")));
        //番剧地址
        animeListBean.setUrl(url);
        // 先封装TAG
        Elements tags = document.select("p.data").select("a");
        List<String> tagTitles = new ArrayList<>();
        List<String> tagUrls = new ArrayList<>();
        for (Element tag : tags) {
            tagTitles.add(tag.text().toUpperCase());
            tagUrls.add(tag.attr("href"));
        }
        animeListBean.setTagTitles(tagTitles);
        animeListBean.setTagUrls(tagUrls);
        Elements span = document.select("span.text-muted");
        for (Element s : span) {
            if (s.text().contains("更新")) {
                animeListBean.setUpdateTime(s.parent().text());
                break;
            }
        }
        animeListBean.setScore(document.select("div.v_sd_r").select("span.data-favs-num").text());
        Elements desc = document.select("div.v_cont");
        desc.select("div.v_sd").remove();
        desc.select("span").remove();
        animeListBean.setDesc(desc.text().replaceAll(" ", ""));
        return animeListBean;
    }

    /**
     * 获取番剧播放列表、推荐列表
     * 2022年6月29日20:40:53 修改
     * @param source
     * @param dramaStr
     * @return 如果没有播放列表则返回null
     */
    public static AnimeDescListBean getAnimeDescList(String source, String dramaStr) {
        AnimeDescListBean animeDescListBean = new AnimeDescListBean();
        Document document = Jsoup.parse(source);
        // 获取所有播放列表
        Elements playBox = document.select("div.play-pannel-box");
        if (playBox.size() > 0) {
            List<AnimeDramasBean> animeDramasBeans = new ArrayList<>();
            for (Element element : playBox) {
                AnimeDramasBean animeDramasBean = new AnimeDramasBean();
                String playListTitle = element.select("div.widget-title").text();
                if (playListTitle.toLowerCase(Locale.ROOT).contains("no.x")) playListTitle += " → 需二次解析，无法播放（网站问题？）";
                if (playListTitle.contains("下载")) continue;
                animeDramasBean.setListTitle(playListTitle);
                Elements liList = element.select("ul > li");
                List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
                for (Element drama : liList) {
                    String name = drama.select("a").text();
                    String watchUrl = drama.select("a").attr("href");
//                    Log.e("dramaStr - > " , dramaStr + "- > " + watchUrl);
                    animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
                }
                animeDramasBean.setAnimeDescDetailsBeanList(animeDescDramasBeans);
                animeDramasBeans.add(animeDramasBean);
            }
            animeDescListBean.setAnimeDramasBeans(animeDramasBeans);
            //** 封装推荐 **//
            Elements recommendElements = document.select("div.vod_hl_list").select("a"); //相关推荐
            if (recommendElements.size() > 0) {
                List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
                for (int i = 0, size = recommendElements.size(); i < size; i++) {
                    String title = recommendElements.get(i).select("div.list-body").text();
                    String img = getImg(recommendElements.get(i).select("i.thumb").attr("style"));
                    String url = recommendElements.get(i).attr("href");
                    animeDescRecommendBeans.add(new AnimeDescRecommendBean(title, img, url));
                }
                animeDescListBean.setAnimeDescRecommendBeans(animeDescRecommendBeans);
            }
            return animeDescListBean;
        } else
            return null;
    }
    /**************************************  动漫详情解析方法结束  **************************************/

    /**************************************  视频JS解析方法  **************************************/
    /**
     * 获取播放地址
     * 2023年6月16日15:07:00
     * @param source
     * @return
     */
    public static String getImomoePlayUrl(String source) throws UnsupportedEncodingException {
        Document document = Jsoup.parse(source);
        Elements element = document.select("div.play_hl");
        Matcher matcher = Pattern.compile("\\{.*\\}").matcher(element.html());
        if (matcher.find()) {
            String obj = matcher.group();
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(obj);
            String base64Str = jsonObject.getString("url");
            byte[] decodedBytes=  Base64.decode(base64Str, Base64.DEFAULT);
            String decodedString = new String(decodedBytes);
            String playUrl = URLDecoder.decode(decodedString,"UTF-8");
            Log.e("playUrl", playUrl);
            return playUrl;
        }
        return "";
    }

    /**************************************  选集解析方法开始  **************************************/
    /**
     * deprecated
     * 获取番剧所有剧集(用于播放界面选集)
     * 2022年6月29日21:03:25 修改
     * @param source
     * @param dramaStr 用户已观看过的url
     * @return
     */
    @Deprecated
    public static List<AnimeDescDetailsBean> getAllDrama(String source, String dramaStr) {
        Document document = Jsoup.parse(source);
        List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
        Elements dataElement = document.select("ul.playlist");
        if (dataElement.size() > 0) {
            Elements playing = null;
            for (int i=0, size=dataElement.size(); i<size; i++) {
                Elements playElements = dataElement.get(i).select("a"); //剧集列表
                for (Element dramaList : playElements) {
                    String watchUrl = dramaList.attr("href");
                    if (dramaStr.contains(watchUrl)) {
                        playing = playElements;
                        break;
                    }
                }
            }
            if (playing != null) {
                for (Element element : playing) {
                    String name = element.text();
                    String watchUrl = element.attr("href");
                    animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
                }
            }
            /*Elements playElements = dataElement.select("li").get(0).select("a"); //剧集列表
            for (Element dramaList : playElements) {
                String name = dramaList.text();
                String watchUrl = dramaList.attr("href");
                animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
            }*/
        /*Elements playElements = document.select("div#sort-item-1 > a"); //剧集列表
        List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
        for (Element dramaList : playElements) {
            String name = dramaList.select("span").text();
            String watchUrl = dramaList.attr("href");
            animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
        }*/
            Log.e("size", animeDescDramasBeans.size() + "");
        }
        return animeDescDramasBeans;
    }
    /**************************************  选集解析方法结束  **************************************/


    /**************************************  更新图片方方法开始  **************************************/
    /**
     * 获取番剧图片
     * 2023年6月16日15:48:55 修改
     * @param source
     * @return
     */
    public static String getAinmeImg(String source) {
        Document document = Jsoup.parse(source);
        return getImg(document.select("div.v_sd_l > img").attr("src"));
    }
    /**************************************  更新图片方法结束  **************************************/

    /**
     * 处理图片
     * @param text
     * @return
     */
    private static String getImg(String text) {
        Matcher m = IMG_PATTERN.matcher(text);
        while (m.find())
            return m.group().replaceAll("\\)", "").replaceAll(";", "");
        return text;
    }

    /**
     * 获取动漫专题列表
     * 2023年6月19日11:03:04 新增
     * @param source
     * @return
     */
    public static List<AnimeListBean> getAnimeTopicList(String source) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.search-image > a");
        if (elements.size() > 0) {
            for (int i = 0, size = elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).attr("title"));
                bean.setUrl(elements.get(i).attr("href"));
                bean.setImg(elements.get(i).select("img").attr("src"));
                animeListBeans.add(bean);
            }
        }
        return animeListBeans;
    }

    /**
     * 排行榜
     * 2023年6月19日20:35:37 新增
     * @param source
     * @return
     */
    public static List<SiliSiliRankBean> getRankList(String source) {
        Document document = Jsoup.parse(source);
        List<SiliSiliRankBean> siliSiliRankBeans = new ArrayList<>();
        Elements elements = document.select("div.top-item-box");
        if (elements.size() > 0) {
            for (Element e : elements) {
                String topTitle = e.select("div.widget-title").text();
                Elements items = e.select("div.top-item");
                for (Element item : items) {
                    String subTitle = item.select("h5").text();
                    Elements as = item.select("ul.top-list > li > a");
                    List<SiliSiliRankBean.RankItem> rankItems = new ArrayList<>();
                    for (Element a : as) {
                        SiliSiliRankBean.RankItem rankItem = new SiliSiliRankBean.RankItem();
                        rankItem.setIndex(a.select("span.badge").text());
                        rankItem.setTitle(a.select("span.tit").text());
                        rankItem.setUrl(a.attr("href"));
                        rankItem.setHot(a.select("span.remen").text());
                        a.select("span.remen").remove();
                        rankItem.setScore(a.select("span.score").text());
                        rankItems.add(rankItem);
                    }
                    siliSiliRankBeans.add(new SiliSiliRankBean(topTitle + "-" + subTitle, rankItems));
                }
            }
        }
        return siliSiliRankBeans;
    }
}