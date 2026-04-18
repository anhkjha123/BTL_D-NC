package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.NewsDetailActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.ReportDetailActivity;
import com.example.btl_dnc.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    Context context;
    ArrayList<Notification> list;

    public NotificationAdapter(Context context, ArrayList<Notification> list) {
        this.context = context;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView imgType;
        View dot;

        public VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            imgType = v.findViewById(R.id.imgType);
            dot = v.findViewById(R.id.dot);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(VH h, int i) {

        Notification n = list.get(i);

        h.tvTitle.setText(n.title);
        h.tvMessage.setText(n.message);

        // ===== TIME =====
        if (n.createdAt != null) {
            String t = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                    .format(n.createdAt.toDate());
            h.tvTime.setText(t);
        }

        // ===== TYPE ICON =====
        if ("news".equals(n.type)) {
            h.imgType.setImageResource(R.drawable.ic_news);
        } else {
            h.imgType.setImageResource(R.drawable.ic_report);
        }

        // ===== DOT =====
        h.dot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        // ===== CLICK =====
        h.itemView.setOnClickListener(v -> {

            // đánh dấu đã đọc
            if (!n.isRead()) {
                FirebaseFirestore.getInstance()
                        .collection("notifications")
                        .document(n.id)
                        .update("isRead", true);
            }

            // ===== MỞ THEO TYPE =====
            if ("news".equals(n.type)) {

                Intent intent = new Intent(context, NewsDetailActivity.class);
                intent.putExtra("newsID", n.refId);
                context.startActivity(intent);

            } else if ("reports".equals(n.type)) {

                Intent intent = new Intent(context, ReportDetailActivity.class);
                intent.putExtra("id", n.refId);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
