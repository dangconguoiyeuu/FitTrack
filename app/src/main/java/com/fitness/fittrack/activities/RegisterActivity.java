package com.fitness.fittrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.fitness.fittrack.utils.OfflineAuthHelper;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);

        TextInputEditText etN = findViewById(R.id.etName);
        TextInputEditText etE = findViewById(R.id.etEmail);
        TextInputEditText etP = findViewById(R.id.etPassword);
        TextInputEditText etC = findViewById(R.id.etConfirmPassword);
        Button btn = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        tvGoLogin.setOnClickListener(v -> finish());

        btn.setOnClickListener(v -> {
            String name = etN.getText().toString().trim();
            String email = etE.getText().toString().trim();
            String pass = etP.getText().toString().trim();
            String conf = etC.getText().toString().trim();

            if (TextUtils.isEmpty(name)) { etN.setError("Nhập họ tên"); return; }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etE.setError("Email không hợp lệ"); return; }
            if (pass.length() < 6) { etP.setError("Tối thiểu 6 ký tự"); return; }
            if (!pass.equals(conf)) { etC.setError("Không khớp"); return; }

            btn.setEnabled(false);
            FirebaseHelper.getInstance().getAuth().createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        btn.setEnabled(true);
                        if (task.isSuccessful()) {
                            User user = new User();
                            user.setUid(FirebaseHelper.getInstance().getUid());
                            user.setEmail(email);
                            user.setName(name);
                            FirebaseHelper.getInstance().saveProfile(user, saveTask -> {});
                            openProfileAfterRegister("Đăng ký thành công!");
                        } else {
                            registerOffline(name, email, pass);
                        }
                    });
        });
    }

    // Ham xu ly dang ky offline khi Firebase Auth khong hoat dong hoac khong co mang.
    private void registerOffline(String name, String email, String password) {
        if (OfflineAuthHelper.getInstance(this).register(name, email, password)) {
            openProfileAfterRegister("Đăng ký offline thành công!");
        } else {
            Toast.makeText(this, "Email đã tồn tại trên thiết bị này.", Toast.LENGTH_LONG).show();
        }
    }

    // Ham chuyen sang man hinh ho so sau khi dang ky thanh cong de nguoi dung cap nhat BMI.
    private void openProfileAfterRegister(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("IS_FIRST_TIME", true);
        startActivity(intent);
        finish();
    }
}
