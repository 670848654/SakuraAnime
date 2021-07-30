package my.project.sakuraproject.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatDelegate;
import my.project.sakuraproject.R;
import my.project.sakuraproject.services.DownloadService;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class Sakura extends Application {
    private static Sakura appContext;
    private List<Activity> oList;
    private static Map<String, Activity> destoryMap = new HashMap<>();
    public static String DOMAIN;
    public static String TAG_API, JCB_API, SEARCH_API;
    public static String YHDM_MOVIE_API = "/movie/";
    public static String YHDM_ZT_API = "/topic/";
    public String error;
    public JSONObject week = new JSONObject();
    public static boolean isImomoe = false;
    public static Sakura getInstance() {
        return appContext;
    }
    // yhdm最近更新动漫
    public static String YHDM_UPDATE;
    // imomoe最近更新动漫
    public static String IMOMOE_UPDATE;

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        if (Utils.isServiceRunning(this, "my.project.sakuraproject.services.DownloadService")) {
            // 应用异常重启时，停止服务，关闭下载
            stopService(new Intent(this, DownloadService.class));
            Aria.download(this).stopAllTask();
        }
        if ((Boolean) SharedPreferencesUtils.getParam(this, "darkTheme", false))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        oList = new ArrayList<>();
        appContext = this;
        Utils.init(this);
        setApi();
//        initTBS();
        Aria.get(this).getDownloadConfig().setMaxTaskNum((Integer) SharedPreferencesUtils.getParam(this, "download_number", 0) + 1).setConvertSpeed(true).setMaxSpeed(0);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    public static void setApi() {
        isImomoe = Utils.isImomoe();
        DOMAIN = isImomoe ? (String) SharedPreferencesUtils.getParam(appContext, "imomoe_domain", Utils.getString(R.string.imomoe_url)) : (String) SharedPreferencesUtils.getParam(appContext, "domain", Utils.getString(R.string.domain_url));
        TAG_API = isImomoe ?  DOMAIN + "/so.asp" : DOMAIN + "/sitemap";
        JCB_API = isImomoe ? "/search.asp?page=1&searchword=%BE%E7%B3%A1" : "/37/";
        SEARCH_API = isImomoe ? DOMAIN + "/search.asp" : DOMAIN + "/search/";
        YHDM_UPDATE = String.format("%s/new/", SharedPreferencesUtils.getParam(appContext, "domain", Utils.getString(R.string.domain_url)));
        IMOMOE_UPDATE = String.format("%s/top/new.html", SharedPreferencesUtils.getParam(appContext, "imomoe_domain", Utils.getString(R.string.imomoe_url)));
    }

    /*private void initTBS() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.setDownloadWithoutWifi(true);//非wifi条件下允许下载X5内核
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                if (arg0) SharedPreferencesUtils.setParam(appContext, "X5State", true);
                else SharedPreferencesUtils.setParam(appContext, "X5State", false);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
    }*/

    /*public void showToastShortMsg(String msg) {
        Toasty.warning(getApplicationContext(), "Load data url\n" + msg, Toast.LENGTH_SHORT, true).show();
    }

    public void showToastMsg(String msg){
        Toasty.warning(getApplicationContext(), msg, Toast.LENGTH_LONG, true).show();
    }

    public void showSuccessToastMsg(String msg){
        Toasty.success(getApplicationContext(), msg, Toast.LENGTH_LONG, true).show();
    }

    public void showErrorToastMsg(String msg){
        Toasty.error(getApplicationContext(), msg, Toast.LENGTH_LONG, true).show();
    }

    public void showCustomToastMsg(String msg, @DrawableRes int iconRes, @ColorRes int color){
        Toasty.custom(this, msg,
                iconRes, color, Toast.LENGTH_LONG, true, true).show();
    }*/

    public void showSnackbarMsgAction(View view, String msg, String actionMsg, View.OnClickListener listener) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction(actionMsg, listener).show();
    }

    public void showSnackbarMsg(View view, String msg) {
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorAccent));
        snackbar.show();
    }

    public void addActivity(Activity activity) {
        if (!oList.contains(activity)) {
            oList.add(activity);
        }
    }

    public void removeActivity(Activity activity) {
        if (oList.contains(activity)) {
            oList.remove(activity);
            activity.finish();
        }
    }

    public void removeALLActivity() {
        for (Activity activity : oList) {
            activity.finish();
        }
    }

    public static void addDestoryActivity(Activity activity, String activityName) {
        destoryMap.put(activityName, activity);
    }

    public static void destoryActivity(String activityName) {
        Set<String> keySet = destoryMap.keySet();
        if (keySet.size() > 0) {
            for (String key : keySet) {
                if (activityName.equals(key)) {
                    destoryMap.get(key).finish();
                }
            }
        }
    }
}
