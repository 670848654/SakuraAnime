package my.project.sakuraproject.util;

import android.util.Log;

import com.chad.library.adapter.base.entity.MultiItemEntity;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.bean.TagHeaderBean;

public class ImomoeJsoupUtils {

    /** 星期数组 **/
    private static final String[] TABS = Utils.getArray(R.array.week_array);
    private final static Pattern PAGE_PATTERN = Pattern.compile("\\/(.*)页");
    private final static Pattern PLAY_URL_PATTERN = Pattern.compile("(https?|ftp|file):\\/\\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    /**
     * 获取总页数
     * @param source
     * @return
     */
    public static int getPageCount(String source) {
        Document document = Jsoup.parse(source);
        int pageCount = 0;
        Matcher m = PAGE_PATTERN.matcher(document.select("div.pages > span").text());
        while (m.find()) {
            pageCount = Integer.parseInt(m.group().replaceAll("/", "").replaceAll("页", ""));
            break;
        }
        return pageCount;
    }

    /**************************************  获取首页相关信息解析方法开始  **************************************/
    public static List<HomeBean> getHomeAllData(String source) {
        Document document = Jsoup.parse(source);
        Elements titles = document.select("div.firs > div.tame");
        Log.e("titles", titles.size() + "");
        Elements data = document.select("div.firs > div.imgs");
        List<HomeBean> homeBeanList = new ArrayList<>();
        for (int i=0,size=titles.size(); i<size; i++) {
            HomeBean homeBean = new HomeBean();
            String title = titles.get(i).select("h2").text();
            String moreUrl = titles.get(i).select("span > a").attr("href");
            if (title.contains("日本动漫"))
                moreUrl = "/so.asp?page=1&fl=0&dq=%C8%D5%B1%BE";
            else if (title.contains("国产动漫"))
                moreUrl = "/so.asp?page=1&fl=0&dq=%B4%F3%C2%BD";
            else if (title.contains("美国动漫"))
                moreUrl = "/so.asp?page=1&fl=0&dq=%C3%C0%B9%FA";
            homeBean.setTitle(title);
            homeBean.setMoreUrl(moreUrl);
            List<HomeBean.HomeItemBean> homeItemBeanList = new ArrayList<>();
            Elements animes = data.get(i).select("ul > li");
            for (Element anime : animes) {
                Elements animeInfo = anime.select("a");
                Elements episodesInfo = anime.select("p");
                HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
                String animeTitle = animeInfo.get(1).text();
                String url = animeInfo.get(1).attr("href");
                String img = animeInfo.get(0).select("img").attr("src");
                String episodes = episodesInfo.size() == 2 ? episodesInfo.get(1).text() : "";
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

    /**************************************  新番时间表解析方法开始  **************************************/
    /**
     * 首页新番时间表解析方法
     * @param source
     * @throws JSONException
     */
    public static LinkedHashMap getHomeData(String source) throws JSONException {
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.tists > ul");
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
    private static JSONArray setWeekJsonArray(Elements els) {
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
    public static List<AnimeUpdateInfoBean> getUpdateInfoList(String source) {
        List<AnimeUpdateInfoBean> animeUpdateInfoBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.tists > ul > li");
        for (int i=0,size=elements.size(); i<size; i++) {
            Elements aList = elements.get(i).select("a");
            if (aList.size() > 1) {
                AnimeUpdateInfoBean animeUpdateInfoBean = new AnimeUpdateInfoBean();
                animeUpdateInfoBean.setSource(1);
                animeUpdateInfoBean.setTitle(elements.get(i).select("a").get(1).text());
                animeUpdateInfoBean.setPlayNumber(elements.get(i).select("a").get(0).attr("href"));
                animeUpdateInfoBeans.add(animeUpdateInfoBean);
                Log.e("IMOMOE", animeUpdateInfoBean.getTitle() + " > " + animeUpdateInfoBean.getPlayNumber());
            }
        }
        return animeUpdateInfoBeans;
    }

    /**************************************  新番时间表解析方法结束  **************************************/

    /**************************************  动漫分类解析方法开始  **************************************/
    /**
     * 获取动漫分类列表
     * @param source
     * @return
     */
    public static List<MultiItemEntity> getTagList(String source) {
        List<MultiItemEntity> tagList = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements titles = document.select("div.ters > p > label");
        Elements items = document.select("div.ters > p");
        if (titles.size() == items.size()) {
            for (int i=0,tagSize=titles.size(); i<tagSize; i++) {
                TagHeaderBean tagHeaderBean = new TagHeaderBean(titles.get(i).text().replaceAll("：", ""));
                Elements itemElements = items.get(i).select("a");
                for (int j = 0, itemSize = itemElements.size(); j < itemSize; j++) {
                    if (!itemElements.get(j).text().equals("全部"))
                        tagHeaderBean.addSubItem(
                                new TagBean(
                                        tagHeaderBean.getTitle() + " - " + itemElements.get(j).text(),
                                        itemElements.get(j).text(),
                                        itemElements.get(j).attr("href")
                                )
                        );
                }
                tagList.add(tagHeaderBean);
            }
            return tagList;
        } else
            return tagList;
    }
    /**************************************  动漫分类解析方法结束  **************************************/

    /**************************************  番剧列表&&搜索列表解析方法开始  **************************************/
    /**
     *  获取番剧列表集合
     * @param source
     * @return
     */
    public static List<AnimeListBean> getAnimeList(String source, boolean isMovie) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        if (isMovie) {
            Elements elements = document.select("div#contrainer > div.img > ul > li");
            if (elements.size() > 0) {
                for (int i=0,size=elements.size(); i < size; i++) {
                    AnimeListBean bean = new AnimeListBean();
                    Elements info = elements.get(i).select("a");
                    bean.setTitle(info.get(1).text());
                    bean.setUrl(info.get(1).attr("href"));
                    bean.setImg(info.get(0).select("img").attr("src"));
                    bean.setDesc("");
                    animeListBeans.add(bean);
                }
            }
        } else {
            Elements elements = document.select("div.fire > div.pics > ul > li");
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
        animeListBean.setTitle(document.select("span.names").text());
        animeListBean.setDesc(document.select("div.info").text());
//        animeListBean.setScore(document.select("div.score > em").text());
        animeListBean.setUpdateTime(document.select("div.alex > p").get(1).text().isEmpty() ? Utils.getString(R.string.no_update) : document.select("div.alex > p").get(1).text());
        //番剧图片
        animeListBean.setImg(document.select("div.tpic > img").attr("src"));
        //番剧地址
        animeListBean.setUrl(url);
        Elements tagElements = document.select("div.alex > span > a");
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
     * 获取番剧播放列表、推荐列表
     * @param source
     * @param dramaStr
     * @return 如果没有播放列表则返回null
     */
    public static AnimeDescListBean getAnimeDescList(String source, String dramaStr) {
        AnimeDescListBean animeDescListBean = new AnimeDescListBean();
        Document document = Jsoup.parse(source);
        Elements playElements = document.select("div.movurl"); //剧集列表
        if (playElements.size() > 0) {
            /** 封装剧集 **/
            List<List<AnimeDescDetailsBean>> multipleAnimeDescDetailsBeans = new ArrayList<>();
            boolean select;
            for (Element playList : playElements) {
                Elements dramaElements = playList.select("ul > li");
                List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
                for (Element dramaList : dramaElements) {
                    String name = dramaList.select("a").text();
                    String watchUrl = dramaList.select("a").attr("href");
                    Log.e("dramaStr - > " , dramaStr + "- > " + watchUrl);
                    if (dramaStr.contains(watchUrl)) select = true;
                    else select = false;
                    animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, select));
                }
                multipleAnimeDescDetailsBeans.add(animeDescDramasBeans);
            }
            animeDescListBean.setMultipleAnimeDescDetailsBeans(multipleAnimeDescDetailsBeans);
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

    /**************************************  视频解析方法开始  **************************************/
    public static String getPlayDataJs(String source) {
        Document document = Jsoup.parse(source);
        Elements elements = document.select("script");
        String js = "";
        for (Element srcipt : elements) {
            if (srcipt.attr("src").contains("playdata")) {
                js = srcipt.attr("src");
                break;
            }
        }
        return js;
    }
    /**************************************  视频解析方法结束  **************************************/

    /**************************************  视频JS解析方法  **************************************/
    public static List<List<ImomoeVideoUrlBean>> getImomoePlayUrl(String json) {
        List<List<ImomoeVideoUrlBean>> allList = new ArrayList<>();
        com.alibaba.fastjson.JSONArray jsonArray = com.alibaba.fastjson.JSONArray.parseArray(json);
        for (int i=0,size=jsonArray.size(); i<size; i++) {
            com.alibaba.fastjson.JSONArray sourceArr = com.alibaba.fastjson.JSONArray.parseArray(jsonArray.getJSONArray(i).getString(1));
            List<ImomoeVideoUrlBean> imomoeVideoUrlBeans = new ArrayList<>();
            for (int j=0,size2=sourceArr.size(); j<size2; j++) {
                String str = sourceArr.getString(j);
                String[] strs = str.split("\\$");
                Matcher matcher = Pattern.compile("\\$(.*)\\$").matcher(str);
                if (matcher.find()) {
                    ImomoeVideoUrlBean imomoeVideoUrlBean = new ImomoeVideoUrlBean();
                    String find = matcher.group().replaceAll("\\$", "");
                    if (find.contains("http")) {
                        // 是http
                        imomoeVideoUrlBean.setHttp(true);
                        imomoeVideoUrlBean.setVidOrUrl(find);
                    } else {
                        // 非http
                        imomoeVideoUrlBean.setHttp(false);
                        imomoeVideoUrlBean.setVidOrUrl(find);
                        imomoeVideoUrlBean.setParam(strs[strs.length-1]);
                    }
                    imomoeVideoUrlBeans.add(imomoeVideoUrlBean);
                }
            }
            allList.add(imomoeVideoUrlBeans);
        }
        return allList;
    }

    public static String getImomoeApiPlayUrl(String source) {
        String playUrl = "";
        Document document = Jsoup.parse(source);
        Elements scripts = document.select("script");
        for (Element element : scripts) {
            if (element.html().contains("var video")) {
                Matcher m = PLAY_URL_PATTERN.matcher(element.html());
                if (m.find()) {
                    playUrl = m.group();
                    break;
                }
            }
        }
        return playUrl;
    }

    /**************************************  选集解析方法开始  **************************************/
    /**
     * 获取番剧所有剧集（用于 ImomoePlayerActivity 选集）
     * @param source
     * @param dramaStr 用户已观看过的url
     * @return
     */
    public static List<List<AnimeDescDetailsBean>> getAllDrama(String source, String dramaStr) {
        List<List<AnimeDescDetailsBean>> multipleAnimeDescDetailsBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements playElements = document.select("div.movurls"); //剧集列表
        for (Element playList : playElements) {
            Elements dramaElements = playList.select("ul > li");
            List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
            for (Element dramaList : dramaElements) {
                String name = dramaList.select("a").text();
                String watchUrl = dramaList.select("a").attr("href");
                animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
            }
            multipleAnimeDescDetailsBeans.add(animeDescDramasBeans);
        }
        return multipleAnimeDescDetailsBeans;
    }
    /**************************************  选集解析方法开始  **************************************/

    /**************************************  最近更新动漫解析方法开始  **************************************/
    /*public static List<AnimeUpdateInfoBean> getUpdateInfoList(String source) {
        List<AnimeUpdateInfoBean> animeUpdateInfoBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.topli > ul > li");
        for (int i=0,size=elements.size(); i<size; i++) {
            Elements aList = elements.get(i).select("a");
            AnimeUpdateInfoBean animeUpdateInfoBean = new AnimeUpdateInfoBean();
            animeUpdateInfoBean.setSource(1);
            animeUpdateInfoBean.setTitle(aList.size() > 2 ? aList.get(1).text() : aList.get(0).text());
            String playNumber = aList.size() > 2 ? aList.get(2).text(): aList.get(1).text();
            animeUpdateInfoBean.setPlayNumber(replaceStr(playNumber));
            animeUpdateInfoBeans.add(animeUpdateInfoBean);
        }
        return animeUpdateInfoBeans;
    }

    // 处理奇葩格式
    private static String replaceStr(String content) {
        content = content.split("\\+")[0];
        Matcher matcher = Pattern.compile("[0-9]*").matcher(content);
        if (matcher.find()) {
            content = "第" + matcher.group() + "集";
        }
        return content;
    }*/
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
            animeUpdateBean.setRegion(aList.get(0).text());
            animeUpdateBean.setTitle(aList.get(1).text());
            animeUpdateBean.setUrl(aList.get(1).attr("href"));
            animeUpdateBean.setEpisodes(aList.get(2).text());
            animeUpdateBean.setUpdateTime(li.select("em").text());
            animeUpdateBeans.add(animeUpdateBean);
        }
        return animeUpdateBeans;
    }
    /**************************************  最近更新动漫解析方法结束  **************************************/

    /**************************************  更新图片方方法开始  **************************************/
    /**
     * 获取番剧图片
     * @param source
     * @return
     */
    public static String getAinmeImg(String source) {
        Document document = Jsoup.parse(source);
        return document.select("div.tpic > img").attr("src");
    }
    /**************************************  更新图片方法结束  **************************************/
}
