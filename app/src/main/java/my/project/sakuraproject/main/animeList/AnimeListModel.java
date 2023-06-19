package my.project.sakuraproject.main.animeList;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AnimeListModel extends BaseModel implements AnimeListContract.Model {
    private final static Pattern MALIMALI_WD = Pattern.compile("-(.*)-");

    @Override
    public void getData(String url, int page, boolean isMain, boolean isMovie, boolean isImomoe, boolean isToptic, List<String> siliParams, AnimeListContract.LoadDataCallback callback) throws UnsupportedEncodingException {
        String htmlUrl = getUlr(url, page, isImomoe);
        if (isImomoe)
            parserImomoe(htmlUrl, page, isMain, isToptic, siliParams, callback);
        else
            parserYhdm(htmlUrl, isMain, isMovie, callback);
    }

    @Override
    public void getData(String[] params, boolean isMain, AnimeListContract.LoadDataCallback callback) {
        /*String url = getDomain(true) + String.format(Api.MALIMALI_TAG, params[0], params[1], params[2], params[3], params[4], params[5]);
        parserImomoe(url, isMain, callback);*/
    }

    /**
     * 获取网页链接
     * @param url
     * @param page
     * @return
     */
    private String getUlr(String url, int page, boolean isImomoe) throws UnsupportedEncodingException {
        if (isImomoe) {
            /*if (isMovie) {
                if (page != 1)
                    return getDomain(true) + String.format(Sakura.MOVIE_API, "1_"+page);
                else
                    return getDomain(true) + String.format(Sakura.MOVIE_API, "1");
            } else {
                if (page != 1)
                    return encodeUrl(getDomain(true) + url.replaceAll("page=[0-9]{0,}\\&", String.format(replaceStr, page)));
                else
                    return encodeUrl(getDomain(true) + url);
            }*/
            /*Matcher m = MALIMALI_WD.matcher(url);
            if (m.find())
                return getDomain(true) + String.format(Api.MALIMALI_SEARCH, m.group().replaceAll("-", ""), page);
            else
                return "";*/
            return getDomain(true) + url;
        } else {
            if (page != 1)
                return getDomain(false) + url + page + ".html";
            else
                return getDomain(false) + url;
        }
    }

    private void parserYhdm(String url, boolean isMain, boolean isMovie, AnimeListContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getHtmlBody(response, false);
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), isMain, isMovie, callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(url, isMain, isMovie, callback);
                    else {
                        if (isMain)
                            callback.pageCount(YhdmJsoupUtils.getPageCount(source));
                        List<AnimeListBean>  animeListBeans = YhdmJsoupUtils.getAnimeList(source, isMovie);
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

    private void parserImomoe(String url, int page, boolean isMain, boolean isToptic, List<String> siliParams, AnimeListContract.LoadDataCallback callback) {
        Log.e("SiliSiliUrl", url);
        callback.log(url);
        if (isToptic)
            new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getHtmlBody(response, true);
                    if (isMain)
                        callback.pageCount(1); // 不知道是否有分页参数
                    List<AnimeListBean>  animeListBeans = ImomoeJsoupUtils.getAnimeList(source, true);
                    if (animeListBeans.size() > 0)
                        callback.success(isMain, animeListBeans);
                    else
                        callback.error(isMain, Utils.getString(R.string.error_msg));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(isMain, e.getMessage());
                }
            }
        });
        else {
            String pageStr = "/page/%s/";
            String pageParam = String.format(pageStr, page);
            for (String params : siliParams) {
                url += params;
            }
            url += pageParam;
            Log.e("SiliSiliUrl", url);
            new HttpGet(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.error(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        String source = getHtmlBody(response, true);
                        if (isMain)
                            callback.pageCount(ImomoeJsoupUtils.getPageCount(source));
                        List<AnimeListBean>  animeListBeans = ImomoeJsoupUtils.getAnimeList(source, false);
                        if (animeListBeans.size() > 0)
                            callback.success(isMain, animeListBeans);
                        else
                            callback.error(isMain, Utils.getString(R.string.error_msg));
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.error(isMain, e.getMessage());
                    }
                }
            });
        }
    }
}
