package my.project.sakuraproject.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;

import my.project.sakuraproject.R;

public class DownloadNotification {

    private NotificationManager mManager;
    private Context context;
    private android.app.Notification.Builder oldBuilder;
    private NotificationCompat.Builder newBuilder;
    private Map<Integer, Notification> notifications;
    private String CHANNEL_ID = "download";
    private CharSequence NAME = "下载通知";
    private String Description = "下载通知";

    public DownloadNotification(Context context) {
        this.context = context;
        notifications = new HashMap<>();
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void showNotification(int notificationId, String title, String videoNumber) {
        if (!notifications.containsKey(notificationId)) {
            Notification notification = null;
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
            setTextColor(remoteViews);
            remoteViews.setTextViewText(R.id.title, title);
            remoteViews.setTextViewText(R.id.video_number, videoNumber);
            remoteViews.setTextViewText(R.id.state, "下载中...");
            if (Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.O) {
                oldBuilder = new Notification.Builder(context);
                oldBuilder.setAutoCancel(true).setSmallIcon(R.drawable.baseline_download_white_48dp);
                notification = oldBuilder.setContent(remoteViews).build();
                mManager.notify(notificationId, notification);
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NAME, NotificationManager.IMPORTANCE_LOW);
                mChannel.setDescription(Description);
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mChannel.setShowBadge(false);
                mManager.createNotificationChannel(mChannel);

                newBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
                newBuilder.setAutoCancel(true).setSmallIcon(R.drawable.baseline_download_white_48dp);
                notification = newBuilder.setContent(remoteViews).build();
                mManager.notify(notificationId, notification);
            }
            notifications.put(notificationId, notification);
        }
    }

    private void setTextColor(RemoteViews remoteViews) {
        boolean isNight = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES)!= 0;
        remoteViews.setTextColor(R.id.title, context.getResources().getColor(isNight ? R.color.grey50 : R.color.grey900));
        remoteViews.setTextColor(R.id.video_number, context.getResources().getColor(isNight ? R.color.grey50 : R.color.grey900));
        remoteViews.setTextColor(R.id.state, context.getResources().getColor(isNight ? R.color.grey50 : R.color.grey900));
    }

    public void upload(int notificationId, int progress) {
        Notification notification = notifications.get(notificationId);
        if (notification != null) {
            setTextColor(notification.contentView);
            notification.contentView.setProgressBar(R.id.progress, 100, progress, false);
            mManager.notify(notificationId, notification);
        }
    }

    public void uploadInfo(int notificationId, boolean success) {
        Notification notification = notifications.get(notificationId);
        if (notification != null) {
            setTextColor(notification.contentView);
            notification.contentView.setTextViewText(R.id.state, success ? "下载完成" : "下载失败");
            notification.contentView.setProgressBar(R.id.progress, 100, success ? 100 : 0, false);
            mManager.notify(notificationId, notification);
        }
    }

    public void cancelNotification(int notificationId) {
        mManager.cancel(notificationId);
        notifications.remove(notificationId);
    }
}
