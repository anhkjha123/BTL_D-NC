package com.example.btl_dnc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.CreateReportActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.ReportAdapter;
import com.example.btl_dnc.model.Report;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class BookmarkFragment extends Fragment {

    RecyclerView rv;
    ReportAdapter adapter;
    ArrayList<Report> list = new ArrayList<>();

    TextView txtSort;
    boolean isNewest = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_bookmark, container, false);

        rv = v.findViewById(R.id.rvReports);
        txtSort = v.findViewById(R.id.txtSort);
        Button btnAdd = v.findViewById(R.id.btnAdd);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReportAdapter(getContext(), list, "resident");
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v1 ->
                startActivity(new Intent(getContext(), CreateReportActivity.class))
        );

        txtSort.setOnClickListener(view -> {
            isNewest = !isNewest;

            if (isNewest) {
                txtSort.setText("Sắp xếp: Mới ▼");
            } else {
                txtSort.setText("Sắp xếp: Cũ ▼");
            }

            loadData();
        });

        loadData();

        return v;
    }

    void loadData() {

        Query.Direction direction = isNewest
                ? Query.Direction.DESCENDING
                : Query.Direction.ASCENDING;

        FirebaseFirestore.getInstance()
                .collection("reports")
                .whereEqualTo("status", "Đang xử lý")
                .orderBy("createdAt", direction)
                .get()
                .addOnSuccessListener(value -> {

                    list.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Report r = doc.toObject(Report.class);

                        if (r != null) {
                            r.setId(doc.getId());
                            list.add(r);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();

                    Toast.makeText(getContext(),
                            "Lỗi: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}