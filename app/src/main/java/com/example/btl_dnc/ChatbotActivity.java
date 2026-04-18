package com.example.btl_dnc;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.adapter.ChatAdapter;
import com.example.btl_dnc.model.ChatMessage;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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

    Handler handler = new Handler();

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

        addThinking();
        callGeminiWithRetry(msg, 0);
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

    // ===== CALL GEMINI =====
    void callGeminiWithRetry(String userMessage, int retryCount) {

        String apiKey = "AIzaSyCUe9MIONeFJiIpjVN5rxn8TxVd3sKd1E0";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        try {


            String prompt = "Bạn là trợ lý chung cư.\n" +
                    "- Chỉ trả lời 1 câu không quá dài khoảng 7-8 từ\n" +
                    "- Chỉ đưa ra kết quả cuối\n" +
                    "- Không giải thích\n" +
                    "- Không dùng tiếng Anh\n" +
                    "- Không hiển thị THOUGHT\n\n" +
                    "Câu hỏi: " + userMessage;

            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject root = new JSONObject();
            root.put("contents", contents);

            JSONObject config = new JSONObject();
            config.put("maxOutputTokens", 256);
            root.put("generationConfig", config);

            RequestBody body = RequestBody.create(
                    root.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    retryOrFail(userMessage, retryCount);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String res = response.body().string();
                    Log.d("GEMINI_RAW", res);

                    try {
                        JSONObject obj = new JSONObject(res);

                        if (obj.has("error")) {
                            int code = obj.getJSONObject("error").getInt("code");

                            if (code == 503 && retryCount < 3) {
                                handler.postDelayed(() ->
                                        callGeminiWithRetry(userMessage, retryCount + 1), 1000);
                                return;
                            } else {
                                runOnUiThread(() -> {
                                    removeThinking();
                                    saveMessage("AI đang bận 😅", false);
                                });
                                return;
                            }
                        }

                        JSONArray partsArr = obj
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts");

                        StringBuilder reply = new StringBuilder();

                        for (int i = 0; i < partsArr.length(); i++) {
                            String text = partsArr.getJSONObject(i).optString("text");


                            if (text.contains("THOUGHT")) continue;

                            reply.append(text);
                        }

                        String full = reply.toString().trim();


                        String[] lines = full.split("\n");
                        String finalReply = lines[lines.length - 1];

                        runOnUiThread(() -> {
                            removeThinking();
                            saveMessage(finalReply, false);
                        });

                    } catch (Exception e) {
                        retryOrFail(userMessage, retryCount);
                    }
                }
            });

        } catch (Exception e) {
            retryOrFail(userMessage, retryCount);
        }
    }

    // ===== RETRY =====
    void retryOrFail(String userMessage, int retryCount) {
        if (retryCount < 3) {
            handler.postDelayed(() ->
                    callGeminiWithRetry(userMessage, retryCount + 1), 1000);
        } else {
            runOnUiThread(() -> {
                removeThinking();
                saveMessage("AI quá tải 😢", false);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}