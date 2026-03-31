package com.example.btl_dnc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_dnc.manager.ManagerActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin, btnGoogle;
    TextView tvRegister;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            try {
                                GoogleSignInAccount account =
                                        GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                                                .getResult(ApiException.class);

                                if (account != null) {
                                    firebaseAuthWithGoogle(account.getIdToken());
                                }

                            } catch (ApiException e) {
                                Toast.makeText(this, "Google login thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvRegister = findViewById(R.id.tvRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> loginEmail());
        btnGoogle.setOnClickListener(v ->
                googleLauncher.launch(googleSignInClient.getSignInIntent())
        );

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    // ===== LOGIN EMAIL =====
    private void loginEmail() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    Log.d("LOGIN", "Login OK");
                    checkUserRole();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // ===== LOGIN GOOGLE =====
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> checkUserRole())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Google lỗi", Toast.LENGTH_SHORT).show()
                );
    }

    // ===== CHECK ROLE =====
    private void checkUserRole() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        db.collection("user").document(uid)
                .get(Source.SERVER)
                .addOnSuccessListener(doc -> {

                    if (doc == null || !doc.exists()) {

                        Log.d("FIRESTORE", "User chưa tồn tại -> tạo mới");

                        createUserAndGoHome(user);

                    } else {
                        String role = doc.getString("role");
                        String status = doc.getString("status");

                        if (role == null) role = "resident";


                        if ("blocked".equals(status)) {

                            Toast.makeText(this, "Tài khoản của bạn đã bị khóa!", Toast.LENGTH_LONG).show();

                            FirebaseAuth.getInstance().signOut(); // logout luôn

                            return;
                        }

                        Log.d("FIRESTORE", "Role: " + role);
                        goHome(role);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "GET FAIL: " + e.getMessage());
                    createUserAndGoHome(user);
                });
    }

    // ===== CREATE USER =====
    private void createUserAndGoHome(FirebaseUser user) {
        String uid = user.getUid();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("email", user.getEmail());
        newUser.put("name", user.getEmail());
        newUser.put("role", "resident");
        newUser.put("status", "active");
        newUser.put("apartmentID", "");
        newUser.put("phone", "");
        newUser.put("startDate", FieldValue.serverTimestamp());

        db.collection("user").document(uid)
                .set(newUser)
                .addOnSuccessListener(unused -> {
                    Log.d("FIRESTORE", "SAVE OK");
                    goHome("resident");
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "SAVE FAIL: " + e.getMessage());
                    goHome("resident");
                });
    }

    private void goHome(String role) {
        if ("manager".equals(role)) {
            startActivity(new Intent(this, ManagerActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}