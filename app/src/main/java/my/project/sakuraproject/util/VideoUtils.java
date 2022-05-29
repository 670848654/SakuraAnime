package my.project.sakuraproject.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Serializable;
import java.security.spec.AlgorithmParameterSpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import androidx.appcompat.app.AlertDialog;
import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.AnimeDescDetailsBean;
import my.project.sakuraproject.main.player.PlayerActivity;
import my.project.sakuraproject.main.webview.normal.DefaultNormalWebActivity;

public class VideoUtils {
    private static AlertDialog alertDialog;
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
     * @param isPlayerActivity 是否是播放界面
     */
    public static void showMultipleVideoSources(Context context,
                                                List<String> list,
                                                DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listener2, int type, boolean isPlayerActivity) {
        String[] items = new String[list.size()];
        for (int i = 0, size = list.size(); i < size; i++) {
            if (type == 0) items[i] = getVideoUrl(list.get(i));
            else items[i] = list.get(i);
        }
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setTitle(Utils.getString(R.string.select_video_source));
        builder.setCancelable(false);
        builder.setItems(items, listener);
        if (!isPlayerActivity)
            builder.setNegativeButton(Utils.getString(R.string.cancel), listener2);
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 网络出错时弹窗
     *
     * @param context
     * @param listener
     */
    public static void showPlayerNetworkErrorDialog(Context context, DialogInterface.OnClickListener listener) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setCancelable(false);
        builder.setMessage(Utils.getString(R.string.error_700));
        builder.setNegativeButton(Utils.getString(R.string.try_again), listener);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public static String getVideoUrl(String url) {
        // String playStr = "";
        if (!url.contains("$mp4"))
            return url.replaceAll("changeplay\\('", "").replaceAll("'\\);", "");
        else
            return url.replaceAll("\\$(.*)", "").replaceAll("changeplay\\('", "").replaceAll("'\\);", "");
        /*
        //如果网址
        if (Patterns.WEB_URL.matcher(url.replace(" ", "")).matches()) {
            Matcher m = PLAY_URL_PATTERN.matcher(url);
            while (m.find()) {
                playStr = m.group();
                break;
            }
        } else playStr = url;
        return playStr;
        */
    }

    /**
     * 打开播放器
     *
     * @param isDescActivity
     * @param activity
     * @param witchTitle
     * @param url
     * @param animeTitle
     * @param dramaUrl
     * @param list
     * @param clickIndex
     */
    public static void openPlayer(boolean isDescActivity, Activity activity, String witchTitle, String url, String animeTitle, String dramaUrl,
                                  List<AnimeDescDetailsBean> list, int clickIndex, String animeId, boolean isMaliMali) {
        Bundle bundle = new Bundle();
        bundle.putString("title", witchTitle);
        bundle.putString("url", url);
        bundle.putString("animeTitle", animeTitle);
        bundle.putString("dramaUrl", dramaUrl);
        bundle.putSerializable("list", (Serializable) list);
        bundle.putInt("clickIndex", clickIndex);
        bundle.putString("animeId", animeId);
        bundle.putBoolean("isMaliMali", isMaliMali);
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
     * @param dramaUrl
     * @param list
     * @param bean
     * @param clickIndex
     */
    /*public static void openImomoePlayer(boolean isDescActivity, Activity activity, String witchTitle, String url, String animeTitle, String dramaUrl,
                                        List<List<AnimeDescDetailsBean>> list, List<List<ImomoeVideoUrlBean>> bean, int nowSource, int clickIndex, String animeId) {
        Bundle bundle = new Bundle();
        bundle.putString("title", witchTitle);
        bundle.putString("url", url);
        bundle.putString("animeTitle", animeTitle);
        bundle.putString("dramaUrl", dramaUrl);
        bundle.putSerializable("list", (Serializable) list);
        bundle.putSerializable("playList", (Serializable) bean);
        bundle.putInt("nowSource", nowSource);
        bundle.putInt("clickIndex", clickIndex);
        bundle.putString("animeId", animeId);
        Sakura.destoryActivity("playerImomoe");
        if (isDescActivity)
            activity.startActivityForResult(new Intent(activity, ImomoePlayerActivity.class).putExtras(bundle), 0x10);
        else {
            activity.startActivity(new Intent(activity, ImomoePlayerActivity.class).putExtras(bundle));
            activity.finish();
        }
    }*/

    /**
     * 打开常规webview
     *
     * @param activity
     * @param url
     */
    public static void openDefaultWebview(Activity activity, String url) {
        /*if (Utils.loadX5())
            activity.startActivity(new Intent(activity, DefaultX5WebActivity.class).putExtra("url",url));
        else*/
        activity.startActivity(new Intent(activity, DefaultNormalWebActivity.class).putExtra("url",url));
    }

    public static String getUrl(String url) {
        return url.contains(Sakura.DOMAIN) ? url : Sakura.DOMAIN + url;
    }



    /**
     * 读取key内容
     * @param file
     * @return
     */
    public static String readKeyInfo(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString().replaceAll("\\n", "").replaceAll(" ","");
    }

    /**
     * 解密ASE-128 ts切片
     *
     * @param fileBytes ts文件字节数组
     * @param key 密钥
     * @param iv IV标签
     * @return 解密后的字节数组
     */
    public static byte[] decrypt(byte[] fileBytes, String key, byte[] iv) {
        try {
            // 判断Key是否正确
            if (key.isEmpty()) return null;
            // 判断Key是否为16位
            if (key.length() != 16) {
                Log.e("KeyError", "Key长度不是16位");
                return null;
            }
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("utf-8"), "AES");
            // 如果m3u8有IV标签，那么IvParameterSpec构造函数就把IV标签后的内容转成字节数组传进去
            if (iv.length != 16) iv = new byte[16];
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return cipher.doFinal(fileBytes);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 合并ts
     * @param savePath
     * @param fileList
     * @return
     */
    public static boolean merge(String savePath, List<File> fileList) {
        try {
            File file = new File(savePath.replaceAll("m3u8", "mp4"));
            if (file.exists()) file.delete();
            else file.createNewFile();
            FileOutputStream fs = new FileOutputStream(file);
            byte[] b = new byte[4096];
            for (File f : fileList) {
                FileInputStream fileInputStream = new FileInputStream(f);
                int len;
                while ((len = fileInputStream.read(b)) != -1) {
                    fs.write(b, 0, len);
                }
                fileInputStream.close();
                fs.flush();
            }
            fs.close();
            Log.e("TsMergeHandler", "合并TS成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TsMergeHandler", "合并TS失败，请重新下载....");
            return false;
        }
    }

}
