package com.fitness.fittrack.activities;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText etN, etH, etW, etA;
    private Spinner spG, spF;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_profile);
        etN=findViewById(R.id.etName); etH=findViewById(R.id.etHeight);
        etW=findViewById(R.id.etWeight); etA=findViewById(R.id.etAge);
        spG=findViewById(R.id.spGender); spF=findViewById(R.id.spFitness);

        spG.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Nam","Nu"}));
        spF.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"It van dong","Trung binh","Thuong xuyen"}));

        loadExisting();
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadExisting() {
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;
        FirebaseHelper.getInstance().getProfile(uid, t -> {
            if (t.isSuccessful() && t.getResult().exists()) {
                var d = t.getResult();
                if(d.getString("name")!=null) etN.setText(d.getString("name"));
                if(d.getDouble("height")!=null && d.getDouble("height")>0) etH.setText(String.valueOf(d.getDouble("height").intValue()));
                if(d.getDouble("weight")!=null && d.getDouble("weight")>0) etW.setText(String.valueOf(d.getDouble("weight").intValue()));
                if(d.getLong("age")!=null && d.getLong("age")>0) etA.setText(String.valueOf(d.getLong("age")));
            }
        });
    }

    private void save() {
        String name=etN.getText().toString().trim();
        if(TextUtils.isEmpty(name)){etN.setError("Nhap ho ten");return;}
        if(TextUtils.isEmpty(etH.getText().toString())){etH.setError("Nhap chieu cao");return;}
        if(TextUtils.isEmpty(etW.getText().toString())){etW.setError("Nhap can nang");return;}
        if(TextUtils.isEmpty(etA.getText().toString())){etA.setError("Nhap tuoi");return;}

        double h,w; int age;
        try { h=Double.parseDouble(etH.getText().toString()); w=Double.parseDouble(etW.getText().toString());
            age=Integer.parseInt(etA.getText().toString()); } catch(Exception e) { return; }
        if(h<50||h>300){etH.setError("50-300 cm");return;}
        if(w<10||w>500){etW.setError("10-500 kg");return;}
        if(age<5||age>120){etA.setError("5-120");return;}

        // TÍNH TOÁN BMI
        double rawBmi = w / ((h / 100) * (h / 100));
        final double finalBmi = Math.round(rawBmi * 10.0) / 10.0;

        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        // DÙNG MAP ĐỂ CHỈ GHI ĐÈ THÔNG TIN HỒ SƠ, BẢO TOÀN STREAK
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", name);
        updates.put("age", age);
        updates.put("height", h);
        updates.put("weight", w);
        updates.put("gender", spG.getSelectedItem().toString());
        updates.put("fitnessLevel", spF.getSelectedItem().toString());
        updates.put("bmi", finalBmi);

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnCompleteListener(t -> {
                    if(t.isSuccessful()) {
                        Intent i = new Intent(this, BmiResultActivity.class);
                        i.putExtra("bmi", finalBmi);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this,"Lỗi lưu!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
