package my.project.sakuraproject.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpGet {
    private final static int connectTimeout = 10;
    private final static int readTimeout = 20;

    public HttpGet(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .build();
        Request request;
        if (url.contains("silisili")) // 临时解决S站无法访问的问题
            request =new Request.Builder()
                    .url(url)
                    .addHeader("Cookie", "silisili=on;path=/;max-age=86400")
                    .get()
                    .build();
        else
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static boolean isSuccess(String url) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response httpResponse =  client.newCall(request).execute();
        return httpResponse.isSuccessful();
    }
}
