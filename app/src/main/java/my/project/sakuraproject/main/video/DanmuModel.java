package my.project.sakuraproject.main.video;

import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DanmuModel extends BaseModel implements DanmuContract.Model {
    // 只用到一个API懒得使用 retrofit
    @Override
    public void getDanmu(String title, String drama, DanmuContract.LoadDataCallback callback) {
        new HttpGet(String.format(Api.DANMU_API, title, drama), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.errorDanmu(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String source = getHtmlBody(response, false);
                String danmu = Jsoup.parse(source).html();
                if (!danmu.isEmpty()) {
                    Matcher matcher = Pattern.compile("\\{.*\\}").matcher(danmu);
                    if (matcher.find()) {
                        String obj = matcher.group();
                        try {
                            JSONObject jsonObject = JSONObject.parseObject(obj);
                            callback.successDanmu(jsonObject);
                        } catch (Exception e) {
                            callback.errorDanmu("弹幕接口返回JSON格式异常，内容解析失败！");
                        }
                    } else
                        callback.errorDanmu("获取弹幕信息失败！");
                }
                else
                    callback.errorDanmu("获取弹幕信息失败！");
            }
        });
    }
}
