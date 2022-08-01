package my.project.sakuraproject.main.my;

import java.io.IOException;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateImgModel extends BaseModel implements UpdateImgContract.Model {

    @Override
    public void getData(String oldImgUrl, String descUrl, UpdateImgContract.LoadDataCallback callback) {
        if (descUrl.contains("/voddetail/"))
            parserImomoe(oldImgUrl, getDomain(true) + descUrl, callback);
        else
            parserYhdm(oldImgUrl, getDomain(false) + descUrl, callback);
    }

    private void parserYhdm(String oldImgUrl, String url, UpdateImgContract.LoadDataCallback callback) {
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String source = getHtmlBody(response, false);
                    if (YhdmJsoupUtils.hasRedirected(source))
                        parserYhdm(oldImgUrl, Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                    else if (YhdmJsoupUtils.hasRefresh(source))
                        parserYhdm(oldImgUrl, url, callback);
                    else {
                        String img = YhdmJsoupUtils.getAinmeImg(source);
                        if (!img.isEmpty())
                            callback.success(oldImgUrl, img);
                        else
                            callback.error("更新番剧图片失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    private void parserImomoe(String oldImgUrl, String url, UpdateImgContract.LoadDataCallback callback) {
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String source = getHtmlBody(response, true);
                    String img = ImomoeJsoupUtils.getAinmeImg(source);
                    if (!img.isEmpty())
                        callback.success(oldImgUrl, img);
                    else
                        callback.error("更新番剧图片失败！");
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }
}
