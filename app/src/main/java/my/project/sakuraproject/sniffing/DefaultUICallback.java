package my.project.sakuraproject.sniffing;

import android.view.View;

import java.util.List;

public class DefaultUICallback implements SniffingUICallback {

    @Override
    public void onSniffingStart(View webView, String url) {
    }

    @Override
    public void onSniffingFinish(View webView, String url) {
    }

    @Override
    public void onSniffingSuccess(View webView, String url, int position, List<SniffingVideo> videos) {
    }

    @Override
    public void onSniffingError(View webView, String url, int position, int errorCode) {
    }

}