package com.fitness.fittrack.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.fitness.fittrack.R;
import com.fitness.fittrack.activities.SplashActivity;
import com.fitness.fittrack.utils.ReminderScheduler;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_BASE_ID = "fittrack_workout_reminders";
    private static final String REMINDER_SOUND_RAW_NAME = "reminder_sound";
    private static final int NOTIFICATION_ID = 301;

    @Override
    public void onReceive(Context context, Intent intent) {
        showReminder(context);

        if (ReminderScheduler.isReminderEnabled(context)) {
            ReminderScheduler.scheduleDailyReminder(
                    context,
                    ReminderScheduler.getHour(context),
                    ReminderScheduler.getMinute(context)
            );
        }
    }

    private void showReminder(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        int customSoundResId = context.getResources().getIdentifier(
                REMINDER_SOUND_RAW_NAME,
                "raw",
                context.getPackageName()
        );
        Uri soundUri = getReminderSoundUri(context, customSoundResId);
        String channelId = CHANNEL_BASE_ID + (customSoundResId != 0 ? "_custom" : "_default");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Nhắc nhở tập luyện",
                    NotificationManager.IMPORTANCE_HIGH
            );
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent openIntent = new Intent(context, SplashActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Đến giờ tập luyện")
                .setContentText("Mở FitTrack để hoàn thành mục tiêu hôm nay.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Mở FitTrack để hoàn thành mục tiêu hôm nay. Cố gắng giữ chuỗi ngày tập nhé!"))
                .setContentIntent(contentIntent)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 300, 180, 300})
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private Uri getReminderSoundUri(Context context, int customSoundResId) {
        if (customSoundResId != 0) {
            return Uri.parse("android.resource://" + context.getPackageName() + "/" + customSoundResId);
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }
}
