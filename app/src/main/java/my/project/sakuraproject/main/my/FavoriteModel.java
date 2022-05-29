package my.project.sakuraproject.main.my;

import java.io.IOException;
import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.YhdmJsoupUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FavoriteModel extends BaseModel implements FavoriteContract.Model {

    @Override
    public void getData(int offset, int limit, boolean updateOrder, FavoriteContract.LoadDataCallback callback) {
        List<AnimeListBean> list = DatabaseUtil.queryFavoriteByLimit(offset, limit, updateOrder);
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error(Utils.getString(R.string.empty_favorite));
    }

    @Override
    public void getUpdateInfo(int source, List<AnimeUpdateInfoBean> beans, FavoriteContract.LoadDataCallback callback) {
        switch (source) {
            case 0:
                new HttpGet(Sakura.YHDM_UPDATE, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.error(0);
//                        callback.completion(false);
                        callback.completion(true);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try {
                            String source = getHtmlBody(response, false);
                            List<AnimeUpdateInfoBean> animeUpdateInfoBeans = YhdmJsoupUtils.getUpdateInfoList(source, beans);
                            if (animeUpdateInfoBeans.size() > 0)
                                DatabaseUtil.updateFavorite(animeUpdateInfoBeans);
//                            callback.completion(false);
                            callback.completion(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.error(0);
//                            callback.completion(false);
                            callback.completion(true);
                        }
                    }
                });
//                parserYhdm(getDomain(false), callback, "");
                break;
            case 1:
                /*
                new HttpGet(Sakura.IMOMOE_UPDATE, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.error(1);
                        callback.completion(true);
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try {
                            String source = getHtmlBody(response, true);
                            List<AnimeUpdateInfoBean> animeUpdateInfoBeans = ImomoeJsoupUtils.getUpdateInfoList(source);
                            if (animeUpdateInfoBeans.size() > 0)
                                DatabaseUtil.updateFavorite(animeUpdateInfoBeans);
                            callback.completion(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.error(1);
                            callback.completion(true);
                        }
                    }
                });
                parserImomoe(getDomain(true), callback);
                 */
                break;
        }
    }

    /*
    private void parserYhdm(String url, FavoriteContract.LoadDataCallback callback, String RedirectedStr) {
        callback.log(url + RedirectedStr);
        new HttpGet(url + RedirectedStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(0);
                callback.completion(false);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    if (YhdmJsoupUtils.hasRedirected(source)) // 如果有重定向
                        parserYhdm(url, callback, YhdmJsoupUtils.getRedirectedStr(source));
                    else {
                        if (YhdmJsoupUtils.hasRefresh(source)) // 如果有定时跳转
                            parserYhdm(url, callback, "");
                        else {
                            List<AnimeUpdateInfoBean> animeUpdateInfoBeans = YhdmJsoupUtils.getUpdateInfoList(source);
                            if (animeUpdateInfoBeans.size() > 0)
                                DatabaseUtil.updateFavorite(animeUpdateInfoBeans);
                            callback.completion(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(0);
                    callback.completion(false);
                }
            }
        });
    }

    private void parserImomoe(String url, FavoriteContract.LoadDataCallback callback) {
        callback.log(url);
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(1);
                callback.completion(true);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String source = getBody(response);
                    List<AnimeUpdateInfoBean> animeUpdateInfoBeans = ImomoeJsoupUtils.getUpdateInfoList(source);
                    if (animeUpdateInfoBeans.size() > 0)
                        DatabaseUtil.updateFavorite(animeUpdateInfoBeans);
                    callback.completion(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(1);
                    callback.completion(true);
                }
            }
        });
    }
    */
}
