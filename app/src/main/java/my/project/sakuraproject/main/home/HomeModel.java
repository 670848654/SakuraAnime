package my.project.sakuraproject.main.home;

import java.io.IOException;
import java.util.LinkedHashMap;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeModel implements HomeContract.Model {
    @Override
    public void getData(HomeContract.LoadDataCallback callback) {
        getHtml(callback, "");
    }

    private void getHtml(HomeContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(Sakura.DOMAIN + RedirectedStr);
        new HttpGet(Sakura.DOMAIN + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = response.body().string();
                    if (YhdmJsoupUtils.hasRedirected(source)) // 如果有重定向
                        getHtml(callback, YhdmJsoupUtils.getRedirectedStr(source));
                    else {
                        if (YhdmJsoupUtils.hasRefresh(source)) // 如果有定时跳转
                            getHtml(callback, "");
                        else {
                            LinkedHashMap map = YhdmJsoupUtils.getHomeData(source);
                            if ((boolean) map.get("success"))
                                callback.success(map);
                            else
                                callback.error(Utils.getString(R.string.parsing_error));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }
}
