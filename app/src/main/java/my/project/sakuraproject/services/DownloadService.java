package my.project.sakuraproject.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import my.project.sakuraproject.bean.DownloadEvent;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.DownloadNotification;
import my.project.sakuraproject.util.VideoUtils;

public class DownloadService extends Service {
    private DownloadNotification mNotify;
//    private Handler handler;
    PowerManager.WakeLock wakeLock = null;
    private List<Long> taskIds = new ArrayList<>();

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
        mNotify = new DownloadNotification(this);
        mNotify.cancelNotification(-2);
        mNotify.showServiceNotification(-1, "下载服务运行中");
        Log.e("Service onCreate", "DownloadService开始运行");
        Aria.download(this).register();
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        // 服务关闭时存在未下载完成的任务，停止下载
        List<DownloadEntity> downloadEntities = Aria.download(this).getAllNotCompleteTask();
        if (downloadEntities != null && downloadEntities.size() > 0) {
            mNotify.showServiceNotification(-2, "由于下载服务被系统杀死，任务已暂停，后台下载请根据各自系统设置应用白名单...");
            Aria.download(this).stopAllTask();
        }
        mNotify.cancelNotification(-1);
        for (Long id : taskIds) {
            mNotify.cancelNotification(id.intValue());
        }
        Aria.download(this).unRegister();
        Log.e("Service onDestroy", "DownloadService销毁了");
        super.onDestroy();
    }

    @Download.onWait
    public void onTaskWait(DownloadTask downloadTask) {
        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskResume
    public void onTaskResume(DownloadTask downloadTask) {
        mNotify.showDefaultNotification(new Long(downloadTask.getEntity().getId()).intValue(), (String) VideoUtils.getAnimeInfo(downloadTask, 0), downloadTask.getTaskName());
//        EventBus.getDefault().post(new Refresh(3));
    }


    @Download.onTaskStart
    public void onTaskStart(DownloadTask downloadTask) {
        EventBus.getDefault().post(new Refresh(3));
        taskIds.add(new Long(downloadTask.getEntity().getId()));
        mNotify.showDefaultNotification(new Long(downloadTask.getEntity().getId()).intValue(), (String) VideoUtils.getAnimeInfo(downloadTask, 0), downloadTask.getTaskName());
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask downloadTask) {
        EventBus.getDefault().post(new Refresh(3));
        shouldUnRegister();
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        taskIds.add(new Long(downloadTask.getEntity().getId()));
        mNotify.cancelNotification(new Long(downloadTask.getEntity().getId()).intValue());
//        showInfo(downloadTask, "取消下载");
        shouldUnRegister();
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask, Exception e) {
        String animeTitle = (String) VideoUtils.getAnimeInfo(downloadTask, 0);
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), animeTitle, downloadTask.getTaskName(), "下载失败 → " + (e == null ? "未知错误，或许是下载的资源不存在！" : e.getMessage()));
        DatabaseUtil.updateDownloadError((String) VideoUtils.getAnimeInfo(downloadTask, 0), (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
//        handler.post(() -> CustomToast.showToast(getApplicationContext(), VideoUtils.getAnimeInfo(downloadTask, 0) + " " + downloadTask.getTaskName() + "下载失败\n" +  ALog.getExceptionString(e), CustomToast.ERROR));
        shouldUnRegister();
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        String animeTitle = (String) VideoUtils.getAnimeInfo(downloadTask, 0);
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), animeTitle, downloadTask.getTaskName(), "下载成功");
        DatabaseUtil.updateDownloadSuccess(animeTitle, (Integer) VideoUtils.getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        Aria.download(this).load(downloadTask.getEntity().getId()).ignoreCheckPermissions().cancel(false); // 下载完成删除任务
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
            mNotify.cancelNotification(-1);
            EventBus.getDefault().post(new Refresh(100));
        }
    }
}
