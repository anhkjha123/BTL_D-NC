package com.example.btl_dnc;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.adapter.ChatAdapter;
import com.example.btl_dnc.model.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class ChatbotActivity extends AppCompatActivity {

    RecyclerView rv;
    EditText edt;
    ImageView btnSend, btnBack;

    ArrayList<ChatMessage> list;
    ChatAdapter adapter;

    String uid;
    ListenerRegistration listener;

    String thinkingDocId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rv = findViewById(R.id.rvChat);
        edt = findViewById(R.id.edtChat);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        list = new ArrayList<>();
        adapter = new ChatAdapter(this, list);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        listenChat();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    // ===== REALTIME =====
    void listenChat() {
        listener = FirebaseFirestore.getInstance()
                .collection("chatbot")
                .document(uid)
                .collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    list.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatMessage m = doc.toObject(ChatMessage.class);
                        list.add(m);
                    }

                    adapter.notifyDataSetChanged();
                    rv.scrollToPosition(list.size() - 1);
                });
    }

    // ===== SEND =====
    void sendMessage() {
        String msg = edt.getText().toString().trim();
        if (msg.isEmpty()) return;

        saveMessage(msg, true);
        edt.setText("");

        String reply = handleSmart(msg);

        addThinking();

        new android.os.Handler().postDelayed(() -> {
            removeThinking();
            saveMessage(reply, false);
        }, 800);
    }

    // ===== SAVE =====
    void saveMessage(String text, boolean isUser) {
        ChatMessage m = new ChatMessage(text, isUser, Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("chatbot")
                .document(uid)
                .collection("messages")
                .add(m);
    }

    // ===== THINKING =====
    void addThinking() {
        ChatMessage m = new ChatMessage("Đang suy nghĩ...", false, Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("chatbot")
                .document(uid)
                .collection("messages")
                .add(m)
                .addOnSuccessListener(doc -> thinkingDocId = doc.getId());
    }

    void removeThinking() {
        if (thinkingDocId == null) return;

        FirebaseFirestore.getInstance()
                .collection("chatbot")
                .document(uid)
                .collection("messages")
                .document(thinkingDocId)
                .delete();

        thinkingDocId = null;
    }

    // ===== RULE =====
    String handleSmart(String msg) {

        msg = msg.toLowerCase();

        // ===== chào hỏi =====
        if (msg.contains("xin chào") || msg.contains("hello"))
            return "Xin chào 👋 Bạn cần hỗ trợ gì?";

        // ===== báo sự cố =====
        if (msg.contains("báo") || msg.contains("sự cố") || msg.contains("hỏng"))
            return "Bạn vào mục Bookmark để gửi báo cáo sự cố nhé.";

        // ===== trạng thái =====
        if (msg.contains("trạng thái") || msg.contains("tiến trình"))
            return "Bạn vào Bookmark để xem tiến trình xử lý.";

        // ===== điện =====
        if (msg.contains("điện") || msg.contains("mất điện"))
            return "Bạn nên báo ngay sự cố điện trong Bookmark để ban quản lý xử lý kịp thời.";

        // ===== nước =====
        if (msg.contains("nước") || msg.contains("mất nước"))
            return "Bạn nên báo sự cố nước trong Bookmark để tránh ảnh hưởng sinh hoạt.";

        // ===== dột =====
        if (msg.contains("dột") || msg.contains("rò") || msg.contains("chảy nước"))
            return "Nhà bị dột có thể do hệ thống nước. Bạn hãy vào Bookmark để báo sự cố.";

        // ===== tin tức =====
        if (msg.contains("tin") || msg.contains("thông báo"))
            return "Bạn vào trang Home để xem tin tức mới nhất.";

        // ===== profile =====
        if (msg.contains("thông tin") || msg.contains("profile"))
            return "Bạn vào trang Profile để xem hoặc chỉnh sửa thông tin cá nhân.";

        // ===== fallback  =====
        return "Mình chưa hiểu rõ 😅 Bạn có thể nói cụ thể hơn hoặc thử từ khóa như: điện, nước, báo sự cố...";
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}