package com.fitness.fittrack.activities;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_register);
        TextInputEditText etN=findViewById(R.id.etName), etE=findViewById(R.id.etEmail),
            etP=findViewById(R.id.etPassword), etC=findViewById(R.id.etConfirmPassword);
        Button btn=findViewById(R.id.btnRegister);
        findViewById(R.id.tvGoLogin).setOnClickListener(v->finish());

        btn.setOnClickListener(v -> {
            String name=etN.getText().toString().trim(), email=etE.getText().toString().trim(),
                pass=etP.getText().toString().trim(), conf=etC.getText().toString().trim();
            if (TextUtils.isEmpty(name)) { etN.setError("Nhap ho ten"); return; }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etE.setError("Email khong hop le"); return; }
            if (pass.length()<6) { etP.setError("Toi thieu 6 ky tu"); return; }
            if (!pass.equals(conf)) { etC.setError("Khong khop"); return; }
            btn.setEnabled(false);
            FirebaseHelper.getInstance().getAuth().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(t -> {
                    btn.setEnabled(true);
                    if (t.isSuccessful()) {
                        // Luu ten vao Firestore
                        com.fitness.fittrack.models.User u = new com.fitness.fittrack.models.User();
                        u.setUid(FirebaseHelper.getInstance().getUid());
                        u.setEmail(email); u.setName(name);
                        FirebaseHelper.getInstance().saveProfile(u, task -> {});
                        Toast.makeText(this,"Dang ky thanh cong!",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class)); finish();
                        Intent intent = new Intent(this, ProfileActivity.class);
                        intent.putExtra("IS_FIRST_TIME", true); // Gửi thêm flag để biết là lần đầu
                        startActivity(intent);
                        finish();
                    } else Toast.makeText(this,"Loi: "+t.getException().getMessage(),Toast.LENGTH_LONG).show();
                });
        });
    }
}
