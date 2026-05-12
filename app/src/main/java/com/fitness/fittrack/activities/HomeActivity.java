package com.fitness.fittrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.fitness.fittrack.utils.OfflineAuthHelper;
import com.fitness.fittrack.utils.StreakHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private TextView tvW, tvB, tvS, tvStreak;
    private View cardStreak;
    private ImageView ivFlame;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);

        tvW = findViewById(R.id.tvWelcome);
        tvB = findViewById(R.id.tvBmi);
        tvS = findViewById(R.id.tvSuggestion);
        tvStreak = findViewById(R.id.tvStreak);
        cardStreak = findViewById(R.id.cardStreak);
        ivFlame = findViewById(R.id.ivFlame);

        // Nút Bài tập chính -> Chuyển sang SetupActivity
        findViewById(R.id.btnGoExercise).setOnClickListener(v -> {
            Intent i = new Intent(this, SetupActivity.class);
            // Mặc định ban đầu chọn pushup, người dùng sẽ đổi trong Setup
            i.putExtra("type", "pushup");
            startActivity(i);
        });

        // Các nút chức năng khác
        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        findViewById(R.id.btnReminder).setOnClickListener(v ->
                startActivity(new Intent(this, ReminderActivity.class)));

        findViewById(R.id.btnStatistics).setOnClickListener(v ->
                startActivity(new Intent(this, StatisticsActivity.class)));

        findViewById(R.id.btnUsageHistory).setOnClickListener(v ->
                startActivity(new Intent(this, UsageHistoryActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseHelper.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
//        String uid = FirebaseHelper.getInstance().getUid();
//        if (uid != null) {
//            java.util.Map<String, Object> manualStreak = new java.util.HashMap<>();
//            manualStreak.put("streakDays", 5);
//            manualStreak.put("bestStreakDays", 5);
//            // Giả sử hôm qua bạn vừa tập xong để hệ thống hiểu chuỗi đang tiếp diễn
//            manualStreak.put("lastWorkoutDate", java.time.LocalDate.now().minusDays(1).toString());
//
//            com.google.firebase.firestore.FirebaseFirestore.getInstance()
//                    .collection("users").document(uid)
//                    .set(manualStreak, com.google.firebase.firestore.SetOptions.merge())
//                    .addOnSuccessListener(aVoid -> android.util.Log.d("FitTrack", "Đã ép streak lên 5 thành công!"));
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseHelper.getInstance().isOfflineSession()) {
            loadProfile();
            return;
        }

        String uid = FirebaseHelper.getInstance().getUid();
        if (uid != null) {
            // Bước 1: Kiểm tra xem có bị mất chuỗi do bỏ tập hôm qua không
            StreakHelper.checkAndResetStreakIfNeeded(uid, (success, currentStreak) -> {
                // Bước 2: Dù kết quả thế nào cũng tải lại Profile để hiển thị mới nhất
                loadProfile();
            });
        }
    }

    private void loadProfile() {
        if (FirebaseHelper.getInstance().isOfflineSession()) {
            User user = OfflineAuthHelper.getInstance(this).getCurrentUserProfile();
            if (user == null) {
                tvW.setText("Xin chào!");
                tvB.setText("");
                tvS.setText("");
                updateStreak(0);
                return;
            }

            tvW.setText("Xin chào, " + (user.getName() != null ? user.getName() : "") + "!");
            updateStreak(0);

            double bmi = user.getBmi();
            if (bmi > 0) {
                tvB.setText("BMI: " + bmi + " - " + User.getBMICategory(bmi));
                tvS.setText(User.getSuggestion(bmi));
            } else {
                tvB.setText("Chưa tính BMI");
                tvS.setText("Vào Hồ sơ để cập nhật");
            }
            return;
        }

        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getProfile(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String name = task.getResult().getString("name");
                Double bmi = task.getResult().getDouble("bmi");
                Long streak = task.getResult().getLong("streakDays");

                tvW.setText("Xin chào, " + (name != null ? name : "") + "!");

                updateStreak(streak != null ? streak.intValue() : 0);

                if (bmi != null && bmi > 0) {
                    tvB.setText("BMI: " + bmi + " - " + User.getBMICategory(bmi));
                    tvS.setText(User.getSuggestion(bmi));
                } else {
                    tvB.setText("Chưa tính BMI");
                    tvS.setText("Vào Hồ sơ để cập nhật");
                }
            } else {
                tvW.setText("Xin chào!");
                tvB.setText("");
                tvS.setText("");
                updateStreak(0);
            }
        });
    }

    private void updateStreak(int days) {
        if (cardStreak == null || tvStreak == null || ivFlame == null) return;

        if (days < 3) {
            ivFlame.clearAnimation();
            cardStreak.setVisibility(View.GONE);
            return;
        }

        cardStreak.setVisibility(View.VISIBLE);
        tvStreak.setText(days + " ngày liên tiếp");
        ivFlame.startAnimation(createFlameAnimation());
    }

    private Animation createFlameAnimation() {
        ScaleAnimation scale = new ScaleAnimation(
                0.92f, 1.08f,
                0.92f, 1.08f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.85f
        );
        scale.setRepeatCount(Animation.INFINITE);
        scale.setRepeatMode(Animation.REVERSE);

        AlphaAnimation alpha = new AlphaAnimation(0.75f, 1.0f);
        alpha.setRepeatCount(Animation.INFINITE);
        alpha.setRepeatMode(Animation.REVERSE);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setDuration(650);
        return set;
    }

}
