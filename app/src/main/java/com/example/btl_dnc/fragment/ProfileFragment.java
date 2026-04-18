package com.example.btl_dnc.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.EditProfileActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    TextView tvName, tvEmail, tvApartment, tvBuilding, tvArea, tvStartDate, tvUsername;
    ImageView img, btnSetting;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = v.findViewById(R.id.tvName);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvApartment = v.findViewById(R.id.tvApartment);
        tvBuilding = v.findViewById(R.id.tvBuilding);
        tvArea = v.findViewById(R.id.tvArea);
        tvStartDate = v.findViewById(R.id.tvStartDate);
        tvUsername = v.findViewById(R.id.tvUsername);
        img = v.findViewById(R.id.imgAvatar);
        btnSetting = v.findViewById(R.id.btnSetting);

        btnSetting.setOnClickListener(v1 ->
                startActivity(new Intent(getContext(), EditProfileActivity.class))
        );

        loadUser();

        return v;
    }

    void loadUser() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .get(Source.SERVER)
                .addOnSuccessListener(doc -> {

                    User u = doc.toObject(User.class);

                    if (u == null) return;

                    tvName.setText(u.name);
                    tvEmail.setText(u.email);
                    tvUsername.setText(u.email);

                    tvApartment.setText("Số căn hộ: " + u.apartmentID);
                    tvBuilding.setText("Tòa nhà: A");
                    tvArea.setText("Diện tích: 75m²");

                    if (u.startDate != null) {
                        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(u.startDate.toDate());
                        tvStartDate.setText("Ngày bắt đầu cư trú: " + date);
                    }

                    loadAvatar(u.avatarUrl);
                });
    }

    private void loadAvatar(String avatarUrl) {

        if (avatarUrl == null || avatarUrl.isEmpty()) {
            img.setImageResource(R.drawable.placeholder_image);
            return;
        }

        // 👉 CASE 1: URL (Google / Firebase Storage)
        if (avatarUrl.startsWith("http")) {

            if (getContext() != null) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop()
                        .into(img);
            }

            return;
        }


        try {
            String clean = avatarUrl.replace("\n", "").replace(" ", "");

            byte[] decoded = Base64.decode(clean, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

            img.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            img.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUser();
    }
}