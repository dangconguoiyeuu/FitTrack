package com.fitness.fittrack.activities;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.fitness.fittrack.utils.OfflineAuthHelper;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        TextInputEditText etE = findViewById(R.id.etEmail), etP = findViewById(R.id.etPassword);
        Button btn = findViewById(R.id.btnLogin);
        TextView tvR = findViewById(R.id.tvGoRegister);

        btn.setOnClickListener(v -> {
            String e = etE.getText().toString().trim(), p = etP.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { etE.setError("Email không hợp lệ"); return; }
            if (p.length() < 6) { etP.setError("Tối thiểu 6 ký tự"); return; }
            btn.setEnabled(false);

            if (OfflineAuthHelper.getInstance(this).login(e, p)) {
                btn.setEnabled(true);
                Toast.makeText(this, "Đăng nhập offline thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return;
            }

            FirebaseHelper.getInstance().getAuth().signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(t -> {
                    btn.setEnabled(true);
                    if (t.isSuccessful()) { startActivity(new Intent(this, HomeActivity.class)); finish(); }
                    else Toast.makeText(this, "Sai email hoặc mật khẩu. Nếu Firebase đang tắt, hãy đăng ký tài khoản offline trước.", Toast.LENGTH_LONG).show();
                });
        });
        tvR.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
