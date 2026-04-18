package com.example.btl_dnc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_dnc.model.PaymentNotification;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PaymentDetailActivity extends AppCompatActivity {

    private ImageView imgThumb, imgQR, btnBack;
    // Cập nhật thêm tvStartDate
    private TextView tvTitle, tvContent, tvStartDate, tvEndDate;
    private String paymentID;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_detail);

        db = FirebaseFirestore.getInstance();
        initViews();

        paymentID = getIntent().getStringExtra("paymentID");

        if (paymentID != null) {
            loadPaymentDetails();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin thanh toán!", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnDone).setOnClickListener(v -> {
            Toast.makeText(this, "Thông báo đã được gửi tới Ban quản lý", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void initViews() {
        imgThumb = findViewById(R.id.imgThumbDetail);
        imgQR = findViewById(R.id.imgQRDetail);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitleDetail);
        tvContent = findViewById(R.id.tvContentDetail);

        // Ánh xạ 2 biến thời gian mới
        tvStartDate = findViewById(R.id.tvStartDateDetail);
        tvEndDate = findViewById(R.id.tvEndDateDetail);
    }

    private void loadPaymentDetails() {
        db.collection("payments").document(paymentID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        PaymentNotification p = documentSnapshot.toObject(PaymentNotification.class);
                        if (p != null) {
                            bindData(p);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show());
    }

    private void bindData(PaymentNotification p) {
        tvTitle.setText(p.title);
        tvContent.setText(p.content);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Định dạng và hiển thị Ngày bắt đầu
        if (p.startDate != null) {
            String startStr = sdf.format(p.startDate.toDate());
            tvStartDate.setText("Bắt đầu thu: " + startStr);
        }

        // Định dạng và hiển thị Hạn cuối
        if (p.endDate != null) {
            String endStr = sdf.format(p.endDate.toDate());
            tvEndDate.setText("Hạn cuối: " + endStr);
        }

        // Giải mã ảnh Thumbnail
        if (p.thumbnailBase64 != null && !p.thumbnailBase64.isEmpty()) {
            imgThumb.setImageBitmap(decodeBase64(p.thumbnailBase64));
        }

        // Giải mã ảnh QR Code
        if (p.qrCodeBase64 != null && !p.qrCodeBase64.isEmpty()) {
            imgQR.setImageBitmap(decodeBase64(p.qrCodeBase64));
        }
    }

    private Bitmap decodeBase64(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}