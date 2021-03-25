package my.project.sakuraproject.main.animeList;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
    private String replaceStr = "page=%s&";

    @Override
    public void getData(String url, int page, boolean isMain, boolean isMovie, AnimeListContract.LoadDataCallback callback) throws UnsupportedEncodingException {
        String htmlUrl = getUlr(url, page);
        System.out.println(htmlUrl);
        if (isImomoe())
            parserImomoe(htmlUrl, isMain, callback);
        else
            parserYhdm(htmlUrl, isMain, isMovie, callback);
    }

    /**
     * 获取网页链接
     * @param url
     * @param page
     * @return
     */
    private String getUlr(String url, int page) throws UnsupportedEncodingException {
        if (isImomoe()) {
            if (page != 1)
                return encodeUrl(url.contains(Sakura.DOMAIN) ? url.replaceAll("page=[0-9]{0,}\\&", String.format(replaceStr, page)) : Sakura.DOMAIN + url.replaceAll("page=[0-9]{0,}\\&", String.format(replaceStr, page)));
            else
                return encodeUrl(url);
        } else {
            if (page != 1)
                return url.contains(Sakura.DOMAIN) ? url + page + ".html" : Sakura.DOMAIN + url + page + ".html";
            else
                return url;
        }
    }

    private String encodeUrl(String url) throws UnsupportedEncodingException {
        String resultURL = "";
        //遍历字符串
        for (int i = 0; i < url.length(); i++) {
            char charAt = url.charAt(i);
            //只对汉字处理
            if (isChineseChar(charAt)) {
                String encode = URLEncoder.encode(charAt+"","GB2312");
                resultURL+=encode;
            }else {
                resultURL+=charAt;
            }
        }
        return resultURL;
    }

    private static boolean isChineseChar(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    private void parserYhdm(String url,boolean isMain, boolean isMovie, AnimeListContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
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

    private void parserImomoe(String url,boolean isMain, AnimeListContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    if (isMain)
                        callback.pageCount(ImomoeJsoupUtils.getPageCount(source));
                    List<AnimeListBean>  animeListBeans = ImomoeJsoupUtils.getAnimeList(source);
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
