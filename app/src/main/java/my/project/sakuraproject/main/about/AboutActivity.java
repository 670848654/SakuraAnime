package my.project.sakuraproject.main.about;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.net.DownloadUtil;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.StatusBarUtil;
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
    @BindView(R.id.open_source)
    TextView open_source;
    @BindView(R.id.version)
    TextView version;
    private ProgressDialog p;
    private String downloadUrl;
    private Call downCall;

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
        StatusBarUtil.setColorForSwipeBack(AboutActivity.this, getResources().getColor(R.color.night), 0);
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initViews();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar.setTitle("关于");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void initViews() {
        version.setText(Utils.getASVersionName());
        cache.setText(Environment.getExternalStorageDirectory() + Utils.getString(R.string.cache_text));
        open_source.setOnClickListener(v -> {
            if (Utils.isFastClick())
                startActivity(new Intent(AboutActivity.this, OpenSourceActivity.class));
        });
    }

    @OnClick(R.id.dilidili)
    public void openDilidili() {
        Utils.viewInChrome(this, Sakura.DOMAIN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.check_update) {
            if (Utils.isFastClick()) checkUpdate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkUpdate() {
        p = Utils.getProDialog(this, R.string.check_update_text);
        new Handler().postDelayed(() -> new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> application.showSnackbarMsg(toolbar, "连接服务器超时", "重试", view -> checkUpdate()));
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
                            application.showToastMsg("没有新版本");
                        });
                    else {
                        downloadUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        String body = obj.getString("body");
                        runOnUiThread(() -> {
                            Utils.cancelProDialog(p);
                            Utils.findNewVersion(AboutActivity.this,
                                    newVersion,
                                    body,
                                    (dialog, which) -> {
                                        p = Utils.showProgressDialog(AboutActivity.this);
                                        p.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", (dialog1, which1) -> {
                                            if (null != downCall)
                                                downCall.cancel();
                                            dialog1.dismiss();
                                        });
                                        p.show();
                                        downNewVersion(downloadUrl);
                                    },
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                    });
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }), 1000);
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
                    application.showSnackbarMsg(toolbar, "下载失败~");
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
