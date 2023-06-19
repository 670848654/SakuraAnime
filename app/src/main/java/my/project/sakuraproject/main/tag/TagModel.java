package my.project.sakuraproject.main.tag;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.TagBean;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TagModel extends BaseModel implements TagContract.Model {

    @Override
    public void getData(String url, String[] siliParams, TagContract.LoadDataCallback callback) {
        if (isImomoe())
            parserImomoe(url, siliParams, callback);
        else
            parserYhdm(url, callback, "");
    }

    private void parserYhdm(String url, TagContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(url);
        new HttpGet(url + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(url, callback, YhdmJsoupUtils.getRedirectedStr(source));
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(url, callback, "");
                    else {
                        List<TagBean> tagList = YhdmJsoupUtils.getTagList(source);
                        if (tagList.size() > 0)
                            callback.success(false, tagList);
                        else
                            callback.error(Utils.getString(R.string.parsing_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }

            }
        });
    }

    private void parserImomoe(String url, String[] siliParams, TagContract.LoadDataCallback callback) {
        callback.log(url);
        String tagUrl = getDomain(true) + url;
        Log.e("tagUrl", tagUrl);
        new HttpGet(tagUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    List<TagBean> tagList = ImomoeJsoupUtils.getTagList(source, siliParams);
                    if (tagList.size() > 0) {
                        callback.success(true, tagList);
                        List<AnimeListBean> animeListBeans = ImomoeJsoupUtils.getAnimeList(source, false);
                        int pageCount = ImomoeJsoupUtils.getPageCount(source);
                        callback.siliAnimeList(animeListBeans, pageCount);
                    }
                    else
                        callback.error(Utils.getString(R.string.parsing_error));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }

            }
        });
    }
}
