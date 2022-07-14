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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.AnimeDescListBean;
import my.project.sakuraproject.bean.AnimeDescRecommendBean;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.HomeBean;
import my.project.sakuraproject.bean.MaliTagBean;

public class ImomoeJsoupUtils {

    /** 星期数组 **/
    private static final String[] TABS = Utils.getArray(R.array.week_array);
    private final static Pattern PAGE_PATTERN = Pattern.compile("\\/(.*)页");
//    private final static Pattern PAGE_PATTERN = Pattern.compile("-[0-9].*?-");
    private final static Pattern PLAY_URL_PATTERN = Pattern.compile("(https?|ftp|file):\\/\\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    /**
     * 获取总页数
     * 2022年6月29日19:01:34 修改
     * @param source
     * @return
     */
    public static int getPageCount(String source) {
        Document document = Jsoup.parse(source);
        /*if (document.getElementById("page") == null)
            return 0;
        int pageCount = 0;
        Matcher m = PAGE_PATTERN.matcher(document.getElementById("page").select("a").last().attr("href"));
        while (m.find()) {
            pageCount = Integer.parseInt(m.group().replaceAll("-", ""));
            break;
        }*/
        if (document.select("div.pagebox.clearfix > a").size() == 0)
            return 0;
        int pageCount = 0;
        Matcher m = PAGE_PATTERN.matcher(document.select("div.pagebox.clearfix > a.pagelink_b").get(0).text().replaceAll("&nbsp;", ""));
        while (m.find()) {
            pageCount = Integer.parseInt(m.group().replaceAll("/", "").replaceAll("页", ""));
            break;
        }
        return pageCount;
    }

    /**************************************  获取首页相关信息解析方法开始  **************************************/
    /**
     * 获取首页展示数据
     * 2022年5月25日 修改
     * @param source
     * @return
     */
    public static List<HomeBean> getHomeAllData(String source) {
        Document document = Jsoup.parse(source);
        List<HomeBean> homeBeanList = new ArrayList<>();
        /*Elements elements = document.select("div.module-lines-list");
        homeBeanList.add(getHomeListData(elements, 0, "小编推荐"));
        homeBeanList.add(getHomeListData(elements, 2, "日本动漫"));
        homeBeanList.add(getHomeListData(elements, 4, "国产动漫"));
        homeBeanList.add(getHomeListData(elements, 5, "欧美动漫"));
        homeBeanList.add(getHomeListData(elements, 6, "OVA剧场版"));*/
        // 热门推荐
        Elements hotsLi = document.getElementById("movie_content_div").select("li");
        homeBeanList.add(getHomeListData(
                true,
                document.getElementById("movie_content_div").parent().select("h3 > span").text(),
                "",
                hotsLi));
        // 日本动漫
        Elements japanLi = document.getElementById("mytab3").select("li");
        homeBeanList.add(getHomeListData(
                false,
                document.getElementById("mytab3").parent().select("h3 > span").text(),
                document.getElementById("mytab3").parent().select("a.aMore").attr("href"),
                japanLi));
        // 国产动漫
        Elements chinaLi = document.getElementById("mytab2").select("li");
        homeBeanList.add(getHomeListData(
                false,
                document.getElementById("mytab2").parent().select("h3 > span").text(),
                document.getElementById("mytab2").parent().select("a.aMore").attr("href"),
                chinaLi));
        // 欧美动漫
        Elements europeLi = document.getElementById("mytab").select("li");
        homeBeanList.add(getHomeListData(
                false,
                document.getElementById("mytab").parent().select("h3 > span").text(),
                document.getElementById("mytab").parent().select("a.aMore").attr("href"),
                europeLi));
        // OVA动漫
        Elements ovaLi = document.getElementById("mytab4").select("li");
        homeBeanList.add(getHomeListData(
                false,
                document.getElementById("mytab4").parent().select("h3 > span").text(),
                document.getElementById("mytab4").parent().select("a.aMore").attr("href"),
                ovaLi));
        return homeBeanList;
    }

    /**
     * 首页 > 动漫数据
     * 2022年6月29日19:25:45 修改
     * @param elements
     * @param index
     * @param title
     * @return
     */
    /*private static HomeBean getHomeListData(Elements elements, int index, String title) {
        // 小编推荐
        Element items = elements.get(index);
        Elements list = items.select("div.module-item");
        HomeBean homeBean = new HomeBean();
        homeBean.setTitle(title);
        homeBean.setMoreUrl("");
        List<HomeBean.HomeItemBean> homeItemBeanList = new ArrayList<>();
        for (Element element : list) {
            HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
            String animeTitle = element.select("div.module-item-cover > div.module-item-pic > a").attr("title");
            String url = element.select("div.module-item-cover > div.module-item-pic > a").attr("href");
            String img = element.select("div.module-item-cover > div.module-item-pic > img").attr("data-src");
            String episodes = element.select("div.module-item-text").text();
            homeItemBean.setTitle(animeTitle);
            homeItemBean.setUrl(url);
            homeItemBean.setImg(img);
            homeItemBean.setEpisodes(episodes);
            homeItemBeanList.add(homeItemBean);
        }
        homeBean.setData(homeItemBeanList);
        return homeBean;
    }*/

    /**
     * 首页 > 动漫数据
     * 2022年7月14日09:30:17 修改
     * @param isHot
     * @param title
     * @param moreUrl
     * @param hotsLi
     * @return
     */
    private static HomeBean getHomeListData(boolean isHot, String title, String moreUrl, Elements hotsLi) {
        HomeBean homeBean = new HomeBean();
        homeBean.setTitle(title);
        homeBean.setMoreUrl("");
        List<HomeBean.HomeItemBean> homeItemBeanList = new ArrayList<>();
        for (Element hot : hotsLi) {
            HomeBean.HomeItemBean homeItemBean = new HomeBean.HomeItemBean();
            String animeTitle = hot.select("figcaption.block-title > b").text();
            String url = hot.select("a").attr("href");
            String img = hot.select("img").attr(isHot ? "src" : "data-echo");
            String episodes = hot.select(isHot ? "p.otherinfo" : "p.block-clearfix").text();
            homeItemBean.setTitle(animeTitle);
            homeItemBean.setUrl(url);
            homeItemBean.setImg(img);
            homeItemBean.setEpisodes(episodes);
            homeItemBeanList.add(homeItemBean);
        }
        homeBean.setData(homeItemBeanList);
        return homeBean;
    }

    /**************************************  新番时间表解析方法开始  **************************************/
    /**
     * 新番时间表解析方法
     * 2022年6月29日19:16:20 修改
     * @param source
     * @return
     * @throws JSONException
     */
    /*public static LinkedHashMap getHomeData(String source) throws JSONException {
        LinkedHashMap homeMap = new LinkedHashMap();
        JSONObject weekObj = new JSONObject();
        Document document = Jsoup.parse(source);
        Elements japanWeekItem = document.select("div.week2b > div.weekbb");
        Elements chinaWeekItem = document.select("div.week1b > div.weekbb");
        if (japanWeekItem.size() > 0 && chinaWeekItem.size() > 0) {
            for (int i=0,size=TABS.length; i<size; i++) {
                weekObj.put(TABS[i], setWeekJsonArray(
                        japanWeekItem.get(i).select("div.module-item"),
                        chinaWeekItem.get(i).select("div.module-item")));
            }
            Log.e("week", weekObj.toString());
            homeMap.put("success", weekObj.length() > 0 ? true : false);
            homeMap.put("week", weekObj);
        }
        else
            homeMap.put("success", false);
        return homeMap;
    }*/

    public static LinkedHashMap getHomeData(String japanHtml, String chinaHtml) throws JSONException {
        LinkedHashMap homeMap = new LinkedHashMap();
        JSONObject weekObj = new JSONObject();
        Document japanDocument = Jsoup.parse(japanHtml);
        Document chinaDocument = Jsoup.parse(chinaHtml);
        Elements japanWeekItem = japanDocument.getElementById("mytabweek").select("ul.tab-content");
        Log.e("japanWeekItem", japanWeekItem.html());
        Elements chinaWeekItem = chinaDocument.getElementById("mytabweek1").select("ul.tab-content");
        if (japanWeekItem.size() > 0 && chinaWeekItem.size() > 0) {
            for (int i=0,size=TABS.length; i<size; i++) {
                weekObj.put(TABS[i], setWeekJsonArray(
                        japanWeekItem.get(i).select("li"),
                        chinaWeekItem.get(i).select("li"))
                );
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
     * 2022年5月25日 修改
     * @param japanLi
     * @param chinaLi
     * @return
     */
    private static JSONArray setWeekJsonArray(Elements japanLi, Elements chinaLi) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0, size = japanLi.size(); i < size; i++) {
                JSONObject object = new JSONObject();
                /*object.put("title", japanLi.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("title"));
                object.put("url", japanLi.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("href"));
                object.put("drama", japanLi.get(i).select("div.module-item-text").text());*/
                object.put("title", japanLi.get(i).select("a.item-cover").attr("title"));
                object.put("url", japanLi.get(i).select("a.item-cover").attr("href"));
                object.put("drama", japanLi.get(i).select("p.num").text());
                object.put("dramaUrl", "");
                jsonArray.put(object);
            }
            for (int i = 0, size = chinaLi.size(); i < size; i++) {
                JSONObject object = new JSONObject();
               /* object.put("title", chinaLi.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("title"));
                object.put("url", chinaLi.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("href"));
                object.put("drama", chinaLi.get(i).select("div.module-item-text").text());*/
                object.put("title", chinaLi.get(i).select("a.item-cover").attr("title"));
                object.put("url", chinaLi.get(i).select("a.item-cover").attr("href"));
                object.put("drama", chinaLi.get(i).select("p.num").text());
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
     * @param source
     * @return
     */
    public static List<MaliTagBean> getTagList(String source) {
        List<MaliTagBean> tagList = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements titles = document.getElementById("content").select("div.typebox.board").select("span");
        Elements items = document.getElementById("content").select("div.typebox.board").select("ul");
        if (titles.size() -1 == items.size()) {
            for (int i = 0, tagSize = titles.size(); i < tagSize; i++) {
                if (i == tagSize - 1) {
                    // 排序
                } else {
//                    TagHeaderBean tagHeaderBean = new TagHeaderBean(titles.get(i).text().replaceAll("：", ""));
                    String title = titles.get(i).text().replaceAll("：", "");
                    List<MaliTagBean.MaliTagList> maliTagListBeans = new ArrayList<>();
                    Elements itemElements = items.get(i).select("a");
                    for (int j = 0, itemSize = itemElements.size(); j < itemSize; j++) {
                        /*tagHeaderBean.addSubItem(
                                new TagBean(
                                        tagHeaderBean.getTitle() + " - " + itemElements.get(j).text(),
                                        itemElements.get(j).text(),
                                        itemElements.get(j).attr("href")
                                )
                        )*/
                        maliTagListBeans.add(new MaliTagBean.MaliTagList(title + " - " + itemElements.get(j).text(),
                                itemElements.get(j).text(),
                                itemElements.get(j).attr("href")));
                    }
                    tagList.add(new MaliTagBean(title, maliTagListBeans));
//                    tagList.add(tagHeaderBean);
                }
            }
        }
        /*Elements elements = document.select("div.box > div.scroll-box");
        for (int i=0,size=elements.size(); i<size; i++) {
            if (i == 0)
                continue;
            if (i == size-1) {
                // 排序
            } else {
                String title = elements.get(i).select("div.scroll-content > a").text().replaceAll("字母查找", "全部字母");
                String url = elements.get(i).select("div.scroll-content > a").attr("href");
                List<MaliTagBean.MaliTagList> maliTagListBeans = new ArrayList<>();
                maliTagListBeans.add(new MaliTagBean.MaliTagList(title,
                        title,
                        url));
                Elements itemElements = elements.get(i).select("div.library-list > a");
                for (int j = 0, itemSize = itemElements.size(); j < itemSize; j++) {
                    maliTagListBeans.add(new MaliTagBean.MaliTagList(title + " - " + itemElements.get(j).text(),
                            itemElements.get(j).text(),
                            itemElements.get(j).attr("href")));
                }
                tagList.add(new MaliTagBean(title,maliTagListBeans ));
//                    tagList.add(tagHeaderBean);
            }
        }*/
        return tagList;
    }
    /**************************************  动漫分类解析方法结束  **************************************/

    /**************************************  番剧列表&&搜索列表解析方法开始  **************************************/
    /**
     *  获取番剧列表集合(搜索界面)
     *  2022年6月29日20:11:35 修改
     * @param source
     * @return
     */
    public static List<AnimeListBean> getSearchAnimeList(String source) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.searchbox > ul > li");
        if (elements.size() > 0) {
            for (int i = 0, size = elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).select("a").get(0).attr("title"));
                bean.setUrl(elements.get(i).select("a").get(0).attr("href"));
                bean.setImg(elements.get(i).select("img").attr("data-echo"));
                bean.setDesc(elements.get(i).select("span.listbox-mask").text());
                animeListBeans.add(bean);
            }
        }
        /*Elements elements = document.select("div.module-items > div.module-search-item");
        if (elements.size() > 0) {
            for (int i=0,size=elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).select("div.module-item-pic > img").attr("alt"));
                bean.setUrl(elements.get(i).select("a.video-serial").attr("href"));
                bean.setImg(elements.get(i).select("div.module-item-pic > img").attr("data-src"));
                bean.setDesc(elements.get(i).select("a.video-serial").text());
                animeListBeans.add(bean);
            }
        }*/
        return animeListBeans;
    }

    /**
     *  获取番剧列表集合(分类界面)
     *  2022年6月29日20:29:47 修改
     * @param source
     * @return
     */
    public static List<AnimeListBean> getAnimeList(String source) {
        List<AnimeListBean> animeListBeans = new ArrayList<>();
        Document document = Jsoup.parse(source);
        Elements elements = document.select("div.listbox > ul > li");
        if (elements.size() > 0) {
            for (int i = 0, size = elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).select("a").get(0).attr("title"));
                bean.setUrl(elements.get(i).select("a").get(0).attr("href"));
                bean.setImg(elements.get(i).select("img").attr("data-echo"));
                bean.setDesc(elements.get(i).select("span.listbox-mask").text());
                animeListBeans.add(bean);
            }
        }
        /*Elements elements = document.select("div.module-list > div.module-items > div.module-item");
        if (elements.size() > 0) {
            for (int i=0,size=elements.size(); i < size; i++) {
                AnimeListBean bean = new AnimeListBean();
                bean.setTitle(elements.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("title"));
                bean.setUrl(elements.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("href"));
                bean.setImg(elements.get(i).select("div.module-item-pic > img").attr("data-src"));
                bean.setDesc(elements.get(i).select("div.module-item-text").text());
                animeListBeans.add(bean);
            }
        }*/
        return animeListBeans;
    }
    /**************************************  番剧列表&&电影列表&&搜索列表解析方法结束  **************************************/

    /**************************************  动漫详情解析方法开始  **************************************/
    /**
     * 获取番剧详情信息
     * 2022年6月29日20:29:52 修改
     * @param source
     * @param url
     * @return
     */
    public static AnimeListBean getAinmeInfo(String source, String url) {
        Log.e("url", url);
        AnimeListBean animeListBean = new AnimeListBean();
        Document document = Jsoup.parse(source);
        animeListBean.setTitle(document.select("div.drama-box > div#thumb > img").attr("alt"));
        //番剧图片
        animeListBean.setImg(document.select("div.drama-box > div#thumb > img").attr("src"));
        Elements labels = document.select("div.drama-box").select("label");
        for (Element label : labels) {
            if (label.text().contains("动漫剧情"))
                animeListBean.setDesc(label.text().replaceAll("动漫剧情：", ""));
            if (label.text().contains("时间"))
                animeListBean.setUpdateTime(label.text().replaceAll("时间：", ""));
        }
        //番剧地址
        animeListBean.setUrl(url);
        Elements tagElements = labels.select("a");
        List<String> tagTitles = new ArrayList<>();
        List<String> tagUrls = new ArrayList<>();
        for (int i=0,size=tagElements.size(); i<size; i++) {
            tagTitles.add(tagElements.get(i).text().toUpperCase());
            tagUrls.add(tagElements.get(i).attr("href"));
        }
        animeListBean.setTagTitles(tagTitles);
        animeListBean.setTagUrls(tagUrls);
        /*animeListBean.setTitle(document.select("div.box div.video-info > div.video-info-header > h1.page-title").text());
        //番剧图片
        animeListBean.setImg(document.select("div.box > div.video-cover > div.module-item-cover > div.module-item-pic > img").attr("data-src"));
        *//*Elements labels = document.select("div.drama-box").select("label");
        for (Element label : labels) {
            if (label.text().contains("动漫剧情"))
                animeListBean.setDesc(label.text().replaceAll("动漫剧情：", ""));
            if (label.text().contains("时间"))
                animeListBean.setUpdateTime(label.text().replaceAll("时间：", ""));
        }*//*
        animeListBean.setDesc(document.select("div.vod_content").text());
        animeListBean.setUpdateTime(document.select("div.video-info-main > div.video-info-items").get(3).select("div.video-info-item").text());
        //番剧地址
        animeListBean.setUrl(url);
        Elements tagElements = document.select("div.video-info-aux > a");
        tagElements.addAll(document.select("div.tag-link > a"));
        List<String> tagTitles = new ArrayList<>();
        List<String> tagUrls = new ArrayList<>();
        for (int i=0,size=tagElements.size(); i<size; i++) {
            tagTitles.add(tagElements.get(i).text().toUpperCase());
            tagUrls.add(tagElements.get(i).attr("href"));
        }
        animeListBean.setTagTitles(tagTitles);
        animeListBean.setTagUrls(tagUrls);*/
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
//        List<List<AnimeDescDetailsBean>> multipleAnimeDescDetailsBeans = new ArrayList<>();
        // 该网站存在多各播放地址，推荐使用主路线，故只获取主线路剧集列表
        Elements playElements = document.select("div#vlink_1 > ul > li"); //剧集列表
        if (playElements.size() > 0) {
            boolean select;
            List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
            for (Element dramaList : playElements) {
                String name = dramaList.select("a").text();
                String watchUrl = dramaList.select("a").attr("href");
                Log.e("dramaStr - > " , dramaStr + "- > " + watchUrl);
                if (dramaStr.contains(watchUrl)) select = true;
                else select = false;
                animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, select));
            }
           /* multipleAnimeDescDetailsBeans.add(animeDescDramasBeans);
            animeDescListBean.setMultipleAnimeDescDetailsBeans(multipleAnimeDescDetailsBeans);*/
            animeDescListBean.setAnimeDescDetailsBeans(animeDescDramasBeans);
            //** 封装推荐 **//
            Elements recommendElements = document.select("div#mytab8r").select("li"); //相关推荐
            if (recommendElements.size() > 0) {
                List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
                for (int i = 0, size = recommendElements.size(); i < size; i++) {
                    String title = recommendElements.get(i).select("b").text();
                    String img = recommendElements.get(i).select("img").attr("data-echo");
                    String url = recommendElements.get(i).select("a").attr("href");
                    animeDescRecommendBeans.add(new AnimeDescRecommendBean(title, img, url));
                }
                animeDescListBean.setAnimeDescRecommendBeans(animeDescRecommendBeans);
            }
            return animeDescListBean;
        } else
            return null;
//        Elements playElements = document.select("div#sort-item-1 > a"); //剧集列表
//        if (playElements.size() > 0) {
//            boolean select;
//            List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
//            for (Element dramaList : playElements) {
//                String name = dramaList.select("a").text();
//                String watchUrl = dramaList.select("a").attr("href");
//                Log.e("dramaStr - > " , dramaStr + "- > " + watchUrl);
//                if (dramaStr.contains(watchUrl)) select = true;
//                else select = false;
//                animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, select));
//            }
//           /* multipleAnimeDescDetailsBeans.add(animeDescDramasBeans);
//            animeDescListBean.setMultipleAnimeDescDetailsBeans(multipleAnimeDescDetailsBeans);*/
//            animeDescListBean.setAnimeDescDetailsBeans(animeDescDramasBeans);
//            //** 封装推荐 **//
//            Elements recommendElements = document.select("div.module-lines-list > div.module-items > div.module-item"); //相关推荐
//            if (recommendElements.size() > 0) {
//                List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
//                for (int i = 0, size = recommendElements.size(); i < size; i++) {
//                    String title = recommendElements.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("title");
//                    String img = recommendElements.get(i).select("div.module-item-pic > img").attr("data-src");
//                    String url = recommendElements.get(i).select("div.module-item-cover > div.module-item-pic > a").attr("href");
//                    animeDescRecommendBeans.add(new AnimeDescRecommendBean(title, img, url));
//                }
//                animeDescListBean.setAnimeDescRecommendBeans(animeDescRecommendBeans);
//            }
//            return animeDescListBean;
//        } else
//            return null;
    }
    /**************************************  动漫详情解析方法结束  **************************************/

    /**************************************  视频JS解析方法  **************************************/
    /**
     * 获取播放地址
     * 2022年5月29日
     * @param source
     * @return
     */
    public static String getImomoePlayUrl(String source) {
        Document document = Jsoup.parse(source);
        Element element = document.getElementById("player");
        Matcher matcher = Pattern.compile("\\{.*\\}").matcher(element.html());
        if (matcher.find()) {
            String obj = matcher.group();
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(obj);
            Log.e("url", jsonObject.getString("url"));
            return jsonObject.getString("url");
        }
        return "";
    }

    /**************************************  选集解析方法开始  **************************************/
    /**
     * 获取番剧所有剧集(用于播放界面选集)
     * 2022年6月29日21:03:25 修改
     * @param source
     * @param dramaStr 用户已观看过的url
     * @return
     */
    public static List<AnimeDescDetailsBean> getAllDrama(String source, String dramaStr) {
        Document document = Jsoup.parse(source);
        Elements playElements = document.select("div#tabDatelist").select("ul").get(0).select("a"); //剧集列表
        List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
        for (Element dramaList : playElements) {
            String name = dramaList.text();
            String watchUrl = dramaList.attr("href");
            animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
        }
        /*Elements playElements = document.select("div#sort-item-1 > a"); //剧集列表
        List<AnimeDescDetailsBean> animeDescDramasBeans = new ArrayList<>();
        for (Element dramaList : playElements) {
            String name = dramaList.select("span").text();
            String watchUrl = dramaList.attr("href");
            animeDescDramasBeans.add(new AnimeDescDetailsBean(name, watchUrl, dramaStr.contains(watchUrl)));
        }*/
        Log.e("size", animeDescDramasBeans.size() + "");
        return animeDescDramasBeans;
    }
    /**************************************  选集解析方法结束  **************************************/


    /**************************************  更新图片方方法开始  **************************************/
    /**
     * 获取番剧图片
     * 2022年6月29日21:05:07 修改
     * @param source
     * @return
     */
    public static String getAinmeImg(String source) {
        Document document = Jsoup.parse(source);
        return document.select("div.drama-box > div#thumb > img").attr("src");
//        return document.select("div.box > div.video-cover > div.module-item-cover > div.module-item-pic > img").attr("data-src");
    }
    /**************************************  更新图片方法结束  **************************************/
}
