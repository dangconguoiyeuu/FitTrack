package com.fitness.fittrack.activities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.fittrack.R;
import com.fitness.fittrack.adapters.UsageLogAdapter;
import com.fitness.fittrack.models.UsageLog;
import com.fitness.fittrack.utils.UsageLogDatabase;

import java.util.ArrayList;
import java.util.List;

public class UsageHistoryActivity extends AppCompatActivity implements SensorEventListener {
    private static final int PAGE_SIZE = 3;

    private EditText etSearchTime;
    private TextView tvEmpty, tvLoadHint;
    private UsageLogAdapter adapter;
    private UsageLogDatabase database;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean sensorIsNear = false;
    private final List<UsageLog> searchResults = new ArrayList<>();
    private final List<UsageLog> visibleLogs = new ArrayList<>();
    private int visibleCount = 0;
    private boolean isWaitingForSensor = false; // Cờ chờ cảm biến
    private long lastPushTime = 0; // Lưu thời điểm kích hoạt gần nhất

    // Hàm khởi tạo màn hình, ánh xạ UI, cấu hình RecyclerView, sensor và tải lịch sử sử dụng ban đầu.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_history);

        database = UsageLogDatabase.getInstance(this);
        etSearchTime = findViewById(R.id.etSearchTime);
        tvEmpty = findViewById(R.id.tvUsageEmpty);
        tvLoadHint = findViewById(R.id.tvLoadHint);

        adapter = new UsageLogAdapter();
        RecyclerView recyclerView = findViewById(R.id.rvUsageLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearchUsage).setOnClickListener(v -> searchLogs());
        findViewById(R.id.btnClearSearch).setOnClickListener(v -> clearSearch());
        // Tìm đoạn này trong onCreate và sửa lại:
        findViewById(R.id.btnLoadMore).setOnClickListener(v -> {
            // Chỉ bật cờ chờ cảm biến, TUYỆT ĐỐI KHÔNG gọi loadNextBlock() ở đây
            isWaitingForSensor = true;
            tvLoadHint.setText("Hãy đưa tay qua cảm biến để tải dữ liệu...");
            Toast.makeText(this, "Đã sẵn sàng! Hãy vẫy tay qua cảm biến tiệm cận.", Toast.LENGTH_SHORT).show();
        });

        setupProximitySensor();
        loadAllLogs();
    }

    // Hàm lấy toàn bộ lịch sử sử dụng từ SQLite và hiển thị khối 3 bản ghi đầu tiên.
    private void loadAllLogs() {
        searchResults.clear();
        searchResults.addAll(database.getAllLogs());
        resetVisibleBlocks();
    }

    // Hàm tìm lịch sử theo mốc thời gian người dùng nhập, ví dụ 12/05/2026 hoặc 12/05/2026 08.
    private void searchLogs() {
        String keyword = etSearchTime.getText().toString().trim();
        searchResults.clear();
        searchResults.addAll(database.searchByTime(keyword));
        resetVisibleBlocks();
    }

    // Hàm xóa nội dung tìm kiếm và quay lại danh sách toàn bộ lịch sử sử dụng.
    private void clearSearch() {
        etSearchTime.setText("");
        loadAllLogs();
    }

    // Hàm reset danh sách đang hiển thị về rỗng rồi tải khối 3 bản ghi đầu tiên.
    private void resetVisibleBlocks() {
        visibleLogs.clear();
        visibleCount = 0;
        loadNextBlock();
    }

    // Hàm hiển thị thêm tối đa 3 bản ghi tiếp theo, được gọi bởi nút Xem thêm hoặc cảm biến tiệm cận.
    private void loadNextBlock() {
        if (visibleCount >= searchResults.size()) {
            updateListState();
            return;
        }

        int nextLimit = Math.min(visibleCount + PAGE_SIZE, searchResults.size());
        for (int i = visibleCount; i < nextLimit; i++) {
            visibleLogs.add(searchResults.get(i));
        }
        visibleCount = nextLimit;
        adapter.submitList(visibleLogs);
        updateListState();
    }

    // Hàm cập nhật trạng thái rỗng, số lượng bản ghi và hướng dẫn dùng cảm biến tiệm cận.
    private void updateListState() {
        boolean isEmpty = searchResults.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        boolean hasMore = visibleCount < searchResults.size();
        findViewById(R.id.btnLoadMore).setVisibility(hasMore ? View.VISIBLE : View.GONE);
        if (hasMore) {
            tvLoadHint.setText(proximitySensor != null
                    ? "Đưa tay qua cảm biến tiệm cận để xem thêm 3 bản ghi."
                    : "Thiết bị không có cảm biến tiệm cận, dùng nút Xem thêm.");
            tvLoadHint.setVisibility(View.VISIBLE);
        } else {
            tvLoadHint.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvLoadHint.setText(isEmpty ? "" : "Đã hiển thị hết kết quả.");
        }
    }

    // Hàm chuẩn bị cảm biến tiệm cận để dùng cử chỉ che/mở cảm biến cho việc tải thêm kết quả.
    private void setupProximitySensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        if (proximitySensor == null) {
            Toast.makeText(this, "Thiết bị không có cảm biến tiệm cận.", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm đăng ký lắng nghe cảm biến khi màn hình đang hiển thị.
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // Hàm hủy lắng nghe cảm biến khi rời màn hình để tiết kiệm pin.
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // Hàm xử lý dữ liệu cảm biến tiệm cận; mỗi lần đưa tay lại gần sẽ tải thêm một khối 3 bản ghi.
    @Override
    public void onSensorChanged(SensorEvent e) {
        // Kiểm tra đúng loại cảm biến tiệm cận
        if (proximitySensor == null || e.sensor.getType() != Sensor.TYPE_PROXIMITY) return;

        float dist = e.values[0];

        // BƯỚC 1: Phát hiện người dùng đưa tay lại gần (giống lúc hạ người xuống khi pushup)
        if (dist < proximitySensor.getMaximumRange()) {
            sensorIsNear = true;
        }
        // BƯỚC 2: Phát hiện người dùng đưa tay ra xa (giống lúc đẩy người lên)
        else if (sensorIsNear) {
            // Chỉ thực hiện tải dữ liệu nếu trước đó đã nhấn nút "Xem thêm"
            if (isWaitingForSensor) {
                long now = android.os.SystemClock.elapsedRealtime();

                // Kiểm tra thời gian để tránh việc cảm biến bị kích hoạt quá nhạy
                if (now - lastPushTime > 500) {
                    loadNextBlock(); // ĐÂY MỚI LÀ LÚC DỮ LIỆU ĐƯỢC HIỂN THỊ

                    lastPushTime = now;
                    isWaitingForSensor = false; // Tắt chế độ chờ sau khi đã tải xong 3 bản ghi
                    tvLoadHint.setText("Dùng cảm biến để xem thêm tiếp");
                }
            }
            sensorIsNear = false; // Reset trạng thái để chuẩn bị cho lần kích hoạt sau
        }
    }

    // Hàm bắt buộc của SensorEventListener; hiện không cần xử lý thay đổi độ chính xác cảm biến.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
