package com.example.btl_dnc.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.CreateNewsActivity;
import com.example.btl_dnc.CreatePaymentActivity;
import com.example.btl_dnc.EditProfileActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.NewsAdapter;
import com.example.btl_dnc.model.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ManagerProfileFragment extends Fragment {

    private TextView tvAdminName, tvAdminEmail, tvAdminStatus, tvSortBy;
    private ImageView imgAvatar, btnSettings, btnViewGrid, btnViewList;
    private RecyclerView rvAdminPosts;
    private Button btnCreatePost;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;

    private NewsAdapter adapter;
    private ArrayList<News> postList;
    private ListenerRegistration profileListener;
    private ListenerRegistration postsListener;

    private boolean isGridView = false;
    private boolean isSortDescending = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_manager, container, false);

        initViews(view);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            postList = new ArrayList<>();

            // 1. Initialize Adapter ONLY ONCE
            adapter = new NewsAdapter(getContext(), postList, "ADMIN", NewsAdapter.TYPE_PROFILE, this::onDeleteNewsClick);

            // 2. Set default LayoutManager and Adapter ONLY ONCE
            rvAdminPosts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvAdminPosts.setAdapter(adapter);

            // 3. Update Layout state if needed
            updateLayoutManager();

            // 4. Load Data
            loadAdminProfile();
            loadAdminPosts();
        }

        setupListeners();
        return view;
    }

    private void initViews(View view) {
        tvAdminName = view.findViewById(R.id.tvAdminName);
        tvAdminEmail = view.findViewById(R.id.tvAdminEmail);
        tvAdminStatus = view.findViewById(R.id.tvAdminStatus);
        tvSortBy = view.findViewById(R.id.tvSortBy);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnViewGrid = view.findViewById(R.id.btnViewGrid);
        btnViewList = view.findViewById(R.id.btnViewList);
        rvAdminPosts = view.findViewById(R.id.rvAdminPosts);
        btnCreatePost = view.findViewById(R.id.btnCreatePost);
    }

    private void setupListeners() {
        btnCreatePost.setOnClickListener(v -> {
            String adminName = tvAdminName.getText().toString();

            // Mảng chứa các tùy chọn
            String[] options = {"Tạo tin tức (News) mới", "Tạo thông báo thu phí (Payment)"};

            new AlertDialog.Builder(requireContext())
                    .setTitle("Bạn muốn tạo gì?")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Người dùng chọn "Tạo tin tức mới"
                            Intent intent = new Intent(getContext(), CreateNewsActivity.class);
                            intent.putExtra("ADMIN_NAME", adminName);
                            startActivity(intent);
                        } else if (which == 1) {
                            // Người dùng chọn "Tạo thông báo đóng phí"
                            Intent intent = new Intent(getContext(), CreatePaymentActivity.class);
                            intent.putExtra("ADMIN_NAME", adminName);
                            startActivity(intent);
                        }
                    })
                    .show();
        });
        btnViewGrid.setOnClickListener(v -> {
            isGridView = true;
            updateLayoutManager();
        });
        btnSettings.setOnClickListener(v -> {
            // Chuyển sang màn hình Chỉnh sửa hồ sơ
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });
        btnViewList.setOnClickListener(v -> {
            isGridView = false;
            updateLayoutManager();
        });

        tvSortBy.setOnClickListener(v -> showSortMenu());
    }

    private void updateLayoutManager() {
        if (isGridView) {
            btnViewGrid.setColorFilter(Color.parseColor("#03A9F4"));
            btnViewList.setColorFilter(Color.parseColor("#000000"));
            rvAdminPosts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            btnViewList.setColorFilter(Color.parseColor("#03A9F4"));
            btnViewGrid.setColorFilter(Color.parseColor("#000000"));
            rvAdminPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showSortMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), tvSortBy);
        popupMenu.getMenu().add("Mới nhất");
        popupMenu.getMenu().add("Cũ nhất");
        popupMenu.setOnMenuItemClickListener(item -> {
            isSortDescending = item.getTitle().toString().equals("Mới nhất");
            tvSortBy.setText(isSortDescending ? "Mới" : "Cũ");
            loadAdminPosts();
            return true;
        });
        popupMenu.show();
    }


    private void loadAdminProfile() {
        profileListener= db.collection("user").document(currentUserId)
                .addSnapshotListener((doc, error) -> {
                    // SAFETY CHECK: Prevent crash if fragment is detached
                    if (!isAdded() || getContext() == null) return;

                    if (error != null) return;

                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String status = doc.getString("status");
                        String avatarUrl = doc.getString("avatarUrl");

                        tvAdminName.setText(name != null ? name : "Độ Rothy");
                        tvAdminEmail.setText(email != null ? email : mAuth.getCurrentUser().getEmail());
                        tvAdminStatus.setText(status != null ? status : "Xinh nhưng bị khùng?!");

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(requireContext()) // Use requireContext() for safety
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.placeholder_image)
                                    .circleCrop()
                                    .into(imgAvatar);
                        }
                    }
                });
    }

    private void loadAdminPosts() {
        if (postsListener != null) postsListener.remove();

        Query query = db.collection("news")
                .whereEqualTo("authorID", currentUserId);


        query = query.orderBy("createAt", isSortDescending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        postsListener = query.addSnapshotListener((value, error) -> {
            if (!isAdded() || getContext() == null) return;

            if (error != null) {
                android.util.Log.e("FirestoreError", "Error fetching posts: " + error.getMessage());

                Toast.makeText(getContext(), "Lỗi tải bài viết: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            if (value != null) {
                postList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    News news = doc.toObject(News.class);
                    if (news != null) {
                        news.id = doc.getId();
                        postList.add(news);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileListener != null) {
            profileListener.remove();
        }
        if (postsListener != null) {
            postsListener.remove();
        }
    }
    private void onDeleteNewsClick(String newsId, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa bài viết")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("news").document(newsId).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}