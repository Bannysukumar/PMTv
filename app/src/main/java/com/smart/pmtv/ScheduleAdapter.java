package com.smart.pmtv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private Context context;
    private List<ScheduleModel> scheduleList;
    private NavController navController;

    public ScheduleAdapter(Context context, List<ScheduleModel> scheduleList, NavController navController) {
        this.context = context;
        this.scheduleList = scheduleList;
        this.navController = navController;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        ScheduleModel schedule = scheduleList.get(position);

        holder.tvProgramName.setText(schedule.getProgramName());
        holder.tvProgramHost.setText("Hosted by " + schedule.getHostName());

        if (schedule.getTimeString() != null) {
            String[] parts = schedule.getTimeString().split(" ");
            if (parts.length == 2) {
                holder.tvScheduleTime.setText(parts[0]);
                holder.tvScheduleAmPm.setText(parts[1]);
            } else {
                holder.tvScheduleTime.setText(schedule.getTimeString());
                holder.tvScheduleAmPm.setText("");
            }
        }

        holder.btnSetReminder.setOnClickListener(v -> {
            UserManager.getInstance().requireLogin(context, navController, "set reminders");
            if (UserManager.getInstance().isAuthenticated()) {
                Toast.makeText(context, "Reminder set for " + schedule.getProgramName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public void updateList(List<ScheduleModel> newList) {
        scheduleList.clear();
        scheduleList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvScheduleTime, tvScheduleAmPm, tvProgramName, tvProgramHost;
        ImageButton btnSetReminder;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScheduleTime = itemView.findViewById(R.id.tvScheduleTime);
            tvScheduleAmPm = itemView.findViewById(R.id.tvScheduleAmPm);
            tvProgramName = itemView.findViewById(R.id.tvProgramName);
            tvProgramHost = itemView.findViewById(R.id.tvProgramHost);
            btnSetReminder = itemView.findViewById(R.id.btnSetReminder);
        }
    }
}
