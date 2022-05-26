package my.project.sakuraproject.main.tag;

import android.util.Log;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
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
    public void getData(TagContract.LoadDataCallback callback) {
        if (isImomoe())
            parserImomoe(callback);
        else
            parserYhdm(callback, "");
    }

    private void parserYhdm(TagContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(Sakura.TAG_API + RedirectedStr);
        new HttpGet(Sakura.TAG_API + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(callback, YhdmJsoupUtils.getRedirectedStr(source));
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(callback, "");
                    else {
                        List<MultiItemEntity> tagList = YhdmJsoupUtils.getTagList(source);
                        if (tagList.size() > 0)
                            callback.success(tagList);
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

    private void parserImomoe(TagContract.LoadDataCallback callback) {
        callback.log(Sakura.TAG_API);
        String url = getDomain(true) + String.format(Api.MALIMALI_TAG, Api.MALIMALI_JAPAN, "", "", "", "", "");
        Log.e("url", url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    List<MultiItemEntity> tagList = ImomoeJsoupUtils.getTagList(source);
                    if (tagList.size() > 0)
                        callback.success(tagList);
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
