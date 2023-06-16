package my.project.sakuraproject.main.video;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.ImomoeJsoupUtils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DownloadVideoModel extends BaseModel implements DownloadVideoContract.Model {
    private final static Pattern PLAY_DATA_PATTERN = Pattern.compile("\\[(.*)\\]");

    @Override
    public void getData(String url, String playNumber, int source, DownloadVideoContract.LoadDataCallback callback) {
        if (source == 1)
            parserImomoeVideoUrls(getDomain(true) + url, playNumber, callback);
        else
            parserYhdmVideoUrls(getDomain(false) + url, playNumber, callback);
    }

    private void parserYhdmVideoUrls(String url, String playNumber,  DownloadVideoContract.LoadDataCallback callback ) {
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(playNumber);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, false);
                if (YhdmJsoupUtils.hasRedirected(source))
                    parserYhdmVideoUrls(Sakura.DOMAIN + YhdmJsoupUtils.getRedirectedStr(source), playNumber, callback);
                else if (YhdmJsoupUtils.hasRefresh(source))
                    parserYhdmVideoUrls(url, playNumber, callback);
                else {
                    List<String> urls = YhdmJsoupUtils.getVideoUrlList(source);
                    /*if (urls.size() > 0) {
                        YhdmVideoUrlBean yhdmViideoUrlBean = null;
                        for (String url : urls) {
                            if (!url.contains("$")) {
                                if (HttpGet.isSuccess(url)) {
                                    yhdmViideoUrlBean = new YhdmVideoUrlBean();
                                    yhdmViideoUrlBean.setHttp(true);
                                    yhdmViideoUrlBean.setVidOrUrl(url);
                                    break;
                                } else {
                                    Log.e("ResponseError", url + " -> 资源访问失败！");
                                    continue;
                                }
                            } else {
                                yhdmViideoUrlBean = new YhdmVideoUrlBean();
                                yhdmViideoUrlBean.setHttp(false);
                                yhdmViideoUrlBean.setVidOrUrl(url);
                                break;
                            }
                        }
                        if (yhdmViideoUrlBean == null)
                            callback.error(playNumber);
                        else
                            callback.successYhdmVideoUrls(yhdmViideoUrlBean, playNumber);
                    }*/
                    if (urls.size() > 0)
                        callback.successYhdmVideoUrls(urls, playNumber);
                    else
                        callback.error(playNumber);
                }
            }
        });
    }

    private void parserImomoeVideoUrls( String url, String playNumber, DownloadVideoContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(playNumber);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, true);
                String playUrl = ImomoeJsoupUtils.getImomoePlayUrl(source);
                if (!playUrl.isEmpty())
                    callback.successImomoeVideoUrls(playUrl, playNumber);
                else
                    callback.error(playNumber);
            }
        });
    }
}
