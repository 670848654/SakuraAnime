package my.project.sakuraproject.main.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.main.home.MainActivity;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public abstract class BaseActivity<V, P extends Presenter<V>> extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    protected P mPresenter;
    public View errorView, emptyView;
    public TextView errorTitle;
    public Sakura application;
    private Unbinder mUnBinder;
    protected boolean mActivityFinish = false;
    protected boolean isDarkTheme;
    protected static String PREVIDEOSTR = "上一集：%s";
    protected static String NEXTVIDEOSTR = "下一集：%s";
    protected boolean isPortrait;
    protected int position = 0;
    protected int change;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration mConfiguration = getResources().getConfiguration();
        change = mConfiguration.orientation;
        if (change == mConfiguration.ORIENTATION_LANDSCAPE) isPortrait = false;
        else if (change == mConfiguration.ORIENTATION_PORTRAIT) isPortrait = true;
        isDarkTheme = Utils.getTheme();
        if (isDarkTheme)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        if (!getRunningActivityName().equals("StartActivity")) overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        initBeforeView();
        setContentView(setLayoutRes());
        if (Utils.checkHasNavigationBar(this)) {
            /*if (!getRunningActivityName().equals("PlayerActivity"))
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                );*/
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getWindow().setNavigationBarContrastEnforced(false);
                getWindow().setStatusBarContrastEnforced(false);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                );
                getWindow().setNavigationBarColor(Color.TRANSPARENT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
            }
        }
        mUnBinder = ButterKnife.bind(this);
        if (application == null) {
            application = (Sakura) getApplication();
        }
        application.addActivity(this);
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS)) {
            isManager();
        } else {
/*            EasyPermissions.requestPermissions(this, Utils.getString(R.string.permissions),
                    300, Manifest.permission.WRITE_EXTERNAL_STORAGE);*/
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, 300, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .setRationale(R.string.permissions)
                            .setPositiveButtonText(R.string.page_positive)
                            .setTheme(R.style.DialogStyle)
                            .build());
        }
    }

    protected abstract P createPresenter();

    protected abstract void loadData() ;

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
        isManager();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
//        application.showErrorToastMsg(Utils.getString(R.string.permissions_error));
        CustomToast.showToast(this, Utils.getString(R.string.permissions_error), CustomToast.ERROR);
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

    public boolean gtSdk30() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    public boolean gtSdk33() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private String getRunningActivityName() {
        String contextString = this.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1,
                contextString.indexOf("@"));
    }

    public void setStatusBarColor() {
        if (!getRunningActivityName().equals("DescActivity") &&
                !getRunningActivityName().equals("PlayerActivity") &&
                !getRunningActivityName().equals("LocalPlayerActivity") &&
                !getRunningActivityName().equals("ImomoePlayerActivity") &&
                !getRunningActivityName().equals("NormalWebActivity") &&
                !getRunningActivityName().equals("DLNAActivity")) {
            if (gtSdk23()) {
                StatusBarUtil.setColorForSwipeBack(this, getColor(R.color.colorPrimary), 0);
                if (!isDarkTheme)
                    this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else
                StatusBarUtil.setColorForSwipeBack(this, getResources().getColor(R.color.colorPrimaryDark), 0);
        }
    }

    private void isManager() {
        if (gtSdk30()) {
            if (Environment.isExternalStorageManager()) {
                if (gtSdk33()) {
                    int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        //动态申请
                        ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.POST_NOTIFICATIONS}, 0x99);
                    } else {
                        build();
                    }
                } else
                    build();
            }
            else getManager();
        } else build();
    }

    private void build() {
        //创建database路路径
        Utils.createFile();
        DatabaseUtil.CREATE_TABLES();
        if (getRunningActivityName().equals("StartActivity")) {
            DatabaseUtil.dataTransfer();
            DatabaseUtil.deleteImomoeData();
            DatabaseUtil.updatePlayUrl();
            DatabaseUtil.deleteMaliMaliData();
        }
        hideGap();
        setStatusBarColor();
        initCustomViews();
        init();
        mPresenter = createPresenter();
        loadData();
    }

    private void getManager() {
        AlertDialog alertDialog;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.DialogStyle);
        builder.setPositiveButton(Utils.getString(R.string.authorize_msg), null);
        builder.setTitle(Utils.getString(R.string.authorize_title_msg));
        builder.setMessage(Utils.getString(R.string.file_manger_msg));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0x99);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x99) isManager();
    }

    @Override
    public void finish() {
        super.finish();
//        if (!getRunningActivityName().equals("StartActivity")) overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 防止两次调用
        if (newConfig.orientation == change) return;
        change = newConfig.orientation;
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        setConfigurationChanged();
    }

    protected abstract void setConfigurationChanged();
}
