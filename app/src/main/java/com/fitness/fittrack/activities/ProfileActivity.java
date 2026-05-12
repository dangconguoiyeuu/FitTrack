package com.fitness.fittrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.fitness.fittrack.utils.OfflineAuthHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etN, etH, etW, etA;
    private Spinner spG, spF;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_profile);

        etN = findViewById(R.id.etName);
        etH = findViewById(R.id.etHeight);
        etW = findViewById(R.id.etWeight);
        etA = findViewById(R.id.etAge);
        spG = findViewById(R.id.spGender);
        spF = findViewById(R.id.spFitness);

        spG.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Nam", "Nữ"}));
        spF.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Ít vận động", "Trung bình", "Thường xuyên"}));

        loadExisting();
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Ham tai ho so hien co; neu dang dang nhap offline thi doc SQLite, neu khong thi doc Firestore.
    private void loadExisting() {
        if (FirebaseHelper.getInstance().isOfflineSession()) {
            User user = OfflineAuthHelper.getInstance(this).getCurrentUserProfile();
            if (user != null) fillForm(user);
            return;
        }

        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getProfile(uid, task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = new User();
                user.setName(task.getResult().getString("name"));
                Double height = task.getResult().getDouble("height");
                Double weight = task.getResult().getDouble("weight");
                Long age = task.getResult().getLong("age");
                user.setHeight(height != null ? height : 0);
                user.setWeight(weight != null ? weight : 0);
                user.setAge(age != null ? age.intValue() : 0);
                user.setGender(task.getResult().getString("gender"));
                user.setFitnessLevel(task.getResult().getString("fitnessLevel"));
                fillForm(user);
            }
        });
    }

    // Ham do du lieu user len cac o nhap lieu cua man hinh ho so.
    private void fillForm(User user) {
        if (user.getName() != null) etN.setText(user.getName());
        if (user.getHeight() > 0) etH.setText(String.valueOf((int) user.getHeight()));
        if (user.getWeight() > 0) etW.setText(String.valueOf((int) user.getWeight()));
        if (user.getAge() > 0) etA.setText(String.valueOf(user.getAge()));
        selectSpinnerValue(spG, user.getGender());
        selectSpinnerValue(spF, user.getFitnessLevel());
    }

    // Ham chon dung gia tri spinner neu ho so da tung luu gioi tinh hoac muc do van dong.
    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equals(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    // Ham kiem tra du lieu nhap, tinh BMI va luu ho so vao SQLite offline hoac Firestore tuy phien dang nhap.
    private void save() {
        String name = etN.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { etN.setError("Nhập họ tên"); return; }
        if (TextUtils.isEmpty(etH.getText().toString())) { etH.setError("Nhập chiều cao"); return; }
        if (TextUtils.isEmpty(etW.getText().toString())) { etW.setError("Nhập cân nặng"); return; }
        if (TextUtils.isEmpty(etA.getText().toString())) { etA.setError("Nhập tuổi"); return; }

        double h, w;
        int age;
        try {
            h = Double.parseDouble(etH.getText().toString());
            w = Double.parseDouble(etW.getText().toString());
            age = Integer.parseInt(etA.getText().toString());
        } catch (Exception e) {
            Toast.makeText(this, "Dữ liệu nhập chưa hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (h < 50 || h > 300) { etH.setError("50-300 cm"); return; }
        if (w < 10 || w > 500) { etW.setError("10-500 kg"); return; }
        if (age < 5 || age > 120) { etA.setError("5-120"); return; }

        double rawBmi = w / ((h / 100) * (h / 100));
        final double finalBmi = Math.round(rawBmi * 10.0) / 10.0;

        if (FirebaseHelper.getInstance().isOfflineSession()) {
            saveOfflineProfile(name, age, h, w, finalBmi);
            return;
        }

        saveFirebaseProfile(name, age, h, w, finalBmi);
    }

    // Ham tao model User va cap nhat ho so offline trong SQLite.
    private void saveOfflineProfile(String name, int age, double height, double weight, double bmi) {
        OfflineAuthHelper offlineAuth = OfflineAuthHelper.getInstance(this);
        User user = new User();
        user.setUid(offlineAuth.getCurrentUid());
        user.setEmail(offlineAuth.getCurrentEmail());
        user.setName(name);
        user.setAge(age);
        user.setHeight(height);
        user.setWeight(weight);
        user.setGender(spG.getSelectedItem().toString());
        user.setFitnessLevel(spF.getSelectedItem().toString());
        user.setBmi(bmi);

        if (offlineAuth.saveProfile(user)) {
            openBmiResult(bmi);
        } else {
            Toast.makeText(this, "Lỗi lưu hồ sơ offline!", Toast.LENGTH_SHORT).show();
        }
    }

    // Ham cap nhat ho so Firebase bang merge de khong ghi de cac truong khac nhu streak.
    private void saveFirebaseProfile(String name, int age, double height, double weight, double bmi) {
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", age);
        updates.put("height", height);
        updates.put("weight", weight);
        updates.put("gender", spG.getSelectedItem().toString());
        updates.put("fitnessLevel", spF.getSelectedItem().toString());
        updates.put("bmi", bmi);

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        openBmiResult(bmi);
                    } else {
                        Toast.makeText(this, "Lỗi lưu!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Ham mo man hinh ket qua BMI sau khi ho so da duoc luu thanh cong.
    private void openBmiResult(double bmi) {
        Intent intent = new Intent(this, BmiResultActivity.class);
        intent.putExtra("bmi", bmi);
        startActivity(intent);
        finish();
    }
}
