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

    EditText edtName, edtPhone;
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

    // ===== LOAD USER =====
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

                        avatarUrlOld = u.avatarUrl;

                        Glide.with(this)
                                .load(u.avatarUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(imgAvatar);
                    }
                });
    }

    // ===== CHỌN ẢNH =====
    void chooseImage() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && data != null) {
            imageUri = data.getData();
            imgAvatar.setImageURI(imageUri);
        }
    }

    // ===== SAVE =====
    void save() {
        String name = edtName.getText().toString();
        String phone = edtPhone.getText().toString();

        if (imageUri != null) {
            uploadImageAndSave(name, phone);
        } else {
            updateUser(name, phone, avatarUrlOld);
        }
    }

    // ===== UPLOAD ẢNH =====
    void uploadImageAndSave(String name, String phone) {

        String fileName = "avatar/" + UUID.randomUUID();

        FirebaseStorage.getInstance()
                .getReference(fileName)
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    updateUser(name, phone, url);
                                })
                );
    }

    // ===== UPDATE FIRESTORE =====
    void updateUser(String name, String phone, String avatarUrl) {

        FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .update(
                        "name", name,
                        "phone", phone,
                        "avatarUrl", avatarUrl
                )
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}