package com.example.btl_dnc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    EditText edtName, edtPhone, edtStatus;
    Button btnSave, btnChooseImage;
    ImageView imgAvatar;
    ImageButton imgBack;
    String uid;
    Uri imageUri;
    String avatarUrlOld = "";

    static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtStatus = findViewById(R.id.edtStatus);
        btnSave = findViewById(R.id.btnSave);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        imgAvatar = findViewById(R.id.imgAvatar);
        imgBack = findViewById(R.id.imgBack);

        imgBack.setOnClickListener(v -> finish());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadUser();

        btnChooseImage.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> save());
    }

    void loadUser() {
        FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u != null) {
                        edtName.setText(u.name);
                        edtPhone.setText(u.phone);
                        edtStatus.setText(u.status);

                        avatarUrlOld = u.avatarUrl;

                        Glide.with(this)
                                .load(u.avatarUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .circleCrop()
                                .into(imgAvatar);
                    }
                });
    }

    void chooseImage() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            imageUri = data.getData();

            Glide.with(this).load(imageUri).circleCrop().into(imgAvatar);
        }
    }

    void save() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String status = edtStatus.getText().toString().trim(); // Lấy dữ liệu status

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageAndSave(name, phone, status);
        } else {
            updateUser(name, phone, status, avatarUrlOld);
        }
    }

    void uploadImageAndSave(String name, String phone, String status) {
        String fileName = "avatar/" + uid; // Dùng UID làm tên file để ghi đè ảnh cũ, tiết kiệm Storage

        FirebaseStorage.getInstance()
                .getReference(fileName)
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(uri -> updateUser(name, phone, status, uri.toString()))
                );
    }

    void updateUser(String name, String phone, String status, String avatarUrl) {
        FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .update(
                        "name", name,
                        "phone", phone,
                        "status", status,
                        "avatarUrl", avatarUrl
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}