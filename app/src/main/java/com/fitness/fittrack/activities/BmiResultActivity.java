package com.fitness.fittrack.activities;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;

public class BmiResultActivity extends AppCompatActivity {
    // Chỉ giữ lại tSet nếu bạn muốn dùng ở nhiều hàm khác nhau,
    // còn không thì để biến cục bộ trong onCreate cho sạch code.
    private int tP, tS, tR, tSet;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_bmi_result);

        double bmi = getIntent().getDoubleExtra("bmi", 0);

        // 1. Hiển thị BMI và Lời khuyên
        ((TextView)findViewById(R.id.tvBmiValue)).setText(String.valueOf(bmi));
        ((TextView)findViewById(R.id.tvBmiCategory)).setText(User.getBMICategory(bmi));
        ((TextView)findViewById(R.id.tvBmiSuggestion)).setText(User.getSuggestion(bmi));

        // 2. ProgressBar
        ProgressBar pb = findViewById(R.id.pbBmi);
        pb.setMax(400);
        pb.setProgress(getMappedProgress(bmi));

        // 3. Lấy mục tiêu từ User.java (trả về 4 giá trị)
        int[] targets = User.getDefaultTargets(bmi);
        tP = targets[0];
        tS = targets[1];
        tSet = targets[2];
        tR = targets[3];

        // 4. Hiển thị mục tiêu dạng chuyên gia
        ((TextView)findViewById(R.id.tvTargetPushup)).setText(tP + " lần x " + tSet + " hiệp");
        ((TextView)findViewById(R.id.tvTargetSitup)).setText(tS + " lần x " + tSet + " hiệp");
        ((TextView)findViewById(R.id.tvTargetSteps)).setText(tR + " bước");

        // 5. Lưu mục tiêu vào Firebase
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String uid = FirebaseHelper.getInstance().getUid();
            if (uid != null) {
                java.util.Map<String, Object> m = new java.util.HashMap<>();
                m.put("targetPushup", tP);
                m.put("targetSitup", tS);
                m.put("targetSets", tSet);
                m.put("targetSteps", tR);

                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(uid).update(m)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Lộ trình 10 ngày đã kích hoạt!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show());
            }
        });

        findViewById(R.id.btnEdit).setOnClickListener(v -> finish());
    }

    private int getMappedProgress(double bmi) {
        if (bmi < 18.5) return (int) ((bmi / 18.5) * 80);
        if (bmi < 23) return 80 + (int) (((bmi - 18.5) / (23 - 18.5)) * 80);
        if (bmi < 25) return 160 + (int) (((bmi - 23) / (25 - 23)) * 80);
        if (bmi < 30) return 240 + (int) (((bmi - 25) / (30 - 25)) * 80);
        return 320 + (int) (Math.min(((bmi - 30) / 10.0), 1.0) * 80);
    }
}