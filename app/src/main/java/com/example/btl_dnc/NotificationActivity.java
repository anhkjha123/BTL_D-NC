package com.example.btl_dnc;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.adapter.NotificationAdapter;
import com.example.btl_dnc.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView rv;
    ArrayList<Notification> list;
    NotificationAdapter adapter;
    ImageView btnBack;

    ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rv = findViewById(R.id.rvNoti);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        list = new ArrayList<>();
        adapter = new NotificationAdapter(this, list);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadNoti();
    }

    void loadNoti() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish(); // đóng màn hình luôn
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        listener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value == null) return;

                    list.clear();

                    for (DocumentSnapshot d : value) {
                        Notification n = d.toObject(Notification.class);
                        if (n != null) {
                            n.id = d.getId();
                            list.add(n);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}