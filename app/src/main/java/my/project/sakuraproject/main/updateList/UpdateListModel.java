package my.project.sakuraproject.main.updateList;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeUpdateBean;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateListModel extends BaseModel implements UpdateListContract.Model {

    @Override
    public void getData(String url, boolean isImomoe, UpdateListContract.LoadDataCallback callback) {
        if (isImomoe)
            parserImomoe(getDomain(true) + url, callback);
        else
            parserYhdm(getDomain(false) + url, callback);
    }

    private void parserYhdm(String url, UpdateListContract.LoadDataCallback callback) {
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
                        parserYhdm(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(url, callback);
                    else {
                        List<AnimeUpdateBean> animeUpdateBeans = YhdmJsoupUtils.getUpdateInfoList2(source);
                        if (animeUpdateBeans.size() > 0)
                            callback.success(animeUpdateBeans);
                        else
                            callback.error(Utils.getString(R.string.error_msg));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void parserImomoe(String url, UpdateListContract.LoadDataCallback callback) {
        // 暂不支持
        /*callback.log(url);
        Log.e("url", url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getHtmlBody(response, true);
                    List<AnimeUpdateBean> animeUpdateBeans = ImomoeJsoupUtils.getUpdateInfoList2(source);
                    if (animeUpdateBeans.size() > 0)
                        callback.success(animeUpdateBeans);
                    else
                        callback.error(Utils.getString(R.string.error_msg));
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });*/
    }
}
