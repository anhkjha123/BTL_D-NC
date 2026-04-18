package com.example.btl_dnc;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.adapter.NotificationIncidentAdapter;
import com.example.btl_dnc.model.ManagerNotification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class NotificationIncidentActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvNotifications;
    private TextView tvEmpty;

    private NotificationIncidentAdapter adapter;
    private ArrayList<ManagerNotification> notiList;

    private ListenerRegistration notiListener;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_incident);

        btnBack = findViewById(R.id.btnBack);
        rvNotifications = findViewById(R.id.rvIncidentNotifications);

        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        // Setup Adapter
        notiList = new ArrayList<>();
        adapter = new NotificationIncidentAdapter(this, notiList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        loadIncidentNotifications();
    }

    private void loadIncidentNotifications() {
        // CÁCH SỬA: Tạm bỏ whereEqualTo để tránh lỗi Index của Firebase.
        // Chỉ dùng orderBy để lấy toàn bộ thông báo mới nhất.
        notiListener = db.collection("managernotifications")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        // Hiển thị hẳn lỗi ra màn hình để bạn dễ thấy (thay vì giấu trong Logcat)
                        Toast.makeText(this, "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        android.util.Log.e("NotiError", error.getMessage());
                        return;
                    }

                    if (value != null) {
                        notiList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            ManagerNotification noti = doc.toObject(ManagerNotification.class);
                            if (noti != null) {
                                // CÁCH SỬA: Tự lọc thủ công bằng Java ở đây
                                if ("INCIDENT_ALERT".equals(noti.type)) {
                                    noti.id = doc.getId();
                                    notiList.add(noti);
                                }
                            }
                        }

                        // Kiểm tra xem danh sách có dữ liệu không
                        if (notiList.isEmpty()) {
                            Toast.makeText(this, "Chưa có thông báo nào!", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notiListener != null) {
            notiListener.remove();
        }
    }
}