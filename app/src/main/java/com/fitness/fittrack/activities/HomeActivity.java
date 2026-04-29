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

        findViewById(R.id.btnDemoStreak).setOnClickListener(v -> seedDemoStreak(7));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseHelper.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
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

    private void seedDemoStreak(int days) {
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("streakDays", days);
        data.put("bestStreakDays", days);
        data.put("lastWorkoutDate", LocalDate.now().toString());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    updateStreak(days);
                    Toast.makeText(this, "Đã tạo dữ liệu demo " + days + " ngày liên tiếp.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không thể tạo dữ liệu demo.", Toast.LENGTH_SHORT).show());
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
