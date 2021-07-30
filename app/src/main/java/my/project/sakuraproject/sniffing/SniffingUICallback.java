package my.project.sakuraproject.sniffing;

import android.view.View;

public interface SniffingUICallback extends SniffingCallback {
    /**
     * 开始视频嗅探
     * @param webView
     * @param url
     */
    void onSniffingStart(View webView,String url);

    /**
     * 视频嗅探结束
     * @param webView
     * @param url
     */
    void onSniffingFinish(View webView, String url);
}