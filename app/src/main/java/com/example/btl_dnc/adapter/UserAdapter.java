package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.User;
import com.example.btl_dnc.manager.ProfileActivity;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    ArrayList<User> list;
    Context context;

    public UserAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvDetail, tvStatus;
        ImageView img;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvDetail = v.findViewById(R.id.tvDetail);
            img = v.findViewById(R.id.imgAvatar);


        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup p, int v) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, p, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        User u = list.get(i);

        h.tvName.setText(u.name != null ? u.name : "Chưa cập nhật tên");
        h.tvEmail.setText(u.email != null ? u.email : "");
        String avatarData = u.avatarUrl;
        if (avatarData != null && !avatarData.trim().isEmpty()) {
            if (avatarData.startsWith("http")) {
                // Trường hợp 1: Nếu là Link ảnh mạng bình thường
                Glide.with(context)
                        .load(avatarData)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .circleCrop() // Tự động cắt tròn ảnh giống các app chat
                        .into(h.img);
            } else {
                // Trường hợp 2: Nếu ảnh được lưu dưới dạng chuỗi Base64
                try {
                    byte[] imageByteArray = android.util.Base64.decode(avatarData, android.util.Base64.DEFAULT);
                    Glide.with(context)
                            .asBitmap()
                            .load(imageByteArray)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .circleCrop()
                            .into(h.img);
                } catch (Exception e) {
                    h.img.setImageResource(R.drawable.placeholder_image);
                }
            }
        } else {
            // Trường hợp 3: User chưa cài avatar
            h.img.setImageResource(R.drawable.placeholder_image);
        }

        h.tvDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userID", u.id);


            intent.putExtra("USER_STATUS", u.status);
            intent.putExtra("USER_ROLE", u.role);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}