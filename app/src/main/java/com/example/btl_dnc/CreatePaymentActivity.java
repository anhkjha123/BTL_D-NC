package com.example.btl_dnc;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.model.News;
import com.example.btl_dnc.model.PaymentNotification;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreatePaymentActivity extends AppCompatActivity {

    // Views
    LinearLayout step1, step2, step3, step4;
    // Sửa các biến EditText thành:
    EditText edtAdminName, edtAdminEmail, edtTitle, edtContent, edtStartDate, edtEndDate;
    ImageView imgQR, imgThumb;

    // Data
    String currentAdminId = "";
    String qrBase64 = "";
    String thumbBase64 = "";
    boolean isPickingQR = true; // Dùng để phân biệt đang chọn ảnh QR hay Thumbnail

    ActivityResultLauncher<String> picker;
    FirebaseFirestore db;
    private String imageBase64 = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_payment);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupImagePicker();
        loadAdminInfo();
        setupNavigation();
    }

    private void initViews() {
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

        edtAdminName = findViewById(R.id.edtAdminName);
        edtAdminEmail = findViewById(R.id.edtAdminEmail);
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);

        imgQR = findViewById(R.id.imgQR);
        imgThumb = findViewById(R.id.imgThumb);
    }

    private void loadAdminInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentAdminId = user.getUid();
            edtAdminEmail.setText(user.getEmail());

            db.collection("user").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("name")) {
                            edtAdminName.setText(doc.getString("name"));
                        }
                    });
        }
    }

    private void setupNavigation() {
        // NEXT BUTTONS
        findViewById(R.id.btnNext1).setOnClickListener(v -> switchStep(2));
        findViewById(R.id.btnNext2).setOnClickListener(v -> switchStep(3));
        findViewById(R.id.btnNext3).setOnClickListener(v -> switchStep(4));

        // BACK BUTTONS
        findViewById(R.id.btnBack1).setOnClickListener(v -> finish());
        findViewById(R.id.btnBack2).setOnClickListener(v -> switchStep(1));
        findViewById(R.id.btnBack3).setOnClickListener(v -> switchStep(2));
        findViewById(R.id.btnBack4).setOnClickListener(v -> switchStep(3));
        edtStartDate.setOnClickListener(v -> showDatePicker(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePicker(edtEndDate));
        // PICKERS
        findViewById(R.id.btnPickQR).setOnClickListener(v -> {
            isPickingQR = true;
            picker.launch("image/*");
        });
        findViewById(R.id.btnPickThumb).setOnClickListener(v -> {
            isPickingQR = false;
            picker.launch("image/*");
        });

        // HOÀN TẤT
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitPaymentNews());
    }
    private void showDatePicker(EditText targetEditText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, yearSelected);
                    targetEditText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
    private void switchStep(int stepIndex) {
        step1.setVisibility(stepIndex == 1 ? View.VISIBLE : View.GONE);
        step2.setVisibility(stepIndex == 2 ? View.VISIBLE : View.GONE);
        step3.setVisibility(stepIndex == 3 ? View.VISIBLE : View.GONE);
        step4.setVisibility(stepIndex == 4 ? View.VISIBLE : View.GONE);
    }

    private void setupImagePicker() {
        picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                // 1. Hiển thị ảnh ngay lập tức ra màn hình bằng Glide (Giống y hệt CreateNews)
                if (isPickingQR) {
                    Glide.with(this)
                            .load(uri)
                            .into(imgQR);

                    // Nén sang Base64 chạy ngầm để chuẩn bị lưu Firebase
                    qrBase64 = encodeImage(uri);
                } else {
                    imgThumb.setPadding(0, 0, 0, 0); // Bỏ viền trắng
                    Glide.with(this)
                            .load(uri)
                            .into(imgThumb);

                    // Nén sang Base64 chạy ngầm
                    thumbBase64 = encodeImage(uri);
                }
            }
        });
    }

    // ===== BƯỚC QUAN TRỌNG NHẤT: LƯU & GỬI THÔNG BÁO HÀNG LOẠT =====
    private void submitPaymentNews() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        String startDateStr = edtStartDate.getText().toString().trim();
        String endDateStr = edtEndDate.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin và chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentNotification payment = new PaymentNotification();
        payment.authorID = currentAdminId;
        payment.title = title;
        payment.content = content;

        // ===== CHUYỂN STRING THÀNH TIMESTAMP =====
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date parsedStart = sdf.parse(startDateStr);
            Date parsedEnd = sdf.parse(endDateStr);

            if (parsedStart != null) payment.startDate = new Timestamp(parsedStart);
            if (parsedEnd != null) payment.endDate = new Timestamp(parsedEnd);

            // Logic bắt lỗi: Ngày kết thúc không được nhỏ hơn ngày bắt đầu
            if (parsedEnd != null && parsedStart != null && parsedEnd.before(parsedStart)) {
                Toast.makeText(this, "Hạn cuối không thể diễn ra trước ngày bắt đầu!", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Định dạng ngày không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        payment.qrCodeBase64 = qrBase64;
        payment.thumbnailBase64 = thumbBase64;
        payment.createdAt = Timestamp.now();

        Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();

        db.collection("payments")
                .add(payment)
                .addOnSuccessListener(docRef -> {
                    String paymentId = docRef.getId();
                    docRef.update("id", paymentId);
                    publishNewsToResidents(paymentId, title, content);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void publishNewsToResidents(String paymentId, String title, String content) {
        News newsPost = new News();

        newsPost.authorID = currentAdminId;
        newsPost.title = title;
        newsPost.content = content;
        newsPost.type = "payment";
        newsPost.refId = paymentId;
        newsPost.imageBase64 = thumbBase64;

        newsPost.setCreateAt(Timestamp.now());
        newsPost.setUpdateAt(Timestamp.now());

        db.collection("news").add(newsPost)
                .addOnSuccessListener(docRef -> {
                    // LẤY ID CỦA BÀI NEWS VỪA TẠO
                    String newsId = docRef.getId();
                    docRef.update("id", newsId);

                    Toast.makeText(this, "Đã đăng lên Bảng tin thành công!", Toast.LENGTH_SHORT).show();

                    // SỬA Ở ĐÂY: Truyền newsId vào hàm gửi thông báo
                    sendNotificationToAllResidents(newsId, title);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi đăng bảng tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ===== HÀM NÉN ẢNH (Dùng chung cho cả QR và Thumb) =====
    private String encodeImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            // Thu nhỏ ảnh để Firebase không bị quá tải
            float ratio = Math.min((float) 800 / bitmap.getWidth(), (float) 800 / bitmap.getHeight());
            int width = Math.round((float) ratio * bitmap.getWidth());
            int height = Math.round((float) ratio * bitmap.getHeight());

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);

            // DÙNG NO_WRAP LÀ QUAN TRỌNG NHẤT
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }
    // SỬA Ở ĐÂY: Nhận biến newsId thay vì paymentId
    private void sendNotificationToAllResidents(String newsId, String title) {
        db.collection("user").whereEqualTo("role", "resident").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String residentId = doc.getId();

                        com.example.btl_dnc.model.Notification noti = new com.example.btl_dnc.model.Notification();
                        noti.userId = residentId;
                        noti.title = "Thông báo thu phí mới";
                        noti.message = title;

                        // ĐỂ TYPE LÀ "news" ĐỂ NÓ MỞ NEWS DETAIL
                        noti.type = "news";

                        // GẮN ĐÚNG ID CỦA BÀI NEWS
                        noti.refId = newsId;

                        noti.setIsRead(false);
                        noti.createdAt = Timestamp.now();

                        db.collection("notifications").add(noti);
                    }

                    Toast.makeText(this, "Đã đăng Bảng tin và Gửi thông báo thành công!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Đăng bảng tin thành công nhưng lỗi gửi thông báo!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}