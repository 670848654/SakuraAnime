package my.project.sakuraproject.main.video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.VideoUtils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoModel extends BaseModel implements VideoContract.Model {
    private final static Pattern PLAY_DATA_PATTERN = Pattern.compile("\\[(.*)\\]");

    @Override
    public void getData(String title, String url, VideoContract.LoadDataCallback callback) {
        if (url.contains("/player/"))
            parserImomoe(title, BaseModel.getDomain(true) + url, false, callback);
        else
            parserYhdm(title, BaseModel.getDomain(false) + url, callback);
    }

    private void parserYhdm(String title, String url, VideoContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, false);
                if (YhdmJsoupUtils.hasRedirected(source))
                    parserYhdm(title, Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), callback);
                else if (YhdmJsoupUtils.hasRefresh(source))
                    parserYhdm(title, url, callback);
                else {
                    String fid = DatabaseUtil.getAnimeID(title);
                    DatabaseUtil.addIndex(fid, url);
                    String dataBaseDrama = DatabaseUtil.queryAllIndex(fid);
                    callback.successDrama(YhdmJsoupUtils.getAllDrama(source, dataBaseDrama));
                    List<String> urls = YhdmJsoupUtils.getVideoUrlList(source);
                    if (urls.size() > 0)
                        callback.success(urls);
                    else
                        callback.empty();
                }
            }
        });
    }

    private void parserImomoe(String title, String url, boolean isJs, VideoContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, true);
                String js = "";
                if (!isJs) {
                    String fid = DatabaseUtil.getAnimeID(title);
                    DatabaseUtil.addIndex(fid, url);
                    js = ImomoeJsoupUtils.getPlayDataJs(source);
                    if (js.isEmpty()) callback.empty();
                    else parserImomoe(title, getDomain(true) + js, true, callback);
                } else {
                    Matcher matcher = PLAY_DATA_PATTERN.matcher(source);
                    String json = "";
                    if (matcher.find()) {
                        json = matcher.group();
                    }
                    if (json.isEmpty()) return;
                    else {
                        List<List<ImomoeVideoUrlBean>> imomoeBeans = VideoUtils.getImomoePlayUrl(json);
                        if (imomoeBeans.size() > 0)
                            callback.successImomoeDrama(imomoeBeans);
                        else
                            callback.empty();
                    }
                }
            }
        });
    }
}
