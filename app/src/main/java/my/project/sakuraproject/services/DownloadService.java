package my.project.sakuraproject.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.task.DownloadTask;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.Nullable;
import my.project.sakuraproject.bean.Refresh;
import my.project.sakuraproject.custom.CustomToast;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.DownloadNotification;

public class DownloadService extends Service {
    private DownloadNotification mNotify;
    private Handler handler;

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
        handler = new Handler(Looper.getMainLooper());
        handler.post(() -> CustomToast.showToast(getApplicationContext(), "下载服务已开启", CustomToast.SUCCESS));
        mNotify = new DownloadNotification(this);
        Log.e("onCreate", "DownloadService开始运行");
        EventBus.getDefault().post(new Refresh(3));
        Aria.download(this).resumeAllTask();
        Aria.download(this).register();
    }

    @Override
    public void onDestroy() {
        Aria.download(this).unRegister();
        Log.e("onDestroy", "DownloadService销毁了");
        handler.post(() -> CustomToast.showToast(getApplicationContext(), "下载服务已关闭", CustomToast.SUCCESS));
        super.onDestroy();
    }

    @Download.onWait
    public void onTaskWait(DownloadTask downloadTask) {
        Log.e("onTaskWait", downloadTask.getTaskName() + "，等待处理");
        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskResume
    public void onTaskResume(DownloadTask downloadTask) {
        Log.e("onTaskStart", downloadTask.getTaskName() + "，恢复下载");
        mNotify.showNotification(new Long(downloadTask.getEntity().getId()).intValue(), (String) getAnimeInfo(downloadTask, 0), downloadTask.getTaskName());
//        EventBus.getDefault().post(new Refresh(3));
    }


    @Download.onTaskStart
    public void onTaskStart(DownloadTask downloadTask) {
        Log.e("onTaskStart", downloadTask.getTaskName() + "，开始下载");
        mNotify.showNotification(new Long(downloadTask.getEntity().getId()).intValue(), (String) getAnimeInfo(downloadTask, 0), downloadTask.getTaskName());
//        EventBus.getDefault().post(new Refresh(3));
    }

    @Download.onTaskStop
    public void onTaskStop(DownloadTask downloadTask) {
        Log.e("onTaskStop", downloadTask.getTaskName() + "，停止下载");
        shouldDestroy();
    }

    @Download.onTaskCancel
    public void onTaskCancel(DownloadTask downloadTask) {
        Log.e("onTaskCancel", downloadTask.getTaskName() + "，取消下载");
//        showInfo(downloadTask, "取消下载");
        shouldDestroy();
    }

    @Download.onTaskFail
    public void onTaskFail(DownloadTask downloadTask) {
        Log.e("onTaskFail", downloadTask.getTaskName() + "，下载失败");
        DatabaseUtil.updateDownloadError((String) getAnimeInfo(downloadTask, 0), (Integer) getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), false);
        shouldDestroy();
    }

    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask downloadTask) {
        Log.e("onTaskComplete", downloadTask.getTaskName() + "，下载完成");
        mNotify.uploadInfo(new Long(downloadTask.getEntity().getId()).intValue(), true);
        DatabaseUtil.updateDownloadSuccess((String) getAnimeInfo(downloadTask, 0), (Integer) getAnimeInfo(downloadTask, 1), downloadTask.getFilePath(), downloadTask.getEntity().getId(), downloadTask.getFileSize());
        shouldDestroy();
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask downloadTask) {
        mNotify.upload(new Long(downloadTask.getEntity().getId()).intValue(), downloadTask.getPercent());
    }

    private void shouldDestroy() {
        List<DownloadEntity> list = Aria.download(this).getDRunningTask();
        if (list == null || list.size() == 0) stopSelf();
    }

    /**
     * 根据任务ID查询数据库信息
     * @param downloadTask
     * @param choose 0 返回番剧标题 1 返回番剧来源
     * @return
     */
    private Object getAnimeInfo(DownloadTask downloadTask, int choose) {
        List<Object> objects = DatabaseUtil.queryDownloadAnimeInfo(downloadTask.getEntity().getId());
        return objects.get(choose);
    }
}
