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
        TextView tvName, tvEmail, tvDetail, tvStatus; // 1. BỔ SUNG tvStatus NẾU BẠN MUỐN HIỂN THỊ
        ImageView img;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvDetail = v.findViewById(R.id.tvDetail);
            img = v.findViewById(R.id.imgAvatar);

            // 2. ÁNH XẠ tvStatus (Đảm bảo trong file item_user.xml của bạn đã có 1 TextView mang id này)
            // tvStatus = v.findViewById(R.id.tvStatus);
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

        // 3. HIỂN THỊ STATUS LÊN GIAO DIỆN (Bỏ comment nếu bạn có dùng tvStatus ở trên)
        /*
        if (u.status != null && !u.status.isEmpty()) {
            h.tvStatus.setText(u.status);
            h.tvStatus.setVisibility(View.VISIBLE);
        } else {
            h.tvStatus.setVisibility(View.GONE);
        }
        */

        Glide.with(context)
                .load(u.avatarUrl)
                .placeholder(R.drawable.placeholder_image) // Nên dùng placeholder có bo góc
                .into(h.img);

        h.tvDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userID", u.id);

            // 4. TRUYỀN STATUS SANG MÀN HÌNH CHI TIẾT
            intent.putExtra("USER_STATUS", u.status);
            intent.putExtra("USER_ROLE", u.role); // Có thể truyền thêm cả role nếu cần

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}