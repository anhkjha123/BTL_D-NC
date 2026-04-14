package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.R;
import com.example.btl_dnc.ReportDetailActivity;
import com.example.btl_dnc.model.Report;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    Context context;
    List<Report> list;
    String userRole;
    public ReportAdapter(Context context, List<Report> list, String userRole) {
        this.context = context;
        this.list = list;
        this.userRole = userRole;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgReport, imgAvatar;
        TextView txtTitle, txtDate, txtUserName;

        public ViewHolder(View v) {
            super(v);
            imgReport = v.findViewById(R.id.imgReport);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtDate = v.findViewById(R.id.txtDate);
            txtUserName = v.findViewById(R.id.txtUserName);
            imgAvatar = v.findViewById(R.id.imgAvatar);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        Report r = list.get(i);

        // ===== TITLE  =====
        if (r.title != null && !r.title.trim().isEmpty()) {
            h.txtTitle.setText(r.title);
        } else if (r.content != null && !r.content.trim().isEmpty()) {
            // fallback cho dữ liệu cũ
            h.txtTitle.setText(r.content);
        } else {
            h.txtTitle.setText("");
        }

        // ===== USER NAME =====
        h.txtUserName.setText(
                (r.name != null && !r.name.isEmpty())
                        ? r.name
                        : "Người dùng ẩn danh"
        );

        // ===== DATE =====
        if (r.createdAt > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(r.createdAt);

            String date = "Ngày " + c.get(Calendar.DAY_OF_MONTH)
                    + " Tháng " + (c.get(Calendar.MONTH) + 1)
                    + " Năm " + c.get(Calendar.YEAR);

            h.txtDate.setText(date);
        } else {
            h.txtDate.setText("");
        }

        // ===== IMAGE REPORT =====
        if (r.imageBase64 != null && !r.imageBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(r.imageBase64, Base64.DEFAULT);

                Glide.with(context)
                        .asBitmap()
                        .load(imageBytes)
                        .placeholder(R.color.background_gray)
                        .error(R.color.background_gray)
                        .into(h.imgReport);

            } catch (Exception e) {
                e.printStackTrace();
                h.imgReport.setBackgroundColor(0xFFDDDDDD);
            }
        } else {
            Glide.with(context).clear(h.imgReport);
            h.imgReport.setBackgroundColor(0xFFDDDDDD);
        }

        // ===== RESET AVATAR =====
        Glide.with(context).clear(h.imgAvatar);
        h.imgAvatar.setImageResource(R.color.background_gray);

        // ===== LOAD AVATAR FROM FIRESTORE =====
        if (r.userId != null && !r.userId.isEmpty()) {

            FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(r.userId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (doc.exists()) {
                            String avatarData = doc.getString("avatarUrl");

                            if (avatarData != null && !avatarData.isEmpty()) {

                                try {
                                    if (avatarData.startsWith("http")) {

                                        Glide.with(context)
                                                .load(avatarData)
                                                .placeholder(R.color.background_gray)
                                                .circleCrop()
                                                .into(h.imgAvatar);

                                    } else {

                                        byte[] avatarBytes = Base64.decode(avatarData, Base64.DEFAULT);

                                        Glide.with(context)
                                                .asBitmap()
                                                .load(avatarBytes)
                                                .placeholder(R.color.background_gray)
                                                .circleCrop()
                                                .into(h.imgAvatar);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }

        // ===== CLICK ITEM =====
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("id", r.id);
            intent.putExtra("USER_ROLE", this.userRole);
            context.startActivity(intent);
        });
    }

    // ===== OPTIONAL: BASE64 =====
    private Bitmap decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== UPDATE LIST =====
    public void updateList(List<Report> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}