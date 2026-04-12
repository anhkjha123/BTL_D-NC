package com.example.btl_dnc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_dnc.model.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class CreateReportActivity extends AppCompatActivity {

    FrameLayout container;
    boolean isAvatarLoaded = false;
    String userAvatarBase64 = "";
    String name = "", email = "", content = "", type = "";
    Uri imageUri;

    ActivityResultLauncher<String> picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        container = findViewById(R.id.container);


        picker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;

                        ImageView img = findViewById(R.id.imgPreview);
                        if (img != null) img.setImageURI(uri);
                    }
                });

        showStep1();
    }

    // ===== STEP 1 =====
    void showStep1() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_step1, container, false);
        container.removeAllViews();
        container.addView(v);

        EditText edtName = v.findViewById(R.id.edtName);
        EditText edtEmail = v.findViewById(R.id.edtEmail);
        Button btnNext1 = v.findViewById(R.id.btnNext1);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (email.isEmpty()) email = user.getEmail();
            if (name.isEmpty() && user.getDisplayName() != null)
                name = user.getDisplayName();
            if (!isAvatarLoaded) {
                // 1. Tạm thời khóa nút Next để tránh người dùng bấm nhanh quá
                btnNext1.setEnabled(false);
                btnNext1.setText("Đang tải dữ liệu...");

                FirebaseFirestore.getInstance().collection("user").document(user.getUid())
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists() && doc.contains("avatar")) {
                                userAvatarBase64 = doc.getString("avatar");
                                android.util.Log.d("DEBUG", "Đã lấy được Avatar Base64!");
                            }

                            // 2. Load xong (dù có avatar hay không) thì mở khóa nút Next
                            isAvatarLoaded = true;
                            btnNext1.setEnabled(true);
                            btnNext1.setText("Tiếp tục");
                        })
                        .addOnFailureListener(e -> {
                            // 3. Nếu mạng lỗi, cũng phải mở khóa nút Next để họ đi tiếp (chấp nhận mất avatar)
                            isAvatarLoaded = true;
                            btnNext1.setEnabled(true);
                            btnNext1.setText("Tiếp tục");
                        });
            }
        }

        edtName.setText(name);
        edtEmail.setText(email);
        edtEmail.setEnabled(false);

        v.findViewById(R.id.btnNext1).setOnClickListener(view -> {
            name = edtName.getText().toString();
            email = edtEmail.getText().toString();
            showStep2();
        });
    }

    // ===== STEP 2 =====
    void showStep2() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_step2, container, false);
        container.removeAllViews();
        container.addView(v);

        EditText edtContent = v.findViewById(R.id.edtContent);

        edtContent.setText(content);

        v.findViewById(R.id.btnNext2).setOnClickListener(view -> {
            content = edtContent.getText().toString();
            showStep3();
        });

        v.findViewById(R.id.btnBack2).setOnClickListener(view -> showStep1());
    }

    // ===== STEP 3 =====
    void showStep3() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_step3, container, false);
        container.removeAllViews();
        container.addView(v);

        Spinner spType = v.findViewById(R.id.spType);
        ImageView imgPreview = v.findViewById(R.id.imgPreview);

        String[] types = {"Điện", "Nước", "Thang máy", "An ninh", "PCCC"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        if (imageUri != null) {
            imgPreview.setImageURI(imageUri);
        }

        v.findViewById(R.id.btnPickImage).setOnClickListener(view ->
                picker.launch("image/*")
        );

        v.findViewById(R.id.btnSubmit).setOnClickListener(view -> {
            type = spType.getSelectedItem().toString();
            saveReport();
        });

        v.findViewById(R.id.btnBack3).setOnClickListener(view -> showStep2());
    }

    // ===== CONVERT BASE64 =====
    private String encodeImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            // Đọc ảnh thành Bitmap
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);

            // Tính toán thu nhỏ ảnh (Tối đa 800x800 để chống tràn bộ nhớ 1MB của Firestore)
            int maxWidth = 800;
            int maxHeight = 800;
            float ratio = Math.min(
                    (float) maxWidth / bitmap.getWidth(),
                    (float) maxHeight / bitmap.getHeight());
            int width = Math.round((float) ratio * bitmap.getWidth());
            int height = Math.round((float) ratio * bitmap.getHeight());

            android.graphics.Bitmap resizedBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true);

            // Nén ảnh ra mảng byte (Định dạng JPEG, chất lượng 70%)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();

            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // ===== SAVE =====
    void saveReport() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        Report r = new Report();
        r.userId = user.getUid();
        r.name = name;
        r.email = email;
        r.content = content;
        r.type = type;
        r.createdAt = System.currentTimeMillis();
        r.status = "Chờ duyệt";
        r.userAvatar = userAvatarBase64;
        if (imageUri != null) {
            r.imageBase64 = encodeImage(imageUri);
            if (r.imageBase64 == null) {
                Toast.makeText(this, "Lỗi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        FirebaseFirestore.getInstance()
                .collection("reports")
                .add(r)
                .addOnSuccessListener(doc -> {
                    String id = doc.getId();
                    doc.update("id", id);

                    Toast.makeText(this, "Đã gửi chờ duyệt", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Hiển thị lỗi rõ ràng nếu Firebase từ chối lưu
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}