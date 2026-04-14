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

    public HistoryAdapter(List<WorkoutSession> l) { list = l; }
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_history, p, false)); }
    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        WorkoutSession s = list.get(i);
        h.tvType.setText(s.getTypeName());
        h.tvDuration.setText(s.getFormattedDuration());
        h.tvCount.setText("running".equals(s.getType()) ? s.getCount()+" buoc | "+s.getDistance()+" km" : s.getCount()+"/"+s.getTargetCount()+" nhip");
        h.tvCalo.setText((int)s.getCalories()+" kcal");
        if(s.getDate()!=null) h.tvDate.setText(sdf.format(s.getDate().toDate()));
    }
    @Override public int getItemCount() { return list.size(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvCount, tvDuration, tvDate, tvCalo;
        VH(View v) { super(v);
            tvType=v.findViewById(R.id.tvType); tvCount=v.findViewById(R.id.tvCount);
            tvDuration=v.findViewById(R.id.tvDuration); tvDate=v.findViewById(R.id.tvDate);
            tvCalo=v.findViewById(R.id.tvCalo);
        }
    }
}
