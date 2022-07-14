package my.project.sakuraproject.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.processor.IVodTsUrlConverter;
import com.arialyy.aria.core.task.DownloadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.DownloadNotification;
import my.project.sakuraproject.util.VideoUtils;

public class DownloadService extends Service {
    private DownloadNotification mNotify;
    private Handler handler;
    PowerManager.WakeLock wakeLock = null;
    private M3U8VodOption m3U8VodOption; // 下载m3u8配置

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, DownloadService.class.getName());
        if (null != wakeLock)  {
            wakeLock.acquire();
        }
        handler = new Handler(Looper.getMainLooper());
        handler.post(() -> CustomToast.showToast(getApplicationContext(), "下载服务已开启", CustomToast.SUCCESS));
        mNotify = new DownloadNotification(this);
        Log.e("Service onCreate", "DownloadService开始运行");
        EventBus.getDefault().post(new Refresh(3));
        List<DownloadEntity> list = Aria.download(this).getAllNotCompleteTask();
        if (list != null && list.size() > 0) {
            setM3U8VodOption();
            Aria.download(this).register();
            for (DownloadEntity entity : list) {
                if (entity.getUrl().contains("m3u8")) {
                    Log.e("恢复下载M3U8", "....");
                    Aria.download(this).load(entity.getId()).m3u8VodOption(m3U8VodOption).resume();
                } else {
                    Aria.download(this).load(entity.getId()).resume();
                    Log.e("恢复下载MP4", "....");
                }
            }
        }
    }

    private void setM3U8VodOption() {
        // m3u8下载配置
        m3U8VodOption = new M3U8VodOption();
        m3U8VodOption.ignoreFailureTs();
        m3U8VodOption.setUseDefConvert(false);
        m3U8VodOption.setBandWidthUrlConverter(new BandWidthUrlConverter());
        m3U8VodOption.setVodTsUrlConvert(new VodTsUrlConverter());
        m3U8VodOption.setMergeHandler(new TsMergeHandler());
    }

    /************************************************************ m3u8下载配置 START ************************************************************/
    static class BandWidthUrlConverter implements IBandWidthUrlConverter {
        @Override
        public String convert(String m3u8Url, String bandWidthUrl) {
            try {
                URL url = new URL(m3u8Url);
                m3u8Url = m3u8Url.replace(url.getPath(), "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return m3u8Url + bandWidthUrl;
        }
    }

    static class VodTsUrlConverter implements IVodTsUrlConverter {
        @Override public List<String> convert(String m3u8Url, List<String> tsUrls) {
            // 转换ts文件的url地址
            try {
                URL url = new URL(m3u8Url);
                m3u8Url = m3u8Url.replace(url.getPath(), "").replaceAll("\\?.*", "");;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            List<String> newUrls = new ArrayList<>();
            for (String url : tsUrls) {
                newUrls.add(url.contains("http") ? url : m3u8Url + url);
            }
            return newUrls; // 返回有效的ts文件url集合
        }
    }

    static class TsMergeHandler implements ITsMergeHandler {
        public boolean merge(@Nullable M3U8Entity m3U8Entity, List<String> tsPath) {
            Log.e("TsMergeHandler", "合并TS....");
            String tsKey = m3U8Entity.getKeyPath() == null ? "" : VideoUtils.readKeyInfo(new File(m3U8Entity.getKeyPath()));
            byte[] tsIv = m3U8Entity.getIv() == null ? new byte[16] : m3U8Entity.getIv().getBytes();
            OutputStream outputStream = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            List<File> finishedFiles = new ArrayList<>();
            for (String path : tsPath) {
                try {
                    File pathFile = new File(path);
                    if (!tsKey.isEmpty()) {
                        Log.e("TsMergeHandler", "存在加密");
                        // 存在加密
                        inputStream= new FileInputStream(pathFile);
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);
                        fileOutputStream = new FileOutputStream(pathFile);
                        // 解密ts片段
                        fileOutputStream.write(VideoUtils.decrypt(bytes, tsKey, tsIv));
                    }
                    finishedFiles.add(pathFile);
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) outputStream.close();
                        if (inputStream != null) inputStream.close();
                        if (fileOutputStream != null) fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return VideoUtils.merge(m3U8Entity.getFilePath(), finishedFiles);
        }
    }
    /************************************************************ m3u8下载配置 END ************************************************************/

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        // Aria.download(this).unRegister();
        Log.e("Service onDestroy", "DownloadService销毁了");
        handler.post(() -> CustomToast.showToast(getApplicationContext(), "下载服务已关闭", CustomToast.SUCCESS));
        super.onDestroy();
    }

    @Download.onWait
    public void onTaskWait(DownloadTask downloadTask) {
        Log.e("Service onTaskWait", downloadTask.getTaskName() + "，等待处理");
        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskResume
    public void onTaskResume(DownloadTask downloadTask) {
        Log.e("Service onTaskStart", downloadTask.getTaskName() + "，恢复下载");
        mNotify.showNotification(new Long(downloadTask.getEntity().getId()).intValue(), (String) VideoUtils.getAnimeInfo(downloadTask, 0), downloadTask.getTaskName());
//        EventBus.getDefault().post(new Refresh(3));
    }


    @Download.onTaskStart
    public void onTaskStart(DownloadTask downloadTask) {
        Log.e("Service onTaskStart", downloadTask.getTaskName() + "，开始下载");
        mNotify.showNotification(new Long(downloadTask.getEntity().getId()).intValue(), (String) VideoUtils.getAnimeInfo(downloadTask, 0), downloadTask.getTaskName());
//        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask downloadTask) {
        Log.e("Service onTaskStop", downloadTask.getTaskName() + "，停止下载");
        shouldUnRegister();
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        Log.e("Service onTaskCancel", downloadTask.getTaskName() + "，取消下载");
//        showInfo(downloadTask, "取消下载");
        shouldUnRegister();
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask) {
        Log.e("Service onTaskFail", downloadTask.getTaskName() + "，下载失败");
        DatabaseUtil.updateDownloadError((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), false);
        shouldUnRegister();
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        Log.e("Service onTaskComplete", downloadTask.getTaskName() + "，下载完成");
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), true);
        DatabaseUtil.updateDownloadSuccess((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        shouldUnRegister();
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask downloadTask) {
        mNotify.upload(new Long(downloadTask.getEntity().getId()).intValue(), downloadTask.getPercent());
    }

    private void shouldUnRegister() {
        List<DownloadEntity> list = Aria.download(this).getDRunningTask();
        if (list == null || list.size() == 0) {
            Aria.download(this).unRegister();
            Log.e("Service onTaskFail", "Aria取消注册");
        }
    }
}
