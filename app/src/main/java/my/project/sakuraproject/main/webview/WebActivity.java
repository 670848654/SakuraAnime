package my.project.sakuraproject.main.webview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.DramaAdapter;
import my.project.sakuraproject.adapter.WebviewAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescBean;
import my.project.sakuraproject.bean.ApiBean;
import my.project.sakuraproject.bean.WebviewBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.video.VideoContract;
import my.project.sakuraproject.main.video.VideoPresenter;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;
import my.project.sakuraproject.util.VideoUtils;

public class WebActivity extends BaseActivity implements VideoContract.View {
    private final static String PC_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36";
    private final static String PHONE_USER_AGENT = "Mozilla/5.0 (Linux; Android 9; ONEPLUS A6010 Build/PKQ1.180716.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.158 Mobile Safari/537.36";
    private final static String REFERER = "referer";
    private List<WebviewBean> list = new ArrayList<>();
    private String url = "", diliUrl = "";
    private String animeTitle;
    private String witchTitle;
    private String api = Api.SOURCE_1_API;
    private String newUrl = "";
    @BindView(R.id.x5_webview)
    X5WebView mX5WebView;
    private ProgressBar pg;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private List<AnimeDescBean> dramaList = new ArrayList<>();
    private DramaAdapter dramaAdapter;
    private BottomSheetDialog mBottomSheetDialog;
    private ProgressDialog p;
    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private IX5WebChromeClient.CustomViewCallback customViewCallback;
    private VideoPresenter presenter;
    private List<ApiBean> apiList;
    private com.tencent.smtt.sdk.WebSettings webSettings;
    private boolean mModel = false;
    private MenuItem menuItem;
    private Boolean isFullscreen = false;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_webview;
    }

    @Override
    protected void init() {
        hideGap();
        getBundle();
        initToolbar();
        initView();
        initApiData();
        initAdapter();
        initWebView();
    }

    @Override
    protected void initBeforeView() {
    }

    public void initToolbar() {
        toolbar.setTitle(witchTitle);
        toolbar.setSubtitle("loading...");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void getBundle() {
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            witchTitle = bundle.getString("witchTitle");
            animeTitle = bundle.getString("title");
            url = bundle.getString("url");
            diliUrl = bundle.getString("dili");
            dramaList = (List<AnimeDescBean>) bundle.getSerializable("list");
        }
    }

    public void initView() {
        pg = findViewById(R.id.progressBar);
    }

    private void initApiData() {
        apiList = DatabaseUtil.queryAllApi();
    }

    public void initAdapter() {
        list.add(new WebviewBean(Utils.getString(R.string.source_1), Api.SOURCE_1_API, true, false, false));
        list.add(new WebviewBean(Utils.getString(R.string.source_2), Api.SOURCE_2_API, false, false, false));
        list.add(new WebviewBean(Utils.getString(R.string.source_3), Api.SOURCE_3_API, false, false, false));
        list.add(new WebviewBean(Utils.getString(R.string.source_4), Api.SOURCE_4_API, false, false, false));
        list.add(new WebviewBean(Utils.getString(R.string.source_5), Api.SOURCE_5_API, false, false, false));
        list.add(new WebviewBean(Utils.getString(R.string.source_6), Api.SOURCE_6_API, false, false, false));
        if (apiList.size() > 0) {
            for (int i = 0; i < apiList.size(); i++) {
                list.add(new WebviewBean(apiList.get(i).getTitle(), apiList.get(i).getUrl(), false, false, false));
            }
        }
        list.add(new WebviewBean(Utils.getString(R.string.source_8), "", false, true, false));
        list.add(new WebviewBean(Utils.getString(R.string.source_9), "", false, false, true));
        View dramaView = LayoutInflater.from(this).inflate(R.layout.dialog_webview, null);
        RecyclerView lineRecyclerView = dramaView.findViewById(R.id.line_list);
        lineRecyclerView.setNestedScrollingEnabled(false);
        lineRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        WebviewAdapter webviewAdapter = new WebviewAdapter(this, list);
        webviewAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (list.get(position).isOriginalPage()) {
                Utils.viewInChrome(WebActivity.this, diliUrl);
            } else if (list.get(position).isOriginalAddress()) {
                Utils.viewInChrome(WebActivity.this, url);
            } else {
                mBottomSheetDialog.dismiss();
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setSelect(false);
                }
                list.get(position).setSelect(true);
                adapter.notifyDataSetChanged();
                Map<String, String> map = new HashMap<>();
                map.put(REFERER, diliUrl);
                api = list.get(position).getUrl();
                newUrl = api + url;
                mX5WebView.loadUrl(newUrl, map);
            }
        });
        lineRecyclerView.setAdapter(webviewAdapter);
        RecyclerView dramaRecyclerView = dramaView.findViewById(R.id.drama_list);
        dramaRecyclerView.setNestedScrollingEnabled(false);
        TextView titleTextView = dramaView.findViewById(R.id.title);
        titleTextView.setText(animeTitle);
        dramaRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        dramaAdapter = new DramaAdapter(this, dramaList);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            setResult(0x20);
            mBottomSheetDialog.dismiss();
            final AnimeDescBean bean = (AnimeDescBean) adapter.getItem(position);
            switch (bean.getType()) {
                case "play":
                    p = Utils.getProDialog(WebActivity.this, R.string.parsing);
                    Button v = (Button) adapter.getViewByPosition(dramaRecyclerView, position, R.id.tag_group);
                    v.setBackgroundResource(R.drawable.button_selected);
                    diliUrl = VideoUtils.getUrl(bean.getUrl());
                    witchTitle = animeTitle + " - " + bean.getTitle();
                    presenter = new VideoPresenter(animeTitle, diliUrl, WebActivity.this);
                    presenter.loadData(true);
                    break;
            }

        });
        dramaRecyclerView.setAdapter(dramaAdapter);
        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(dramaView);
    }

    public void goToPlay(List<String> list) {
        if (list.size() == 1) oneSource(list);
        else multipleSource(list);
    }

    public void initWebView() {
        webSettings = mX5WebView.getSettings();
        webSettings.setUserAgentString(PHONE_USER_AGENT);
        getWindow().getDecorView().addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            ArrayList<View> outView = new ArrayList<>();
            getWindow().getDecorView().findViewsWithText(outView, "下载该视频", View.FIND_VIEWS_WITH_TEXT);
            if (outView != null && outView.size() > 0) {
                outView.get(0).setVisibility(View.GONE);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mX5WebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        //需要升级complieversion为21以上，不升级的话，用反射的方式来实现，如下代码所示
        try {
            Method m = WebSettings.class.getMethod("setMixedContentMode", int.class);
            if (m == null) {
                Log.e("WebSettings", "Error getting setMixedContentMode method");
            } else {
                m.invoke(mX5WebView.getSettings(), 2); // 2 = MIXED_CONTENT_COMPATIBILITY_MODE
                Log.i("WebSettings", "Successfully set MIXED_CONTENT_COMPATIBILITY_MODE");
            }
        } catch (Exception ex) {
            Log.e("WebSettings", "Error calling setMixedContentMode: " + ex.getMessage(), ex);
        }
        //视频源地址
        URL urlHost;
        try {
            urlHost = new URL(url);
            toolbar.setSubtitle(urlHost.getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        newUrl = Api.SOURCE_1_API + url;
        mX5WebView.loadUrl(newUrl);
        initHardwareAccelerate();
        if (null != mX5WebView.getX5WebViewExtension()) {
            Bundle data = new Bundle();
            data.putBoolean("standardFullScreen", false);
            //true表示标准全屏，false表示X5全屏；不设置默认false，
            data.putBoolean("supportLiteWnd", false);
            //false：关闭小窗；true：开启小窗；不设置默认true，
            data.putInt("DefaultVideoScreen", 2);
            //1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
            mX5WebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);
        } else application.showToastMsg("X5内核加载失败,切换到系统内核");
        mX5WebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pg.setVisibility(View.GONE);
                } else {
                    pg.setVisibility(View.VISIBLE);
                    pg.setProgress(newProgress);
                }
            }

            /** 视频播放相关的方法 **/
            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(WebActivity.this);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
                showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideFullCustomView();
            }
        });
    }

    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        WebActivity.this.getWindow().getDecorView();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(WebActivity.this);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        isFullscreen = true;
        hideNavBar();
        customViewCallback = callback;
        // 设置横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * 隐藏视频全屏
     */
    private void hideFullCustomView() {
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
        mX5WebView.setVisibility(View.VISIBLE);
        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    @Override
    public void cancelDialog() {
        Utils.cancelProDialog(p);
    }

    @Override
    public void getVideoSuccess(List<String> list) {
        runOnUiThread(() -> goToPlay(list));
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> VideoUtils.showErrorInfo(WebActivity.this, diliUrl));
    }

    @Override
    public void getVideoError() {
        //网络出错
        runOnUiThread(() -> application.showToastMsg(Utils.getString(R.string.error_700)));
    }

    @Override
    public void showSuccessDramaView(List<AnimeDescBean> list) {
        dramaList = list;
        runOnUiThread(() -> dramaAdapter.setNewData(dramaList));
    }

    @Override
    public void errorDramaView() {
        runOnUiThread(() -> application.showToastMsg("获取剧集信息出错"));
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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mX5WebView.canGoBack()) mX5WebView.goBack();//返回上个页面
        else finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFullscreen) hideNavBar();
        else showNavBar();
    }

    @Override
    protected void onDestroy() {
        //销毁Webview
        if (mX5WebView != null)
            mX5WebView.destroy();
        if (null != presenter)
            presenter.detachView();
        Utils.deleteAllFiles(new File(android.os.Environment.getExternalStorageDirectory() + "/Android/data/my.project.sakuraproject/cache"));
        Utils.deleteAllFiles(new File(android.os.Environment.getExternalStorageDirectory() + "/Android/data/my.project.sakuraproject/files/VideoCache/main"));
        super.onDestroy();
    }


    /**
     * 只有一个播放地址
     *
     * @param list
     */
    private void oneSource(List<String> list) {
        playAnime(VideoUtils.getVideoUrl(list.get(0)));
    }

    /**
     * 多个播放地址
     *
     * @param list
     */
    private void multipleSource(List<String> list) {
        VideoUtils.showMultipleVideoSources(this,
                list,
                (dialog, index) ->
                        playAnime(VideoUtils.getVideoUrl(list.get(index)))
        );
    }

    private void playAnime(String animeUrl) {
        url = animeUrl;
        if (Patterns.WEB_URL.matcher(animeUrl.replace(" ", "")).matches()) {
            if (animeUrl.contains("jx.618g.com")) {
                url = animeUrl.replaceAll("http://jx.618g.com/\\?url=", "");
                loadUrl();
            } else if (animeUrl.contains(".mp4") || animeUrl.contains(".m3u8")) {
                switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
                    case 0:
                        //调用播放器
                        VideoUtils.openPlayer(false, WebActivity.this, witchTitle, url, animeTitle, diliUrl, dramaList);
                        break;
                    case 1:
                        Utils.selectVideoPlayer(this, url);
                        break;
                }
            }else {
                Sakura.getInstance().showToastMsg("该播放地址貌似应该使用网页播放！？");
                startActivity(new Intent(WebActivity.this, DefaultWebActivity.class).putExtra("url", url));
            }
        }  else {
            Sakura.getInstance().showToastMsg("注意：该播放地址可能无法正常播放！");
            startActivity(new Intent(WebActivity.this, DefaultWebActivity.class).putExtra("url",String.format(Api.PARSE_API, url)));
        }
    }

    /**
     * 加载新地址
     */
    private void loadUrl() {
        toolbar.setTitle(witchTitle);
        //视频源地址
        URL urlHost;
        try {
            urlHost = new URL(url);
            toolbar.setSubtitle(urlHost.getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        newUrl = api + url;
        mX5WebView.loadUrl(newUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_menu, menu);
        menuItem = menu.findItem(R.id.model);
        return true;
    }

    @OnClick(R.id.drama)
    public void dramaClick() {
        if (!mBottomSheetDialog.isShowing()) mBottomSheetDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.model:
                if (mModel) {
                    //切换成手机版
                    mModel = false;
                    webSettings.setUserAgent(PHONE_USER_AGENT);
                    menuItem.setIcon(R.drawable.baseline_stay_primary_portrait_white_48dp);
                    menuItem.setTitle(Utils.getString(R.string.phone_model));
                    application.showToastMsg("已切换成手机版");
                } else {
                    //切换成电脑版
                    mModel = true;
                    webSettings.setUserAgent(PC_USER_AGENT);
                    menuItem.setIcon(R.drawable.baseline_language_white_48dp);
                    menuItem.setTitle(Utils.getString(R.string.pc_model));
                    application.showToastMsg("已切换成电脑版");
                }
                Map<String, String> map = new HashMap<>();
                map.put(REFERER, diliUrl);
                mX5WebView.loadUrl(newUrl, map);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
