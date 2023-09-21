package my.project.sakuraproject.main.video;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.main.base.BaseModel;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DanmuModel extends BaseModel implements DanmuContract.Model {
    // 只用到一个API懒得使用 retrofit
    @Override
    public void getDanmu(String title, String drama, DanmuContract.LoadDataCallback callback) {

        new HttpGet(String.format(Api.SILISILI_DANMU_API, getDomain(true), title, drama), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.errorDanmu(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String danmu = response.body().string();
                callback.successDanmuXml(danmu);
                /*if (!danmu.isEmpty()) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(danmu);
                        if (jsonObject.getInteger("code") == 200)
                            callback.successDanmu(jsonObject);
                        else
                            callback.errorDanmu("接口服务返回异常，请稍后再试！");
                    } catch (Exception e) {
                        callback.errorDanmu("弹幕接口返回JSON格式异常，内容解析失败！");
                    }
                }
                else
                    callback.errorDanmu("获取弹幕信息失败！");*/

            }
        });
    }
}
