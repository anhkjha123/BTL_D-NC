package com.example.btl_dnc.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.R;
import com.example.btl_dnc.adapter.NewsAdapter;
import com.example.btl_dnc.model.News;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ManagerNewsFragment extends Fragment {


    private EditText edtSearchNews;
    private ListenerRegistration newsListener;
    private RecyclerView rvNews;

    private NewsAdapter adapter;
    private ArrayList<News> newsList;
    private ArrayList<News> filteredList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manager_news, container, false);


        edtSearchNews = view.findViewById(R.id.edtSearchNews);
        rvNews = view.findViewById(R.id.rvNews);



        newsList = new ArrayList<>();
        filteredList = new ArrayList<>();


        adapter = new NewsAdapter(getContext(), filteredList, "ADMIN", NewsAdapter.TYPE_MANAGER, this::onDeleteNewsClick);
        rvNews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNews.setAdapter(adapter);

        setupSearch();
        fetchNewsData();

        return view;
    }

    private void setupSearch() {
        edtSearchNews.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                filteredList.clear();

                if (query.isEmpty()) {

                    filteredList.addAll(newsList);
                } else {
                    // Lọc theo tiêu đề
                    for (News news : newsList) {
                        if (news.title != null && news.title.toLowerCase().contains(query)) {
                            filteredList.add(news);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchNewsData() {

        newsListener = FirebaseFirestore.getInstance().collection("news")
                .orderBy("createAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (!isAdded() || getContext() == null) return;
                    if (error != null || value == null) return;

                    newsList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        News news = doc.toObject(News.class);
                        if (news != null) {
                            news.id = doc.getId();
                            newsList.add(news);
                        }
                    }
                    showFilteredResult(edtSearchNews.getText().toString());
                });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (newsListener != null) {
            newsListener.remove();
        }
    }
    private void showFilteredResult(String query) {
        filteredList.clear();
        String filter = query.toLowerCase().trim();
        if (filter.isEmpty()) {
            filteredList.addAll(newsList);
        } else {
            for (News n : newsList) {
                if (n.title != null && n.title.toLowerCase().contains(filter)) {
                    filteredList.add(n);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Xử lý sự kiện khi Admin bấm nút X
    private void onDeleteNewsClick(String newsId, int position) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bản tin này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {


                    FirebaseFirestore.getInstance().collection("news").document(newsId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã xóa tin tức thành công", Toast.LENGTH_SHORT).show();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}