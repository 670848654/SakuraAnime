package my.project.sakuraproject.sniffing;

import android.view.View;

import java.util.List;

public class DefaultCallback implements SniffingCallback {

    @Override
    public void onSniffingSuccess(View webView, String url, int position, List<SniffingVideo> videos) {

    }

    @Override
    public void onSniffingError(View webView, String url, int position, int errorCode) {
    }
}
