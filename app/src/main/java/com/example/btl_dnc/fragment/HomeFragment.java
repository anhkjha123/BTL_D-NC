package com.example.btl_dnc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.NotificationActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.NewsAdapter;
import com.example.btl_dnc.model.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView rv;
    ArrayList<News> list;
    NewsAdapter adapter;

    ImageView btnNoti;
    TextView tvBadge, tvWelcome;

    ListenerRegistration notiListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        // ===== VIEW =====
        rv = v.findViewById(R.id.rvNews);
        btnNoti = v.findViewById(R.id.btnNoti);
        tvBadge = v.findViewById(R.id.tvBadge);
        tvWelcome = v.findViewById(R.id.tvWelcome);


        // ===== RECYCLER =====
        list = new ArrayList<>();


        adapter = new NewsAdapter(
                getContext(),
                list,
                "ADMIN",
                NewsAdapter.TYPE_MANAGER,
                null
        );
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        // ===== CLICK NOTIFICATION =====
        btnNoti.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), NotificationActivity.class));
        });

        loadUser();
        loadNews();
        listenNotification();

        return v;
    }

    // ===== LOAD NEWS =====
    void loadNews() {
        FirebaseFirestore.getInstance()
                .collection("news")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value == null) return;

                    list.clear();

                    for (DocumentSnapshot d : value.getDocuments()) {
                        News n = d.toObject(News.class);
                        if (n != null) {
                            n.id = d.getId();
                            list.add(n);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // ===== LISTEN NOTIFICATION REALTIME =====
    void listenNotification() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (notiListener != null) notiListener.remove();

        notiListener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value == null) return;

                    int count = value.size();

                    if (count > 0) {
                        tvBadge.setVisibility(View.VISIBLE);
                        tvBadge.setText(String.valueOf(count));
                    } else {
                        tvBadge.setVisibility(View.GONE);
                    }
                });
    }

    void loadUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        String name = doc.getString("name");

                        if (name != null && !name.isEmpty()) {
                            tvWelcome.setText("Chào mừng trở lại, " + name);
                        } else {
                            tvWelcome.setText("Chào mừng trở lại");
                        }

                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (notiListener != null) {
            notiListener.remove();
        }
    }
}