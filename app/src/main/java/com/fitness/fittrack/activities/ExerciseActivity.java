package com.fitness.fittrack.activities;
import android.app.AlertDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.models.WorkoutSession;
import com.fitness.fittrack.utils.FirebaseHelper;

/**
 * Man hinh "Dang tap" theo wireframe:
 * - Vong tron lon hien thi so dem / target
 * - Icon radar bat chuyen dong
 * - Thoi gian + Calo
 * - Phat hien dung 20s -> hien dialog
 * - Am thanh beep moi nhip hop le
 * - Rung khi nhip khong hop le (proximity)
 */
public class ExerciseActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sm;
    private Sensor sensor;
    private String type;
    private int target, sets, currentSet = 1;
    private int count = 0;
    private boolean active = false;
    private double userWeight = 65;

    // UI
    private TextView tvTitle, tvCount, tvTarget, tvUnit, tvTime, tvCalo, tvStatus;

    // Timer
    private final Handler handler = new Handler();
    private long startTime, elapsed;
    private final Runnable ticker = new Runnable() {
        public void run() {
            elapsed = (SystemClock.elapsedRealtime() - startTime) / 1000;
            long h=elapsed/3600, m=(elapsed%3600)/60, s=elapsed%60;
            tvTime.setText(h>0 ? String.format("%02d:%02d:%02d",h,m,s) : String.format("%02d:%02d",m,s));
            tvCalo.setText((int)User.estimateCalories(type, count, elapsed, userWeight) + " kcal");
            handler.postDelayed(this, 1000);
        }
    };

    // Phat hien dung 20s
    private long lastRepTime = 0;
    private boolean dialogShowing = false;
    private final Runnable inactivityCheck = new Runnable() {
        public void run() {
            if (!active || dialogShowing) return;
            if (SystemClock.elapsedRealtime() - lastRepTime > 20000) {
                showPauseDialog();
            }
            handler.postDelayed(this, 5000);
        }
    };

    // Sensor logic
    private boolean peakDetected = false; // running
    private boolean isNear = false; // pushup
    private long lastPushTime = 0;
    private boolean isLying = true; // situp

    // Sound
    private ToneGenerator tone;
    private Vibrator vibrator;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_exercise);

        type = getIntent().getStringExtra("type");
        target = getIntent().getIntExtra("target", 30);
        sets = getIntent().getIntExtra("sets", 1);

        tvTitle = findViewById(R.id.tvTitle);
        tvCount = findViewById(R.id.tvCount);
        tvTarget = findViewById(R.id.tvTarget);
        tvUnit = findViewById(R.id.tvUnit);
        tvTime = findViewById(R.id.tvTime);
        tvCalo = findViewById(R.id.tvCalo);
        tvStatus = findViewById(R.id.tvStatus);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        switch(type) {
            case "pushup":
                tvTitle.setText("Dang tap : Chong day");
                tvUnit.setText("Lan");
                sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                break;
            case "situp":
                tvTitle.setText("Dang tap : Gap bung");
                tvUnit.setText("Lan");
                sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                break;
            default:
                tvTitle.setText("Dang tap : Chay bo");
                tvUnit.setText("Buoc");
                sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                break;
        }

        tvTarget.setText("/ " + target);
        if (sensor == null) { Toast.makeText(this, "Sensor khong kha dung!", Toast.LENGTH_LONG).show(); }

        // Load user weight
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid != null) {
            FirebaseHelper.getInstance().getProfile(uid, t -> {
                if (t.isSuccessful() && t.getResult().exists()) {
                    Double w = t.getResult().getDouble("weight");
                    if (w != null && w > 0) userWeight = w;
                }
            });
        }

        startExercise();
    }

    private void startExercise() {
        active = true; count = 0;
        tvCount.setText("0");
        tvStatus.setText("Dang bat nhip chuyen dong");

        if (sensor != null) sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        startTime = SystemClock.elapsedRealtime();
        lastRepTime = startTime;
        handler.postDelayed(ticker, 0);
        handler.postDelayed(inactivityCheck, 20000);
    }

    private void onValidRep() {
        count++;
        lastRepTime = SystemClock.elapsedRealtime();
        tvCount.setText(String.valueOf(count));
        tvStatus.setText("Nhip hop le!");

        // Am thanh beep
        try { tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100); } catch(Exception e) {}

        // Kiem tra hoan thanh muc tieu
        if (count >= target) {
            if (currentSet >= sets) {
                finishExercise();
            } else {
                currentSet++;
                count = 0;
                tvCount.setText("0");
                Toast.makeText(this, "Hoan thanh set " + (currentSet-1) + "! Nghi 30s...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override public void onSensorChanged(SensorEvent e) {
        if (!active) return;

        switch(type) {
            case "running":
                if (e.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
                float mag = (float)Math.sqrt(e.values[0]*e.values[0]+e.values[1]*e.values[1]+e.values[2]*e.values[2]);
                if (mag > 11.5f && !peakDetected) { peakDetected = true; onValidRep(); }
                else if (mag < 9.0f) peakDetected = false;
                break;

            case "pushup":
                if (e.sensor.getType() != Sensor.TYPE_PROXIMITY) return;
                float dist = e.values[0];
                if (dist < sensor.getMaximumRange()) { isNear = true; }
                else if (isNear) {
                    long now = SystemClock.elapsedRealtime();
                    if (now - lastPushTime > 500) { onValidRep(); lastPushTime = now; }
                    isNear = false;
                }
                break;

            case "situp":
                if (e.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
                float z = Math.abs(e.values[2]);
                if (isLying && z < 3.5f) isLying = false;
                else if (!isLying && z > 6.5f) { onValidRep(); isLying = true; }
                break;
        }
    }

    private void showPauseDialog() {
        if (dialogShowing) return;
        dialogShowing = true;
        active = false;
        sm.unregisterListener(this);
        handler.removeCallbacks(ticker);

        // Rung thong bao
        if (vibrator != null) vibrator.vibrate(500);

        new AlertDialog.Builder(this)
            .setTitle("Tam dung tap luyen")
            .setMessage("He thong khong nhan dien duoc chuyen dong trong 20s qua.\nBan co muon ket thuc som khong?")
            .setCancelable(false)
            .setPositiveButton("Tiep tuc tap luyen", (d, w) -> {
                dialogShowing = false;
                active = true;
                if (sensor != null) sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
                lastRepTime = SystemClock.elapsedRealtime();
                handler.postDelayed(ticker, 0);
                handler.postDelayed(inactivityCheck, 20000);
            })
            .setNegativeButton("Ket thuc & Luu ket qua", (d, w) -> {
                dialogShowing = false;
                finishExercise();
            })
            .show();
    }

    private void finishExercise() {
        active = false;
        sm.unregisterListener(this);
        handler.removeCallbacks(ticker);
        handler.removeCallbacks(inactivityCheck);

        double calo = User.estimateCalories(type, count, elapsed, userWeight);
        double dist = "running".equals(type) ? Math.round(count * 0.7 / 1000.0 * 100.0) / 100.0 : 0;

        // Luu len Firebase
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid != null && count > 0) {
            WorkoutSession s = new WorkoutSession(uid, type, count, target, dist, calo, elapsed);
            FirebaseHelper.getInstance().saveWorkout(s, t -> {
                Toast.makeText(this, t.isSuccessful() ? "Da luu ket qua!" : "Loi luu!", Toast.LENGTH_SHORT).show();
            });
        }

        // Hien thi ket qua
        tvStatus.setText("Hoan thanh! " + count + "/" + target + " | " + (int)calo + " kcal");
        Toast.makeText(this, "Ket thuc: " + count + " nhip, " + (int)calo + " kcal", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override public void onAccuracyChanged(Sensor s, int a) {}
    @Override protected void onPause() { super.onPause(); if(active && sensor!=null) sm.unregisterListener(this); }
    @Override protected void onResume() { super.onResume(); if(active && sensor!=null) sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI); }
    @Override protected void onDestroy() { super.onDestroy(); if(tone!=null)tone.release(); handler.removeCallbacksAndMessages(null); }
}
