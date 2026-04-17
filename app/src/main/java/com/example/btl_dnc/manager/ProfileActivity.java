package com.example.btl_dnc.manager;

import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvApartment, tvBuilding, tvArea, tvStartDate, tvUsername;
    ImageView img;
    Button btnBlock;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvApartment = findViewById(R.id.tvApartment);
        tvBuilding = findViewById(R.id.tvBuilding);
        tvArea = findViewById(R.id.tvArea);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvUsername = findViewById(R.id.tvUsername);

        img = findViewById(R.id.imgAvatar);
        btnBlock = findViewById(R.id.btnBlock);


        userID = getIntent().getStringExtra("userID");

        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "Không có userID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUser();

        btnBlock.setOnClickListener(v -> blockUser());
    }

    // ===== LOAD USER =====
    private void loadUser() {
        FirebaseFirestore.getInstance()
                .collection("user")
                .document(userID)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc == null || !doc.exists()) {
                        Toast.makeText(this, "User không tồn tại", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User u = doc.toObject(User.class);

                    if (u != null) {

                        // ===== TEXT =====
                        tvName.setText(u.name != null ? u.name : "No name");
                        tvEmail.setText(u.email != null ? u.email : "");
                        tvUsername.setText(u.email != null ? u.email : "");

                        tvApartment.setText("Số căn hộ: " +
                                (u.apartmentID != null ? u.apartmentID : "012"));

                        tvBuilding.setText("Tòa nhà: A");
                        tvArea.setText("Diện tích: 75m²");

                        // ===== DATE =====
                        if (u.startDate != null) {
                            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(u.startDate.toDate());
                            tvStartDate.setText("Ngày bắt đầu cư trú: " + date);
                        } else {
                            tvStartDate.setText("Ngày bắt đầu cư trú: --");
                        }

                        // ===== IMAGE =====
                        Glide.with(this)
                                .load(u.avatarUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background)
                                .into(img);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi load: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ===== BLOCK USER =====
    private void blockUser() {
        FirebaseFirestore.getInstance()
                .collection("user")
                .document(userID)
                .update("status", "blocked")
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Đã chặn user", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}