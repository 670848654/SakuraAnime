package my.project.sakuraproject.main.base;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class BaseActivity<V, P extends Presenter<V>> extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    protected P mPresenter;
    public View errorView, emptyView;
    public TextView errorTitle;
    public Sakura application;
    private Unbinder mUnBinder;
    protected boolean mActivityFinish = false;
    protected boolean isDarkTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDarkTheme = (Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false);
        if (isDarkTheme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        if (!getRunningActivityName().equals("StartActivity") && !getRunningActivityName().equals("HomeActivity")) overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        initBeforeView();
        setContentView(setLayoutRes());
        if (Utils.checkHasNavigationBar(this)) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
            }
        }
        mUnBinder = ButterKnife.bind(this);
        if (application == null) {
            application = (Sakura) getApplication();
        }
        application.addActivity(this);
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //创建database路路径
            Utils.creatFile();
            DatabaseUtil.CREATE_TABLES();
            init();
            setStatusBarColor();
            initCustomViews();
            mPresenter = createPresenter();
            loadData();
        } else {
            EasyPermissions.requestPermissions(this, Utils.getString(R.string.permissions),
                    300, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    protected abstract P createPresenter();

    protected abstract void loadData();

    protected abstract int setLayoutRes();

    protected abstract void init();

    protected abstract void initBeforeView();

    protected void initCustomViews() {
        errorView = getLayoutInflater().inflate(R.layout.base_error_view, null);
        errorTitle = errorView.findViewById(R.id.title);
        emptyView = getLayoutInflater().inflate(R.layout.base_emnty_view, null);
    }

    /**
     * 隐藏虚拟导航按键
     */
    protected void hideNavBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * 虚拟导航按键
     */
    protected void showNavBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * Android 9 异形屏适配
     */
    protected void hideGap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
    }

    @Override
    protected void onDestroy() {
        mActivityFinish = true;
        //取消View的关联
        if (null != mPresenter)
            mPresenter.detachView();
        mUnBinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        init();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        application.showErrorToastMsg(Utils.getString(R.string.permissions_error));
        application.removeALLActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public boolean gtSdk23() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public boolean gtSdk26() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    private String getRunningActivityName() {
        String contextString = this.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1,
                contextString.indexOf("@"));
    }

    public void setStatusBarColor() {
        if (!getRunningActivityName().equals("HomeActivity") &&
                !getRunningActivityName().equals("DescActivity") &&
                !getRunningActivityName().equals("PlayerActivity") &&
                !getRunningActivityName().equals("DefaultX5WebActivity") &&
                !getRunningActivityName().equals("X5WebActivity") &&
                !getRunningActivityName().equals("DefaultNormalWebActivity") &&
                !getRunningActivityName().equals("NormalWebActivity")) {
            if (gtSdk23()) {
                StatusBarUtil.setColorForSwipeBack(this, getColor(R.color.colorPrimary), 0);
                if (!(Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false))
                    this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else
                StatusBarUtil.setColorForSwipeBack(this, getResources().getColor(R.color.colorPrimaryDark), 0);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (!getRunningActivityName().equals("StartActivity") && !getRunningActivityName().equals("HomeActivity")) overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
