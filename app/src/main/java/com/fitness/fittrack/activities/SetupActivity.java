package com.fitness.fittrack.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

public class SetupActivity extends AppCompatActivity {
    // 1. Chỉ khai báo 1 lần duy nhất
    private String selectedType = "running";

    // Biến lưu trữ mục tiêu phân tích từ Firebase
    private int targetPushup = 30, targetSitup = 30, targetRunning = 2000, targetSets = 3;

    // Biến người dùng đang chọn hiện tại
    private int reps = 30, sets = 1;

    private TextView tvReps, tvSets, tvAiSuggestion, tvNote;
    private View cardRun, cardPush, cardSit, layoutReps, layoutSets;
    private ExoPlayer player;
    private PlayerView playerView;
    private String currentInput = "";
    private TextView tvDisplayInput;
    private AlertDialog numberPickerDialog;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_setup);

        playerView = findViewById(R.id.playerView);
        initPlayer();

        // Lấy type từ Intent, nếu không có thì mặc định là running
        String typeFromIntent = getIntent().getStringExtra("type");
        if (typeFromIntent != null) selectedType = typeFromIntent;

        cardRun = findViewById(R.id.cardRunning);
        cardPush = findViewById(R.id.cardPushup);
        cardSit = findViewById(R.id.cardSitup);
        tvReps = findViewById(R.id.tvReps);
        tvSets = findViewById(R.id.tvSets);
        tvAiSuggestion = findViewById(R.id.tvAiSuggestion);
        tvNote = findViewById(R.id.tvNote);

        layoutReps = (View) tvReps.getParent();
        layoutSets = (View) tvSets.getParent();

        cardRun.setOnClickListener(v -> selectType("running"));
        cardPush.setOnClickListener(v -> selectType("pushup"));
        cardSit.setOnClickListener(v -> selectType("situp"));

        findViewById(R.id.btnRepsMinus).setOnClickListener(v -> { if (reps > 5) reps -= 5; upd(); });
        findViewById(R.id.btnRepsPlus).setOnClickListener(v -> { reps += 5; upd(); });
        findViewById(R.id.btnSetsMinus).setOnClickListener(v -> { if (sets > 1) sets--; upd(); });
        findViewById(R.id.btnSetsPlus).setOnClickListener(v -> { sets++; upd(); });

        layoutReps.setOnClickListener(v -> showNumberPickerDialog(true));
        layoutSets.setOnClickListener(v -> showNumberPickerDialog(false));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            Intent i = new Intent(this, ExerciseActivity.class);
            i.putExtra("type", selectedType);
            i.putExtra("target", reps);
            i.putExtra("sets", sets);
            startActivity(i);
            finish();
        });

        // Chỉ cần gọi duy nhất 1 hàm load dữ liệu
        loadFirebaseData();
    }

    private void loadFirebaseData() {
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Lấy đầy đủ 4 thông số phân tích BMI
                        Long tp = doc.getLong("targetPushup");
                        Long ts = doc.getLong("targetSitup");
                        Long tr = doc.getLong("targetSteps");
                        Long tSet = doc.getLong("targetSets");

                        if (tp != null) targetPushup = tp.intValue();
                        if (ts != null) targetSitup = ts.intValue();
                        if (tr != null) targetRunning = tr.intValue();
                        if (tSet != null) targetSets = tSet.intValue();

                        // Sau khi tải xong dữ liệu, mới cập nhật hiển thị
                        selectType(selectedType);
                    }
                });
    }

    private void selectType(String type) {
        selectedType = type;
        int videoResId;

        switch(type) {
            case "pushup":
                videoResId = R.raw.guide_pushup;
                reps = targetPushup;
                sets = targetSets; // Đồng bộ số hiệp
                break;
            case "situp":
                videoResId = R.raw.guide_situp;
                reps = targetSitup;
                sets = targetSets; // Đồng bộ số hiệp
                break;
            default:
                videoResId = R.raw.guide_run;
                reps = targetRunning;
                sets = 1; // Chạy bộ mặc định 1 hiệp
                break;
        }
        playVideo(videoResId);

        // Hiển thị dòng đề xuất từ phân tích BMI
        if ("pushup".equals(type) || "situp".equals(type)) {
            tvNote.setText("Lưu ý: Thực hiện đúng tư thế để đạt hiệu quả cao nhất.");
            tvAiSuggestion.setText("Phân tích BMI đề xuất: " + sets + " hiệp x " + reps + " cái");
        } else {
            tvNote.setText("Lưu ý: Đi hoặc chạy đều đặn, bỏ điện thoại vào túi quần.");
            tvAiSuggestion.setText("Phân tích BMI đề xuất: " + reps + " bước");
        }
        if ("running".equals(type)) {
            tvAiSuggestion.setText("Phân tích BMI đề xuất: " + reps + " bước");
            layoutSets.setVisibility(View.GONE); // Ẩn chọn hiệp khi chạy bộ
        } else {
            tvAiSuggestion.setText("Phân tích BMI đề xuất: " + sets + " hiệp x " + reps + " cái");
            layoutSets.setVisibility(View.VISIBLE); // Hiện lại khi tập gym
        }

        updateCardStyles(type);
        upd();
    }

    // Các hàm showNumberPickerDialog, confirmInput, updateCardStyles, upd, initPlayer, playVideo, onDestroy GIỮ NGUYÊN NHƯ CŨ

    // --- Hàm hiển thị hộp thoại bàn phím số (0-9) để người dùng nhập trực tiếp số Reps hoặc Sets ---
    private void showNumberPickerDialog(boolean isReps) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
        builder.setView(view);

        tvDisplayInput = view.findViewById(R.id.tvDisplayInput);
        currentInput = isReps ? String.valueOf(reps) : String.valueOf(sets);
        tvDisplayInput.setText(currentInput);

        GridLayout grid = view.findViewById(R.id.gridLayout);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View v = grid.getChildAt(i);
            if (v instanceof Button) {
                Button b = (Button) v;
                String txt = b.getText().toString();
                if (txt.equals("OK")) b.setOnClickListener(btn -> confirmInput(isReps));
                else if (txt.equals("⌫")) b.setOnClickListener(btn -> {
                    if (currentInput.length() > 1) currentInput = currentInput.substring(0, currentInput.length() - 1);
                    else currentInput = "0";
                    tvDisplayInput.setText(currentInput);
                });
                else b.setOnClickListener(btn -> {
                        if (currentInput.equals("0")) currentInput = txt;
                        else if (currentInput.length() < 5) currentInput += txt;
                        tvDisplayInput.setText(currentInput);
                    });
            }
        }
        numberPickerDialog = builder.create();
        numberPickerDialog.show();
    }

    // --- Hàm xác nhận và kiểm tra giá trị người dùng vừa nhập từ bàn phím số trước khi áp dụng ---
    private void confirmInput(boolean isReps) {
        int val = Integer.parseInt(currentInput);
        if (val <= 0) {
            Toast.makeText(this, "Vui lòng nhập số lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isReps) reps = val; else sets = val;
        upd();
        if (numberPickerDialog != null) numberPickerDialog.dismiss();
    }

    // --- Hàm cập nhật trạng thái hiển thị (Màu nền, Màu chữ) của 3 thẻ chọn bài tập (Run/Push/Sit) ---
    private void updateCardStyles(String type) {
        boolean isRun = "running".equals(type), isPush = "pushup".equals(type), isSit = "situp".equals(type);
        cardRun.setBackgroundResource(isRun ? R.drawable.card_selected : R.drawable.card_unselected);
        cardPush.setBackgroundResource(isPush ? R.drawable.card_selected : R.drawable.card_unselected);
        cardSit.setBackgroundResource(isSit ? R.drawable.card_selected : R.drawable.card_unselected);
        ((TextView)cardRun.findViewById(R.id.tvCardLabel1)).setTextColor(isRun ? 0xFFFFFFFF : 0xFF000000);
        ((TextView)cardPush.findViewById(R.id.tvCardLabel2)).setTextColor(isPush ? 0xFFFFFFFF : 0xFF000000);
        ((TextView)cardSit.findViewById(R.id.tvCardLabel3)).setTextColor(isSit ? 0xFFFFFFFF : 0xFF000000);
    }

    // --- Hàm cập nhật văn bản hiển thị con số Reps và Sets hiện tại lên giao diện người dùng ---
    private void upd() {
        tvReps.setText(String.valueOf(reps));
        tvSets.setText(String.valueOf(sets));
    }

    // --- Hàm khởi tạo trình phát ExoPlayer và thiết lập chế độ lặp lại video vô hạn ---
    private void initPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    // --- Hàm xử lý việc nạp và phát một file video cụ thể từ thư mục tài nguyên (res/raw) ---
    private void playVideo(int resId) {
        if (player != null) {
            String path = "android.resource://" + getPackageName() + "/" + resId;
            player.setMediaItem(MediaItem.fromUri(Uri.parse(path)));
            player.prepare();
            player.play();
        }
    }

    // --- Hàm giải phóng bộ nhớ và trình phát video khi người dùng thoát khỏi màn hình này ---
    @Override protected void onDestroy() {
        super.onDestroy();
        if (player != null) { player.release(); player = null; }
    }
}