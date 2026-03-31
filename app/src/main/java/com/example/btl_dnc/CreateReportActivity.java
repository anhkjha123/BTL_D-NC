package com.example.btl_dnc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_dnc.model.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class CreateReportActivity extends AppCompatActivity {

    FrameLayout container;

    String name = "", email = "", content = "", type = "";
    Uri imageUri;

    ActivityResultLauncher<String> picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);

        container = findViewById(R.id.container);


        picker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;

                        ImageView img = findViewById(R.id.imgPreview);
                        if (img != null) img.setImageURI(uri);
                    }
                });

        showStep1();
    }

    // ===== STEP 1 =====
    void showStep1() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_step1, container, false);
        container.removeAllViews();
        container.addView(v);

        EditText edtName = v.findViewById(R.id.edtName);
        EditText edtEmail = v.findViewById(R.id.edtEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (email.isEmpty()) email = user.getEmail();
            if (name.isEmpty() && user.getDisplayName() != null)
                name = user.getDisplayName();
        }

        edtName.setText(name);
        edtEmail.setText(email);
        edtEmail.setEnabled(false);

        v.findViewById(R.id.btnNext1).setOnClickListener(view -> {
            name = edtName.getText().toString();
            email = edtEmail.getText().toString();
            showStep2();
        });
    }

    // ===== STEP 2 =====
    void showStep2() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_step2, container, false);
        container.removeAllViews();
        container.addView(v);

        EditText edtContent = v.findViewById(R.id.edtContent);

        edtContent.setText(content);

        v.findViewById(R.id.btnNext2).setOnClickListener(view -> {
            content = edtContent.getText().toString();
            showStep3();
        });

        v.findViewById(R.id.btnBack2).setOnClickListener(view -> showStep1());
    }

    // ===== STEP 3 =====
    void showStep3() {
        View v = LayoutInflater.from(this).inflate(R.layout.layout_step3, container, false);
        container.removeAllViews();
        container.addView(v);

        Spinner spType = v.findViewById(R.id.spType);
        ImageView imgPreview = v.findViewById(R.id.imgPreview);

        String[] types = {"Điện", "Nước", "Thang máy", "An ninh", "PCCC"};
        spType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));

        if (imageUri != null) {
            imgPreview.setImageURI(imageUri);
        }

        v.findViewById(R.id.btnPickImage).setOnClickListener(view ->
                picker.launch("image/*")
        );

        v.findViewById(R.id.btnSubmit).setOnClickListener(view -> {
            type = spType.getSelectedItem().toString();
            saveReport();
        });

        v.findViewById(R.id.btnBack3).setOnClickListener(view -> showStep2());
    }

    // ===== CONVERT BASE64 =====
    private String encodeImage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== SAVE =====
    void saveReport() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Report r = new Report();
        r.userId = user.getUid();
        r.name = name;
        r.email = email;
        r.content = content;
        r.type = type;
        r.createdAt = System.currentTimeMillis();
        r.status = "Chờ duyệt";

        if (imageUri != null) {
            r.imageBase64 = encodeImage(imageUri);
        }

        FirebaseFirestore.getInstance()
                .collection("reports")
                .add(r)
                .addOnSuccessListener(doc -> {

                    String id = doc.getId();


                    doc.update("id", id);

                    Toast.makeText(this, "Đã gửi chờ duyệt", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}