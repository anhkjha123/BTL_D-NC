package com.example.btl_dnc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.adapter.CommentAdapter;
import com.example.btl_dnc.model.Comment;
import com.example.btl_dnc.model.Report;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportDetailActivity extends AppCompatActivity {

    ImageView imgBack, imgReport;
    TextView tvTitle, tvStatus, tvType, tvTime, tvContent, tvReplyTitle, tvEmptyComment;
    String userRole;
    EditText edtComment;
    Button btnSend;
    RecyclerView rvComment;
    LinearLayout layoutReplyCard;
    ImageButton btnDeleteReport;
    TextView tvReplyName, tvReplyContent, tvReplyTime;


    String reportID;
    Report currentReport;
    Button btnChangeStatus;
    ArrayList<Comment> list;
    CommentAdapter adapter;
    ListenerRegistration commentListener;
    ListenerRegistration reportListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        imgBack = findViewById(R.id.imgBack);
        imgReport = findViewById(R.id.imgReport);
        tvTitle = findViewById(R.id.tvTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvType = findViewById(R.id.tvType);
        tvTime = findViewById(R.id.tvTime);
        tvContent = findViewById(R.id.tvContent);
        tvReplyTitle = findViewById(R.id.tvReplyTitle);
        tvEmptyComment = findViewById(R.id.tvEmptyComment);

        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        rvComment = findViewById(R.id.rvComment);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);
        layoutReplyCard = findViewById(R.id.layoutReplyCard);
        tvReplyName = findViewById(R.id.tvReplyName);
        tvReplyContent = findViewById(R.id.tvReplyContent);
        tvReplyTime = findViewById(R.id.tvReplyTime);
        btnDeleteReport = findViewById(R.id.btnDeleteReport);
        imgBack.setOnClickListener(v -> finish());



        reportID = getIntent().getStringExtra("id");
        userRole = getIntent().getStringExtra("USER_ROLE");
        if (userRole == null) {
            userRole = "resident";
        }
        if (reportID == null || reportID.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu reportID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupUIByRole();
        list = new ArrayList<>();
        adapter = new CommentAdapter(this, list);

        rvComment.setLayoutManager(new LinearLayoutManager(this));
        rvComment.setAdapter(adapter);

        loadReport();
        listenCommentsRealtime();

        btnSend.setOnClickListener(v -> sendComment());
    }

    private void loadReport() {
        reportListener = FirebaseFirestore.getInstance()
                .collection("reports")
                .document(reportID)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) return;

                    currentReport = snapshot.toObject(Report.class);
                    if (currentReport == null) return;

                    currentReport.setId(snapshot.getId());
                    bindReport(currentReport);
                });
    }

    private void bindReport(Report r) {
        tvTitle.setText("Chi tiết báo cáo");

        tvStatus.setText(r.status != null ? r.status : "");
        tvType.setText(r.type != null ? r.type : "");

        if (r.createdAt > 0) {
            String time = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(r.createdAt));
            tvTime.setText(time);
        } else {
            tvTime.setText("");
        }

        tvContent.setText(r.content != null ? r.content : "");

        // đổi màu trạng thái đơn giản
        if ("Đang xử lý".equals(r.status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if ("Đã xử lý".equals(r.status)) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));}

         else {
            tvStatus.setTextColor(getResources().getColor(android.R.color.black));
        }

        // ảnh
        if (r.imageBase64 != null && !r.imageBase64.trim().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(r.imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgReport.setImageBitmap(bitmap);
                imgReport.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                imgReport.setVisibility(View.GONE);
            }
        } else {
            imgReport.setVisibility(View.GONE);
        }
    }

    private void listenCommentsRealtime() {
        commentListener = FirebaseFirestore.getInstance()
                .collection("reports")
                .document(reportID)
                .collection("comments")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    if (value == null) return;

                    list.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.id = doc.getId();
                            list.add(c);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        tvEmptyComment.setVisibility(View.VISIBLE);
                        rvComment.setVisibility(View.GONE);
                    } else {
                        tvEmptyComment.setVisibility(View.GONE);
                        rvComment.setVisibility(View.VISIBLE);
                    }

                    showLatestAdminReply();
                });
    }
    private void showStatusUpdateDialog() {
        // 1. Khởi tạo BottomSheetDialog và nạp file XML vừa tạo
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_status, null);
        bottomSheetDialog.setContentView(view);

        // 2. Ánh xạ các thành phần bên trong cái XML đó
        RadioGroup rgStatus = view.findViewById(R.id.rgStatus);
        Button btnCancel = view.findViewById(R.id.btnCancelStatus);
        Button btnConfirm = view.findViewById(R.id.btnConfirmStatus);

        // 3. Đọc trạng thái hiện tại trên giao diện để "Tích sẵn" vào RadioButton tương ứng
        String currentStatus = tvStatus.getText().toString();
        if ("Chờ duyệt".equals(currentStatus)) rgStatus.check(R.id.rbPending);
        else if ("Đang xử lý".equals(currentStatus)) rgStatus.check(R.id.rbProcessing);
        else if ("Đã xử lý".equals(currentStatus)) rgStatus.check(R.id.rbCompleted);

        // 4. Sự kiện nút Hủy
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // 5. Sự kiện nút Xác nhận
        btnConfirm.setOnClickListener(v -> {
            int selectedId = rgStatus.getCheckedRadioButtonId();
            String newStatus = "";

            if (selectedId == R.id.rbPending) newStatus = "Chờ duyệt";
            else if (selectedId == R.id.rbProcessing) newStatus = "Đang xử lý";
            else if (selectedId == R.id.rbCompleted) newStatus = "Đã xử lý";

            if (!newStatus.isEmpty() && !newStatus.equals(currentStatus)) {
                // Gọi hàm cập nhật lên Firebase
                updateStatusInFirestore(newStatus);
            }

            bottomSheetDialog.dismiss(); // Tắt popup
        });

        // 6. Hiển thị lên màn hình
        bottomSheetDialog.show();
    }
    private void updateStatusInFirestore(String newStatus) {
        if (reportID == null || reportID.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy thông tin report trước
        db.collection("reports")
                .document(reportID)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) return;

                    String userId = doc.getString("userId");
                    String content = doc.getString("content");

                    // UPDATE STATUS
                    db.collection("reports")
                            .document(reportID)
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {

                                //  TẠO NOTIFICATION
                                if (userId != null) {

                                    com.example.btl_dnc.model.Notification noti =
                                            new com.example.btl_dnc.model.Notification();

                                    noti.userId = userId;
                                    noti.title = "Cập nhật báo cáo";
                                    noti.message = "Báo cáo của bạn đã chuyển sang: " + newStatus;
                                    noti.type = "reports";
                                    noti.refId = reportID;
                                    noti.setIsRead(false);
                                    noti.createdAt = Timestamp.now();

                                    db.collection("notifications")
                                            .add(noti);
                                }

                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không lấy được report", Toast.LENGTH_SHORT).show()
                );
    }
    private void showLatestAdminReply() {
        Comment latestAdmin = null;

        for (int i = list.size() - 1; i >= 0; i--) {
            Comment c = list.get(i);
            if ("admin".equals(c.role)) {
                latestAdmin = c;
                break;
            }
        }

        if (latestAdmin == null) {
            layoutReplyCard.setVisibility(View.GONE);
            tvReplyTitle.setVisibility(View.GONE);
            return;
        }

        layoutReplyCard.setVisibility(View.VISIBLE);
        tvReplyTitle.setVisibility(View.VISIBLE);

        tvReplyName.setText("Quản lý");
        tvReplyContent.setText(latestAdmin.content != null ? latestAdmin.content : "");

        if (latestAdmin.time != null) {
            String t = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                    .format(latestAdmin.time.toDate());
            tvReplyTime.setText(t);
        } else {
            tvReplyTime.setText("");
        }
    }

    private void sendComment() {
        String text = edtComment.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Nhập nội dung phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // mặc định là user
        Comment c = new Comment();
        c.userId = uid;
        c.content = text;
        c.time = Timestamp.now();
        c.role = "user";

        // nếu muốn tự động nhận diện admin
        FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.getString("role");
                    if ("admin".equals(role)) {
                        c.role = "admin";
                    }

                    FirebaseFirestore.getInstance()
                            .collection("reports")
                            .document(reportID)
                            .collection("comments")
                            .add(c)
                            .addOnSuccessListener(unused -> edtComment.setText(""))
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không đọc được tài khoản", Toast.LENGTH_SHORT).show());
    }
    private void setupUIByRole() {
        if ("ADMIN".equals(userRole)) {
            // ADMIN
            if (btnChangeStatus != null) {
                btnChangeStatus.setVisibility(View.VISIBLE);
                btnChangeStatus.setOnClickListener(v -> {

                    showStatusUpdateDialog();
                });
                btnDeleteReport.setVisibility(View.GONE);
            }
            edtComment.setHint("Nhập phản hồi với tư cách Admin...");
        } else {
            // USER
            if (btnChangeStatus != null) {
                btnChangeStatus.setVisibility(View.GONE);
            }
            btnDeleteReport.setVisibility(View.VISIBLE);
            btnDeleteReport.setOnClickListener(v -> deleteReportConfirm());
            edtComment.setHint("Nhập bình luận của bạn...");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentListener != null) commentListener.remove();
        if (reportListener != null) reportListener.remove();
    }
    private void deleteReportConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa báo cáo")
                .setMessage("Bạn có chắc chắn muốn xóa báo cáo này không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteReportFromFirestore();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteReportFromFirestore() {
        if (reportID == null || reportID.isEmpty()) return;

        FirebaseFirestore.getInstance()
                .collection("reports")
                .document(reportID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa báo cáo", Toast.LENGTH_SHORT).show();
                    // Đóng màn hình chi tiết sau khi xóa thành công
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}