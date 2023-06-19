package my.project.sakuraproject.application;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.arialyy.aria.core.Aria;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeUpdateInfoBean;
import my.project.sakuraproject.util.CropUtil;
import my.project.sakuraproject.util.SharedPreferencesUtils;
import my.project.sakuraproject.util.Utils;

public class Sakura extends Application {
    private static Sakura appContext;
    private List<Activity> oList;
    private static Map<String, Activity> destoryMap = new HashMap<>();
    public static String DOMAIN;
    public static String TAG_API, JCB_API, SEARCH_API, MOVIE_API;
    public static String YHDM_ZT_API = "/topic/";
    public String error;
    public JSONObject week = new JSONObject();
    public static boolean isImomoe = false;
    public List<AnimeUpdateInfoBean> animeUpdateInfoBeans;
    public static Sakura getInstance() {
        return appContext;
    }
    // yhdm最近更新动漫
    public static String YHDM_UPDATE;

    public static Handler mainHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(this.getMainLooper());
        Utils.init(this);
        // 忽略Https验证
        HttpsURLConnection.setDefaultSSLSocketFactory(CropUtil.getUnsafeSslSocketFactory());
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        /*if (Utils.isServiceRunning(this, "my.project.sakuraproject.services.DownloadService")) {
            // 应用异常重启时，停止服务，关闭下载
            stopService(new Intent(this, DownloadService.class));
            Aria.download(this).stopAllTask();
        }*/
        if (Utils.getTheme())
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        oList = new ArrayList<>();
        appContext = this;
        Utils.init(this);
        setApi();
        // 2022年2月28日18:09:33 默认改为1
//        Aria.get(this).getDownloadConfig().setMaxTaskNum((Integer) SharedPreferencesUtils.getParam(this, "download_number", 0) + 1).setConvertSpeed(true).setMaxSpeed(0);
        Aria.get(this).getDownloadConfig().setMaxTaskNum(1);
        Aria.get(this).getDownloadConfig().setConvertSpeed(true);
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
        TAG_API = DOMAIN + "/sitemap";
        JCB_API =  "/37/";
        SEARCH_API = isImomoe ? DOMAIN : DOMAIN + "/search/";
        MOVIE_API = "/movie/";
        YHDM_UPDATE = String.format("%s/new/", SharedPreferencesUtils.getParam(appContext, "domain", Utils.getString(R.string.domain_url)));
    }

    public void showSnackbarMsgAction(View view, String msg, String actionMsg, View.OnClickListener listener) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction(actionMsg, listener).show();
    }

    public void showSnackbarMsg(View view, String msg, View anchorView) {
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        snackbar.setAnchorView(anchorView);
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
