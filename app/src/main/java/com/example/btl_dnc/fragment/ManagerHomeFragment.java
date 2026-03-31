package com.example.btl_dnc.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.UserAdapter;
import com.example.btl_dnc.model.User;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class ManagerHomeFragment extends Fragment {

    RecyclerView rv;
    ArrayList<User> list;
    UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_manager, container, false);

        rv = v.findViewById(R.id.rvUser);

        list = new ArrayList<>();
        adapter = new UserAdapter(getContext(), list);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        loadUsers();

        return v;
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance()
                .collection("user")
                .whereEqualTo("role", "resident")
                .get()
                .addOnSuccessListener(q -> {

                    list.clear();

                    for (DocumentSnapshot doc : q) {
                        User u = doc.toObject(User.class);
                        if (u != null) {
                            u.id = doc.getId();
                            list.add(u);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Lỗi: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}