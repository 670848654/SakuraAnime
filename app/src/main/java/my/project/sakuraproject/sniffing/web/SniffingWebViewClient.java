package my.project.sakuraproject.sniffing.web;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.project.sakuraproject.sniffing.LogUtil;
import my.project.sakuraproject.sniffing.SniffingCallback;
import my.project.sakuraproject.sniffing.SniffingFilter;
import my.project.sakuraproject.sniffing.SniffingUICallback;
import my.project.sakuraproject.sniffing.SniffingVideo;
import my.project.sakuraproject.sniffing.Util;
import my.project.sakuraproject.sniffing.node.Node;

/**
 * SniffingWebViewClient
 */
public class SniffingWebViewClient extends WebViewClient implements SniffingUICallback {
    public static final int READ_TIME_OUT = 1;
    public static final int RECEIVED_ERROR = 2;
    public static final int NOT_FIND = 3;
    public static final int CONNECTION_ERROR = 4;
    public static final int CONTENT_ERROR = 5;

    public static final int TYPE_CONN = 0;
    public static final int TYPE_READ = 1;

    private boolean isCompleteLoader = true;

    private Handler mH = new Handler(Looper.getMainLooper());

    private List<SniffingVideo> mVideos = new ArrayList<>();
    private Map<String, String> mHeader;
    private SniffingFilter mFilter;
    private SniffingCallback mCallback;

    private long mLastStartTime;
    private long mLastEndTime = System.currentTimeMillis();

    private FinishedRunnable mFinished = null;
    private TimeOutRunnable mConnTimeout = null;
    private TimeOutRunnable mReadTimeout = null;
    private ParserHtmlRunnable mJSRunnable = null;
    private long mConnTimeOut = 20 * 1000;
    private long mReadTimeOut = 45 * 1000;
    private long mFinishedTimeOut = 800;
    private WebView mWebView;
    private String mURL;
    private int mPosition;

    public SniffingWebViewClient(WebView mWebView, String mURL, int mPosition, Map<String, String> mHeader,
                                 SniffingFilter mFilter, SniffingCallback mCallback) {
        this.mHeader = mHeader;
        this.mWebView = mWebView;
        this.mURL = mURL;
        this.mPosition = mPosition;
        this.mFilter = mFilter;
        this.mCallback = mCallback;
    }

    public void parserHtml(WebView webView, String url, String html) {
        mH.removeCallbacks(mJSRunnable);
        if (html.contains(".m3u8")) {
            int end = html.indexOf(".m3u8");
            String http = html.substring(0, end + 5);
            int start = http.lastIndexOf("http");
            String m3u8 = http.substring(start, end + 5);
            if (m3u8.contains(";") || m3u8.contains(",") || m3u8.contains("\"") || m3u8.contains("'") ) {
                parserNode(webView, url, new Node(html));
            } else if(m3u8.contains("=")){
                LogUtil.e("SniffingUtil", "onSuccess(containsType)  --> " + url);
                mVideos.add(new SniffingVideo(m3u8.split("=")[1], ".m3u8"));
                this.onSniffingSuccess(webView, url, mPosition, mVideos);
                this.onSniffingFinish(webView, url);
            }else{
                LogUtil.e("SniffingUtil", "onSuccess(containsType)  --> " + url);
                mVideos.add(new SniffingVideo(m3u8, ".m3u8"));
                this.onSniffingSuccess(webView, url, mPosition, mVideos);
                this.onSniffingFinish(webView, url);
            }
        } else if (html.contains(".mp4")) {
            int end = html.indexOf(".mp4");
            String http = html.substring(0, end + 4);
            int start = http.lastIndexOf("mp4");
            String mp4 = http.substring(start, end + 4);
            if (mp4.contains(";") || mp4.contains(",") || mp4.contains("\"") || mp4.contains("'") ) {
                parserNode(webView, url, new Node(html));
            } else if(mp4.contains("=")){
                LogUtil.e("SniffingUtil", "onSuccess(containsType)  --> " + url);
                mVideos.add(new SniffingVideo(mp4.split("=")[1], ".mp4"));
                this.onSniffingSuccess(webView, url, mPosition, mVideos);
                this.onSniffingFinish(webView, url);
            }else{
                LogUtil.e("SniffingUtil", "onSuccess(containsType)  --> " + url);
                mVideos.add(new SniffingVideo(mp4, ".mp4"));
                this.onSniffingSuccess(webView, url, mPosition, mVideos);
                this.onSniffingFinish(webView, url);
            }
        } else {
            parserNode(webView, url, new Node(html));
        }
    }

    private void parserNode(WebView webView, String url, Node node) {
        String video = node.attr("video", "src");
        String source = node.attr("source", "src");
        String iframe = node.attr("iframe", "src");
        List<Node> iframes = node.list("iframe");
        String type;
        if (!TextUtils.isEmpty(video) || !TextUtils.isEmpty(source)) {//找到了video url
            video = Util.warpUrl(mURL, TextUtils.isEmpty(video) ? source : video);
            if ((type = Util.containsType(video)) != null) {
                LogUtil.e("SniffingUtil", "onSuccess(containsType)  --> " + url);
                mVideos.add(new SniffingVideo(video, type));
                this.onSniffingSuccess(webView, url, mPosition, mVideos);
                this.onSniffingFinish(webView, url);
            } else {
                LogUtil.e("SniffingUtil", "onSuccess(ConnectionThread)  --> " + url);
                new ConnectionThread(webView, video, "unknow").start();
            }
        } else if (!TextUtils.isEmpty(iframe)) {//还需要请求一次
            if (iframes.size() <= 1 && mWebView != null) {
                LogUtil.e("SniffingUtil", "reLoadUrl(URL - 1)  --> " + iframe);
                if (mConnTimeout != null) {
                    mH.removeCallbacks(mConnTimeout);
                }
                mH.postDelayed(mConnTimeout = new TimeOutRunnable(webView, url, TYPE_CONN), mConnTimeOut);
                mHeader.put("Referer", webView.getUrl());
                mWebView.loadUrl(Util.warpUrl(mURL, iframe), mHeader);
            } else {
                String newIFrame = null;
                for (Node ifran : iframes) {
                    if (!TextUtils.isEmpty(newIFrame = Util.extracIframe(ifran)))
                        break;
                }
                if (TextUtils.isEmpty(newIFrame) && !TextUtils.isEmpty(iframe)) {
                    newIFrame = iframe;
                }
                if (!TextUtils.isEmpty(newIFrame)) {
                    String s = Util.warpUrl(mURL, newIFrame);
                    LogUtil.e("SniffingUtil", "reLoadUrl(URL - N)  --> " + s);
                    if (mConnTimeout != null) {
                        mH.removeCallbacks(mConnTimeout);
                    }
                    mH.postDelayed(mConnTimeout = new TimeOutRunnable(webView, url, TYPE_CONN), mConnTimeOut);
                    mHeader.put("Referer", webView.getUrl());
                    mWebView.loadUrl(s, mHeader);
                } else {
                    LogUtil.e("SniffingUtil", "onError(NOT FIND)  --> " + url);
                    this.onSniffingError(webView, url, mPosition, NOT_FIND);
                    this.onSniffingFinish(webView, url);
                }
            }
        } else {
            LogUtil.e("SniffingUtil", "onError(NOT FIND)  --> " + url);
            this.onSniffingError(webView, url, mPosition, NOT_FIND);
            this.onSniffingFinish(webView, url);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("http"))
            view.loadUrl(url, mHeader);
        return true;
    }

    public void setConnTimeOut(long connTimeOut) {
        this.mConnTimeOut = connTimeOut;
    }

    public void setFinishedTimeOut(long mFinishedTimeOut) {
        this.mFinishedTimeOut = mFinishedTimeOut;
    }

    public void setReadTimeOut(long readTimeOut) {
        this.mReadTimeOut = readTimeOut;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (mLastEndTime - mLastStartTime <= 500 || !isCompleteLoader) { // 基本上是302 重定向才会走这段逻辑
            LogUtil.e("SniffingUtil", "onStart( 302 )  --> " + url);
            if (mFinished != null) {
                mH.removeCallbacks(mFinished);
            }
            return;
        }
        if (this.mConnTimeout != null) {
            this.mH.removeCallbacks(this.mConnTimeout);
        }
        this.mH.postDelayed(this.mConnTimeout = new TimeOutRunnable(view, url, TYPE_CONN), mConnTimeOut);
        LogUtil.e("SniffingUtil", "onStart(onPageStarted)  --> " + url);
        this.onSniffingStart(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        mLastEndTime = System.currentTimeMillis();
        mH.postDelayed(mFinished = new FinishedRunnable(view, url), mFinishedTimeOut);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            LogUtil.e("SniffingUtil", "shouldInterceptRequest(URL)  --> " + url);
            if(url.lastIndexOf(".") < url.length() - 5){
                Object[] content = Util.getContent(url);
                String s = content[1].toString();
                if(s.toLowerCase().contains("video") || s.toLowerCase().contains("mpegurl")){
                    mVideos.add(new SniffingVideo(url,"m3u8",(int) content[0],"m3u8"));
                }
            }else if (mFilter != null) {
                SniffingVideo video = mFilter.onFilter(view, url);
                if (video != null) mVideos.add(video);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (!mVideos.isEmpty()) {
            LogUtil.e("SniffingUtil", "onReceivedError(SUCCESS)  --> " + failingUrl);
            SniffingWebViewClient.this.onSniffingSuccess(view, failingUrl, mPosition, mVideos);
            SniffingWebViewClient.this.onSniffingFinish(view, failingUrl);
        } else {
            LogUtil.e("SniffingUtil", "onReceivedError(ReceivedError)  --> " + failingUrl);
            this.onSniffingError(view, failingUrl, mPosition, RECEIVED_ERROR);
            this.onSniffingFinish(view, failingUrl);
        }
    }

    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        sslErrorHandler.proceed();//證書不對的時候，繼續加載
    }

    @Override
    public void onSniffingStart(final View webView, final String url) {
        this.isCompleteLoader = false;
        this.mLastStartTime = System.currentTimeMillis();
        this.mVideos.clear();

        if (this.mReadTimeout != null) {
            this.mH.removeCallbacks(this.mReadTimeout);
        }

        this.mH.postDelayed(this.mReadTimeout = new TimeOutRunnable((WebView) webView, url, TYPE_READ), mReadTimeOut);
        if (this.mCallback instanceof SniffingUICallback) {
            this.mH.post(new Runnable() {

                @Override
                public void run() {
                    ((SniffingUICallback) mCallback).onSniffingStart(webView, url);
                }

            });
        }
    }

    @Override
    public void onSniffingSuccess(final View webView, final String url, int position, final List<SniffingVideo> videos) {
        if (this.mCallback != null) {
            this.mH.post(new Runnable() {

                @Override
                public void run() {
                    mCallback.onSniffingSuccess(webView, url, position, videos);
                }

            });
        }
    }

    @Override
    public void onSniffingError(final View webView, final String url, int position, final int errorCode) {
        if (this.mCallback != null) {
            this.mH.post(new Runnable() {

                @Override
                public void run() {
                    mCallback.onSniffingError(webView, url, position, errorCode);
                }

            });
        }
    }

    @Override
    public void onSniffingFinish(final View webView, final String url) {
        this.isCompleteLoader = true;
        this.mH.removeCallbacks(mReadTimeout);
        this.mReadTimeout = null;
        if (this.mCallback instanceof SniffingUICallback) {
            this.mH.post(new Runnable() {

                @Override
                public void run() {
                    ((SniffingUICallback) mCallback).onSniffingFinish(webView, url);
                }

            });
        }
    }

    private class ParserHtmlRunnable implements Runnable {

        private WebView view;
        private String method;

        public ParserHtmlRunnable(WebView view, String method) {
            this.view = view;
            this.method = method;
        }

        @Override
        public void run() {
            Util.evalScript(view, method);
        }

    }

    //一次网页加载结束
    private class FinishedRunnable implements Runnable {

        private WebView view;
        private String url;

        public FinishedRunnable(WebView view, String url) {
            this.view = view;
            this.url = url;
        }

        @Override
        public void run() {
            if (mConnTimeout == null) return;
            mH.removeCallbacks(mConnTimeout);
            mConnTimeout = null;
            if (mVideos.isEmpty()) {
                LogUtil.e("SniffingUtil", "FinishedRunnable( postDelayed  【alert ，confirm】 )  --> " + url);

                mH.postDelayed(new ParserHtmlRunnable(view, "alert"), 5000);
                mH.postDelayed(mJSRunnable = new ParserHtmlRunnable(view, "confirm"), 6000);

                SniffingWebViewClient.this.onSniffingError(view, url, mPosition,  READ_TIME_OUT);
                SniffingWebViewClient.this.onSniffingFinish(view, url);
            } else {
                LogUtil.e("SniffingUtil", "FinishedRunnable( mVideos not Empty )  --> " + url);
                SniffingWebViewClient.this.onSniffingSuccess(view, url, mPosition, mVideos);
                SniffingWebViewClient.this.onSniffingFinish(view, url);
            }
        }

    }

    //一次网页加载，解析超时
    private class TimeOutRunnable implements Runnable {

        private WebView view;
        private String url;
        private int type;

        public TimeOutRunnable(WebView view, String url, int type) {
            this.view = view;
            this.url = url;
            this.type = type;
        }

        @Override
        public void run() {
            //加载网页超时了
            if (type == TYPE_CONN) {
                LogUtil.e("SniffingUtil", "ConnTimeOutRunnable( postDelayed  【alert ，confirm】 )  --> " + url);
                if (mConnTimeout == null) return;
                mH.removeCallbacks(mConnTimeout);
                mConnTimeout = null;

                mH.postDelayed(new ParserHtmlRunnable(view, "alert"), 5000);
                mH.postDelayed(mJSRunnable = new ParserHtmlRunnable(view, "confirm"), 8000);

                SniffingWebViewClient.this.onSniffingError(view, url, mPosition, READ_TIME_OUT);
                SniffingWebViewClient.this.onSniffingFinish(view, url);
            } else if (type == TYPE_READ) {
                if (!mVideos.isEmpty()) {
                    LogUtil.e("SniffingUtil", "ReadTimeOutRunnable(SUCCESS)  --> " + url);
                    SniffingWebViewClient.this.onSniffingSuccess(view, url, mPosition, mVideos);
                    SniffingWebViewClient.this.onSniffingFinish(view, url);
                } else {
                    LogUtil.e("SniffingUtil", "ReadTimeOutRunnable  --> " + url);
                    SniffingWebViewClient.this.onSniffingError(view, url, mPosition, READ_TIME_OUT);
                    SniffingWebViewClient.this.onSniffingFinish(view, url);
                }
            }
        }

    }

    private class ConnectionThread extends Thread {

        private String url;
        private String type;
        private WebView view;

        public ConnectionThread(WebView view, String url, String type) {
            this.view = view;
            this.url = url;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                Object[] content = Util.getContent(url);
                Object contentType = content[1];
                if (contentType == null) {
                    LogUtil.e("SniffingUtil", "onError(contentType == null)  --> " + url);
                    SniffingWebViewClient.this.onSniffingError(view, url, mPosition, CONTENT_ERROR);
                    SniffingWebViewClient.this.onSniffingFinish(view, url);
                } else if (contentType.toString().contains("html")) {
                    LogUtil.e("SniffingUtil", "RELOAD()  --> " + url);
                    if (mConnTimeout != null) {
                        mH.removeCallbacks(mConnTimeout);
                    }
                    mH.postDelayed(mConnTimeout = new TimeOutRunnable(view, url, TYPE_CONN), mConnTimeOut);
                    mHeader.put("Referer", mWebView.getUrl());
                    mWebView.loadUrl(Util.warpUrl(mURL, url), mHeader);
                } else if (contentType.toString().contains("video") || contentType.toString().contains("mpegurl")) {
                    LogUtil.e("SniffingUtil", "onSuccess(mpegurl video)  --> " + url);
                    mVideos.add(new SniffingVideo(url, type, (int) content[0], contentType.toString()));
                    SniffingWebViewClient.this.onSniffingSuccess(view, url, mPosition, mVideos);
                    SniffingWebViewClient.this.onSniffingFinish(view, url);
                }
            } catch (Throwable e) {
                LogUtil.e("SniffingUtil", "onError(Throwable)  --> " + url);
                SniffingWebViewClient.this.onSniffingError(view, url, mPosition, CONNECTION_ERROR);
                SniffingWebViewClient.this.onSniffingFinish(view, url);
            }
        }

    }

}