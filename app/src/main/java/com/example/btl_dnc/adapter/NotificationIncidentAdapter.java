package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.R;
import com.example.btl_dnc.ReportDetailActivity;
import com.example.btl_dnc.model.ManagerNotification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NotificationIncidentAdapter extends RecyclerView.Adapter<NotificationIncidentAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ManagerNotification> list;

    public NotificationIncidentAdapter(Context context, ArrayList<ManagerNotification> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification_incident, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        ManagerNotification noti = list.get(i);

        h.tvTitle.setText(noti.title != null ? noti.title : "Thông báo mới");
        h.tvMessage.setText(noti.message != null ? noti.message : "");

        if (noti.getCreateAt() != null) {
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(noti.getCreateAt().toDate());
            h.tvTime.setText(time);
        }

        // Đổi giao diện nếu chưa đọc
        if (!noti.isRead()) {
            h.dotUnread.setVisibility(View.VISIBLE);
            h.tvTitle.setTypeface(null, Typeface.BOLD);
            h.tvMessage.setTypeface(null, Typeface.BOLD);
            h.tvMessage.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            h.dotUnread.setVisibility(View.INVISIBLE);
            h.tvTitle.setTypeface(null, Typeface.NORMAL);
            h.tvMessage.setTypeface(null, Typeface.NORMAL);
            h.tvMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Sự kiện click vào thông báo
        h.itemView.setOnClickListener(v -> {
            // Đánh dấu đã đọc trên Firebase
            if (!noti.isRead()) {
                FirebaseFirestore.getInstance().collection("managernotifications").document(noti.id)
                        .update("isRead", true);
            }

            if (noti.incidentId != null && !noti.incidentId.isEmpty()) {
                Intent intent = new Intent(context, ReportDetailActivity.class);
                intent.putExtra("id", noti.incidentId);
                intent.putExtra("USER_ROLE", "ADMIN");// Gửi ID của báo cáo/sự cố đi
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Không tìm thấy ID sự cố!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View dotUnread;

        public ViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvNotiTitle);
            tvMessage = v.findViewById(R.id.tvNotiMessage);
            tvTime = v.findViewById(R.id.tvNotiTime);
            dotUnread = v.findViewById(R.id.dotUnread);
        }
    }
    public int getUnreadCount() {
        int count = 0;
        if (list != null) {
            for (ManagerNotification noti : list) {
                if (!noti.isRead()) {
                    count++;
                }
            }
        }
        return count;
    }
}