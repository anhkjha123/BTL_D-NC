package com.example.btl_dnc.adapter;
import com.bumptech.glide.Glide; // Nhớ import Glide lên đầu file
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.R;
import com.example.btl_dnc.ReportDetailActivity;
import com.example.btl_dnc.model.Report;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    Context context;

    List<Report> list;

    public ReportAdapter(Context context, List<Report> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgReport;
        TextView txtTitle, txtDate;
        // Trong lớp ViewHolder:
        TextView txtUserName;
        ImageView imgAvatar;

// Trong hàm tạo ViewHolder:

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

        // ===== TITLE & NAME =====
        h.txtTitle.setText(r.content != null ? r.content : "");
        h.txtUserName.setText(r.name != null ? r.name : "Người dùng ẩn danh");

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

        // ===== LOAD ẢNH BÁO CÁO (IMAGE BASE64) =====
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
                h.imgReport.setBackgroundColor(0xFFDDDDDD);
            }
        } else {
            Glide.with(context).clear(h.imgReport);
            h.imgReport.setBackgroundColor(0xFFDDDDDD);
        }

        // ===== RESET AVATAR (Tránh hiển thị nhầm avatar cũ khi cuộn màn hình) =====
        Glide.with(context).clear(h.imgAvatar);
        h.imgAvatar.setImageResource(R.color.background_gray);

        // ===== TẢI AVATAR NGƯỜI DÙNG TỪ BẢNG 'user' =====
        if (r.userId != null && !r.userId.isEmpty()) {
            // Lấy trực tiếp từ Firestore dựa trên userId của báo cáo
            FirebaseFirestore.getInstance().collection("user").document(r.userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Giả định trường lưu ảnh trong bảng user là "avatarUrl" (khớp với class User của bạn)
                            // Nếu bạn dùng tên khác như "avatar" hay "image", hãy đổi chữ "avatarUrl" bên dưới
                            String avatarData = documentSnapshot.getString("avatarUrl");

                            if (avatarData != null && !avatarData.isEmpty()) {
                                try {
                                    // Kiểm tra xem nó là link (http) hay chuỗi Base64
                                    if (avatarData.startsWith("http")) {
                                        Glide.with(context)
                                                .load(avatarData)
                                                .placeholder(R.color.background_gray)
                                                .circleCrop()
                                                .into(h.imgAvatar);
                                    } else {
                                        // Giải mã Base64 nếu lưu bằng chuỗi
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
            context.startActivity(intent);
        });
    }

    // ===== DECODE BASE64 =====
    private Bitmap decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
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