package my.project.sakuraproject.main.start;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import androidx.core.splashscreen.SplashScreen;
import butterknife.BindView;
import my.project.sakuraproject.R;
import my.project.sakuraproject.api.Api;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.main.home.MainActivity;
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
    private String downUrl;
    private SplashScreen splashScreen;
    private boolean keep = true;
    private final int DELAY = 1000;

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
        //Keep returning false to Should Keep On Screen until ready to begin.
        splashScreen.setKeepVisibleCondition((SplashScreen.KeepOnScreenCondition) () -> keep);
        Handler handler = new Handler();
        handler.postDelayed(runner, DELAY);
        splashScreen.setOnExitAnimationListener(splashScreenView -> {
            Path path = new Path();
            path.moveTo(1.0f, 1.0f);
            path.lineTo(0f, 0f);
            final ObjectAnimator scaleOut = ObjectAnimator.ofFloat(
                    splashScreenView.getIconView(),
                    View.SCALE_X,
                    View.SCALE_Y,
                    path
            );
            scaleOut.setInterpolator(new AnticipateInterpolator());
            scaleOut.setDuration(200L);

            // Call SplashScreenView.remove at the end of your custom animation.
            scaleOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashScreenView.remove();
                }
            });

            // Run your animation.
            scaleOut.start();
        });
    }

    /**Will cause a second process to run on the main thread**/
    private final Runnable runner = () -> {
        keep = false;
        if ((Integer) SharedPreferencesUtils.getParam(this, "start_check_update", 0) == 0) {
            new Handler().postDelayed(() -> {
                linearLayout.setVisibility(View.VISIBLE);
                checkUpdate();
            }, 1000);
        } else
            openMain();
    };

    @Override
    protected void initBeforeView() {
        splashScreen = SplashScreen.installSplashScreen(this);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.logo_bg));
    }

    @Override
    protected void setConfigurationChanged() {

    }

    private void checkUpdate() {
        new HttpGet(Api.CHECK_UPDATE, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
//                    application.showErrorToastMsg(Utils.getString(R.string.ck_network_error_start));
                    CustomToast.showToast(StartActivity.this, Utils.getString(R.string.ck_network_error_start), CustomToast.ERROR);
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
                        runOnUiThread(() ->
                        Utils.showAlert(StartActivity.this,
                                getString(R.string.find_new_version) + newVersion,
                                body,
                                false,
                                getString(R.string.update_now),
                                getString(R.string.update_after),
                                null,
                                (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                    viewInBrowser();
                                },
                                null,
                                null)
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
//                    application.showErrorToastMsg(Utils.getString(R.string.ck_error_start));
                    CustomToast.showToast(StartActivity.this, Utils.getString(R.string.ck_error_start), CustomToast.ERROR);
                    openMain();
                }
            }
        });
    }

    private void viewInBrowser() {
        Utils.putTextIntoClip(downUrl);
        CustomToast.showToast(this, Utils.getString(R.string.url_copied), CustomToast.SUCCESS);
        Utils.viewInChrome(this, downUrl);
    }

    private void openMain() {
        startActivity(new Intent(StartActivity.this, MainActivity.class));
        StartActivity.this.finish();
    }
}
