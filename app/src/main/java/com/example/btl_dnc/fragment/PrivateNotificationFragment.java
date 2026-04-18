package com.example.btl_dnc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.PaymentAdapter;
import com.example.btl_dnc.model.PaymentNotification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class PrivateNotificationFragment extends Fragment {

    private RecyclerView rvPayments;
    private PaymentAdapter adapter;
    private ArrayList<PaymentNotification> paymentList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_notification, container, false);

        rvPayments = view.findViewById(R.id.rvPayments);
        db = FirebaseFirestore.getInstance();

        paymentList = new ArrayList<>();
        adapter = new PaymentAdapter(getContext(), paymentList);

        rvPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPayments.setAdapter(adapter);

        loadPayments();

        return view;
    }

    private void loadPayments() {
        // Lấy tất cả thông báo thu phí, sắp xếp mới nhất lên đầu
        db.collection("payments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (!isAdded() || getContext() == null) return;

                    if (error != null) {
                        Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        paymentList.clear();

                        // Lấy thời gian hiện tại của máy người dùng
                        long currentTimeMillis = System.currentTimeMillis();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            PaymentNotification payment = doc.toObject(PaymentNotification.class);
                            if (payment != null) {
                                payment.id = doc.getId();

                                // ===== LỌC THEO TIME RANGE =====
                                boolean isWithinRange = true;

                                // 1. Kiểm tra Ngày bắt đầu
                                // Nếu Hiện tại < Ngày bắt đầu -> Chưa tới thời gian thu -> ẨN
                                if (payment.startDate != null) {
                                    long startMillis = payment.startDate.toDate().getTime();
                                    if (currentTimeMillis < startMillis) {
                                        isWithinRange = false;
                                    }
                                }

                                // 2. Kiểm tra Hạn cuối
                                // Cộng thêm 24 tiếng (86,400,000 mili giây) để bao trọn hết ngày cuối cùng
                                // Nếu Hiện tại > (Hạn cuối + 24h) -> Đã hết hạn thu -> ẨN
                                if (payment.endDate != null) {
                                    long endOfDayMillis = payment.endDate.toDate().getTime() + (24 * 60 * 60 * 1000) - 1;
                                    if (currentTimeMillis > endOfDayMillis) {
                                        isWithinRange = false;
                                    }
                                }

                                // Nếu thỏa mãn điều kiện thời gian (Nằm giữa Bắt đầu và Kết thúc) thì mới hiển thị
                                if (isWithinRange) {
                                    paymentList.add(payment);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}