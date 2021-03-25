package my.project.sakuraproject.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import com.alibaba.fastjson.JSONArray;

import androidx.appcompat.app.AlertDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.bean.ImomoeVideoUrlBean;
import my.project.sakuraproject.main.player.ImomoePlayerActivity;
import my.project.sakuraproject.main.player.PlayerActivity;
import my.project.sakuraproject.main.webview.normal.DefaultNormalWebActivity;
import my.project.sakuraproject.main.webview.normal.NormalWebActivity;
import my.project.sakuraproject.main.webview.x5.DefaultX5WebActivity;
import my.project.sakuraproject.main.webview.x5.X5WebActivity;

public class VideoUtils {
    private static AlertDialog alertDialog;
    private final static Pattern PLAY_URL_PATTERN = Pattern.compile("(https?|ftp|file):\\/\\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");

    /**
     * 解析失败提示弹窗
     *
     * @param context
     * @param HTML_url
     */
    public static void showErrorInfo(Context context, String HTML_url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setPositiveButton(Utils.getString(R.string.play_not_found_positive), null);
        builder.setNegativeButton(Utils.getString(R.string.play_not_found_negative), null);
        builder.setTitle(Utils.getString(R.string.play_not_found_title));
        builder.setMessage(Utils.getString(R.string.error_800));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
//            context.startActivity(new Intent(context, DefaultNormalWebActivity.class).putExtra("url", HTML_url));
            Utils.viewInChrome(context, HTML_url);
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> alertDialog.dismiss());
    }

    /**
     * 发现多个播放地址时弹窗
     *
     * @param context
     * @param list
     * @param listener
     * @param type 0 old 1 new
     */
    public static void showMultipleVideoSources(Context context,
                                                List<String> list,
                                                DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listener2, int type) {
        String[] items = new String[list.size()];
        for (int i = 0, size = list.size(); i < size; i++) {
            if (type == 0) items[i] = getVideoUrl(list.get(i));
            else items[i] = list.get(i);
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.select_video_source));
        builder.setCancelable(false);
        builder.setItems(items, listener);
        builder.setNegativeButton(Utils.getString(R.string.cancel), listener2);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public static String getVideoUrl(String url) {
        String playStr = "";
        url = url.replaceAll("\\$(.*)", "").replaceAll("changeplay\\('", "").replaceAll("'\\);", "");
        //如果网址
        if (Patterns.WEB_URL.matcher(url.replace(" ", "")).matches()) {
            Matcher m = PLAY_URL_PATTERN.matcher(url);
            while (m.find()) {
                playStr = m.group();
                break;
            }
        } else playStr = url;
        return playStr;
    }

    /**
     * 打开播放器
     *
     * @param isDescActivity
     * @param activity
     * @param witchTitle
     * @param url
     * @param animeTitle
     * @param diliUrl
     * @param list
     */
    public static void openPlayer(boolean isDescActivity, Activity activity, String witchTitle, String url, String animeTitle, String diliUrl, List<AnimeDescDetailsBean> list) {
        Bundle bundle = new Bundle();
        bundle.putString("title", witchTitle);
        bundle.putString("url", url);
        bundle.putString("animeTitle", animeTitle);
        bundle.putString("dili", diliUrl);
        bundle.putSerializable("list", (Serializable) list);
        Sakura.destoryActivity("player");
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, PlayerActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, PlayerActivity.class).putExtras(bundle));
            activity.finish();
        }
    }

    /**
     * 打开播放器 Imomoe
     * @param isDescActivity
     * @param activity
     * @param witchTitle
     * @param url
     * @param animeTitle
     * @param diliUrl
     * @param list
     * @param bean
     */
    public static void openImomoePlayer(boolean isDescActivity, Activity activity, String witchTitle, String url, String animeTitle, String diliUrl,  List<List<AnimeDescDetailsBean>> list, List<List<ImomoeVideoUrlBean>> bean, int nowSource) {
        Bundle bundle = new Bundle();
        bundle.putString("title", witchTitle);
        bundle.putString("url", url);
        bundle.putString("animeTitle", animeTitle);
        bundle.putString("dili", diliUrl);
        bundle.putSerializable("list", (Serializable) list);
        bundle.putSerializable("playList", (Serializable) bean);
        bundle.putInt("nowSource", nowSource);
        Sakura.destoryActivity("playerImomoe");
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, ImomoePlayerActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, ImomoePlayerActivity.class).putExtras(bundle));
            activity.finish();
        }
    }

    /**
     * 打开webview
     *
     * @param isDescActivity
     * @param activity
     * @param witchTitle
     * @param animeTitle
     * @param url
     * @param diliUrl
     * @param list
     */
    public static void openWebview(boolean isDescActivity, Activity activity, String witchTitle, String animeTitle, String url, String diliUrl, List<AnimeDescDetailsBean> list) {
        Bundle bundle = new Bundle();
        bundle.putString("witchTitle", witchTitle);
        bundle.putString("title", animeTitle);
        bundle.putString("url", url);
        bundle.putString("dili", diliUrl);
        bundle.putSerializable("list", (Serializable) list);
        if (isDescActivity)
            if (Utils.loadX5())
                activity.startActivityForResult(new Intent(activity, X5WebActivity.class).putExtras(bundle), 0x10);
            else
                activity.startActivityForResult(new Intent(activity, NormalWebActivity.class).putExtras(bundle), 0x10);
        else {
            if (Utils.loadX5())
                activity.startActivity(new Intent(activity, X5WebActivity.class).putExtras(bundle));
            else
                activity.startActivity(new Intent(activity, NormalWebActivity.class).putExtras(bundle));
            activity.finish();
        }
    }

    /**
     * 打开常规webview
     *
     * @param activity
     * @param url
     */
    public static void openDefaultWebview(Activity activity, String url) {
        if (Utils.loadX5())
            activity.startActivity(new Intent(activity, DefaultX5WebActivity.class).putExtra("url",url));
        else
            activity.startActivity(new Intent(activity, DefaultNormalWebActivity.class).putExtra("url",url));
    }

    public static String getUrl(String url) {
        return url.contains(Sakura.DOMAIN) ? url : Sakura.DOMAIN + url;
    }

    /**************************************  imomoe视频解析方法  **************************************/
    public static List<List<ImomoeVideoUrlBean>> getImomoePlayUrl(String json) {
        List<List<ImomoeVideoUrlBean>> allList = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(json);
        for (int i=0,size=jsonArray.size(); i<size; i++) {
            JSONArray sourceArr = JSONArray.parseArray(jsonArray.getJSONArray(i).getString(1));
            List<ImomoeVideoUrlBean> imomoeVideoUrlBeans = new ArrayList<>();
            for (int j=0,size2=sourceArr.size(); j<size2; j++) {
                String str = sourceArr.getString(j);
                String[] strs = str.split("\\$");
                Matcher matcher = Pattern.compile("\\$(.*)\\$").matcher(str);
                if (matcher.find()) {
                    ImomoeVideoUrlBean imomoeVideoUrlBean = new ImomoeVideoUrlBean();
                    String find = matcher.group().replaceAll("\\$", "");
                    if (find.contains("http")) {
                        // 是http
                        imomoeVideoUrlBean.setHttp(true);
                        imomoeVideoUrlBean.setVidOrUrl(find);
                    } else {
                        // 非http
                        imomoeVideoUrlBean.setHttp(false);
                        imomoeVideoUrlBean.setVidOrUrl(find);
                        imomoeVideoUrlBean.setParam(strs[strs.length-1]);
                    }
                    imomoeVideoUrlBeans.add(imomoeVideoUrlBean);
                }
            }
            allList.add(imomoeVideoUrlBeans);
        }
        return allList;
    }
}
