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



        Glide.with(context)
                .load(u.avatarUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(h.img);

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