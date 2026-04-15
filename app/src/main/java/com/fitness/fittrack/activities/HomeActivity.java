package com.fitness.fittrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;

public class HomeActivity extends AppCompatActivity {
    private TextView tvW, tvB, tvS;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);

        tvW = findViewById(R.id.tvWelcome);
        tvB = findViewById(R.id.tvBmi);
        tvS = findViewById(R.id.tvSuggestion);

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

                tvW.setText("Xin chào, " + (name != null ? name : "") + "!");

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
            }
        });
    }
}