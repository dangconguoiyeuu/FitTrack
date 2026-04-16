package com.fitness.fittrack.activities;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;

public class BmiResultActivity extends AppCompatActivity {
    private int tP, tS, tR;
    private TextView tvP, tvS, tvR;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_bmi_result);
        double bmi = getIntent().getDoubleExtra("bmi", 0);

        ((TextView)findViewById(R.id.tvBmiValue)).setText(String.valueOf(bmi));
        ((TextView)findViewById(R.id.tvBmiCategory)).setText(User.getBMICategory(bmi));
        ((TextView)findViewById(R.id.tvBmiSuggestion)).setText(User.getSuggestion(bmi));
        ProgressBar pb = findViewById(R.id.pbBmi);
        pb.setMax(400); pb.setProgress((int)(Math.min(bmi,40)*10));

        int[] t = User.getDefaultTargets(bmi);
        tP=t[0]; tS=t[1]; tR=t[2];
        tvP=findViewById(R.id.tvTargetPushup); tvS=findViewById(R.id.tvTargetSitup); tvR=findViewById(R.id.tvTargetSteps);
        upd();

        findViewById(R.id.btnPushupMinus).setOnClickListener(v->{if(tP>5)tP-=5;upd();});
        findViewById(R.id.btnPushupPlus).setOnClickListener(v->{tP+=5;upd();});
        findViewById(R.id.btnSitupMinus).setOnClickListener(v->{if(tS>5)tS-=5;upd();});
        findViewById(R.id.btnSitupPlus).setOnClickListener(v->{tS+=5;upd();});
        findViewById(R.id.btnStepsMinus).setOnClickListener(v->{if(tR>500)tR-=500;upd();});
        findViewById(R.id.btnStepsPlus).setOnClickListener(v->{tR+=500;upd();});

        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String uid = FirebaseHelper.getInstance().getUid();
            if (uid != null) {
                java.util.Map<String,Object> m = new java.util.HashMap<>();
                m.put("targetPushup",tP); m.put("targetSitup",tS); m.put("targetSteps",tR);
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid).update(m);
            }
            Toast.makeText(this,"Đã lưu mục tiêu!",Toast.LENGTH_SHORT).show();
            finish();
        });
        findViewById(R.id.btnEdit).setOnClickListener(v -> finish());
    }
    private void upd() { tvP.setText(String.valueOf(tP)); tvS.setText(String.valueOf(tS)); tvR.setText(String.valueOf(tR)); }
}
