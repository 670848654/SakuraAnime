package my.project.sakuraproject.main.home;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedHashMap;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeModel extends BaseModel implements HomeContract.Model {
    private static final String[] TABS = Utils.getArray(R.array.week_array);

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
                    LinkedHashMap map = new LinkedHashMap();
                    JSONObject weekObj = new JSONObject();
                    Document body = Jsoup.parse(response.body().string());
                    if (hasRedirected(body)) {
                        // 如果有重定向
                        getHtml(callback, getRedirectedStr(body));
                        return;
                    } else {
                        if (hasRefresh(body)) getHtml(callback, "");
                        else {
                            setData(body.select("div.tlist > ul"), weekObj, map, callback);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    /**
     * 新番时间表
     *
     * @param title
     * @param els
     * @param jsonObject
     * @throws JSONException
     */
    private void setDataToJson(String title, Elements els, JSONObject jsonObject) throws JSONException {
        JSONArray arr = new JSONArray();
        for (int i = 0, size = els.size(); i < size; i++) {
            JSONObject object = new JSONObject();
            if (els.get(i).select("a").size() > 1) {
                object.put("title", els.get(i).select("a").get(1).text());
                object.put("url", els.get(i).select("a").get(1).attr("href"));
                object.put("drama", els.get(i).select("a").get(0).text());
                object.put("dramaUrl", els.get(i).select("a").get(0).attr("href"));
            } else {
                object.put("title", els.get(i).select("a").get(0).text());
                object.put("url", els.get(i).select("a").get(0).attr("href"));
            }
            arr.put(object);
        }
        jsonObject.put(title, arr);
    }

    private void setData(Elements home, JSONObject weekObj, LinkedHashMap map, HomeContract.LoadDataCallback callback) throws JSONException {
        if (home.size() > 0) {
            setDataToJson(TABS[0], home.get(0).select("li"), weekObj);
            setDataToJson(TABS[1], home.get(1).select("li"), weekObj);
            setDataToJson(TABS[2], home.get(2).select("li"), weekObj);
            setDataToJson(TABS[3], home.get(3).select("li"), weekObj);
            setDataToJson(TABS[4], home.get(4).select("li"), weekObj);
            setDataToJson(TABS[5], home.get(5).select("li"), weekObj);
            setDataToJson(TABS[6], home.get(6).select("li"), weekObj);
            Log.e("week", weekObj.toString());
            map.put("week", weekObj);
            callback.success(map);
        } else
            callback.error(Utils.getString(R.string.parsing_error));
    }
}
