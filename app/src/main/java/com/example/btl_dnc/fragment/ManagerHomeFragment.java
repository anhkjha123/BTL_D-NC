package com.example.btl_dnc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.NotificationIncidentActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.UserAdapter;
import com.example.btl_dnc.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class ManagerHomeFragment extends Fragment {

    RecyclerView rv;
    ArrayList<User> list;
    UserAdapter adapter;
    private TextView tvNotificationBadge;

    // Thêm biến để quản lý việc lắng nghe thông báo
    private ListenerRegistration notiListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_manager, container, false);

        rv = v.findViewById(R.id.rvUser);
        ImageView btnNoti = v.findViewById(R.id.btnNotification);
        tvNotificationBadge = v.findViewById(R.id.tvNotificationBadge);

        list = new ArrayList<>();
        adapter = new UserAdapter(getContext(), list);

        btnNoti.setOnClickListener(view -> {
            // Chuyển sang màn hình thông báo sự cố dành cho Manager
            Intent intent = new Intent(getContext(), NotificationIncidentActivity.class);
            startActivity(intent);
        });

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        loadUsers();

        // Gọi hàm lắng nghe thông báo
        listenUnreadNotifications();

        return v;
    }

    // ===== LẮNG NGHE SỐ LƯỢNG THÔNG BÁO CHƯA ĐỌC =====
    private void listenUnreadNotifications() {
        notiListener = FirebaseFirestore.getInstance().collection("managernotifications")
                .whereEqualTo("isRead", false) // Chỉ lọc những cái chưa đọc
                .addSnapshotListener((value, error) -> {
                    // Check an toàn chống crash
                    if (!isAdded() || getContext() == null) return;

                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        // Số lượng document lấy về chính là số thông báo chưa đọc
                        int unreadCount = value.size();
                        updateNotificationBadge(unreadCount);
                    }
                });
    }

    private void updateNotificationBadge(int unreadCount) {
        if (unreadCount > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            // Nếu số thông báo > 99 thì chỉ hiện "99+" cho đỡ bị tràn khung đỏ
            if (unreadCount > 99) {
                tvNotificationBadge.setText("99+");
            } else {
                tvNotificationBadge.setText(String.valueOf(unreadCount));
            }
        } else {
            // Nếu không có thông báo thì ẩn cục đỏ đi
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance()
                .collection("user")
                .whereEqualTo("role", "resident")
                .get()
                .addOnSuccessListener(q -> {

                    list.clear();

                    for (DocumentSnapshot doc : q) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            u.id = doc.getId();
                            list.add(u);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Lỗi: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // Đừng quên hủy Listener khi thoát Fragment để app không bị sập hay tốn RAM
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (notiListener != null) {
            notiListener.remove();
        }
    }
}