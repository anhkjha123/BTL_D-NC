package com.example.btl_dnc;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.btl_dnc.fragment.BookmarkFragment;
import com.example.btl_dnc.fragment.HomeFragment;
import com.example.btl_dnc.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView nav;
    ImageView btnChatbot;

    float dX, dY;
    long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nav = findViewById(R.id.bottomNav);
        btnChatbot = findViewById(R.id.btnChatbot);

        // ===== LOAD FRAGMENT =====
        loadFragment(new HomeFragment());

        // ===== NAVIGATION =====
        nav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home)
                loadFragment(new HomeFragment());

            else if (item.getItemId() == R.id.nav_bookmark)
                loadFragment(new BookmarkFragment());

            else
                loadFragment(new ProfileFragment());

            return true;
        });

        // ===== CLICK CHATBOT =====
        btnChatbot.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatbotActivity.class));
        });

        // ===== DRAG CHATBOT =====
        btnChatbot.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                View parent = (View) view.getParent();
                int parentWidth = parent.getWidth();
                int parentHeight = parent.getHeight();

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastClickTime = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // ===== GIỚI HẠN KHÔNG RA NGOÀI =====
                        newX = Math.max(0, Math.min(newX, parentWidth - view.getWidth()));
                        newY = Math.max(0, Math.min(newY, parentHeight - view.getHeight()));

                        view.setX(newX);
                        view.setY(newY);

                        return true;

                    case MotionEvent.ACTION_UP:

                        // ===== CLICK =====
                        if (System.currentTimeMillis() - lastClickTime < 200) {
                            view.performClick();
                        }

                        // ===== HÚT VỀ CẠNH =====
                        float middle = parentWidth / 2f;

                        if (view.getX() < middle) {
                            view.animate().x(0).setDuration(200).start();
                        } else {
                            view.animate().x(parentWidth - view.getWidth()).setDuration(200).start();
                        }

                        return true;
                }

                return false;
            }
        });
    }

    // ===== LOAD FRAGMENT =====
    void loadFragment(Fragment f) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, f)
                .commit();
    }
}