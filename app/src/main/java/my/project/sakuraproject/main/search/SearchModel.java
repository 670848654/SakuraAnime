package my.project.sakuraproject.main.search;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.main.animeList.AnimeListContract;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchModel extends BaseModel implements SearchContract.Model {
    private static String IMOMOE_SEARCH_PARAM = "?page=%s&searchword=%s&searchtype=-1";

    @Override
    public void getData(String url, int page, boolean isMain, String imomoeParam, SearchContract.LoadDataCallback callback) throws UnsupportedEncodingException {
        if (isImomoe()) {
            url = url + String.format(IMOMOE_SEARCH_PARAM, page, encodeUrl(imomoeParam));
            parserImomoe(url, isMain, callback);
        } else {
            if (page != 1)
                url = url.contains(Sakura.DOMAIN) ? url + "?page=" + page: Sakura.DOMAIN + url + "?page=" + page ;
            else
                url =  url.contains(Sakura.DOMAIN) ? url : Sakura.DOMAIN + url;
            parserYhdm(url, isMain, callback);
        }
    }

    private void parserYhdm(String url, boolean isMain, SearchContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = response.body().string();
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), isMain, callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(url, isMain, callback);
                    else {
                        if (isMain)
                                callback.pageCount(YhdmJsoupUtils.getPageCount(source));
                        List<AnimeListBean> animeListBeans = YhdmJsoupUtils.getAnimeList(source, false);
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

    private void parserImomoe(String url,boolean isMain, SearchContract.LoadDataCallback callback) {
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
