package com.fitness.fittrack.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.fittrack.R;
import com.fitness.fittrack.models.WorkoutSession;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.fitness.fittrack.utils.StreakHelper;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

public class PostWorkoutStretchActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_COUNT = "count";
    public static final String EXTRA_TARGET = "target";
    public static final String EXTRA_DISTANCE = "distance";
    public static final String EXTRA_CALORIES = "calories";
    public static final String EXTRA_DURATION = "duration";

    private static final String STRETCH_VIDEO_RAW_NAME = "post_workout_stretch";

    private PlayerView playerView;
    private TextView tvVideoFallback;
    private ExoPlayer player;
    private boolean saving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_workout_stretch);

        playerView = findViewById(R.id.playerStretch);
        tvVideoFallback = findViewById(R.id.tvStretchVideoFallback);
        Button btnFinishWorkout = findViewById(R.id.btnFinishWorkout);

        btnFinishWorkout.setOnClickListener(v -> saveWorkoutAndFinish());
        playStretchVideo();
    }

    private void playStretchVideo() {
        int videoResId = getResources().getIdentifier(STRETCH_VIDEO_RAW_NAME, "raw", getPackageName());
        if (videoResId == 0) {
            playerView.setVisibility(View.GONE);
            tvVideoFallback.setVisibility(View.VISIBLE);
            tvVideoFallback.setText("Chưa tìm thấy video post_workout_stretch.mp4 trong res/raw.");
            return;
        }

        tvVideoFallback.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setMediaItem(MediaItem.fromUri(Uri.parse("android.resource://" + getPackageName() + "/" + videoResId)));
        player.prepare();
        player.play();
    }

    private void saveWorkoutAndFinish() {
        if (saving) return;
        saving = true;

        releasePlayer();

        String uid = FirebaseHelper.getInstance().getUid();
        int count = getIntent().getIntExtra(EXTRA_COUNT, 0);
        if (uid == null || count <= 0) {
            finish();
            return;
        }

        if (FirebaseHelper.getInstance().isOfflineSession()) {
            Toast.makeText(getApplicationContext(), "Đã hoàn thành buổi tập offline!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        int target = getIntent().getIntExtra(EXTRA_TARGET, 0);
        double distance = getIntent().getDoubleExtra(EXTRA_DISTANCE, 0);
        double calories = getIntent().getDoubleExtra(EXTRA_CALORIES, 0);
        long duration = getIntent().getLongExtra(EXTRA_DURATION, 0);

        WorkoutSession session = new WorkoutSession(uid, type, count, target, distance, calories, duration);
        FirebaseHelper.getInstance().saveWorkout(session, task -> {
            saving = false;
            if (task.isSuccessful()) {
                StreakHelper.updateAfterWorkout(uid, null);
                Toast.makeText(getApplicationContext(), "Đã lưu kết quả tập luyện!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Lỗi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
