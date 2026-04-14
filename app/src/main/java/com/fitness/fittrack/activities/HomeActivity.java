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
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_home);
        tvW=findViewById(R.id.tvWelcome); tvB=findViewById(R.id.tvBmi); tvS=findViewById(R.id.tvSuggestion);

        // Bat dau phien tap -> SetupActivity
        findViewById(R.id.btnRunning).setOnClickListener(v -> goSetup("running"));
        findViewById(R.id.btnPushUp).setOnClickListener(v -> goSetup("pushup"));
        findViewById(R.id.btnSitUp).setOnClickListener(v -> goSetup("situp"));
        findViewById(R.id.btnHistory).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseHelper.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class)); finish();
        });
    }

    private void goSetup(String type) {
        Intent i = new Intent(this, SetupActivity.class);
        i.putExtra("type", type);
        startActivity(i);
    }

    @Override protected void onResume() {
        super.onResume(); loadProfile();
    }

    private void loadProfile() {
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;
        FirebaseHelper.getInstance().getProfile(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String name = task.getResult().getString("name");
                Double bmi = task.getResult().getDouble("bmi");
                tvW.setText("Xin chao, " + (name != null ? name : "") + "!");
                if (bmi != null && bmi > 0) {
                    tvB.setText("BMI: " + bmi + " - " + User.getBMICategory(bmi));
                    tvS.setText(User.getSuggestion(bmi));
                } else { tvB.setText("Chua tinh BMI"); tvS.setText("Vao Ho so de cap nhat"); }
            } else { tvW.setText("Xin chao!"); tvB.setText(""); tvS.setText(""); }
        });
    }
}
