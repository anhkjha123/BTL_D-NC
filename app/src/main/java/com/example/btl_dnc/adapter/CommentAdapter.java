package com.example.btl_dnc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
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

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {

        Comment c = list.get(i);

        h.tvContent.setText(c.content != null ? c.content : "");

        // ===== TIME =====
        if (c.time != null) {
            String t = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                    .format(c.time.toDate());
            h.tvTime.setText(t);
        } else {
            h.tvTime.setText("");
        }

        // ===== ADMIN =====
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

        // ===== CACHE =====
        if (nameCache.containsKey(c.userId)) {

            h.tvName.setText(nameCache.get(c.userId));

            loadAvatar(h.img, avatarCache.get(c.userId));
            return;
        }

        // ===== LOAD FIRESTORE USER =====
        FirebaseFirestore.getInstance()
                .collection("user")
                .document(c.userId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    String avatar = doc.getString("avatarUrl");

                    if (name == null || name.trim().isEmpty()) {
                        name = "User";
                    }

                    nameCache.put(c.userId, name);
                    avatarCache.put(c.userId, avatar);

                    h.tvName.setText(name);

                    loadAvatar(h.img, avatar);
                });
    }

    // ===== AVATAR LOADER  =====
    private void loadAvatar(ImageView img, String avatarUrl) {

        if (avatarUrl == null || avatarUrl.isEmpty()) {
            img.setImageResource(R.drawable.ic_launcher_background);
            return;
        }

        // URL (Google / Firebase Storage)
        if (avatarUrl.startsWith("http")) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(img);
            return;
        }

        // Base64 fallback
        try {
            String clean = avatarUrl.replace("\n", "").replace(" ", "");

            byte[] decoded = Base64.decode(clean, Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

            img.setImageBitmap(bitmap);

        } catch (Exception e) {
            img.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}