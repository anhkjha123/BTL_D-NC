package com.example.btl_dnc.adapter;

import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.Comment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

    Context context;
    ArrayList<Comment> list;


    Map<String, String> nameCache = new HashMap<>();
    Map<String, String> avatarCache = new HashMap<>();

    public CommentAdapter(Context context, ArrayList<Comment> list) {
        this.context = context;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvTime;
        ImageView img;

        public VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvContent = v.findViewById(R.id.tvContent);
            tvTime = v.findViewById(R.id.tvTime);
            img = v.findViewById(R.id.imgAvatar);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        Comment c = list.get(i);

        h.tvContent.setText(c.content != null ? c.content : "");

        if (c.time != null) {
            String t = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                    .format(c.time.toDate());
            h.tvTime.setText(t);
        } else {
            h.tvTime.setText("");
        }

        if ("admin".equals(c.role)) {
            h.tvName.setText("Quản lý");
            h.img.setImageResource(R.drawable.ic_admin);
            return;
        }

        if (c.userId == null || c.userId.trim().isEmpty()) {
            h.tvName.setText("User");
            h.img.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        if (nameCache.containsKey(c.userId)) {
            h.tvName.setText(nameCache.get(c.userId));
            Glide.with(context)
                    .load(avatarCache.get(c.userId))
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(h.img);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("user")
                .document(c.userId)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    String avatar = doc.getString("avatarUrl");

                    if (name == null || name.trim().isEmpty()) name = "User";

                    nameCache.put(c.userId, name);
                    avatarCache.put(c.userId, avatar);

                    h.tvName.setText(name);

                    Glide.with(context)
                            .load(avatar)
                            .placeholder(R.drawable.ic_launcher_background)
                            .circleCrop()
                            .into(h.img);
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}