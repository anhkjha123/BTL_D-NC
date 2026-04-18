package com.example.btl_dnc;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

                        if (u.avatarUrl != null && u.avatarUrl.startsWith("/9j")) {
                            loadBase64Image(u.avatarUrl);
                        } else {
                            Glide.with(this)
                                    .load(u.avatarUrl)
                                    .placeholder(R.drawable.placeholder_image)
                                    .circleCrop()
                                    .into(imgAvatar);
                        }
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
    String encodeImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // giảm dung lượng
            byte[] bytes = baos.toByteArray();

            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    void loadBase64Image(String base64) {
        try {
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            imgAvatar.setImageBitmap(bmp);
        } catch (Exception e) {
            e.printStackTrace();
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
            String base64 = encodeImage(imageUri);
            updateUser(name, phone, status, base64);
        } else {
            updateUser(name, phone, status, avatarUrlOld);
        }

    }

    void uploadImageAndSave(String name, String phone, String status) {
        String fileName = "avatar/" + uid + ".jpg";

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