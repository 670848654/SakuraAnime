package my.project.sakuraproject.main.start;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.home.HomeActivity;
import my.project.sakuraproject.net.HttpGet;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StartActivity extends BaseActivity {
    @BindView(R.id.check_update)
    LinearLayout linearLayout;
    private ProgressDialog p;
    private String downUrl;

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_start;
    }

    @Override
    protected void init() {
        SharedPreferencesUtils.setParam(this, "initX5", "init");
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            linearLayout.setVisibility(View.VISIBLE);
            checkUpdate();
        }, 1000);
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, getResources().getColor(R.color.logo_bg));
    }

    private void checkUpdate() {
        new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    application.showErrorToastMsg(Utils.getString(R.string.ck_network_error_start));
                    openMain();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                try {
                    JSONObject obj = new JSONObject(json);
                    String newVersion = obj.getString("tag_name");
                    if (newVersion.equals(Utils.getASVersionName()))
                        runOnUiThread(() -> openMain());
                    else {
                        downUrl = obj.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        String body = obj.getString("body");
                        runOnUiThread(() -> Utils.findNewVersion(StartActivity.this,
                                newVersion,
                                body,
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    Utils.putTextIntoClip(downUrl);
                                    application.showSuccessToastMsg(Utils.getString(R.string.url_copied));
                                    Utils.openBrowser(StartActivity.this, downUrl);
                                },
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    openMain();
                                })
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    application.showErrorToastMsg(Utils.getString(R.string.ck_error_start));
                    openMain();
                }
            }
        });
    }

    private void openMain() {
        startActivity(new Intent(StartActivity.this, HomeActivity.class));
        StartActivity.this.finish();
    }
}
