package my.project.sakuraproject.net;
import java.util.concurrent.TimeUnit;

import my.project.sakuraproject.main.base.BaseModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpPost {
    private final static int connectTimeout = 10;
    private final static int readTimeout = 20;

    /**
     * 仅用于S站获取视频播放地址
     * @param url
     * @param body
     * @param callback
     */
    public HttpPost(String url, FormBody body, Callback callback) {
        String domain = BaseModel.getDomain(true);
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS).build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Cookie", "silisili=on")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("Origin", domain)
                .addHeader(":path", url.replaceAll(domain, ""))
                .addHeader("Sec-Ch-Ua", "\"Microsoft Edge\";v=\"117\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"117\"")
                .addHeader("Sec-Ch-Ua-Mobile", "?0")
                .addHeader("Sec-Ch-Ua-Platform", "\"Windows\"")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.60")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
