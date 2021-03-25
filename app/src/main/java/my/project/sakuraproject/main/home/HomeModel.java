package my.project.sakuraproject.main.home;

import java.io.IOException;
import java.util.LinkedHashMap;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeModel extends BaseModel implements HomeContract.Model {
    @Override
    public void getData(HomeContract.LoadDataCallback callback) {
        if (isImomoe())
            parserImomoe(callback);
        else
            parserYhdm(callback, "");
    }

    private void parserYhdm(HomeContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(Sakura.DOMAIN + RedirectedStr);
        new HttpGet(Sakura.DOMAIN + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    if (YhdmJsoupUtils.hasRedirected(source)) // 如果有重定向
                        parserYhdm(callback, YhdmJsoupUtils.getRedirectedStr(source));
                    else {
                        if (YhdmJsoupUtils.hasRefresh(source)) // 如果有定时跳转
                            parserYhdm(callback, "");
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

    private void parserImomoe(HomeContract.LoadDataCallback callback) {
        callback.log(Sakura.DOMAIN);
        new HttpGet(Sakura.DOMAIN, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    LinkedHashMap map = ImomoeJsoupUtils.getHomeData(source);
                    if ((boolean) map.get("success"))
                        callback.success(map);
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
