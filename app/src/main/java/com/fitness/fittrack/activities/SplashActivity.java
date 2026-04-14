package com.fitness.fittrack.activities;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.utils.FirebaseHelper;

public class SplashActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this,
                FirebaseHelper.getInstance().getCurrentUser() != null
                    ? HomeActivity.class : LoginActivity.class));
            finish();
        }, 2000);
    }
}
