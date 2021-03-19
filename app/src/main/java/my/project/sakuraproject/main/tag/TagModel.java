package my.project.sakuraproject.main.tag;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TagModel implements TagContract.Model {
    @Override
    public void getData(TagContract.LoadDataCallback callback) {
        getHtml(callback, "");
    }

    private void getHtml(TagContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(Sakura.TAG_API + RedirectedStr);
        new HttpGet(Sakura.TAG_API + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = response.body().string();
                    if (YhdmJsoupUtils.hasRedirected(source))
                        getHtml(callback, YhdmJsoupUtils.getRedirectedStr(source));
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        getHtml(callback, "");
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
}
