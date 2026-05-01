package com.fitness.fittrack.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.WorkoutSession;
import com.fitness.fittrack.utils.FirebaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.*;

public class StatisticsActivity extends AppCompatActivity {
    private BarChart barChart;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        String uid = FirebaseHelper.getInstance().getUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getHistory(uid, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<WorkoutSession> sessions = task.getResult().toObjects(WorkoutSession.class);
                processAndDisplayCharts(sessions);
            }
        });
    }

    private void processAndDisplayCharts(List<WorkoutSession> sessions) {
        // ==========================================
        // 1. BIỂU ĐỒ CỘT (Calo tiêu thụ)
        // ==========================================
        List<BarEntry> barEntries = new ArrayList<>();
        // Nhãn cho trục X
        final String[] days = new String[]{"", "T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        // Dữ liệu mẫu 7 ngày
        barEntries.add(new BarEntry(1, 150f));
        barEntries.add(new BarEntry(2, 210f));
        barEntries.add(new BarEntry(3, 180f));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Năng lượng tiêu thụ (kcal)");
        barDataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextSize(13f);
        barDataSet.setValueTextColor(android.graphics.Color.BLACK);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // --- TINH CHỈNH TRỤC X (TRỤC NGANG) ---
        com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter(days));
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM); // Đưa nhãn xuống dưới
        xAxis.setDrawGridLines(false); // Ẩn lưới dọc cho thoáng
        xAxis.setGranularity(1f); // Khoảng cách giữa các cột là 1 đơn vị
        xAxis.setLabelCount(7);
        xAxis.setTextSize(12f);

        // --- TINH CHỈNH TRỤC Y (TRỤC DỌC) ---
        com.github.mikephil.charting.components.YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Bắt đầu từ 0 kcal (Quan trọng!)
        leftAxis.setTextSize(12f);

        barChart.getAxisRight().setEnabled(false); // Ẩn trục bên phải cho đỡ rối
        barChart.getDescription().setEnabled(false); // Xóa chữ Description Label

        barChart.animateY(1000);
        barChart.invalidate();


        // ==========================================
        // 2. BIỂU ĐỒ TRÒN (Phân bổ bài tập)
        // ==========================================
        int run = 0, push = 0, sit = 0;
        for (WorkoutSession s : sessions) {
            if ("running".equals(s.getType())) run++;
            else if ("pushup".equals(s.getType())) push++;
            else if ("situp".equals(s.getType())) sit++;
        }

        List<PieEntry> pieEntries = new ArrayList<>();
        if (run > 0) pieEntries.add(new PieEntry(run, "Chạy bộ"));
        if (push > 0) pieEntries.add(new PieEntry(push, "Chống đẩy"));
        if (sit > 0) pieEntries.add(new PieEntry(sit, "Gập bụng"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.JOYFUL_COLORS);

        // Làm đẹp biểu đồ tròn
        pieDataSet.setValueTextSize(15f);
        pieDataSet.setValueTextColor(android.graphics.Color.BLACK);
        pieChart.setDrawEntryLabels(false); // Chỉ hiện chú thích ở dưới, không hiện trong bánh
        pieChart.getDescription().setEnabled(false);

        pieChart.setData(new PieData(pieDataSet));
        pieChart.setCenterText("Tỉ lệ tập");
        pieChart.setCenterTextSize(16f);
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }
}