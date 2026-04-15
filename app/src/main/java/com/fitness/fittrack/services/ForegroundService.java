package com.fitness.fittrack.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.fitness.fittrack.R;
import com.fitness.fittrack.activities.ExerciseActivity;

public class ForegroundService extends Service {
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        // Xin quyền giữ CPU thức khi tắt màn hình
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FitTrack::WakeLock");
        wakeLock.acquire(60 * 60 * 1000L); // Giữ tối đa 1 tiếng
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopForeground(true); stopSelf(); return START_NOT_STICKY;
        }

        // Nhận dữ liệu từ Activity gửi sang
        String steps = intent != null ? intent.getStringExtra("steps") : "0";
        String time = intent != null ? intent.getStringExtra("time") : "00:00";
        String calo = intent != null ? intent.getStringExtra("calo") : "0 kcal";

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "fittrack_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Dùng IMPORTANCE_LOW để không bị tít tít mỗi giây khi cập nhật
            NotificationChannel channel = new NotificationChannel(channelId, "Tap Luyen", NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // ÉP HIỆN MÀN HÌNH KHÓA
            nm.createNotificationChannel(channel);
        }

        RemoteViews views = new RemoteViews(getPackageName(), R.layout.activity_workout_notification);
        views.setTextViewText(R.id.tv_notif_main_metric, steps);
        views.setTextViewText(R.id.tv_notif_time, "⏱ " + time);
        views.setTextViewText(R.id.tv_notif_calo, "🔥 " + calo);

        // Bấm vào thông báo để quay lại app
        Intent openIntent = new Intent(this, ExerciseActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(views)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(101, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release(); // Nhả CPU
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}