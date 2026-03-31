package com.example.btl_dnc;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtRePassword;
    Button btnRegister;
    TextView tvBack;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtRePassword = findViewById(R.id.edtRePassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());
        tvBack.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String rePassword = edtRePassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(rePassword)) {

            Toast.makeText(this, "Nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu >= 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    btnRegister.setEnabled(true);

                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Đăng ký lỗi: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "User null", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    Map<String, Object> newUser = new HashMap<>();
                    newUser.put("email", email);
                    newUser.put("name", email);
                    newUser.put("role", "resident");
                    newUser.put("status", "active");
                    newUser.put("apartmentID", "");
                    newUser.put("phone", "");
                    newUser.put("avatarUrl", "");
                    newUser.put("startDate", FieldValue.serverTimestamp());

                    db.collection("user").document(uid)
                            .set(newUser)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FIRESTORE", e.getMessage());
                                Toast.makeText(this,
                                        "Lỗi Firestore: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                });
    }
}