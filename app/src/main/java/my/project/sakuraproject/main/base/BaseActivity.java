package my.project.sakuraproject.main.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.StatusBarUtil;
import my.project.sakuraproject.util.Utils;

public abstract class BaseActivity<V, P extends Presenter<V>> extends AppCompatActivity {
    protected P mPresenter;
    public View errorView, emptyView;
    public TextView errorTitle;
    public Sakura application;
    private Unbinder mUnBinder;
    protected boolean mActivityFinish = false;
    protected boolean isDarkTheme;
    protected static String PREVIDEOSTR = "上一集 %s";
    protected static String NEXTVIDEOSTR = "下一集 %s";
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
        if (gtSdk30()) {
            // 安卓11及以上
            if (Environment.isExternalStorageManager()) // 如果授予了所有文件授权
                SharedPreferencesUtils.setParam(this, "set_file_manager_permission", 2);
            int userSetPermission = (int) SharedPreferencesUtils.getParam(this, "set_file_manager_permission", 0);
            if (userSetPermission == 0 && !Environment.isExternalStorageManager())
            {
                Utils.showAlert(this,
                        getString(R.string.authorize_title_msg),
                        getString(R.string.file_manger_msg),
                        false,
                        getString(R.string.accept_msg),
                        Utils.getString(R.string.refuse_msg),
                        null,
                        (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            checkPermissions();
                        },
                        (dialogInterface, i) -> {
                            SharedPreferencesUtils.setParam(this, "set_file_manager_permission", 1);
                            dialogInterface.dismiss();
                            build();
                        },
                        null);
            } else if (userSetPermission == 2)
                checkPermissions();
            else
                build();
        } else // 其他版本获取权限
            checkPermissions();
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (gtSdk33()) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        PermissionX.init(this)
                .permissions(permissions)
                .onExplainRequestReason((scope, deniedList) -> {
                    scope.showRequestReasonDialog(deniedList, getString(R.string.permissions), "同意", "不同意");
                })
                .onForwardToSettings((scope, deniedList) -> {
                    scope.showForwardToSettingsDialog(deniedList, getString(R.string.permissions), "同意", "不同意");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        isManager();
                    } else {
                        Toast.makeText(this, getString(R.string.permissions_error), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
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
    protected void onResume() {
        super.onResume();
        DatabaseUtil.openDB();
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
                SharedPreferencesUtils.setParam(this, "set_file_manager_permission", 2);
                build();
            } else {
                /*int userSetPermission = (int) SharedPreferencesUtils.getParam(this, "set_file_manager_permission", 0);
                if (userSetPermission == 0)
                    getManager();
                else
                    build();*/
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0x99);
            }
        } else {
            SharedPreferencesUtils.setParam(this, "set_file_manager_permission", 2);
            build();
        }
    }

    private void build() {
        //创建database路路径
        Utils.createFile();
        DatabaseUtil.CREATE_TABLES();
        if (getRunningActivityName().equals("StartActivity")) {
            DatabaseUtil.dataTransfer();
            DatabaseUtil.deleteImomoeData();
            DatabaseUtil.updatePlayUrl();
//            DatabaseUtil.deleteMaliMaliData();
        }
        hideGap();
        setStatusBarColor();
        initCustomViews();
        init();
        mPresenter = createPresenter();
        loadData();
    }

    private void getManager() {
        Utils.showAlert(this,
                getString(R.string.authorize_title_msg),
                getString(R.string.file_manger_msg),
                false,
                getString(R.string.authorize_msg),
                Utils.getString(R.string.refuse_msg),
                null,
                (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 0x99);
                },
                (dialogInterface, i) -> {
                    SharedPreferencesUtils.setParam(this, "set_file_manager_permission", 1);
                    dialogInterface.dismiss();
                    build();
                },
                null);
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
