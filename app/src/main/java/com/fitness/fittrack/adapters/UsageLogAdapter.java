package com.fitness.fittrack.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.fittrack.R;
import com.fitness.fittrack.models.UsageLog;

import java.util.ArrayList;
import java.util.List;

public class UsageLogAdapter extends RecyclerView.Adapter<UsageLogAdapter.VH> {
    private final List<UsageLog> logs = new ArrayList<>();

    // Hàm cập nhật dữ liệu hiển thị cho RecyclerView sau khi tải hoặc tìm kiếm lịch sử.
    public void submitList(List<UsageLog> newLogs) {
        logs.clear();
        logs.addAll(newLogs);
        notifyDataSetChanged();
    }

    // Hàm tạo ViewHolder từ layout item_usage_log cho từng dòng lịch sử sử dụng.
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usage_log, parent, false);
        return new VH(view);
    }

    // Hàm gán dữ liệu UsageLog vào các TextView của một dòng trong danh sách.
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UsageLog log = logs.get(position);
        holder.tvScreen.setText(log.getScreenName());
        holder.tvFeature.setText("Chức năng: " + log.getFeatureName());
        holder.tvTime.setText(log.getTimeText());
        holder.tvUser.setText("Người dùng: " + log.getUserName());
    }

    // Hàm trả về số bản ghi hiện đang hiển thị trong RecyclerView.
    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvScreen, tvFeature, tvTime, tvUser;

        // Hàm ánh xạ các TextView trong layout item_usage_log vào ViewHolder.
        VH(View itemView) {
            super(itemView);
            tvScreen = itemView.findViewById(R.id.tvUsageScreen);
            tvFeature = itemView.findViewById(R.id.tvUsageFeature);
            tvTime = itemView.findViewById(R.id.tvUsageTime);
            tvUser = itemView.findViewById(R.id.tvUsageUser);
        }
    }
}
