package com.smart.pmtv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

public class PlayerService extends Service {

    private static final String CHANNEL_ID = "pmtv_channel";
    private static final int NOTIFICATION_ID = 1;
    
    public static final String ACTION_PLAY = "com.smart.pmtv.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.smart.pmtv.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.smart.pmtv.ACTION_STOP";

    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new LocalBinder();
    
    private PlayerActionCallback playerActionCallback;

    public interface PlayerActionCallback {
        void onPlay();
        void onPause();
        void onStop();
    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PMTv::PlayerWakeLock");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY:
                        if (playerActionCallback != null) playerActionCallback.onPlay();
                        updateNotification(true);
                        break;
                    case ACTION_PAUSE:
                        if (playerActionCallback != null) playerActionCallback.onPause();
                        updateNotification(false);
                        break;
                    case ACTION_STOP:
                        if (playerActionCallback != null) playerActionCallback.onStop();
                        stopForegroundService();
                        break;
                }
            } else {
                startForeground(NOTIFICATION_ID, buildNotification(true));
                if (wakeLock != null && !wakeLock.isHeld()) {
                    wakeLock.acquire(10 * 60 * 1000L /*10 minutes max just in case*/); // acquire wake lock
                }
            }
        }
        return START_STICKY;
    }

    public void setPlayerActionCallback(PlayerActionCallback callback) {
        this.playerActionCallback = callback;
    }

    public void updateNotification(boolean isPlaying) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(isPlaying));
        }
    }

    private Notification buildNotification(boolean isPlaying) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, PlayerService.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, PlayerService.class).setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, PlayerService.class).setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(AppConfig.CHANNEL_NAME)
                .setContentText("Live Stream Active")
                .setSmallIcon(android.R.drawable.ic_media_play) // Use built-in for now
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        if (isPlaying) {
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent);
        } else {
            builder.addAction(android.R.drawable.ic_media_play, "Play", playPendingIntent);
        }
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "PMTv Player Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void stopForegroundService() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
