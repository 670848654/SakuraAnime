package my.project.sakuraproject.main.about;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.r0adkll.slidr.Slidr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.LogAdapter;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.LogBean;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.net.DownloadUtil;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.SwipeBackLayoutUtil;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AboutActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cache)
    TextView cache;
    @BindView(R.id.version)
    TextView version;
    private ProgressDialog p;
    private String downloadUrl;
    private Call downCall;
    @BindView(R.id.footer)
    LinearLayout footer;
    @BindView(R.id.show)
    CoordinatorLayout show;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_about;
    }

    @Override
    protected void init() {
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.about));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initViews() {
        LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Utils.getNavigationBarHeight(this));
        footer.findViewById(R.id.footer).setLayoutParams(Params);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) show.getLayoutParams();
        params.setMargins(10, 0, 10, Utils.getNavigationBarHeight(this) - 5);
        show.setLayoutParams(params);
        version.setText(Utils.getASVersionName());
        cache.setText(Environment.getExternalStorageDirectory() + Utils.getString(R.string.cache_text));
    }

    @OnClick({R.id.sakura,R.id.github, R.id.check_update})
    public void openBrowser(RelativeLayout relativeLayout) {
        switch (relativeLayout.getId()) {
            case R.id.sakura:
                Utils.viewInChrome(this, Sakura.DOMAIN);
                break;
            case R.id.github:
                Utils.viewInChrome(this, Utils.getString(R.string.github_url));
                break;
            case R.id.check_update:
                if (Utils.isFastClick()) checkUpdate();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_menu, menu);
        MenuItem updateLogItem = menu.findItem(R.id.update_log);
        MenuItem openSourceItem = menu.findItem(R.id.open_source);
        if (!(Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false)) {
            updateLogItem.setIcon(R.drawable.baseline_insert_chart_outlined_black_48dp);
            openSourceItem.setIcon(R.drawable.baseline_all_inclusive_black_48dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_log:
                showUpdateLogs();
                break;
            case R.id.open_source:
                if (Utils.isFastClick()) startActivity(new Intent(AboutActivity.this,OpenSourceActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showUpdateLogs() {
        AlertDialog alertDialog;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_log, null);
        RecyclerView logs = view.findViewById(R.id.rv_list);
        logs.setLayoutManager(new LinearLayoutManager(this));
        LogAdapter logAdapter = new LogAdapter(createUpdateLogList());
        logs.setAdapter(logAdapter);
        builder.setPositiveButton(Utils.getString(R.string.page_positive), null);
        TextView title = new TextView(this);
        title.setText(Utils.getString(R.string.update_log));
        title.setPadding(30,30,30,30);
        title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        title.setGravity(Gravity.LEFT);
        title.setTextSize(18);
        title.setTextColor(getResources().getColor(R.color.text_color_primary));
        builder.setCustomTitle(title);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
    }

    public List createUpdateLogList() {
        List logsList = new ArrayList();
        logsList.add(new LogBean("版本：1.9.6_1", "域名变更为http://www.yhdm.io"));
        logsList.add(new LogBean("版本：1.9.6", "动漫分类界面改动\n内置播放器快进、后退参数可设置（5s，10s，15s，30s），播放器界面点击“设置”图标，在弹窗界面中配置"));
        logsList.add(new LogBean("版本：1.9.5", "修复部分设备（平板）无法正常播放的问题"));
        logsList.add(new LogBean("版本：1.9.4_redirection_fix1", "再次尝试修复由于网站重定向导致某些获数据取异常的Bug ┐(´д｀)┌"));
        logsList.add(new LogBean("版本：1.9.4", "尝试修复由于网站重定向导致获数据取异常的Bug"));
        logsList.add(new LogBean("版本：1.9.3", "修复番剧详情加载失败闪退Bug"));
        logsList.add(new LogBean("版本：1.9.2", "番剧详情界面布局修改"));
        logsList.add(new LogBean("版本：1.9.1", "修正动漫分类列表"));
        logsList.add(new LogBean("版本：1.9.0", "修复一些Bug\n优化番剧详情界面\n内置播放器新增屏幕锁定、快进、后退操作"));
        logsList.add(new LogBean("版本：1.8.9","修复解析时弹窗不关闭的问题"));
        logsList.add(new LogBean("版本：1.8.8","修复已知问题"));
        logsList.add(new LogBean("版本：1.8.7","部分界面UI改动\n修复番剧详情界面显示问题"));
        logsList.add(new LogBean("版本：1.8.6_b","修复内置播放器播放完毕后程序崩溃的问题"));
        logsList.add(new LogBean("版本：1.8.6_a","修复内置播放器使用Exo内核无限加载的问题"));
        logsList.add(new LogBean("版本：1.8.6","修复一些错误\n修复内置视频播放器存在的一些问题"));
        logsList.add(new LogBean("版本：1.8.5","修复新解析方案资源未释放导致视频声音外放的Bug"));
        logsList.add(new LogBean("版本：1.8.4","修复视频播放器白额头的Bug\n增加新的解析方案，尽量减少使用webView（Test）"));
        logsList.add(new LogBean("版本：1.8.3","修复一些Bug"));
        logsList.add(new LogBean("版本：1.8.2","默认禁用X5内核，X5内核更新后会导致应用闪退（Android 10)，你可以在自定义设置中打开，若发生闪退则关闭该选项"));
        logsList.add(new LogBean("版本：1.8.1","修复某些设备导航栏的显示问题"));
        logsList.add(new LogBean("版本：1.8","修复一些Bugs\n修正部分界面布局\n适配沉浸式导航栏《仅支持原生导航栏，第三方魔改UI无效》（Test）"));
        logsList.add(new LogBean("版本：1.7","修复一些Bugs\n修正部分界面布局\n新增亮色主题（Test）"));
        logsList.add(new LogBean("版本：1.6","修复更新SDK后导致崩溃的严重问题"));
        logsList.add(new LogBean("版本：1.5","升级SDK版本为29（Android 10）"));
        logsList.add(new LogBean("版本：1.4","修复搜索界面无法加载更多动漫的Bug"));
        logsList.add(new LogBean("版本：1.3","部分UI变更，优化体验\n修复存在的一些问题"));
        logsList.add(new LogBean("版本：1.2","修复番剧详情剧集列表的一个显示错误\n新增视频播放解析方法，通过目前使用情况，理论上能正常播放樱花动漫网站大部分视频啦（有一些无法播放的视频绝大多数是网站自身原因）"));
        logsList.add(new LogBean("版本：1.1","修复一个Bug\n修复一个显示错误\n修复部分番剧无法正常播放的问题（兼容性待测试）"));
        logsList.add(new LogBean("版本：1.0","第一个版本（业余时间花了两个下午时间编写，可能存在许多Bug~）"));
        return logsList;
    }

    public void checkUpdate() {
        p = Utils.getProDialog(this, R.string.check_update_text);
        new Handler().postDelayed(() -> new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Utils.cancelProDialog(p);
                    application.showSnackbarMsgAction(show, Utils.getString(R.string.ck_network_error_start), Utils.getString(R.string.try_again), v -> checkUpdate());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String newVersion = obj.getString("tag_name");
                    if (newVersion.equals(Utils.getASVersionName()))
                        runOnUiThread(() -> {
                            Utils.cancelProDialog(p);
                            application.showSnackbarMsg(show, Utils.getString(R.string.no_new_version));
                        });
                    else {
                        downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        String body = obj.getString("body");
                        runOnUiThread(() -> {
                            Utils.cancelProDialog(p);
                            Utils.findNewVersion(AboutActivity.this,
                                    newVersion,
                                    body,
                                    (dialog, which) -> download(),
                                    (dialog, which) -> dialog.dismiss()
                            );
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }), 1000);
    }

    public void download() {
        p = Utils.showProgressDialog(AboutActivity.this);
        p.setButton(ProgressDialog.BUTTON_NEGATIVE, Utils.getString(R.string.page_negative), (dialog1, which1) -> {
            if (null != downCall)
                downCall.cancel();
            dialog1.dismiss();
        });
        p.show();
        downNewVersion(downloadUrl);
    }

    /**
     * 下载apk
     *
     * @param url 下载地址
     */
    private void downNewVersion(String url) {
        downCall = DownloadUtil.get().downloadApk(url, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(final String fileName) {
                runOnUiThread(() -> {
                    Utils.cancelProDialog(p);
                    Utils.startInstall(AboutActivity.this);
                });
            }

            @Override
            public void onDownloading(final int progress) {
                runOnUiThread(() -> p.setProgress(progress));
            }

            @Override
            public void onDownloadFailed() {
                runOnUiThread(() -> {
                    Utils.cancelProDialog(p);
                    application.showSnackbarMsgAction(show, Utils.getString(R.string.download_error), Utils.getString(R.string.try_again), v -> download());
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001) {
            Utils.startInstall(AboutActivity.this);
        }
    }
}
