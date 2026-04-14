package com.example.btl_dnc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.model.News;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class CreateNewsActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private Button btnNext;
    private TextView btnBack, tvStep1, tvStep2, tvStep3;
    private EditText edtTitle, edtAuthorName, edtAuthorEmail, edtContent;
    private LinearLayout btnSelectImage;
    private ImageView imgPreview, icFolder;

    private int currentStep = 1;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_news);

        initViews();
        setupFirebaseData();

        btnNext.setOnClickListener(v -> handleNextStep());
        btnBack.setOnClickListener(v -> handleBackStep());
        btnSelectImage.setOnClickListener(v -> openGallery());
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        tvStep1 = findViewById(R.id.step1);
        tvStep2 = findViewById(R.id.step2);
        tvStep3 = findViewById(R.id.step3);

        edtAuthorName = findViewById(R.id.edtAuthorName);
        edtAuthorEmail = findViewById(R.id.edtAuthorEmail);
        edtContent = findViewById(R.id.edtContent);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imgPreview = findViewById(R.id.imgPreview);
        icFolder = findViewById(R.id.ic_folder);
        edtTitle = findViewById(R.id.edtTitle);
    }

    private void setupFirebaseData() {
        String nameFromIntent = getIntent().getStringExtra("ADMIN_NAME");
        if (nameFromIntent != null) {
            edtAuthorName.setText(nameFromIntent);
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            edtAuthorEmail.setText(email);
        }
    }

    private void handleNextStep() {
        if (currentStep == 1) {
            if (edtAuthorName.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }
            currentStep = 2;
            viewFlipper.showNext();

        } else if (currentStep == 2) {
            if (edtTitle.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
                return;
            }
            if (edtContent.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
                return;
            }
            currentStep = 3;
            viewFlipper.showNext();

        } else if (currentStep == 3) {
            if (imageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            saveNewsToFirestore();
        }

        updateUI();
    }

    private void handleBackStep() {
        if (currentStep > 1) {
            currentStep--;
            viewFlipper.showPrevious();
            updateUI();
        } else {
            finish();
        }
    }

    private void updateUI() {
        if (currentStep == 1) btnNext.setText("Lưu & Tiếp tục");
        else if (currentStep == 2) btnNext.setText("Chuyển tiếp");
        else if (currentStep == 3) btnNext.setText("Hoàn tất");

        updateStepIndicator();
    }

    private void updateStepIndicator() {
        setStepInactive(tvStep1);
        setStepInactive(tvStep2);
        setStepInactive(tvStep3);

        if (currentStep >= 1) setStepActive(tvStep1);
        if (currentStep >= 2) setStepActive(tvStep2);
        if (currentStep >= 3) setStepActive(tvStep3);
    }

    private void setStepActive(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_step_active);
        tv.setTextColor(Color.WHITE);
    }

    private void setStepInactive(TextView tv) {
        tv.setBackgroundResource(R.drawable.bg_step_inactive);
        tv.setTextColor(Color.parseColor("#03A9F4"));
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            imgPreview.setVisibility(View.VISIBLE);
            icFolder.setVisibility(View.GONE);

            Glide.with(this).load(imageUri).into(imgPreview);
        }
    }

    // ✅ Convert ảnh sang Base64
    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap original = BitmapFactory.decodeStream(inputStream, null, options);

            int maxSize = 800;
            int width = original.getWidth();
            int height = original.getHeight();

            float ratio = Math.min((float) maxSize / width, (float) maxSize / height);

            Bitmap resized = Bitmap.createScaledBitmap(
                    original,
                    Math.round(width * ratio),
                    Math.round(height * ratio),
                    true
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 60, baos);

            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ Lưu Firestore
    private void saveNewsToFirestore() {
        btnNext.setEnabled(false);
        btnNext.setText("Đang lưu...");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String base64Image = encodeImageToBase64(imageUri);

        if (base64Image == null) {
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
            btnNext.setEnabled(true);
            btnNext.setText("Hoàn tất");
            return;
        }

        News news = new News();
        news.title = edtTitle.getText().toString().trim();
        news.content = edtContent.getText().toString().trim();
        news.imageBase64 = base64Image;
        news.authorID = uid;
        news.createAt = Timestamp.now();
        news.updateAt = Timestamp.now();

        FirebaseFirestore.getInstance().collection("news")
                .add(news)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnNext.setEnabled(true);
                    btnNext.setText("Hoàn tất");
                });
    }
}