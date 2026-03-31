package com.example.btl_dnc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.adapter.CommentAdapter;
import com.example.btl_dnc.model.Comment;
import com.example.btl_dnc.model.Report;
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
    EditText edtComment;
    Button btnSend;
    RecyclerView rvComment;
    LinearLayout layoutReplyCard;
    TextView tvReplyName, tvReplyContent, tvReplyTime;

    String reportID;
    Report currentReport;

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

        layoutReplyCard = findViewById(R.id.layoutReplyCard);
        tvReplyName = findViewById(R.id.tvReplyName);
        tvReplyContent = findViewById(R.id.tvReplyContent);
        tvReplyTime = findViewById(R.id.tvReplyTime);

        imgBack.setOnClickListener(v -> finish());

        reportID = getIntent().getStringExtra("id");
        if (reportID == null || reportID.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu reportID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentListener != null) commentListener.remove();
        if (reportListener != null) reportListener.remove();
    }
}