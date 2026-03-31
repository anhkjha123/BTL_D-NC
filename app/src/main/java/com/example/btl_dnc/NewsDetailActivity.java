package com.example.btl_dnc;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.adapter.CommentAdapter;
import com.example.btl_dnc.model.Comment;
import com.example.btl_dnc.model.News;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class NewsDetailActivity extends AppCompatActivity {

    ImageView img;ImageButton btnBack;
    TextView tvTitle, tvContent;
    EditText edtComment;
    Button btnSend;
    RecyclerView rvComment;

    ArrayList<Comment> list;
    CommentAdapter adapter;

    String newsID;
    ListenerRegistration commentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        img = findViewById(R.id.img);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        edtComment = findViewById(R.id.edtComment);
        btnSend = findViewById(R.id.btnSend);
        rvComment = findViewById(R.id.rvComment);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        newsID = getIntent().getStringExtra("newsID");

        list = new ArrayList<>();
        adapter = new CommentAdapter(this, list);

        rvComment.setLayoutManager(new LinearLayoutManager(this));
        rvComment.setAdapter(adapter);

        loadNews();
        listenCommentRealtime();

        btnSend.setOnClickListener(v -> sendComment());
    }

    // ===== LOAD NEWS =====
    private void loadNews() {
        FirebaseFirestore.getInstance()
                .collection("news")
                .document(newsID)
                .get()
                .addOnSuccessListener(doc -> {
                    News n = doc.toObject(News.class);
                    if (n != null) {
                        tvTitle.setText(n.title);
                        tvContent.setText(n.content);

                        Glide.with(this)
                                .load(n.imageUrl)
                                .into(img);
                    }
                });
    }

    // ===== REALTIME COMMENT =====
    private void listenCommentRealtime() {
        commentListener = FirebaseFirestore.getInstance()
                .collection("news")
                .document(newsID)
                .collection("comments")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    list.clear();

                    for (DocumentSnapshot doc : value) {
                        Comment c = doc.toObject(Comment.class);
                        if (c != null) {
                            c.id = doc.getId();
                            list.add(c);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    // ===== SEND COMMENT (CHỈ LƯU userId) =====
    private void sendComment() {

        String text = edtComment.getText().toString().trim();
        if (text.isEmpty()) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Comment c = new Comment();
        c.userId = uid;
        c.content = text;
        c.time = Timestamp.now();

        FirebaseFirestore.getInstance()
                .collection("news")
                .document(newsID)
                .collection("comments")
                .add(c);

        edtComment.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentListener != null) commentListener.remove();
    }
}