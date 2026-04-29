package com.fitness.fittrack.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fitness.fittrack.utils.ReminderScheduler;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (!ReminderScheduler.isReminderEnabled(context)) return;

        ReminderScheduler.scheduleDailyReminder(
                context,
                ReminderScheduler.getHour(context),
                ReminderScheduler.getMinute(context)
        );
    }
}
