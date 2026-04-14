package com.fitness.fittrack.activities;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.utils.FirebaseHelper;

/**
 * Man hinh "Thiet lap phien tap" theo wireframe:
 * - Chon bai tap (3 the ngang)
 * - Huong dan ky thuat (video placeholder)
 * - Muc tieu Override (reps +/-, sets +/-)
 * - Nut "Bat dau tap"
 */
public class SetupActivity extends AppCompatActivity {
    private String selectedType;
    private int reps = 30, sets = 1;
    private TextView tvReps, tvSets, tvAiSuggestion, tvNote;
    private View cardRun, cardPush, cardSit;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_setup);
        selectedType = getIntent().getStringExtra("type");
        if (selectedType == null) selectedType = "pushup";

        cardRun = findViewById(R.id.cardRunning);
        cardPush = findViewById(R.id.cardPushup);
        cardSit = findViewById(R.id.cardSitup);
        tvReps = findViewById(R.id.tvReps);
        tvSets = findViewById(R.id.tvSets);
        tvAiSuggestion = findViewById(R.id.tvAiSuggestion);
        tvNote = findViewById(R.id.tvNote);

        cardRun.setOnClickListener(v -> selectType("running"));
        cardPush.setOnClickListener(v -> selectType("pushup"));
        cardSit.setOnClickListener(v -> selectType("situp"));

        findViewById(R.id.btnRepsMinus).setOnClickListener(v -> { if(reps>5) reps-=5; upd(); });
        findViewById(R.id.btnRepsPlus).setOnClickListener(v -> { reps+=5; upd(); });
        findViewById(R.id.btnSetsMinus).setOnClickListener(v -> { if(sets>1) sets--; upd(); });
        findViewById(R.id.btnSetsPlus).setOnClickListener(v -> { sets++; upd(); });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            Intent i = new Intent(this, ExerciseActivity.class);
            i.putExtra("type", selectedType);
            i.putExtra("target", reps);
            i.putExtra("sets", sets);
            startActivity(i); finish();
        });

        // Load target tu Firebase
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid != null) {
            FirebaseHelper.getInstance().getProfile(uid, t -> {
                if (t.isSuccessful() && t.getResult().exists()) {
                    Long tp = t.getResult().getLong("targetPushup");
                    Long ts = t.getResult().getLong("targetSitup");
                    Long tr = t.getResult().getLong("targetSteps");
                    if ("pushup".equals(selectedType) && tp != null && tp > 0) reps = tp.intValue();
                    else if ("situp".equals(selectedType) && ts != null && ts > 0) reps = ts.intValue();
                    else if ("running".equals(selectedType) && tr != null && tr > 0) reps = tr.intValue();
                    upd();
                }
            });
        }

        selectType(selectedType);
    }

    private void selectType(String type) {
        selectedType = type;
        cardRun.setBackgroundResource("running".equals(type)?R.drawable.card_selected:R.drawable.card_unselected);
        cardPush.setBackgroundResource("pushup".equals(type)?R.drawable.card_selected:R.drawable.card_unselected);
        cardSit.setBackgroundResource("situp".equals(type)?R.drawable.card_selected:R.drawable.card_unselected);

        ((TextView)cardRun.findViewById(R.id.tvCardLabel1)).setTextColor("running".equals(type)?0xFFFFFFFF:0xFF000000);
        ((TextView)cardPush.findViewById(R.id.tvCardLabel2)).setTextColor("pushup".equals(type)?0xFFFFFFFF:0xFF000000);
        ((TextView)cardSit.findViewById(R.id.tvCardLabel3)).setTextColor("situp".equals(type)?0xFFFFFFFF:0xFF000000);

        switch(type) {
            case "pushup":
                tvNote.setText("Luu y: Dat dien thoai giua nguc de may tu dong dem chinh xac nhat");
                tvAiSuggestion.setText("De xuat AI (BMI): " + sets + " set, " + reps + " reps");
                break;
            case "situp":
                tvNote.setText("Luu y: Dat dien thoai tren bung/nguc, nam ngua va gap nguoi len");
                tvAiSuggestion.setText("De xuat AI (BMI): " + sets + " set, " + reps + " reps");
                break;
            default:
                tvNote.setText("Luu y: Cam hoac bo dien thoai trong tui quan, di/chay binh thuong");
                tvAiSuggestion.setText("De xuat AI (BMI): " + reps + " buoc");
                break;
        }
        upd();
    }

    private void upd() {
        tvReps.setText(String.valueOf(reps));
        tvSets.setText(String.valueOf(sets));
    }
}
