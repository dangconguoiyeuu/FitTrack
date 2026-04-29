package com.fitness.fittrack.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.fitness.fittrack.receivers.ReminderReceiver;

import java.util.Calendar;
import java.util.Locale;

public final class ReminderScheduler {
    private static final String PREFS = "fittrack_reminder";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final int REQUEST_CODE = 2201;

    private ReminderScheduler() {}

    public static void saveReminder(Context context, boolean enabled, int hour, int minute) {
        prefs(context).edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();
    }

    public static boolean isReminderEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static int getHour(Context context) {
        return prefs(context).getInt(KEY_HOUR, 19);
    }

    public static int getMinute(Context context) {
        return prefs(context).getInt(KEY_MINUTE, 0);
    }

    public static String getFormattedTime(Context context) {
        return String.format(Locale.getDefault(), "%02d:%02d", getHour(context), getMinute(context));
    }

    public static void scheduleDailyReminder(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = getPendingIntent(context);
        long triggerAtMillis = nextTriggerMillis(hour, minute);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } catch (SecurityException ignored) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        }
    }

    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent(context));
        }
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static long nextTriggerMillis(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTimeInMillis();
    }
}
