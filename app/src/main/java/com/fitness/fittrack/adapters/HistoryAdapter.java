package com.fitness.fittrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fitness.fittrack.R;
import com.fitness.fittrack.models.WorkoutSession;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
    private final List<WorkoutSession> list;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<WorkoutSession> l) { this.list = l; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_history, p, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        WorkoutSession s = list.get(i);

        // 1. Loại bài tập & Calo
        h.tvType.setText(s.getTypeName());
        h.tvCalo.setText((int)s.getCalories() + " kcal");

        // 2. Reps mỗi hiệp (Dựa trên TargetCount)
        h.tvRepsPerSet.setText("Reps : " + s.getTargetCount());

        // 3. Tính số hiệp (Tổng / Target)
        int completedSets = (s.getTargetCount() > 0) ? (s.getCount() / s.getTargetCount()) : 1;
        h.tvSetsCount.setText("Sets : " + completedSets);

        // 4. Hiển thị Tổng tích lũy kèm đơn vị đúng
        String unit = "running".equals(s.getType()) ? "bước" : "Reps";
        h.tvTotalCount.setText("Tổng: " + s.getCount() + " " + unit);

        // 5. Thời gian & Ngày tháng
        h.tvDuration.setText("Thời gian: " + s.getFormattedDuration());
        if(s.getDate() != null) h.tvDate.setText(sdf.format(s.getDate().toDate()));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvCalo, tvRepsPerSet, tvSetsCount, tvTotalCount, tvDuration, tvDate;
        VH(View v) {
            super(v);
            tvType = v.findViewById(R.id.tvType);
            tvCalo = v.findViewById(R.id.tvCalo);
            tvRepsPerSet = v.findViewById(R.id.tvRepsPerSet);
            tvSetsCount = v.findViewById(R.id.tvSetsCount);
            tvTotalCount = v.findViewById(R.id.tvTotalCount);
            tvDuration = v.findViewById(R.id.tvDuration);
            tvDate = v.findViewById(R.id.tvDate);
        }
    }
}