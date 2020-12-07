package my.project.sakuraproject.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.File;

import my.project.sakuraproject.util.Utils;

public class ClearVideoCacheService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(){
            @Override
            public void run() {
                Utils.deleteAllFiles(new File(android.os.Environment.getExternalStorageDirectory() + "/Android/data/my.project.sakuraproject/cache"));
                Utils.deleteAllFiles(new File(android.os.Environment.getExternalStorageDirectory() + "/Android/data/my.project.sakuraproject/files/VideoCache/main"));
                onDestroy();
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
