package my.project.sakuraproject.sniffing.web;

import android.app.Activity;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.project.sakuraproject.sniffing.DefaultFilter;
import my.project.sakuraproject.sniffing.SniffingCallback;
import my.project.sakuraproject.sniffing.SniffingFilter;
import my.project.sakuraproject.sniffing.SniffingUICallback;
import my.project.sakuraproject.sniffing.SniffingVideo;

public class SniffingUtil implements SniffingUICallback {

    private static SniffingUtil mSniffingUtil;
    public static boolean WEB_VIEW_DEBUG = false;
    private WebView mWebView;

    private String mUrl;
    private int mPosition;
    private boolean mCallbackChange = false;
    private SniffingCallback mCallback;
    private Map<String, String> mHeader;
    private SoftReference<Activity> mActivity;
    private SniffingFilter mFilter = new DefaultFilter();
    private long mConnTimeOut = 20 * 1000;
    private long mReadTimeOut = 45 * 1000;
    private long mFinishedTimeOut = 800;
    private SniffingWebViewClient mClient = null;
    private boolean mAutoRelease = false;

    private SniffingUtil() {
    }

    public static SniffingUtil get() {
        if (mSniffingUtil == null) {
            synchronized (SniffingUtil.class) {
                if (mSniffingUtil == null) {
                    mSniffingUtil = new SniffingUtil();
                }
            }
        }
        return mSniffingUtil;
    }

    public synchronized SniffingUtil setFinishedTimeOut(long mFinishedTimeOut) {
        this.mFinishedTimeOut = mFinishedTimeOut;
        return this;
    }

    public SniffingUtil autoRelease(boolean mAutoRelease) {
        this.mAutoRelease = mAutoRelease;
        return this;
    }

    public synchronized void releaseWebView() {
        try {
            if (mWebView != null) {
                mWebView.removeAllViews();
                if (mWebView.getParent() != null) {
                    ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
                    viewGroup.removeView(mWebView);
                }
                mWebView.destroy();
            }
            mWebView = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void release() {
        mHeader = null;
        mActivity = null;
        mFilter = new DefaultFilter();
    }

    public synchronized SniffingUtil referer(String referer) {
        if (mHeader == null) mHeader = new HashMap<>();
        mHeader.put("Referer", referer);
        mHeader.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.79 Mobile Safari/537.36");
        mHeader.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        mHeader.put("Accept-Encoding", "gzip, deflate");
        return this;
    }

    public synchronized void releaseAll() {
        releaseWebView();
        release();
    }

    public synchronized void start() {
        try {
            if (mActivity == null) {
                if(mAutoRelease){
                    onSniffingError(mWebView, mUrl, mPosition, -1);
                }else if (mCallback != null) {
                    mCallback.onSniffingError(mWebView, mUrl, mPosition, -1);
                }
                return;
            }
            if (mHeader == null) {
                mHeader = new HashMap<>();
            }
            Activity activity = mActivity.get();
            if (mWebView == null && activity != null) {
                mCallbackChange = true;
                mWebView = new SniffingWebView(activity);
            }
            if (mCallbackChange && mWebView != null) {
                mCallbackChange = false;
                SniffingCallback callback = mAutoRelease ? this : mCallback;
                mClient = new SniffingWebViewClient(mWebView, mUrl, mPosition, mHeader, mFilter, callback);
                mClient.setReadTimeOut(mReadTimeOut);
                mClient.setConnTimeOut(mConnTimeOut);
                mClient.setFinishedTimeOut(mFinishedTimeOut);
                SniffingWebChromeClient chromeClient = new SniffingWebChromeClient(mClient);
                mWebView.setWebViewClient(mClient);
                mWebView.setWebChromeClient(chromeClient);
            }
            if (mWebView != null && activity != null) {
                if (mWebView.getParent() == null) {
                    ViewGroup mainView = (ViewGroup) activity.findViewById(android.R.id.content);
                    if(WEB_VIEW_DEBUG){
                        Display display = activity.getWindowManager().getDefaultDisplay();
                        mWebView.setLayoutParams(new ViewGroup.LayoutParams(display.getWidth() / 2, display.getHeight() / 2));
                    }else{
                        mWebView.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
                    }
                    mainView.addView(mWebView);
                }
                mWebView.loadUrl(mUrl, mHeader);
            } else {
                if(mAutoRelease){
                    onSniffingError(mWebView, mUrl, mPosition, -1);
                }else if (mCallback != null) {
                    mCallback.onSniffingError(mWebView, mUrl, mPosition, -1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(mAutoRelease){
                onSniffingError(mWebView, mUrl, mPosition, -1);
            }else if (mCallback != null) {
                mCallback.onSniffingError(mWebView, mUrl, mPosition, -1);
            }
        }
    }

    public SniffingUtil url(String url) {
        this.mUrl = url;
        return this;
    }

    public SniffingUtil position(int position) {
        this.mPosition = position;
        return this;
    }

    public SniffingUtil activity(Activity activity) {
        this.mActivity = new SoftReference<>(activity);
        return this;
    }

    public SniffingUtil header(Map<String, String> map) {
        this.mHeader = map;
        return this;
    }

    public SniffingUtil filter(SniffingFilter filter) {
        this.mFilter = filter;
        this.mCallbackChange = true;
        return this;
    }

    public SniffingUtil callback(SniffingCallback callback) {
        this.mCallback = callback;
        this.mCallbackChange = true;
        return this;
    }

    public SniffingUtil connTimeOut(long connTimeOut) {
        this.mConnTimeOut = connTimeOut;
        if (mClient != null)
            mClient.setConnTimeOut(connTimeOut);
        return this;
    }

    public SniffingUtil readTimeOut(long readTimeOut) {
        this.mReadTimeOut = readTimeOut;
        if (mClient != null)
            mClient.setReadTimeOut(readTimeOut);
        return this;
    }

    @Override
    public void onSniffingSuccess(View webView, String url, int position, List<SniffingVideo> videos) {
        if (mCallback != null) {
            mCallback.onSniffingSuccess(webView, url, position, videos);
        }
    }

    @Override
    public void onSniffingError(View webView, String url, int position, int errorCode) {
        if (mCallback != null) {
            mCallback.onSniffingError(webView, url, position, errorCode);
        }
    }

    @Override
    public void onSniffingStart(View webView, String url) {
        if (mCallback instanceof SniffingUICallback) {
            ((SniffingUICallback)mCallback).onSniffingStart(webView, url);
        }
    }

    @Override
    public void onSniffingFinish(View webView, String url) {
        if (mCallback instanceof SniffingUICallback) {
            ((SniffingUICallback)mCallback).onSniffingFinish(webView, url);
        }
        releaseAll();
    }
}