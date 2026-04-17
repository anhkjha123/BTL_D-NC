package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.NewsDetailActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.News;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    Context context;
    ArrayList<News> list;
    String userRole;
    OnNewsDeleteListener deleteListener;

    public static final int TYPE_MANAGER = 1;
    public static final int TYPE_PROFILE = 2;
    private int currentType;

    public interface OnNewsDeleteListener {
        void onDeleteClick(String newsId, int position);
    }

    public NewsAdapter(Context context, ArrayList<News> list, String userRole, int currentType, OnNewsDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.userRole = userRole;
        this.currentType = currentType;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getItemViewType(int position) {
        return currentType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_PROFILE) {
            view = LayoutInflater.from(context).inflate(R.layout.item_admin_post_list, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_manager_news, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        News n = list.get(i);

        // 1. Hiển thị dữ liệu bài viết
        if (h.tvTitle != null) {
            h.tvTitle.setText(n.title != null ? n.title : "");
        }
        
        if (n.createAt != null && h.tvDate != null) {
            String time = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(n.createAt.toDate());
            h.tvDate.setText(time);
        }

        if (h.img != null) {
            if (n.imageBase64 != null && !n.imageBase64.isEmpty()) {

                // Nếu là URL (http/https)
                if (n.imageBase64.startsWith("http")) {
                    Glide.with(context)
                            .load(n.imageBase64)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .centerCrop()
                            .into(h.img);

                } else {
                    // Base64
                    try {
                        byte[] decoded = android.util.Base64.decode(n.imageBase64, android.util.Base64.DEFAULT);
                        Glide.with(context)
                                .asBitmap()
                                .load(decoded)
                                .centerCrop()
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.placeholder_image)
                                .into(h.img);
                    } catch (Exception e) {
                        h.img.setImageResource(R.drawable.placeholder_image);
                    }
                }

            } else {
                h.img.setImageResource(R.drawable.placeholder_image);
            }
        }

        // 2. Tự động load thông tin Admin đăng bài
        if (n.authorID != null) {
            FirebaseFirestore.getInstance().collection("user").document(n.authorID)
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists() && h.tvAuthorName != null) {
                            h.tvAuthorName.setText(doc.getString("name"));
                            if (h.imgAuthorAvatar != null) {
                                Glide.with(context).load(doc.getString("avatarUrl"))
                                        .circleCrop().placeholder(R.drawable.placeholder_image).into(h.imgAuthorAvatar);
                            }
                        }
                    });
        }

        // 3. Phân quyền nút Xóa
        if (h.btnDeleteNews != null) {
            if ("ADMIN".equals(userRole)) {
                h.btnDeleteNews.setVisibility(View.VISIBLE);
                h.btnDeleteNews.setOnClickListener(v -> {
                    if (deleteListener != null) deleteListener.onDeleteClick(n.id, i);
                });
            } else {
                h.btnDeleteNews.setVisibility(View.INVISIBLE);
            }
        }

        // 4. Click xem chi tiết
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("newsID", n.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAuthorName;
        ImageView img, imgAuthorAvatar;
        ImageButton btnDeleteNews;

        public ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvNewsItemTitle);
            tvDate = v.findViewById(R.id.tvNewsItemDate);
            img = v.findViewById(R.id.imgNewsCover);
            btnDeleteNews = v.findViewById(R.id.btnDeleteNews);
            tvAuthorName = v.findViewById(R.id.tvAuthorName); // Có thể null ở layout Manager
            imgAuthorAvatar = v.findViewById(R.id.imgAuthorAvatar); // Có thể null ở layout Manager
        }
    }
}