package com.fitness.fittrack.activities;

import android.app.AlertDialog;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.models.WorkoutSession;
import com.fitness.fittrack.services.ForegroundService;
import com.fitness.fittrack.utils.FirebaseHelper;

public class ExerciseActivity extends AppCompatActivity implements SensorEventListener {
    // ... Khai báo các biến UI và Logic ...
    private SensorManager sm;
    private Sensor sensor;
    private String type;
    private int target, sets, currentSet = 1;
    private int count = 0;
    private int totalCount = 0;
    private boolean active = false;
    private double userWeight = 65;

    private TextView tvTitle, tvCount, tvTarget, tvUnit, tvTime, tvCalo, tvStatus;

    private final Handler handler = new Handler();
    private long startTime, elapsed;

    // --- Luồng cập nhật thời gian và tính toán Calo tiêu thụ theo thời gian thực mỗi giây ---
    private final Runnable ticker = new Runnable() {
        public void run() {
            elapsed = (SystemClock.elapsedRealtime() - startTime) / 1000;
            long h = elapsed / 3600, m = (elapsed % 3600) / 60, s = elapsed % 60;
            String timeStr = h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);

            // Gọi hàm tính calo dựa trên MET đã chuẩn hóa
            String caloStr = (int) User.estimateCalories(type, count, elapsed, userWeight) + " kcal";

            tvTime.setText(timeStr);
            tvCalo.setText(caloStr);

            // Gửi dữ liệu sang ForegroundService để hiển thị Notification trên màn hình khóa
            try {
                Intent intent = new Intent(ExerciseActivity.this, ForegroundService.class);
                intent.putExtra("steps", count + " " + tvUnit.getText().toString());
                intent.putExtra("time", timeStr);
                intent.putExtra("calo", caloStr);
                androidx.core.content.ContextCompat.startForegroundService(ExerciseActivity.this, intent);
            } catch (Exception ignored) {}

            handler.postDelayed(this, 1000);
        }
    };

    // --- Luồng kiểm tra trạng thái hoạt động: Nếu sau 20 giây không có nhịp tập mới sẽ hiện cảnh báo ---
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

    private boolean peakDetected = false;
    private boolean isNear = false;
    private long lastPushTime = 0;
    private boolean isLying = true;
    private long lastSitupTime = 0;
    private ToneGenerator tone;
    private Vibrator vibrator;
    private ImageView ivExerciseIcon;

    // --- Hàm khởi tạo: Thiết lập giao diện, chọn loại cảm biến phù hợp (Proximity/Accelerometer) và load cân nặng ---
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_exercise);

        ivExerciseIcon = findViewById(R.id.ivExerciseIcon);
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

        // Phân loại cảm biến dựa trên bài tập người dùng đã chọn từ SetupActivity
        switch (type) {
            case "pushup":

                tvTitle.setText("Đang tập : Chống đẩy");
                tvUnit.setText("Lần");
                ivExerciseIcon.setImageResource(R.drawable.ic_pushup);
                sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY); // Dùng cảm biến tiệm cận
                break;
            case "situp":

                tvTitle.setText("Đang tập : Gập bụng");
                tvUnit.setText("Lần");
                ivExerciseIcon.setImageResource(R.drawable.ic_situp);
                sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // Dùng cảm biến gia tốc
                break;
            default:

                tvTitle.setText("Đang tập : Chạy bộ");
                tvUnit.setText("Bước");
                ivExerciseIcon.setImageResource(R.drawable.ic_run);
                sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                break;
        }
        findViewById(R.id.btnFinish).setOnClickListener(v -> showExitConfirmationDialog());
        tvTarget.setText("/ " + target);
        if (sensor == null) {
            Toast.makeText(this, "Cảm biến không khả dụng trên thiết bị này!", Toast.LENGTH_LONG).show();
        }

        // Lấy cân nặng người dùng từ Firebase để tính Calo chính xác nhất
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

    // --- Hàm bắt đầu phiên tập: Đăng ký lắng nghe cảm biến và kích hoạt bộ đếm thời gian ---
    private void startExercise() {
        active = true;
        count = 0;
        tvCount.setText("0");
        tvStatus.setText("Đang bắt nhịp chuyển động...");

        if (sensor != null) sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        startTime = SystemClock.elapsedRealtime();
        lastRepTime = startTime;
        handler.postDelayed(ticker, 0);
        handler.postDelayed(inactivityCheck, 20000);
    }

    // --- Hàm xử lý khi một nhịp tập hợp lệ được ghi nhận: Tăng số đếm, phát âm thanh và kiểm tra mục tiêu ---
    private void onValidRep() {
        count++;
        totalCount++;
        lastRepTime = SystemClock.elapsedRealtime();
        tvCount.setText(String.valueOf(count));
        tvStatus.setText("Nhịp hợp lệ!");

        // Phát âm thanh Beep ngắn báo hiệu đã đếm thành công
        try {
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
        } catch (Exception e) {}

        // Kiểm tra xem đã hoàn thành số Reps của Set hiện tại chưa
        if (count >= target) {
            if (currentSet >= sets) {
                finishExercise(); // Hoàn thành toàn bộ các Set
            } else {
                playRestSound();
                showRestDialog();
            }
        }
    }

    // --- Hàm cốt lõi: Phân tích dữ liệu thô từ cảm biến để nhận diện chuyển động đặc trưng của từng bài tập ---
    @Override
    public void onSensorChanged(SensorEvent e) {
        if (!active) return;

        switch (type) {
            case "running": // Logic đếm bước chân dựa trên gia tốc tổng hợp
                if (e.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;
                float mag = (float) Math.sqrt(e.values[0] * e.values[0] + e.values[1] * e.values[1] + e.values[2] * e.values[2]);
                if (mag > 11.5f && !peakDetected) {
                    peakDetected = true;
                    onValidRep();
                } else if (mag < 9.0f) peakDetected = false;
                break;

            case "pushup": // Logic chống đẩy dựa trên khoảng cách mặt với điện thoại
                if (e.sensor.getType() != Sensor.TYPE_PROXIMITY) return;
                float dist = e.values[0];
                if (dist < sensor.getMaximumRange()) {
                    isNear = true; // Người dùng đang hạ thấp người xuống gần cảm biến
                } else if (isNear) {
                    long now = SystemClock.elapsedRealtime();
                    if (now - lastPushTime > 500) { // Tránh đếm trùng nhịp quá nhanh
                        onValidRep();
                        lastPushTime = now;
                    }
                    isNear = false;
                }
                break;

            case "situp":
                if (e.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

                // Lấy độ nghiêng trục Z (Màn hình điện thoại so với mặt đất)
                float zVal = Math.abs(e.values[2]);

                // BƯỚC 1: Phát hiện khi ông đã gập người lên (Z giảm xuống)
                // Ngưỡng 5.5f là đủ nhạy để nhận diện cú gập kể cả khi tay để sau đầu
                if (zVal < 5.5f) {
                    isLying = false; // Đã rời khỏi tư thế nằm
                }

                // BƯỚC 2: Phát hiện khi ông đã nằm xuống lại (Z tăng lên)
                else if (!isLying && zVal > 8.0f) {
                    long now = SystemClock.elapsedRealtime();

                    // CHẶN ĐẾM KÉP: Chỉ tính nhịp nếu cách lần trước ít nhất 1.2 giây
                    // Đây là cách fix lỗi đếm 2 lần mà ông gặp phải
                    if (now - lastSitupTime > 1800) {
                        onValidRep();
                        lastSitupTime = now;
                    }

                    isLying = true; // Quay lại trạng thái chuẩn bị nằm
                }
                break;
        }
    }

    // --- Hàm tạm dừng: Hiển thị Dialog khi không thấy chuyển động, cho phép người dùng chọn tập tiếp hoặc nghỉ ---
    private void showPauseDialog() {
        if (dialogShowing) return;
        dialogShowing = true;
        active = false;
        sm.unregisterListener(this);
        handler.removeCallbacks(ticker);

        if (vibrator != null) vibrator.vibrate(500); // Rung để cảnh báo người dùng

        new AlertDialog.Builder(this)
                .setTitle("Tạm dừng tập luyện")
                .setMessage("Hệ thống không nhận diện được chuyển động trong 20s qua.\nBạn có muốn kết thúc sớm không?")
                .setCancelable(false)
                .setPositiveButton("Tiếp tục tập", (d, w) -> {
                    dialogShowing = false;
                    active = true;
                    if (sensor != null) sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
                    lastRepTime = SystemClock.elapsedRealtime();
                    handler.postDelayed(ticker, 0);
                    handler.postDelayed(inactivityCheck, 20000);
                })
                .setNegativeButton("Kết thúc & Lưu", (d, w) -> {
                    dialogShowing = false;
                    finishExercise();
                })
                .show();
    }

    // --- Hàm kết thúc bài tập: Hủy các tiến trình chạy ngầm, tính Calo cuối cùng và lưu kết quả lên Firebase ---
    private void finishExercise() {
        active = false;
        sm.unregisterListener(this);
        handler.removeCallbacks(ticker);
        handler.removeCallbacks(inactivityCheck);
        playSuccessSound();
        try {
            Intent stopIntent = new Intent(this, ForegroundService.class);
            stopIntent.setAction("STOP");
            startService(stopIntent);
        } catch (Exception ignored) {}

        double calo = User.estimateCalories(type, totalCount, elapsed, userWeight);
        double dist = "running".equals(type) ? Math.round(totalCount * 0.7 / 1000.0 * 100.0) / 100.0 : 0;

        // Lưu dữ liệu phiên tập vào Firestore để xem lại trong màn hình Lịch sử
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid != null && totalCount > 0) {
            // TRUYỀN totalCount VÀO ĐÂY để lưu tổng số rep của cả buổi tập
            WorkoutSession s = new WorkoutSession(uid, type, totalCount, target, dist, calo, elapsed);
            FirebaseHelper.getInstance().saveWorkout(s, t -> {
                Toast.makeText(getApplicationContext(), t.isSuccessful() ? "Đã lưu kết quả!" : "Lỗi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
            });
        }

        tvStatus.setText("Hoàn thành! Tổng: " + totalCount + " | " + (int) calo + " kcal");
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor s, int a) {}

    @Override
    protected void onPause() {
        super.onPause();
    }

    // --- Hàm xử lý khi quay lại app: Đảm bảo cảm biến vẫn được lắng nghe nếu phiên tập đang diễn ra ---
    @Override
    protected void onResume() {
        super.onResume();
        if (active && sensor != null) {
            sm.unregisterListener(this);
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // --- Hàm hủy Activity: Giải phóng toàn bộ tài nguyên âm thanh, cảm biến và dừng Service chạy ngầm ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tone != null) tone.release();
        handler.removeCallbacksAndMessages(null);

        try {
            Intent stopIntent = new Intent(this, ForegroundService.class);
            stopService(stopIntent);
        } catch (Exception ignored) {}
    }
    // --- Ghi đè nút Back của hệ thống điện thoại ---
    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    // --- Hàm hiển thị bảng hỏi xác nhận thoát ---
    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Kết thúc sớm?")
                .setMessage("Bạn có muốn dừng bài tập ngay bây giờ không?")
                .setPositiveButton("Kết thúc & Lưu", (d, w) -> finishExercise())
                .setNegativeButton("Tập tiếp", null)
                .show();
    }
    private void playSuccessSound() {
        try {
            android.media.MediaPlayer mp = android.media.MediaPlayer.create(this, R.raw.success_sound);
            mp.setOnCompletionListener(android.media.MediaPlayer::release); // Giải phóng bộ nhớ sau khi phát xong
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void playRestSound() {
        try {
            // Sử dụng file rest_start.mp3 trong thư mục raw
            android.media.MediaPlayer mp = android.media.MediaPlayer.create(this, R.raw.rest_start);
            mp.setOnCompletionListener(android.media.MediaPlayer::release);
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showRestDialog() {
        active = false;
        sm.unregisterListener(this);
        handler.removeCallbacks(ticker);
        handler.removeCallbacks(inactivityCheck);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nghỉ giải lao - Hiệp " + currentSet);

        builder.setMessage("Bắt đầu nghỉ...");

        builder.setCancelable(false);
        builder.setPositiveButton("Tập tiếp ngay", (d, w) -> {
            // Nếu bấm nút này, ta sẽ dismiss và bắt đầu set mới luôn
            d.dismiss();
            startNextSet();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Thiết lập bộ đếm ngược
        new android.os.CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cập nhật lại Message mỗi giây
                if (dialog.isShowing()) {
                    dialog.setMessage("Thời gian nghỉ còn lại: " + (millisUntilFinished / 1000) + " giây");
                }
            }

            @Override
            public void onFinish() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    startNextSet();
                }
            }
        }.start();
    }
    private void startNextSet() {
        currentSet++; // Tăng số hiệp hiện tại
        count = 0;    // Reset số lần tập về 0 cho hiệp mới
        tvCount.setText("0");
        tvTitle.setText("Đang tập : " + (type.equals("pushup") ? "Chống đẩy" :
                type.equals("situp") ? "Gập bụng" : "Chạy bộ") + " (Hiệp " + currentSet + ")");

        // Kích hoạt lại cảm biến và đồng hồ
        active = true;
        if (sensor != null) sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        lastRepTime = SystemClock.elapsedRealtime();
        handler.postDelayed(ticker, 0);

        Toast.makeText(this, "Bắt đầu hiệp " + currentSet + "!", Toast.LENGTH_SHORT).show();
    }
}