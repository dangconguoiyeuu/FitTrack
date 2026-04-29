package com.fitness.fittrack.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.fittrack.R;
import com.fitness.fittrack.utils.ReminderScheduler;

public class ReminderActivity extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATIONS = 4102;

    private Switch swReminder;
    private TimePicker timePicker;
    private TextView tvCurrentReminder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_reminder);

        swReminder = findViewById(R.id.swReminder);
        timePicker = findViewById(R.id.timePicker);
        tvCurrentReminder = findViewById(R.id.tvCurrentReminder);
        Button btnSave = findViewById(R.id.btnSaveReminder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        timePicker.setIs24HourView(true);
        timePicker.setHour(ReminderScheduler.getHour(this));
        timePicker.setMinute(ReminderScheduler.getMinute(this));
        swReminder.setChecked(ReminderScheduler.isReminderEnabled(this));
        updateSummary();

        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> updateSummary());
        btnSave.setOnClickListener(v -> saveReminder());
    }

    private void saveReminder() {
        boolean enabled = swReminder.isChecked();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        ReminderScheduler.saveReminder(this, enabled, hour, minute);
        if (enabled) {
            requestNotificationPermissionIfNeeded();
            ReminderScheduler.scheduleDailyReminder(this, hour, minute);
            Toast.makeText(this, "Đã bật nhắc nhở lúc " + ReminderScheduler.getFormattedTime(this), Toast.LENGTH_SHORT).show();
        } else {
            ReminderScheduler.cancelReminder(this);
            Toast.makeText(this, "Đã tắt nhắc nhở tập luyện", Toast.LENGTH_SHORT).show();
        }
        updateSummary();
    }

    private void updateSummary() {
        if (swReminder.isChecked()) {
            tvCurrentReminder.setText("Đang nhắc mỗi ngày lúc " + String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute()));
        } else {
            tvCurrentReminder.setText("Nhắc nhở đang tắt");
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return;
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
    }
}
