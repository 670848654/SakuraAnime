package my.project.sakuraproject.main.webview.normal;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.Utils;

public class DefaultNormalWebActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String url;
    @BindView(R.id.webview)
    NormalWebView normalWebView;
    @BindView(R.id.progressBar)
    ProgressBar pg;
    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private Boolean isFullscreen = false;
    @BindView(R.id.activity_main)
    LinearLayout linearLayout;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {}

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_default_webview_normal;
    }

    @Override
    protected void init() {
        initToolbar();
        getBundle();
        initWebView();
    }

    @Override
    protected void initBeforeView() {
    }

    public void initToolbar() {
        toolbar.setTitle("网页播放");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            url = bundle.getString("url");
        }
    }

    public void initWebView() {
        normalWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
            params.setMargins(0,
                    0,
                    0,
                    Utils.getNavigationBarHeight(this));
            linearLayout.setLayoutParams(params);
        }

        normalWebView.loadUrl(url);
        initHardwareAccelerate();
        normalWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pg.setVisibility(View.GONE);
                } else {
                    pg.setVisibility(View.VISIBLE);
                    pg.setProgress(newProgress);
                }
            }

            //*** 视频播放相关的方法 **//*
            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(DefaultNormalWebActivity.this);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });
    }

    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        DefaultNormalWebActivity.this.getWindow().getDecorView();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new DefaultNormalWebActivity.FullscreenHolder(DefaultNormalWebActivity.this);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        isFullscreen = true;
        hideNavBar();
        customViewCallback = callback;
        // 设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    /**
     * 隐藏视频全屏
     */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }
        isFullscreen = false;
        showNavBar();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        normalWebView.setVisibility(View.VISIBLE);
        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    /**
     * 启用硬件加速
     */
    private void initHardwareAccelerate() {
        try {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) hideCustomView();
        else finish();
    }

    //销毁Webview
    @Override
    protected void onDestroy() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //释放资源
        if (normalWebView != null)
            normalWebView.destroy();
        super.onDestroy();
    }

    @Override
    protected void setConfigurationChanged() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFullscreen) hideNavBar();
        else showNavBar();
    }
}
