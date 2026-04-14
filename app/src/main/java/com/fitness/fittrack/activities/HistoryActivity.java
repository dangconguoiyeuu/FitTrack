package com.fitness.fittrack.activities;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fitness.fittrack.R;
import com.fitness.fittrack.adapters.HistoryAdapter;
import com.fitness.fittrack.models.WorkoutSession;
import com.fitness.fittrack.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b); setContentView(R.layout.activity_history);
        RecyclerView rv = findViewById(R.id.recyclerView);
        TextView empty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getHistory(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<WorkoutSession> list = task.getResult().toObjects(WorkoutSession.class);
                if (list.isEmpty()) { empty.setVisibility(View.VISIBLE); rv.setVisibility(View.GONE); }
                else { rv.setAdapter(new HistoryAdapter(list)); }
            } else {
                Toast.makeText(this, "Loi tai lich su", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
