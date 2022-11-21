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
import com.arialyy.aria.core.download.m3u8.M3U8VodOption;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.ALog;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.Nullable;
import my.project.sakuraproject.bean.DownloadEvent;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.config.M3U8DownloadConfig;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.DownloadNotification;
import my.project.sakuraproject.util.VideoUtils;

public class DownloadService extends Service {
    private DownloadNotification mNotify;
//    private Handler handler;
    PowerManager.WakeLock wakeLock = null;

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
        /*handler = new Handler(Looper.getMainLooper());
        handler.post(() -> CustomToast.showToast(getApplicationContext(), "下载服务已开启", CustomToast.SUCCESS));*/
        mNotify = new DownloadNotification(this);
        Log.e("Service onCreate", "DownloadService开始运行");
        Aria.download(this).register();
        /*EventBus.getDefault().post(new Refresh(3));
        List<DownloadEntity> list = Aria.download(this).getAllNotCompleteTask();
        Aria.download(this).register();
        if (list != null && list.size() > 0) {
            setM3U8VodOption();
            for (DownloadEntity entity : list) {
                if (entity.getUrl().contains("m3u8")) {
                    Log.e("恢复下载M3U8", "....");
                    Aria.download(this).load(entity.getId()).m3u8VodOption(m3U8VodOption).resume();
                } else {
                    Aria.download(this).load(entity.getId()).resume();
                    Log.e("恢复下载MP4", "....");
                }
            }
        }*/
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        Log.e("Service onDestroy", "DownloadService销毁了");
        Aria.download(this).unRegister();
        /*handler.post(() -> {
            Aria.download(this).unRegister();
            CustomToast.showToast(getApplicationContext(), "下载服务已关闭", CustomToast.SUCCESS);
        });*/
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
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask downloadTask) {
        Log.e("Service onTaskStop", downloadTask.getTaskName() + "，停止下载");
        EventBus.getDefault().post(new Refresh(3));
        shouldUnRegister();
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        Log.e("Service onTaskCancel", downloadTask.getTaskName() + "，取消下载");
        mNotify.cancelNotification(new Long(downloadTask.getEntity().getId()).intValue());
//        showInfo(downloadTask, "取消下载");
        shouldUnRegister();
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask, Exception e) {
        Log.e("Service onTaskFail", downloadTask.getTaskName() + "，下载失败");
        String animeTitle = (String) VideoUtils.getAnimeInfo(downloadTask, 0);
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), animeTitle, downloadTask.getTaskName(), false);
        DatabaseUtil.updateDownloadError((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
//        handler.post(() -> CustomToast.showToast(getApplicationContext(), VideoUtils.getAnimeInfo(downloadTask, 0) + " " + downloadTask.getTaskName() + "下载失败\n" +  ALog.getExceptionString(e), CustomToast.ERROR));
        shouldUnRegister();
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        Log.e("Service onTaskComplete", downloadTask.getTaskName() + "，下载完成");
        String animeTitle = (String) VideoUtils.getAnimeInfo(downloadTask, 0);
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), animeTitle, downloadTask.getTaskName(), true);
        DatabaseUtil.updateDownloadSuccess(animeTitle, (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        EventBus.getDefault().post(new DownloadEvent(animeTitle, downloadTask.getTaskName(), downloadTask.getFilePath(), downloadTask.getFileSize()));
        shouldUnRegister();
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask downloadTask) {
        mNotify.upload(new Long(downloadTask.getEntity().getId()).intValue(), downloadTask.getPercent());
    }

    private void shouldUnRegister() {
        List<DownloadEntity> list = Aria.download(this).getDRunningTask();
        if (list == null || list.size() == 0) {
            // 没有正在执行的任务
            EventBus.getDefault().post(new Refresh(100));
        }
    }
}
